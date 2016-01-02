package tbs.rendering

import javax.media.opengl.GL2

/**
 * Describes objects that can be Renderable by OpenGL thread. May also have state information
 * that updates in a separate thread.
 */
trait Renderable extends Serializable {
  var renderFunc : (GL2) => Unit = (GL2) => {}
  var isVisible : Boolean =  true
  var alpha : Double = 1.0
  
  // gl X and Y coords is bottom left of renderable object
  protected var _glX : Double = 0d
  protected var _glY : Double = 0d
  
  protected var _glWidth : Double = 0
  protected var _glHeight : Double = 0
  
  def setRenderFunc(f : (GL2) => Unit) {
    renderFunc = f
  }
  
  /**  
   * Unless specified otherwise, this is the bottom left corner of the
   * renderable instance.
   */
  def setGlCoords (x : Double, y : Double): Unit = {
      glX_=(x)
      glY_=(y)
  }
  
  def setGlDimensions (w : Double, h : Double): Unit = {
      glWidth_=(w)
      glHeight_=(h)
  }
  
  final def glX_=(glX : Double) = {
    _glX = glX
  }
  
  final def glY_=(glY : Double) = {
    _glY = glY
  }
  
  def glX = _glX
  
  def glY = _glY
  
  
  def glWidth = _glWidth
  
  def glHeight = _glHeight
  
  def glWidth_=(glWidth : Double) = {
    _glWidth = glWidth
  }
  
  def glHeight_=(glHeight : Double) = {
    _glHeight = glHeight
  }
  
  /**
   * Pixel Coord Adapter Methods.
   * 
   * Sometimes it's more useful to think of coords being in pixels, so let's
   * use adapter methods to manipulate GLCoords
   */
  
  import tbs.glcoord.Pixel2GL
  
  private var _pixelX : Int = 0
  private var _pixelY : Int = 0
  private var _pixelW : Int = 0
  private var _pixelH : Int = 0
  
  def pixelX : Int = _pixelX 
  def pixelY : Int = _pixelY
  def pixelW : Int = _pixelW
  def pixelH : Int = _pixelH
  
  def pixelX_=(pixelX : Int) = {
    _pixelX = pixelX
    glX = Pixel2GL.xPixelToGL(pixelX)
  }
  
  def pixelY_=(pixelY : Int) = {
    _pixelY = pixelY
    glY = Pixel2GL.yPixelToGL(pixelY)
  }
  
  def pixelW_=(pixelW : Int) = {
    _pixelW = pixelW
  }
  
  def pixelH_=(pixelH : Int) = {
    _pixelH = pixelH
  }
  
  def centreOnScreen() : Unit = {
      setGlCoords(0 - glWidth / 2, 0 - glHeight / 2)
  }
  
  def render(gl : GL2) = {
    if (isVisible) renderFunc(gl)
  }
  
  /**
   * Render to a rectangle with bottom left corner at (bottomLeft._1, bottomLeft._2)
   * and top right corner at (topRight._1, topRight._2) pixel coordinates.
   */
  def renderAtRect(gl : GL2, bottomLeft : (Int, Int), topRight : (Int, Int)) = {
    
  }
  
  /**
   * Render text to fit a width x height text box, with lower left corner at x, y
   */
  def renderText(gl : GL2, text : String, x : Double, y : Double, width : Double = 2, height : Double = 2) = {

  }
  
  def renderGlyph(gl : GL2, glyph : Char, color : (Double, Double, Double), bottomLeft : (Int, Int), topRight : (Int, Int)) : Unit = {
    GameTextRenderer.drawGlyph(gl, glyph, bottomLeft, topRight, color)
  }
  
  def update() = {
    
  }
  
  def hide () : Unit = {
    isVisible = false
  }
  
  def show () : Unit = {
    isVisible = true
  }
  
  def setAlpha (d : Double) : Unit = {
    alpha = d
  }
}