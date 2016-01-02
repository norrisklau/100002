package tbs.dialog

import javax.media.opengl.GL2

import tbs.rendering.Renderable
import tbs.interactive._

/**
 * Everything that can be attached to a dialog menu : 
 * Buttons, text fields, dialog images.
 * 
 * Carries information about relative placement on the dialog menu, as well
 * as the attached dialogMenu object.
 */
trait DialogComponent extends Renderable {
  var parentDialog : Dialog = null
  // Relative x , y offset within a dialog menu. The bottom of the dialog would be 
  // (0, 0), and the top right would be (1.0, 1.0)
  protected var relativeXCoord : Double = 0
  protected var relativeYCoord : Double = 0
  // Relative width, height of the component compared to the attached dialogMenu. 
  // For example, a relative width of 0.5 would mean this component is half as 
  // wide as the dialogMenu's width.
  protected var relativeWidth : Double = 0
  protected var relativeHeight : Double = 0
  
  def setParentDialog(dialog : Dialog) : Unit = {
    parentDialog = dialog
  }
  
  def setRelativeCoordinates(x : Double, y : Double) : Unit = {
    relativeXCoord = x
    relativeYCoord = y
  }
  
  def setRelativeDimensions(w : Double, h : Double) : Unit = {
    relativeWidth = w
    relativeHeight = h
  }
  
  // Actual X, Y Coords on screen
  override def glX() : Double = {
    parentDialog.glX + parentDialog.glWidth * relativeXCoord
  }
  
  override def glY() : Double = {
    parentDialog.glY + parentDialog.glHeight * relativeYCoord
  }
  
  override def glWidth() : Double = {
    parentDialog.glWidth * relativeWidth
  }
  
  override def glHeight() : Double = {
    parentDialog.glHeight * relativeHeight
  }

}

class DialogButton (val pFunc: () => Unit = () => (), 
                          val x : Double = 0d, 
                          val y : Double = 0d,
                          val w : Double = 1d, 
                          val h : Double = 1d) extends PressableButton with DialogComponent {
  relativeXCoord = x
  relativeYCoord = y
  relativeWidth = w
  relativeHeight = h
  
  pressF = pFunc
  // Use pressable buttons render stuff
}

class DialogBorder(val color : (Double, Double, Double, Double), var leftWidth : Double = 0.05,
                                                                 var rightWidth : Double = 0.05,
                                                                 var topWidth : Double = 0.05,
                                                                 var bottomWidth : Double = 0.05) extends DialogComponent {
  
  override def glWidth () : Double = {
    parentDialog.glWidth
  }
  
  override def glHeight () : Double = {
    parentDialog.glHeight
  }
  
  // Border width as a relative percent of the parent dialogs dimensions. 
  // Left and Right width are % of parent width, top and bottom width are % of parent height
  def setLeftWidth(d : Double) = leftWidth = d
  def setTopWidth(d : Double) = topWidth = d
  def setRightWidth(d : Double) = rightWidth = d
  def setBottomWidth(d : Double) = bottomWidth = d
  
  // Actual GL widths of the border
  def glLeftWidth () : Double = leftWidth * parentDialog.glWidth
  def glRightWidth () : Double = rightWidth * parentDialog.glWidth
  def glTopWidth () : Double = topWidth * parentDialog.glHeight
  def glBottomWidth () : Double = bottomWidth * parentDialog.glHeight
  
  /**
   * The border is actually 4 bars, drawn along the edge of the parent dialog. They should not overlap, 
   * for transparency colouring reasons.
   */
  override def render(gl2 : GL2) : Unit = {
    gl2.glBegin(GL2.GL_POLYGON)
    gl2.glColor4d(color._1, color._2, color._3, color._4)
    val left = glX
    val right = glX + glWidth
    val top = glY + glHeight
    val bottom = glY
    // Bar One, on the left side of the dialog
    gl2.glVertex2d(left, bottom)
    gl2.glVertex2d(left, top)
    gl2.glVertex2d(left + glLeftWidth, top)
    gl2.glVertex2d(left + glLeftWidth, bottom)
    gl2.glEnd()
    
    gl2.glBegin(GL2.GL_POLYGON)
    // Bar No. 2, on the right side of the dialog
    gl2.glVertex2d(right - glRightWidth, bottom)
    gl2.glVertex2d(right - glRightWidth, top)
    gl2.glVertex2d(right, top)
    gl2.glVertex2d(right, bottom)
    gl2.glEnd()
    // Top bar of the border, between the first 2 bars
    
    gl2.glBegin(GL2.GL_POLYGON)
    gl2.glVertex2d(left + glLeftWidth, top - glTopWidth)
    gl2.glVertex2d(left + glLeftWidth, top)
    gl2.glVertex2d(right - glRightWidth, top)
    gl2.glVertex2d(right - glRightWidth, top - glTopWidth)
    gl2.glEnd()
    // Bottom Bar
        // Top bar of the border, between the first 2 bars
    
    gl2.glBegin(GL2.GL_POLYGON)
    gl2.glVertex2d(left + glLeftWidth, bottom)
    gl2.glVertex2d(left + glLeftWidth,bottom + glBottomWidth)
    gl2.glVertex2d(right - glRightWidth, bottom + glBottomWidth)
    gl2.glVertex2d(right - glRightWidth, bottom)
    gl2.glEnd()
  }
}