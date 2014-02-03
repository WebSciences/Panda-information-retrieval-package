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
package uk.ac.ucl.panda.indexing.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import uk.ac.ucl.panda.retrieval.query.QualityQuery;

/**
 * Read TREC topics.
 * <p>
 * Expects this topic format -
 * <pre>
 *   &lt;top&gt;
 *   &lt;num&gt; Number: nnn
 *     
 *   &lt;title&gt; title of the topic
 *     
 *   &lt;desc&gt; Description:
 *   description of the topic
 *     
 *   &lt;narr&gt; Narrative:
 *   "story" composed by assessors.
 *    
 *   &lt;/top&gt;
 * </pre>
 * Comment lines starting with '#' are ignored.
 */
public class TrecTopicsReader {

  private static final String newline = System.getProperty("line.separator");
  
  /**
   *  Constructor for Trec's TopicsReader
   */
  public TrecTopicsReader() {
    super();
  }

  /**
   * Read quality queries from trec format topics file.
   * @param reader where queries are read from.
   * @return the result quality queries.
   * @throws IOException if cannot read the queries.
   */
  public QualityQuery[] readQueries(BufferedReader reader) throws IOException {
   java.util.Hashtable ht = new java.util.Hashtable();
          int [] ids = {303, 322, 344, 353, 363, 378, 394, 408, 426, 439, 
                307, 325, 345, 354, 367, 379, 397, 409, 427, 442, 
                310, 330, 346, 355, 372, 383, 399, 414, 433, 443, 
                314, 336, 347, 356, 374, 389, 401, 416, 435, 445, 
                320, 341, 350, 362, 375, 393, 404, 419, 436, 448};
            for (int i=0; i<ids.length; i++){
                 ht.put (Integer.toString(ids[i]),Integer.toString(ids[i]));
            }

      
    ArrayList res = new ArrayList();
    StringBuffer sb;
    try {
      while (null!=(sb=read(reader,"<TOP>",null,false,false))) {
        HashMap fields = new HashMap();
        // id
        sb = read(reader,"<NUM>",null,true,false);
        int k = sb.indexOf(":");
        String id = sb.substring(k+1).trim();
        // title
        sb = read(reader,"<TITLE>",null,true,false);
        k = sb.indexOf(">");
        String title = sb.substring(k+1).trim();
        // description
        sb = read(reader,"<DESC>",null,false,false);
        sb = read(reader,"<NARR>",null,false,true);
        String descripion = sb.toString().trim();
        // we got a topic!
        fields.put("title",title);
        fields.put("description",descripion);
        //////////////
//        fields.put("", );
        ////////////////
        QualityQuery topic = new QualityQuery(id,fields);
        //if (ht.contains (id))
        res.add(topic);
        // skip narrative, get to end of doc
        read(reader,"</TOP>",null,false,false);
      }
    } finally {
      reader.close();
    }
    // sort result array (by ID) 
    QualityQuery qq[] = (QualityQuery[]) res.toArray(new QualityQuery[0]);
    Arrays.sort(qq);
    return qq;
  }

  // read until finding a line that starts with the specified prefix
  private StringBuffer read (BufferedReader reader, String prefix, StringBuffer sb, boolean collectMatchLine, boolean collectAll) throws IOException {
    sb = (sb==null ? new StringBuffer() : sb);
    String sep = "";
    while (true) {
      String line = reader.readLine();
      if (line==null) {
        return null;
      }
      line = line.toUpperCase();
      if (line.startsWith(prefix)) {
        if (collectMatchLine) {
          sb.append(sep+line);
          sep = newline;
        }
        break;
      }
      if (collectAll) {
        sb.append(sep+line);
        sep = newline;
      }
    }
    //System.out.println("read: "+sb);
    return sb;
  }
}
