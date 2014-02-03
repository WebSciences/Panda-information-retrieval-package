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

import java.io.IOException;

/**
 * Expert: Common scoring functionality for different types of queries.
 *
 * <p>
 * A <code>Scorer</code> either iterates over documents matching a
 * query in increasing order of doc Id, or provides an explanation of
 * the score for a query for a given document.
 * </p>
 * <p>
 * Document scores are computed using a given <code>Similarity</code>
 * implementation.
 * </p>
 * @see BooleanQuery#setAllowDocsOutOfOrder
 */
public abstract class Scorer {
 



  /** Scores and collects all matching documents.
   * @param hc The collector to which all matching documents are passed through
   * {@link HitCollector#collect(int, float)}.
   * <br>When this method is used the {@link #explain(int)} method should not be used.
   */
  public void score(HitCollector hc) throws IOException {
    while (next()) {
      hc.collect(doc(), score());
    }
  }

  /** Expert: Collects matching documents in a range.  Hook for optimization.
   * Note that {@link #next()} must be called once before this method is called
   * for the first time.
   * @param hc The collector to which all matching documents are passed through
   * {@link HitCollector#collect(int, float)}.
   * @param max Do not score documents past this.
   * @return true if more matching documents may remain.
   */
  protected boolean score(HitCollector hc, int max) throws IOException {
    while (doc() < max) {
      hc.collect(doc(), score());
      if (!next())
        return false;
    }
    return true;
  }

  /**
   * Advances to the document matching this Scorer with the lowest doc Id
   * greater than the current value of {@link #doc()} (or to the matching
   * document with the lowest doc Id if next has never been called on
   * this Scorer).
   *
   * <p>
   * When this method is used the {@link #explain(int)} method should not
   * be used.
   * </p>
   *
   * @return true iff there is another document matching the query.
   * @see BooleanQuery#setAllowDocsOutOfOrder
   */
  public abstract boolean next() throws IOException;

  /** Returns the current document number matching the query.
   * Initially invalid, until {@link #next()} is called the first time.
   */
  public abstract int doc();

  /** Returns the score of the current document matching the query.
   * Initially invalid, until {@link #next()} or {@link #skipTo(int)}
   * is called the first time.
   */
  public abstract float score() throws IOException;

  /**
   * Skips to the document matching this Scorer with the lowest doc Id
   * greater than or equal to a given target.
   *
   * <p>
   * The behavior of this method is undefined if the target specified is
   * less than or equal to the current value of {@link #doc()}.
   * <p>
   * Behaves as if written:
   * <pre>
   *   boolean skipTo(int target) {
   *     do {
   *       if (!next())
   * 	     return false;
   *     } while (target > doc());
   *     return true;
   *   }
   * </pre>
   * Most implementations are considerably more efficient than that.
   * </p>
   *
   * <p>
   * When this method is used the {@link #explain(int)} method should not
   * be used.
   * </p>
   *
   * @param target The target document number.
   * @return true iff there is such a match.
   * @see BooleanQuery#setAllowDocsOutOfOrder
   */
  public abstract boolean skipTo(int target) throws IOException;

  

}
