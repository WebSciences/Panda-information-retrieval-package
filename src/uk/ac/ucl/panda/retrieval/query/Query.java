package uk.ac.ucl.panda.retrieval.query;

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import uk.ac.ucl.panda.utility.structure.Term;


/** The abstract base class for queries.
    <p>Instantiable subclasses are:
    <ul>
    <li> {@link TermQuery}
    <li> {@link MultiTermQuery}
    <li> {@link BooleanQuery}
    <li> {@link WildcardQuery}
    <li> {@link PhraseQuery}
    <li> {@link PrefixQuery}
    <li> {@link MultiPhraseQuery}
    <li> {@link FuzzyQuery}
    <li> {@link RangeQuery}
    <li> {@link org.apache.lucene.search.spans.SpanQuery}
    </ul>
    <p>A parser for queries is contained in:
    <ul>
    <li>{@link org.apache.lucene.queryParser.QueryParser QueryParser}
    </ul>
*/
public class Query implements java.io.Serializable, Cloneable {
  private float boost = 1.0f;                     // query boost factor

  /** Sets the boost for this query clause to <code>b</code>.  Documents
   * matching this clause will (in addition to the normal weightings) have
   * their score multiplied by <code>b</code>.
   */
  public void setBoost(float b) { boost = b; }

  /** Gets the boost for this clause.  Documents matching
   * this clause will (in addition to the normal weightings) have their score
   * multiplied by <code>b</code>.   The boost is 1.0 by default.
   */
  public float getBoost() { return boost; }



  private Vector term;
  private String field;


public Vector getTerm(){

    return term;

}

public String getField(){
    return field;

}


  /** Prints a query to a string. */
  public String toString() {
    String out = " ";
    for(int i = 0; i < term.size(); i++){
        out = out + ((Term)term.get(i)).text()+" ";

    }
    return out;
  }


  public Query(Vector term){
    this.term=term;

  }



  /** Returns a clone of this query. */
  public Object clone() {
    try {
      return (Query)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Clone not supported: " + e.getMessage());
    }
  }
}
