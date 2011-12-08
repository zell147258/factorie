/* Copyright (C) 2008-2010 University of Massachusetts Amherst,
   Department of Computer Science.
   This file is part of "FACTORIE" (Factor graphs, Imperative, Extensible)
   http://factorie.cs.umass.edu, http://code.google.com/p/factorie/
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package cc.factorie.app.nlp.parse
import cc.factorie._
import cc.factorie.app.nlp._
import collection.mutable.{HashSet, ArrayBuffer}

// Representation for a dependency parse

object ParseLabelDomain extends CategoricalDomain[String]
class ParseLabel(val edge:ParseEdge, targetValue:String) extends LabelVariable(targetValue) { def domain = ParseLabelDomain } 

object ParseFeaturesDomain extends CategoricalVectorDomain[String]
class ParseFeatures(val token:Token) extends BinaryFeatureVectorVariable[String] { def domain = ParseFeaturesDomain }

class ParseEdge(theChild:Token, initialParent:Token, labelString:String) extends ArrowVariable(theChild, initialParent) {
  @inline final def child = src
  @inline final def parent = dst
  val label = new ParseLabel(this, labelString)
  val childEdges = new ParseChildEdges

  child.attr += label // Really?  Why is the label on the Token and not simply associated with this edge?
  //initialParent.attr[ParseLabel].edge.childEdges.addEntry(this)
  override def set(newValue: Token)(implicit d: DiffList): Unit =
    if (newValue ne value) {
      // update parent's child pointers
      parent.attr[ParseLabel].edge.childEdges.remove(this)
      super.set(newValue)(d)
      //this is necessary b/c I am using a self-loop to indicate root.
      if (child != parent) parent.attr[ParseLabel].edge.childEdges.addEntry(this)
    }
}

class ParseChildEdges extends HashSet[ParseEdge]

// Example usages:
// token.attr[ParseEdge].parent
// token.attr[ParseNode].parent
// token.attr[ParseChildEdges].map(_.child)

