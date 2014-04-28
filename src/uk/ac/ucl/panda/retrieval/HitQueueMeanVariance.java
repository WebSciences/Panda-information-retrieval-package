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


final class HitQueueMeanVariance extends PriorityQueue {
  HitQueueMeanVariance(int size) {
    initialize(size);
  }

  protected final boolean lessThan(Object a, Object b) {
    MeanVarianceScoreDoc hitA = (MeanVarianceScoreDoc)a;
    MeanVarianceScoreDoc hitB = (MeanVarianceScoreDoc)b;
    if (hitA.score == hitB.score)
      return hitA.doc > hitB.doc;
    else
      return hitA.score < hitB.score;
  }
}
