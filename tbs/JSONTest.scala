package tbs

import spray.json._
import DefaultJsonProtocol._ 

object JSONTest {
  def main(args : Array[String]) : Unit = {
    val source = """{ "some": "JSON source" }"""
    val jsonAst = source.parseJson
  }
}