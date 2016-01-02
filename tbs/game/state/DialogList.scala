package tbs.game.state

import javax.media.opengl.GL2
import java.awt.Font
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import com.jogamp.opengl.util.awt.TextRenderer
import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.TextureIO
import java.io.File
import scala.collection.mutable.ArrayBuffer

import tbs.game.Game
import tbs.mouse.TBSMouseEvent
import tbs.interactive._
import tbs.rendering._
import tbs.map._
import tbs.dialog.Dialog

/**
 * Keeps track of dialogs within a gamestate. Basically a wrapper around
 * a list of Dialogs, with some helper functions to deal with rendering
 * and mouse/key interaction.
 */
class DialogList extends ArrayBuffer[Dialog] with MouseInteractive
                                             with KeyboardInteractive
                                             with Renderable
                                             with TextureUsing {
  var focusIndex : Option[Int] = None
  var focus : Option[Dialog] = None
  
  /**
   * The focus is the dialog that has priority in parsing key and mouse commands
   * (i.e. The Focus that is Renderable on the 'top'. 
   */
  def getFocus() : Option[Dialog] = {
    focus
  }
  
  def setFocus(index : Int) : Unit = {
    assert(index >= 0 && index < size)
    setFocus(this(index))
  }
  
  def setFocus(dialog : Dialog) : Unit = {
    if (contains(dialog)) {
      remove(indexOf(dialog))
      append(dialog)
      focus = Some(dialog)
    }
  }
  
    
  def unsetFocus() : Unit = {
    focus = None
  }
 
  // Cycle to the next dialog in the list
  def focusNext() : Unit = {
    // If we're not focused on anything, 
    // focus on front most (rendering-rendering wise) dialog
    if (focus == None && ! isEmpty) {
      focus = Some(last)
    }
    else if (size > 1) {
      setFocus(size - 2) // The next dialog (the one behind the focus) is the 2nd last one
    }
  }
  
  override def parseKeyEvent (ke : KeyEvent) : Boolean = {
    var keyConsumed = false
    if (ke.getKeyCode() == KeyEvent.VK_TAB) { // Tab between sub dialogs/menus
      if (size > 1) {
        keyConsumed = true
        focusNext()
      }
    } else {
      getFocus() match {
        case Some(d) => {
          keyConsumed = true
          d.parseKeyEvent(ke)
        }
        case None =>
      }
    }
    keyConsumed
  }
  
  override def parseMouseScroll(mW : MouseWheelEvent) : Boolean = {
    getFocus() match {
      case Some(d : MouseScrollable) => d.parseMouseScroll(mW)
      case _ => false
    }
  }
  
  override def parseMousePress (pressEvent: TBSMouseEvent) : Boolean = {
    var pressConsumed = false
    for (dialog <- this.reverse) {
      dialog match {
        case a : MousePressable if ! pressConsumed => {
          pressConsumed = a.parseMouseEvent(pressEvent)
        }
        case _ =>
      }
    } 
    if (! pressConsumed) unsetFocus()
    pressConsumed
  }
  
  override def parseMouseDrag (dragEvent : TBSMouseEvent) : Boolean = {
    // Dragging doesn't allow you to change focus (if you drag over a dialog but you hadn't
    // clicked/focused on it before, you don't affect that dialog
    getFocus() match {
      case Some(a : MouseDraggable) => a.parseMouseEvent(dragEvent)
      case _ => false
    }
  }
  
  /**
   * Call updates on the same things that get Renderable. 
   */
  override def update() : Unit = {
    for (dialog <- this) yield {
      dialog.update()
    }
  }
  
  /**
   * Rendering methods
   */
  override def render(gl: GL2) : Unit = {
    // Last dialog is in front
    for (dialog <- this) {
      dialog.render(gl)
    }
  }
  
  /**
   * TextureUsing methods
   */
  
  //Load textures for dialogs that use them
  override def initTextures(table: TextureTable) = {
    for (dialog <- this) {
      dialog match {
        case d : TextureUsing => {
          d.initTextures(table)
        }
        case _ => 
      }
    }
    // Do table.requestTexture(path) here
  }
  
  override def freeTextures(table: TextureTable) = {
    for (dialog <- this) {
      dialog match {
        case d : TextureUsing => {
          d.freeTextures(table)
        }
        case _ => 
      }
    }
  }
  
}