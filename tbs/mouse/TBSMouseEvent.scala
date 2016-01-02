package tbs.mouse

import java.awt.event.MouseEvent
import javax.media.opengl.awt.GLCanvas

// Our wrapper for the GL Mouse Event, that adds GL coords and the 
// type of event it was
class TBSMouseEvent (val mouseEvent: MouseEvent, val canvas: GLCanvas, val eventType : Int) extends Serializable {
  val mX : Double = MouseCoord.xMouseToGLCoord(mouseEvent.getX(), canvas.getWidth())
  val mY : Double = MouseCoord.yMouseToGLCoord(mouseEvent.getY(), canvas.getHeight())
  
  def isRightMouseButton() : Boolean = {
    mouseEvent.getButton == MouseEvent.BUTTON3
  }
}