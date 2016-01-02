package tbs.mouse

import tbs.glcoord.WindowSizeInfo
import tbs.glcoord.Pixel2GL

/**
 * Converts mouse x, y location information.
 * x coords begin at 0, 0 on the bottom left.
 * GL coords begin at -1.0, -1.0 on the bottom left
 */
object MouseCoord {
  // Coordinate tuple
  def mouseToGLCoord(coords : (Int, Int), w : Int, h : Int) = {
    (xMouseToGLCoord(coords._1, w) , yMouseToGLCoord(coords._2, h))
  }
  
  /**

   */
  def xMouseToGLCoord(x: Int, w : Int) : Float = {
    (x * 2 - w) / (w : Float)
  }
  
  def yMouseToGLCoord(y: Int, h : Int) : Float = {
    - (y * 2 - h) / (h : Float)
  }
  
  def xMouseToPixelCoord(x : Int, w : Int) : Int = {
    Pixel2GL.xGLToPixel(1.0f)
  }
  
  def yMouseToPixelCoord(y : Int, h : Int) : Int = {
    Pixel2GL.yGLToPixel(1.0f)
  }
}