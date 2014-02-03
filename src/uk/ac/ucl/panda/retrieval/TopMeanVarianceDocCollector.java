package uk.ac.ucl.panda.retrieval;

import uk.ac.ucl.panda.utility.structure.PriorityQueue;

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



/** A {@link HitCollector} implementation that collects the top-scoring
 * documents, returning them as a {@link TopDocs}.  This is used by {@link
 * IndexSearcher} to implement {@link TopDocs}-based search.
 *
 * <p>This may be extended, overriding the collect method to, e.g.,
 * conditionally invoke <code>super()</code> in order to filter which
 * documents are collected.
 **/
public class TopMeanVarianceDocCollector {

  private MeanVarianceScoreDoc reusableSD;

  int totalHits;
  PriorityQueue hq;

  /** Construct to collect a given number of hits.
   * @param numHits the maximum number of hits to collect
   */
  public TopMeanVarianceDocCollector(int numHits) {
    this(numHits, new HitQueueMeanVariance(numHits));
  }

  public TopMeanVarianceDocCollector ReducedCollector(TopMeanVarianceDocCollector collector, int num) {

        if(num > collector.topDocs().totalHits )num = collector.totalHits;
         TopMeanVarianceDocCollector outCollector = new TopMeanVarianceDocCollector(num);
        int start = -1;
        while(start++ < num){

            outCollector.collect( collector.topDocs().MeanVariancescoreDocs[start].doc,collector.topDocs().MeanVariancescoreDocs[start].score,
                collector.topDocs().MeanVariancescoreDocs [start].mean, collector.topDocs().MeanVariancescoreDocs [start].variance);

        }

        return outCollector;
  }

  TopMeanVarianceDocCollector(int numHits, PriorityQueue hq) {
    this.hq = hq;
  }

  // javadoc inherited
  public void collect(int doc, double score, double mean, double variance) {

      totalHits++;
      if (reusableSD == null) {
        reusableSD = new MeanVarianceScoreDoc(doc, score, mean, variance);
      } else {
        // reusableSD holds the last "rejected" entry, so, if
        // this new score is not better than that, there's no
        // need to try inserting it
        reusableSD.doc = doc;
        reusableSD.score = score;
      }
      reusableSD = (MeanVarianceScoreDoc) hq.insertWithOverflow(reusableSD);

  }

  /** The total number of documents that matched this query. */
  public int getTotalHits() { return totalHits; }

  /** The top-scoring hits. */
  public TopDocsMeanVariance topDocs() {
    MeanVarianceScoreDoc[] scoreDocs = new MeanVarianceScoreDoc[hq.size()];
    for (int i = hq.size()-1; i >= 0; i--)      // put docs in array
      scoreDocs[i] = (MeanVarianceScoreDoc)hq.pop();

    double maxScore = (totalHits==0)
      ? Float.NEGATIVE_INFINITY
      : scoreDocs[0].score;

    return new TopDocsMeanVariance(totalHits, scoreDocs, maxScore);
  }
}
