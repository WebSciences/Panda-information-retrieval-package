package uk.ac.ucl.panda.indexing.io;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import uk.ac.ucl.panda.indexing.*;
import uk.ac.ucl.panda.utility.structure.SegmentInfos;
import uk.ac.ucl.panda.utility.structure.SegmentInfo;
import uk.ac.ucl.panda.indexing.io.DocumentsWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;

/*
 * This class keeps track of each SegmentInfos instance that
 * is still "live", either because it corresponds to a 
 * segments_N file in the Directory (a "commit", i.e. a 
 * committed SegmentInfos) or because it's the in-memory SegmentInfos 
 * that a writer is actively updating but has not yet committed 
 * (currently this only applies when autoCommit=false in IndexWriter).
 * This class uses simple reference counting to map the live
 * SegmentInfos instances to individual files in the Directory. 
 * 
 * The same directory file may be referenced by more than
 * one IndexCommitPoints, i.e. more than one SegmentInfos.
 * Therefore we count how many commits reference each file.
 * When all the commits referencing a certain file have been
 * deleted, the refcount for that file becomes zero, and the
 * file is deleted.
 *
 * A separate deletion policy interface
 * (IndexDeletionPolicy) is consulted on creation (onInit)
 * and once per commit (onCommit), to decide when a commit
 * should be removed.
 * 
 * It is the business of the IndexDeletionPolicy to choose
 * when to delete commit points.  The actual mechanics of
 * file deletion, retrying, etc, derived from the deletion
 * of commit points is the business of the IndexFileDeleter.
 * 
 * The current default deletion policy is {@link
 * KeepOnlyLastCommitDeletionPolicy}, which removes all
 * prior commits when a new commit has completed.  This
 * matches the behavior before 2.2.
 *
 * Note that you must hold the write.lock before
 * instantiating this class.  It opens segments_N file(s)
 * directly with no retry logic.
 */
import uk.ac.ucl.panda.utility.io.CorruptIndexException;
import uk.ac.ucl.panda.utility.structure.Directory;
import uk.ac.ucl.panda.utility.structure.IndexFileNames;

final class IndexFileDeleter {

  /* Files that we tried to delete but failed (likely
   * because they are open and we are running on Windows),
   * so we will retry them again later: */
  private List deletable;

  /* Reference count for all files in the index.  
   * Counts how many existing commits reference a file.
   * Maps String to RefCount (class below) instances: */
  private Map refCounts = new HashMap();

  /* Holds all commits (segments_N) currently in the index.
   * This will have just 1 commit if you are using the
   * default delete policy (KeepOnlyLastCommitDeletionPolicy).
   * Other policies may leave commit points live for longer
   * in which case this list would be longer than 1: */
  private List commits = new ArrayList();

  /* Holds files we had incref'd from the previous
   * non-commit checkpoint: */
  private List lastFiles = new ArrayList();

  /* Commits that the IndexDeletionPolicy have decided to delete: */ 
  private List commitsToDelete = new ArrayList();

  private PrintStream infoStream;
  private Directory directory;
  private IndexDeletionPolicy policy;
  private DocumentsWriter docWriter;

  /** Change to true to see details of reference counts when
   *  infoStream != null */
  public static boolean VERBOSE_REF_COUNTS = false;

  void setInfoStream(PrintStream infoStream) {
    this.infoStream = infoStream;
    if (infoStream != null)
      message("setInfoStream deletionPolicy=" + policy);
  }
  
  private void message(String message) {
    infoStream.println("IFD [" + Thread.currentThread().getName() + "]: " + message);
  }

  /**
   * Initialize the deleter: find all previous commits in
   * the Directory, incref the files they reference, call
   * the policy to let it delete commits.  The incoming
   * segmentInfos must have been loaded from a commit point
   * and not yet modified.  This will remove any files not
   * referenced by any of the commits.
   * @throws CorruptIndexException if the index is corrupt
   * @throws IOException if there is a low-level IO error
   */
  public IndexFileDeleter(Directory directory, IndexDeletionPolicy policy, SegmentInfos segmentInfos, PrintStream infoStream, DocumentsWriter docWriter)
    throws CorruptIndexException, IOException {

    this.docWriter = docWriter;
    this.infoStream = infoStream;

    if (infoStream != null)
      message("init: current segments file is \"" + segmentInfos.getCurrentSegmentFileName() + "\"; deletionPolicy=" + policy);

    this.policy = policy;
    this.directory = directory;

    // First pass: walk the files and initialize our ref
    // counts:
    long currentGen = segmentInfos.getGeneration();
    IndexFileNameFilter filter = IndexFileNameFilter.getFilter();

    String[] files = directory.list();
    if (files == null)
      throw new IOException("cannot read directory " + directory + ": list() returned null");

    CommitPoint currentCommitPoint = null;

    for(int i=0;i<files.length;i++) {

      String fileName = files[i];

      if (filter.accept(null, fileName) && !fileName.equals(IndexFileNames.SEGMENTS_GEN)) {

        // Add this file to refCounts with initial count 0:
        getRefCount(fileName);

        if (fileName.startsWith(IndexFileNames.SEGMENTS)) {

          // This is a commit (segments or segments_N), and
          // it's valid (<= the max gen).  Load it, then
          // incref all files it refers to:
          if (SegmentInfos.generationFromSegmentsFileName(fileName) <= currentGen) {
            if (infoStream != null) {
              message("init: load commit \"" + fileName + "\"");
            }
            SegmentInfos sis = new SegmentInfos();
            try {
              sis.read(directory, fileName);
            } catch (FileNotFoundException e) {
              // LUCENE-948: on NFS (and maybe others), if
              // you have writers switching back and forth
              // between machines, it's very likely that the
              // dir listing will be stale and will claim a
              // file segments_X exists when in fact it
              // doesn't.  So, we catch this and handle it
              // as if the file does not exist
              if (infoStream != null) {
                message("init: hit FileNotFoundException when loading commit \"" + fileName + "\"; skipping this commit point");
              }
              sis = null;
            }
            if (sis != null) {
              CommitPoint commitPoint = new CommitPoint(sis);
              if (sis.getGeneration() == segmentInfos.getGeneration()) {
                currentCommitPoint = commitPoint;
              }
              commits.add(commitPoint);
              incRef(sis, true);
            }
          }
        }
      }
    }

    if (currentCommitPoint == null) {
      // We did not in fact see the segments_N file
      // corresponding to the segmentInfos that was passed
      // in.  Yet, it must exist, because our caller holds
      // the write lock.  This can happen when the directory
      // listing was stale (eg when index accessed via NFS
      // client with stale directory listing cache).  So we
      // try now to explicitly open this commit point:
      SegmentInfos sis = new SegmentInfos();
      try {
        sis.read(directory, segmentInfos.getCurrentSegmentFileName());
      } catch (IOException e) {
        throw new CorruptIndexException("failed to locate current segments_N file");
      }
      if (infoStream != null)
        message("forced open of current segments file " + segmentInfos.getCurrentSegmentFileName());
      currentCommitPoint = new CommitPoint(sis);
      commits.add(currentCommitPoint);
      incRef(sis, true);
    }

    // We keep commits list in sorted order (oldest to newest):
    Collections.sort(commits);

    // Now delete anything with ref count at 0.  These are
    // presumably abandoned files eg due to crash of
    // IndexWriter.
    Iterator it = refCounts.keySet().iterator();
    while(it.hasNext()) {
      String fileName = (String) it.next();
      RefCount rc = (RefCount) refCounts.get(fileName);
      if (0 == rc.count) {
        if (infoStream != null) {
          message("init: removing unreferenced file \"" + fileName + "\"");
        }
        deleteFile(fileName);
      }
    }

    // Finally, give policy a chance to remove things on
    // startup:
    policy.onInit(commits);

    // It's OK for the onInit to remove the current commit
    // point; we just have to checkpoint our in-memory
    // SegmentInfos to protect those files that it uses:
    if (currentCommitPoint.deleted) {
      checkpoint(segmentInfos, false);
    }
    
    deleteCommits();
  }

  /**
   * Remove the CommitPoints in the commitsToDelete List by
   * DecRef'ing all files from each SegmentInfos.
   */
  private void deleteCommits() throws IOException {

    int size = commitsToDelete.size();

    if (size > 0) {

      // First decref all files that had been referred to by
      // the now-deleted commits:
      for(int i=0;i<size;i++) {
        CommitPoint commit = (CommitPoint) commitsToDelete.get(i);
        if (infoStream != null) {
          message("deleteCommits: now remove commit \"" + commit.getSegmentsFileName() + "\"");
        }
        int size2 = commit.files.size();
        for(int j=0;j<size2;j++) {
          decRef((String) commit.files.get(j));
        }
      }
      commitsToDelete.clear();

      // Now compact commits to remove deleted ones (preserving the sort):
      size = commits.size();
      int readFrom = 0;
      int writeTo = 0;
      while(readFrom < size) {
        CommitPoint commit = (CommitPoint) commits.get(readFrom);
        if (!commit.deleted) {
          if (writeTo != readFrom) {
            commits.set(writeTo, commits.get(readFrom));
          }
          writeTo++;
        }
        readFrom++;
      }

      while(size > writeTo) {
        commits.remove(size-1);
        size--;
      }
    }
  }

  /**
   * Writer calls this when it has hit an error and had to
   * roll back, to tell us that there may now be
   * unreferenced files in the filesystem.  So we re-list
   * the filesystem and delete such files.  If segmentName
   * is non-null, we will only delete files corresponding to
   * that segment.
   */
  public void refresh(String segmentName) throws IOException {
    String[] files = directory.list();
    if (files == null)
      throw new IOException("cannot read directory " + directory + ": list() returned null");
    IndexFileNameFilter filter = IndexFileNameFilter.getFilter();
    String segmentPrefix1;
    String segmentPrefix2;
    if (segmentName != null) {
      segmentPrefix1 = segmentName + ".";
      segmentPrefix2 = segmentName + "_";
    } else {
      segmentPrefix1 = null;
      segmentPrefix2 = null;
    }
    
    for(int i=0;i<files.length;i++) {
      String fileName = files[i];
      if (filter.accept(null, fileName) &&
          (segmentName == null || fileName.startsWith(segmentPrefix1) || fileName.startsWith(segmentPrefix2)) &&
          !refCounts.containsKey(fileName) &&
          !fileName.equals(IndexFileNames.SEGMENTS_GEN)) {
        // Unreferenced file, so remove it
        if (infoStream != null) {
          message("refresh [prefix=" + segmentName + "]: removing newly created unreferenced file \"" + fileName + "\"");
        }
        deleteFile(fileName);
      }
    }
  }

  public void refresh() throws IOException {
    refresh(null);
  }

  public void close() throws IOException {
    deletePendingFiles();
  }

  private void deletePendingFiles() throws IOException {
    if (deletable != null) {
      List oldDeletable = deletable;
      deletable = null;
      int size = oldDeletable.size();
      for(int i=0;i<size;i++) {
        if (infoStream != null)
          message("delete pending file " + oldDeletable.get(i));
        deleteFile((String) oldDeletable.get(i));
      }
    }
  }

  /**
   * For definition of "check point" see IndexWriter comments:
   * "Clarification: Check Points (and commits)".
   * 
   * Writer calls this when it has made a "consistent
   * change" to the index, meaning new files are written to
   * the index and the in-memory SegmentInfos have been
   * modified to point to those files.
   *
   * This may or may not be a commit (segments_N may or may
   * not have been written).
   *
   * We simply incref the files referenced by the new
   * SegmentInfos and decref the files we had previously
   * seen (if any).
   *
   * If this is a commit, we also call the policy to give it
   * a chance to remove other commits.  If any commits are
   * removed, we decref their files as well.
   */
  public void checkpoint(SegmentInfos segmentInfos, boolean isCommit) throws IOException {

    if (infoStream != null) {
      message("now checkpoint \"" + segmentInfos.getCurrentSegmentFileName() + "\" [" + segmentInfos.size() + " segments " + "; isCommit = " + isCommit + "]");
    }

    // Try again now to delete any previously un-deletable
    // files (because they were in use, on Windows):
    deletePendingFiles();

    // Incref the files:
    incRef(segmentInfos, isCommit);
    final List docWriterFiles;
    if (docWriter != null) {
      docWriterFiles = docWriter.files();
      if (docWriterFiles != null)
        incRef(docWriterFiles);
    } else
      docWriterFiles = null;

    if (isCommit) {
      // Append to our commits list:
      commits.add(new CommitPoint(segmentInfos));

      // Tell policy so it can remove commits:
      policy.onCommit(commits);

      // Decref files for commits that were deleted by the policy:
      deleteCommits();
    }

    // DecRef old files from the last checkpoint, if any:
    int size = lastFiles.size();
    if (size > 0) {
      for(int i=0;i<size;i++)
        decRef((List) lastFiles.get(i));
      lastFiles.clear();
    }

    if (!isCommit) {
      // Save files so we can decr on next checkpoint/commit:
      size = segmentInfos.size();
      for(int i=0;i<size;i++) {
        SegmentInfo segmentInfo = segmentInfos.info(i);
        if (segmentInfo.dir == directory) {
          lastFiles.add(segmentInfo.files());
        }
      }
    }
    if (docWriterFiles != null)
      lastFiles.add(docWriterFiles);
  }

  void incRef(SegmentInfos segmentInfos, boolean isCommit) throws IOException {
    int size = segmentInfos.size();
    for(int i=0;i<size;i++) {
      SegmentInfo segmentInfo = segmentInfos.info(i);
      if (segmentInfo.dir == directory) {
        incRef(segmentInfo.files());
      }
    }

    if (isCommit) {
      // Since this is a commit point, also incref its
      // segments_N file:
      getRefCount(segmentInfos.getCurrentSegmentFileName()).IncRef();
    }
  }

  void incRef(List files) throws IOException {
    int size = files.size();
    for(int i=0;i<size;i++) {
      String fileName = (String) files.get(i);
      RefCount rc = getRefCount(fileName);
      if (infoStream != null && VERBOSE_REF_COUNTS) {
        message("  IncRef \"" + fileName + "\": pre-incr count is " + rc.count);
      }
      rc.IncRef();
    }
  }

  void decRef(List files) throws IOException {
    int size = files.size();
    for(int i=0;i<size;i++) {
      decRef((String) files.get(i));
    }
  }

  private void decRef(String fileName) throws IOException {
    RefCount rc = getRefCount(fileName);
    if (infoStream != null && VERBOSE_REF_COUNTS) {
      message("  DecRef \"" + fileName + "\": pre-decr count is " + rc.count);
    }
    if (0 == rc.DecRef()) {
      // This file is no longer referenced by any past
      // commit points nor by the in-memory SegmentInfos:
      deleteFile(fileName);
      refCounts.remove(fileName);
    }
  }

  void decRef(SegmentInfos segmentInfos) throws IOException {
    final int size = segmentInfos.size();
    for(int i=0;i<size;i++) {
      SegmentInfo segmentInfo = segmentInfos.info(i);
      if (segmentInfo.dir == directory) {
        decRef(segmentInfo.files());
      }
    }
  }

  private RefCount getRefCount(String fileName) {
    RefCount rc;
    if (!refCounts.containsKey(fileName)) {
      rc = new RefCount();
      refCounts.put(fileName, rc);
    } else {
      rc = (RefCount) refCounts.get(fileName);
    }
    return rc;
  }

  void deleteFiles(List files) throws IOException {
    final int size = files.size();
    for(int i=0;i<size;i++)
      deleteFile((String) files.get(i));
  }

  /** Delets the specified files, but only if they are new
   *  (have not yet been incref'd). */
  void deleteNewFiles(List files) throws IOException {
    final int size = files.size();
    for(int i=0;i<size;i++)
      if (!refCounts.containsKey(files.get(i)))
        deleteFile((String) files.get(i));
  }

  void deleteFile(String fileName)
       throws IOException {
    try {
      if (infoStream != null) {
        message("delete \"" + fileName + "\"");
      }
      directory.deleteFile(fileName);
    } catch (IOException e) {			  // if delete fails
      if (directory.fileExists(fileName)) {

        // Some operating systems (e.g. Windows) don't
        // permit a file to be deleted while it is opened
        // for read (e.g. by another process or thread). So
        // we assume that when a delete fails it is because
        // the file is open in another process, and queue
        // the file for subsequent deletion.

        if (infoStream != null) {
          message("IndexFileDeleter: unable to remove file \"" + fileName + "\": " + e.toString() + "; Will re-try later.");
        }
        if (deletable == null) {
          deletable = new ArrayList();
        }
        deletable.add(fileName);                  // add to deletable
      }
    }
  }

  /**
   * Tracks the reference count for a single index file:
   */
  final private static class RefCount {

    int count;

    public int IncRef() {
      return ++count;
    }

    public int DecRef() {
      assert count > 0;
      return --count;
    }
  }

  /**
   * Holds details for each commit point.  This class is
   * also passed to the deletion policy.  Note: this class
   * has a natural ordering that is inconsistent with
   * equals.
   */

  final private class CommitPoint implements Comparable, IndexCommitPoint {

    long gen;
    List files;
    String segmentsFileName;
    boolean deleted;

    public CommitPoint(SegmentInfos segmentInfos) throws IOException {
      segmentsFileName = segmentInfos.getCurrentSegmentFileName();
      int size = segmentInfos.size();
      files = new ArrayList(size);
      files.add(segmentsFileName);
      gen = segmentInfos.getGeneration();
      for(int i=0;i<size;i++) {
        SegmentInfo segmentInfo = segmentInfos.info(i);
        if (segmentInfo.dir == directory) {
          files.addAll(segmentInfo.files());
        }
      }
    }

    /**
     * Get the segments_N file for this commit point.
     */
    public String getSegmentsFileName() {
      return segmentsFileName;
    }

    public Collection getFileNames() throws IOException {
      return Collections.unmodifiableCollection(files);
    }

    /**
     * Called only be the deletion policy, to remove this
     * commit point from the index.
     */
    public void delete() {
      if (!deleted) {
        deleted = true;
        commitsToDelete.add(this);
      }
    }

    public int compareTo(Object obj) {
      CommitPoint commit = (CommitPoint) obj;
      if (gen < commit.gen) {
        return -1;
      } else if (gen > commit.gen) {
        return 1;
      } else {
        return 0;
      }
    }
  }
}
