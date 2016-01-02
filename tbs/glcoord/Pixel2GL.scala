package tbs.glcoord

import javax.media.opengl.GLDrawable

object Pixel2GL {
  var glDrawable : GLDrawable = null
  
  def init(drawable : GLDrawable) {
    glDrawable = drawable
  }
  
  private def sW = glDrawable.getWidth().toDouble
  private def sH = glDrawable.getHeight().toDouble
  
  def xPixelToGL(x : Int): Double = {
    (x * 2 - sW) / sW
  }
  
  def yPixelToGL(y : Int): Double = {
    (y * 2 - sH) / sH
  }
  
  def xGLToPixel(x : Double) : Int = {
    (x * sW + sW).toInt / 2
  }
  
  def yGLToPixel(y : Double) : Int = {
    (y * sH + sH).toInt / 2
  }
}