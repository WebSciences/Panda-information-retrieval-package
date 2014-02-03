package uk.ac.ucl.panda.retrieval;

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

import java.io.IOException;       // for javadoc
import java.util.HashMap;
import uk.ac.ucl.panda.retrieval.query.Query;
import uk.ac.ucl.panda.utility.io.CorruptIndexException;
import uk.ac.ucl.panda.utility.structure.Document;
import uk.ac.ucl.panda.utility.structure.FieldSelector;
import uk.ac.ucl.panda.utility.structure.Term;

/** The interface for search implementations.
 *
 * <p>Searchable is the abstract network protocol for searching. 
 * Implementations provide search over a single index, over multiple
 * indices, and over indices on remote servers.
 *
 * <p>Queries, filters and sort criteria are designed to be compact so that
 * they may be efficiently passed to a remote index, with only the top-scoring
 * hits being returned, rather than every non-zero scoring hit.
 */
public interface Searchable extends java.rmi.Remote {
  /** Lower-level search API.
   *
   * <p>{@link HitCollector#collect(int,float)} is called for every non-zero
   * scoring document.
   * <br>HitCollector-based access to remote indexes is discouraged.
   *
   * <p>Applications should only use this if they need <i>all</i> of the
   * matching documents.  The high-level search API ({@link
   * Searcher#search(Query)}) is usually more efficient, as it skips
   * non-high-scoring hits.
   *
   * @param weight to match documents
   * @param filter if non-null, a bitset used to eliminate some documents
   * @param results to receive hits
   * @throws BooleanQuery.TooManyClauses
   */
  void search(Query query, Filter filter, TopDocCollector results)
  throws IOException;



  /** Expert: Low-level search implementation.  Finds the top <code>n</code>
   * hits for <code>query</code>, applying <code>filter</code> if non-null.
   *
   * <p>Called by {@link Hits}.
   *
   * <p>Applications should usually call {@link Searcher#search(Query)} or
   * {@link Searcher#search(Query,Filter)} instead.
   * @throws BooleanQuery.TooManyClauses
   */
  TopDocs search(Query query, Filter filter, int n) throws IOException;






  /** Expert: Low-level search implementation with arbitrary sorting.  Finds
   * the top <code>n</code> hits for <code>query</code>, applying
   * <code>filter</code> if non-null, and sorting the hits by the criteria in
   * <code>sort</code>.
   *
   * <p>Applications should usually call {@link
   * Searcher#search(Query,Filter,Sort)} instead.
   * @throws BooleanQuery.TooManyClauses
   */
  TopFieldDocs search(Query query, Filter filter, int n, Sort sort)
  throws IOException;

}
