package uk.ac.ucl.panda.utility.structure;

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

/** A TermInfo is the record of information stored for a term.*/

public final class TermInfo {
  /** The number of documents which contain the term. */
  public int docFreq = 0;
  ////////////////////
   public int CTF = 1;

  ///////////////////
  public long freqPointer = 0;
  public long proxPointer = 0;
  public int skipOffset;

  public TermInfo() {}

  public TermInfo(int df, int ctf, long fp, long pp) {
    docFreq = df;
    CTF = ctf;
    freqPointer = fp;
    proxPointer = pp;
  }

  public TermInfo(TermInfo ti) {
    docFreq = ti.docFreq;
    CTF = ti.CTF;
    freqPointer = ti.freqPointer;
    proxPointer = ti.proxPointer;
    skipOffset = ti.skipOffset;
  }

  public final void set(int docFreq, int CTF,
                 long freqPointer, long proxPointer, int skipOffset) {
    this.docFreq = docFreq;
    this.CTF = CTF;
    this.freqPointer = freqPointer;
    this.proxPointer = proxPointer;
    this.skipOffset = skipOffset;
  }

 public final void set(TermInfo ti) {
    docFreq = ti.docFreq;
    CTF = ti.CTF;
    freqPointer = ti.freqPointer;
    proxPointer = ti.proxPointer;
    skipOffset = ti.skipOffset;
  }

  public void setCTF(int tf){
    this.CTF=tf;
  }

  public int getCTF(){
    return this.CTF;
  }

}
