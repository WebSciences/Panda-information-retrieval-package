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
import uk.ac.ucl.panda.utility.structure.TermDocs;



/** Expert: A <code>Scorer</code> for documents matching a <code>Term</code>.
 */
final class TermScorer extends Scorer {

    @Override
    public boolean next() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int doc() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float score() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean skipTo(int target) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}