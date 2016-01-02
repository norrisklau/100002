package tbs.dialog

import java.awt.event.KeyEvent
import scala.collection.mutable.ListBuffer
import java.awt.event.MouseEvent
import tbs.rendering._
import tbs.mouse._
import tbs.action._
import javax.media.opengl.GL2
import tbs.interactive.Button
import tbs.interactive._
import tbs.game.state._
import tbs.random.UsesRandom

class Dialog extends Renderable with MouseInteractive 
                                with KeyboardInteractive
                                with UsesRandom{
  var attachedGameState : GameState = null
  var borderSize : Double = 0.02
  var color : (Double, Double, Double, Double) = (0, 0, 0, 0)
  val components : ListBuffer[DialogComponent] = new ListBuffer
  
  // Set dialog background colour
  def setColor(r : Double, g : Double, b : Double, a : Double) {
    color = (r, g, b, a)
  }
  
  def attachToGameState(gameState: GameState) : Unit = {
    attachedGameState = gameState
  }
  
  def isWithinDialog(x: Double, y: Double) : Boolean = {
    x >= glX && x <= glX + glWidth && y >= glY && y <= glY + glHeight
  }
  
  // components use relative coordinates of the DIALOG, not the entire screen.
  // Bottom left of box is 0.0, top right is 1, 1
  def addComponent(component: DialogComponent) : Unit = {
    component.setParentDialog(this)
    components.append(component)              
  }
  
  override def render(gl : GL2) : Unit = {
    renderComponents(gl)
  }
  
  def renderComponents(gl : GL2) : Unit = {
    for (i <- components) {
      i.render(gl)
    }
  }
}


class DialogMenu extends Dialog with MouseDraggable with MousePressable{

  
  setColor(getRandom().nextDouble, getRandom().nextDouble, getRandom().nextDouble, 0.5)
  addComponent(new DialogBorder((1, 1, 1, 1)))
  
  override def parseMouseDrag(mEvent : TBSMouseEvent) : Boolean = {
    var wasDragged = false
    // Due to the way parse drag is called, the check should be superfluous, but it 
    // doesn't hurt.
    if (mEvent.eventType == MouseEvent.MOUSE_DRAGGED) {
      val dragVector = getDragVector(mEvent)
      setGlCoords(glX + dragVector._1, glY + dragVector._2)
      previousMouseDragEvent = Some(mEvent)
      wasDragged = true
    }
    wasDragged
  }
  
  override def parseMousePress(mEvent : TBSMouseEvent) : Boolean = {
    var elementPressed = false
    for (e <- components if ! elementPressed) {
      e match {
        case p : MouseInteractive => {
          elementPressed = p.parseMouseEvent(mEvent)
        }
        case _ => 
      }
    }
    val dialogPressed = isWithinDialog(mEvent.mX, mEvent.mY)
    if (dialogPressed) attachedGameState.setDialogFocus(this)
    dialogPressed
  }
  
  val colour : (Double, Double, Double) = (getRandom().nextDouble, getRandom().nextDouble, getRandom().nextDouble)
  override def render (gl :GL2) = {
    gl.glBegin(GL2.GL_POLYGON);
    
    // Draw the dialog rectangle
    gl.glColor4d(color._1, color._2, color._3, color._4)
    gl.glVertex3d(glX, glY, 0)
    gl.glVertex3d(glX, glY + glHeight, 0)
    gl.glVertex3d(glX + glWidth, glY + glHeight, 0)
    gl.glVertex3d(glX + glWidth, glY, 0)

    gl.glEnd()
    renderComponents(gl)
  }
}

