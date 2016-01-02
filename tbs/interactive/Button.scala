package tbs.interactive

import tbs.rendering._
import tbs.mouse.TBSMouseEvent
import javax.media.opengl.GL2
import tbs.random.UsesRandom

// Ordinary, square button.  Uses GL Coordinate system. 
abstract class Button {
}

class PressableButton extends Button  with MouseInteractive with Renderable  with UsesRandom {
  var width : Double = 0
  var height : Double = 0
  var pressF : () => Unit = () => {}
  val col = (getRandom().nextDouble, getRandom().nextDouble, getRandom().nextDouble)
  
  override def glWidth = {
    width
  }
  
  override def glHeight = {
    height
  }
  
  override def parseMousePress(mEvent: TBSMouseEvent) : Boolean = {
    var isPressed = false
    if (mEvent.mX >= glX && mEvent.mX< glX + glWidth &&
      mEvent.mY >= glY && mEvent.mY < glY + glHeight) {
      isPressed = true
      pressF()
    }
    isPressed
  }
  
  override def render(gl2 : GL2) = {
    gl2.glBegin(GL2.GL_POLYGON);
    gl2.glColor4d(col._1, col._2, col._3, 0.75)
    gl2.glVertex3d(glX, glY, 0)
    gl2.glVertex3d(glX, glY + glHeight, 0)
    gl2.glVertex3d(glX + glWidth, glY + glHeight, 0)
    gl2.glVertex3d(glX + glWidth, glY, 0)
    gl2.glEnd()
  }
  
}