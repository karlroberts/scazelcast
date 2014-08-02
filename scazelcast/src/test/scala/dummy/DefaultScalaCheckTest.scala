package dummy

import org.scalacheck._
import org.specs2._
import org.specs2.matcher._


/**
 * Created by IntelliJ IDEA.
 * User: robertk
 */

object DefaultScalaCheckTest extends Properties("String") {
  import Prop.forAll

  val propReverseList  = forAll { l: List[String] => l.reverse.reverse == l }

  val propConcatString = forAll { (s1: String, s2: String) =>
    (s1 + s2).endsWith(s2)
  }

  propReverseList.check


  val x = propConcatString.check
}


class BooksTest extends Specification with ThrownExpectations { def is = s2"""
   Strings:-
      *   s.reverse.reverse = s   : $e1
"""

  def e1 = {
    val s = "hello Karl"
    val rev2 = s.reverse.reverse
    rev2 must be equalTo(s)
  }
}
