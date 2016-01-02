package tbs.mouse

// Trait is applied to any object which reacts to being moused over.
trait MouseOverable {
  
  var isMousedOverF : ((Int, Int) => Boolean) = (_, _) => {true}
  
  var mouseOverF : (() => Unit) = () => {};
  
  def setIsMousedOverFunc(param: ((Int, Int) => Boolean)) = {
    isMousedOverF = param
  }
  
  def isMousedOver(mX : Int, mY : Int) : Boolean = {
    isMousedOverF(mX, mY)
  }
  
  def setMouseOverFunc (param: () => Unit) {
    mouseOverF = param
  }
  
  // Pass a function that runs when 
  def mouseOver() : Unit = {
    mouseOverF()
  }
}