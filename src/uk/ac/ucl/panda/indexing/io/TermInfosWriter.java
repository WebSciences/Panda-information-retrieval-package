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


import uk.ac.ucl.panda.utility.io.IndexOutput;
import java.io.IOException;
import uk.ac.ucl.panda.utility.structure.Directory;
import uk.ac.ucl.panda.utility.structure.FieldInfos;
import uk.ac.ucl.panda.utility.structure.Term;
import uk.ac.ucl.panda.utility.structure.TermInfo;

/** This stores a monotonically increasing set of <Term, TermInfo> pairs in a
  Directory.  A TermInfos can be written once, in order.  */

public final class TermInfosWriter {
  /** The file format version, a negative number. */
  public static final int FORMAT = -3;

  private FieldInfos fieldInfos;
  private IndexOutput output;
  private TermInfo lastTi = new TermInfo();
  private long size;

  // TODO: the default values for these two parameters should be settable from
  // IndexWriter.  However, once that's done, folks will start setting them to
  // ridiculous values and complaining that things don't work well, as with
  // mergeFactor.  So, let's wait until a number of folks find that alternate
  // values work better.  Note that both of these values are stored in the
  // segment, so that it's safe to change these w/o rebuilding all indexes.

  /** Expert: The fraction of terms in the "dictionary" which should be stored
   * in RAM.  Smaller values use more memory, but make searching slightly
   * faster, while larger values use less memory and make searching slightly
   * slower.  Searching is typically not dominated by dictionary lookup, so
   * tweaking this is rarely useful.*/
  int indexInterval = 192;

  /** Expert: The fraction of {@link TermDocs} entries stored in skip tables,
   * used to accellerate {@link TermDocs#skipTo(int)}.  Larger values result in
   * smaller indexes, greater acceleration, but fewer accelerable cases, while
   * smaller values result in bigger indexes, less acceleration and more
   * accelerable cases. More detailed experiments would be useful here. */
  int skipInterval = 16;
  
  /** Expert: The maximum number of skip levels. Smaller values result in 
   * slightly smaller indexes, but slower skipping in big posting lists.
   */
  int maxSkipLevels = 10;

  private long lastIndexPointer;
  private boolean isIndex;
  private char[] lastTermText = new char[10];
  private int lastTermTextLength;
  private int lastFieldNumber = -1;

  private char[] termTextBuffer = new char[10];

  private TermInfosWriter other;

  TermInfosWriter(Directory directory, String segment, FieldInfos fis,
                  int interval)
       throws IOException {
    initialize(directory, segment, fis, interval, false);
    other = new TermInfosWriter(directory, segment, fis, interval, true);
    other.other = this;
  }

  private TermInfosWriter(Directory directory, String segment, FieldInfos fis,
                          int interval, boolean isIndex) throws IOException {
    initialize(directory, segment, fis, interval, isIndex);
  }

  private void initialize(Directory directory, String segment, FieldInfos fis,
                          int interval, boolean isi) throws IOException {
    indexInterval = interval;
    fieldInfos = fis;
    isIndex = isi;
    output = directory.createOutput(segment + (isIndex ? ".tii" : ".tis"));
    output.writeInt(FORMAT);                      // write format
    output.writeLong(0);                          // leave space for size
    output.writeInt(indexInterval);             // write indexInterval
    output.writeInt(skipInterval);              // write skipInterval
    output.writeInt(maxSkipLevels);              // write maxSkipLevels
  }

  void add(Term term, TermInfo ti) throws IOException {

    final int length = term.text().length();
    if (termTextBuffer.length < length)
      termTextBuffer = new char[(int) (length*1.25)];

    term.text().getChars(0, length, termTextBuffer, 0);

    add(fieldInfos.fieldNumber(term.field()), termTextBuffer, 0, length, ti);
  }

  // Currently used only by assert statement
  private int compareToLastTerm(int fieldNumber, char[] termText, int start, int length) {
    int pos = 0;

    if (lastFieldNumber != fieldNumber) {
      final int cmp = fieldInfos.fieldName(lastFieldNumber).compareTo(fieldInfos.fieldName(fieldNumber));
      // If there is a field named "" (empty string) then we
      // will get 0 on this comparison, yet, it's "OK".  But
      // it's not OK if two different field numbers map to
      // the same name.
      if (cmp != 0 || lastFieldNumber != -1)
        return cmp;
    }

    while(pos < length && pos < lastTermTextLength) {
      final char c1 = lastTermText[pos];
      final char c2 = termText[pos + start];
      if (c1 < c2)
        return -1;
      else if (c1 > c2)
        return 1;
      pos++;
    }

    if (pos < lastTermTextLength)
      // Last term was longer
      return 1;
    else if (pos < length)
      // Last term was shorter
      return -1;
    else
      return 0;
  }

  /** Adds a new <<fieldNumber, termText>, TermInfo> pair to the set.
    Term must be lexicographically greater than all previous Terms added.
    TermInfo pointers must be positive and greater than all previous.*/
  void add(int fieldNumber, char[] termText, int termTextStart, int termTextLength, TermInfo ti)
    throws IOException {

    assert compareToLastTerm(fieldNumber, termText, termTextStart, termTextLength) < 0 ||
      (isIndex && termTextLength == 0 && lastTermTextLength == 0) :
      "Terms are out of order: field=" + fieldInfos.fieldName(fieldNumber) + " (number " + fieldNumber + ")" +
      " lastField=" + fieldInfos.fieldName(lastFieldNumber) + " (number " + lastFieldNumber + ")" +
      " text=" + new String(termText, termTextStart, termTextLength) + " lastText=" + new String(lastTermText, 0, lastTermTextLength);

    assert ti.freqPointer >= lastTi.freqPointer: "freqPointer out of order (" + ti.freqPointer + " < " + lastTi.freqPointer + ")";
    assert ti.proxPointer >= lastTi.proxPointer: "proxPointer out of order (" + ti.proxPointer + " < " + lastTi.proxPointer + ")";

    if (!isIndex && size % indexInterval == 0)
      other.add(lastFieldNumber, lastTermText, 0, lastTermTextLength, lastTi);                      // add an index term

    writeTerm(fieldNumber, termText, termTextStart, termTextLength);                        // write term

    output.writeVInt(ti.docFreq);                       // write doc freq
    ////////////////////////////
    output.writeVInt(ti.CTF);                       // write CTF
    ///////////////////////////
    output.writeVLong(ti.freqPointer - lastTi.freqPointer); // write pointers
    output.writeVLong(ti.proxPointer - lastTi.proxPointer);

    if (ti.docFreq >= skipInterval) {
      output.writeVInt(ti.skipOffset);
    }

    if (isIndex) {
      output.writeVLong(other.output.getFilePointer() - lastIndexPointer);
      lastIndexPointer = other.output.getFilePointer(); // write pointer
    }

    if (lastTermText.length < termTextLength)
      lastTermText = new char[(int) (termTextLength*1.25)];
    System.arraycopy(termText, termTextStart, lastTermText, 0, termTextLength);
    lastTermTextLength = termTextLength;
    lastFieldNumber = fieldNumber;

    lastTi.set(ti);
    size++;
  }

  private void writeTerm(int fieldNumber, char[] termText, int termTextStart, int termTextLength)
       throws IOException {

    // Compute prefix in common with last term:
    int start = 0;
    final int limit = termTextLength < lastTermTextLength ? termTextLength : lastTermTextLength;
    while(start < limit) {
      if (termText[termTextStart+start] != lastTermText[start])
        break;
      start++;
    }

    int length = termTextLength - start;

    output.writeVInt(start);                     // write shared prefix length
    output.writeVInt(length);                  // write delta length
    output.writeChars(termText, start+termTextStart, length);  // write delta chars
    output.writeVInt(fieldNumber); // write field num
  }

  /** Called to complete TermInfos creation. */
  void close() throws IOException {
    output.seek(4);          // write size after format
    output.writeLong(size);
    output.close();

    if (!isIndex)
      other.close();
  }

}
