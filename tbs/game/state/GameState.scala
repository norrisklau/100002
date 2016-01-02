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
import tbs.event.{TBSEvent, TBSEventObserver, TBSEventListener}

/**
 * A GameState represents a part of a game with its own distinct behavior and logic. For example,
 * a battle would be a clearly different and distinct section of the game compared to an overworld map.
 * 
 * The game would transition between states during the course of gameplay, deferring logic and 
 * rendering/input to the active state.
 */
abstract class GameState extends Renderable
                         with MouseInteractive
                         with KeyboardInteractive
                         with TBSEventObserver {
  var attachedGame : Game = _
  var dialogFocus : Option[Dialog] = None
  
  // Table containing textures this current game state needs
  protected var textureTable : TextureTable = new TextureTable
  @volatile var dialogList : DialogList = new DialogList
  
  def init() : Unit = {
    initMedia()
  }
  // Initialise all textures and other media we need.  
  def initMedia() = {
    for (d <- dialogList) {
      d match {
        case a : TextureUsing => a.initTextures(textureTable)
        case _ => 
      }
    }
  }
  
  // Free up all media we loaded, save memory!
  def freeMedia() {
    textureTable.clear();
  }
  
  /**
   * Load texture from file path into the table
   */
  def loadTexture(fileName : String) : Int = {
    textureTable.loadTexture(fileName)
  } 
  
  /**
   * 
   */
  def getTexture(id : String) : Texture = {
    textureTable.getTexture(id)
  }
  
  /**
   * Handle keyboard input, apply changes if needed to objects that are part of this gamestate (dialogs, monsters etc.)
   */
  override def parseKeyEvent (ke : KeyEvent) : Boolean = {
    dialogList.parseKeyEvent(ke)
  }
  
  def renderText(s: String, size: Int, glX : Double, glY : Double) {
    val renderer = new TextRenderer(new Font("Monospaced", Font.PLAIN, size), true, true)
    renderer.begin3DRendering()
    renderer.setColor(1.0f, 1.0f, 1.0f, 0.5f)
    renderer.draw3D(s, glX.toFloat, glY.toFloat, 0, 1.0f / size * 1.0f)
    renderer.end3DRendering()
  }
  
  override def render(gl: GL2) : Unit = {
    dialogList.render(gl)
  }
  
  /**
   * Call updates on the same things that get Renderable. 
   */
  override def update() : Unit = {
    dialogList.update()
  }
  
  /**
   * Check if our active dialog (the focus) is scrollable. If it is, tell it to parse
   * this scroll event. If not, don't do anything.
   */
  override def parseMouseScroll(mW : MouseWheelEvent) : Boolean = {
    dialogList.parseMouseScroll(mW)
  }
  
  override def parseMousePress (pressEvent: TBSMouseEvent) : Boolean = {
    dialogList.resetDrag()
    dialogList.parseMousePress(pressEvent)
  }
  
  /**
   * Parse mouse dragging. Returns true iff the dragEvent was consumed by the
   * active dialog. 
   */
  override def parseMouseDrag (dragEvent: TBSMouseEvent) : Boolean = {
    dialogList.parseMouseDrag(dragEvent)
  }
  
  // Add a dialog menu to screen.
  def addDialog (d : Dialog) : Unit = {
    // Append to the back of the list of interactive elements. It is Renderable out front 
    // since it's the last to be called.
    d.attachToGameState(this)
    if (! dialogList.contains(d)) {
      println("Dialog Added")
      dialogList.append(d)
    }
  }
  
  def removeDialog(d : Dialog) : Unit = {
    for (toRemove <- dialogList.filter((dialog : Dialog) => {dialog == d})) {
      dialogList.remove(dialogList.indexOf(toRemove))
    }
  }
  
  def popDialog() : Unit = {
    if (! dialogList.isEmpty) {
      dialogList.remove(dialogList.size - 1)
    }
  }
  
  // Change to a different state. This state is still on the stack, and will be 
  // returned to if we transitionBack()
  def transitionTo(nextState: GameState) : Unit = {
      attachedGame.pushState(nextState)
  }
  
  // Return to the previous state
  def transitionBack() : Unit = {
      attachedGame.popState()
  }
  
  // Return Some(dialog) if one exists that satisfies the condition, otherwise None
  def findDialog(condition: (Dialog) => Boolean) : Option[Dialog] = {
      dialogList.find(condition)
  }
  
  // Focus is the last element of dialogs
  def getDialogFocus() : Option[Dialog] = {
    dialogList.getFocus()
  }
  
  /**
   * Set the focus of this game state's input / output on an dialog box. 
   */
  def setDialogFocus(dialog : Dialog) : Unit = {
    dialogList.setFocus(dialog)
  }
  
  def run() : Unit = {
    
  }
}