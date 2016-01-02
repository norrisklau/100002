package tbs.interactive

import java.awt.event.KeyEvent

trait KeyboardInteractive {
  var parseFuncs : Array[ (KeyEvent) => Boolean] = Array()
  
  def pushKeyParseFunction(func : (KeyEvent) => Boolean) : Unit = {
    if (! parseFuncs.contains(func)) {
      parseFuncs = parseFuncs :+ func
    }
  } 
  
  def parseKeyEvent (kE : KeyEvent) : Boolean = {
    var keyParsed : Boolean = false
    for (f <- parseFuncs if ! keyParsed) {
      keyParsed = f(kE)
    }
    keyParsed
  }
  
  def popKeyParseFunction() : Unit = {   
    if (! parseFuncs.isEmpty) parseFuncs = parseFuncs.dropRight(1)
  }
  
  def popKeyParseFunction(filter : ((KeyEvent) => Boolean) => Boolean) : Unit = {
    parseFuncs = parseFuncs.filterNot(filter)
  }
}