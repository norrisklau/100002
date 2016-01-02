package tbs.game

import javax.media.opengl._
import java.util.Stack
import java.awt.Font
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import com.jogamp.opengl.util.awt.TextRenderer
import io.netty.channel._

import tbs.rendering.Renderable
import tbs.mouse.TBSMouseEvent
import tbs.game.state.GameState
import tbs.game.scenario._

import test._
import tbs.glcoord.WindowSizeInfo

/**
 * The game object called by and started in main. Contains the JOGL AWT frame and
 * the various game states. 
 */
class Game { 
  // Initialization
  val states: ListBuffer[GameState] = new ListBuffer
  var awt = new GameAWT(this);

  var windowSizeInfo : WindowSizeInfo = new WindowSizeInfo(0, 0)
  /**
   * Initialise the JOGL and Update threads. 
   * The JOGL thread is in charge of rendering objects in game, and parsing player input.
   * Update is used to cover state based changes that do not rely on player input. For example,
   * updating the animation frame positions of monsters every X milliseconds would be the
   * Update thread's job, not the JOGL thread's.
   * 
   */
  def init () = {
  
    // JOGL thread in here!
    new Thread(new Runnable{
      def run() = {
        awt.init
      }
    }).start
    
  }
  
  /**
   * Push (and make active) a new game state. State types include 
   * overworld map, battle and title menu. If minigames are implemented (not likely),
   * they would also be a state.
   */
  def pushState(state: GameState) : Unit = {
    println("Pushed!")
    state.attachedGame = this
    states += (state)
    currentState().init()
  }
  
  def currentState() : GameState = {
    states.last
  }
  
  def popState() : Unit = {
    if (! states.isEmpty) {
      states.remove(states.size - 1)
    }
    if (states.size == 0) {
      this.exit()
    }
  }
  
  /**
   * Defer keyboard and mouse input logic to the current game 
   * state.
   */
  def parseMouseEvent(mEvent: TBSMouseEvent) = {
    if (! states.isEmpty) {
      states.last.parseMouseEvent(mEvent)
    }
  }
  
  def parseMouseWheelEvent(mW : MouseWheelEvent) = {
    if (! states.isEmpty) {
      states.last.parseMouseScroll(mW)
    }
  }
  
  def parseKeyEvent(kEvent: KeyEvent) = {
    if (! states.isEmpty) {
      states.last.parseKeyEvent(kEvent);
    }
  }
  
  def render(gl2: GL2) = {
    gl2.glClear(GL.GL_COLOR_BUFFER_BIT)
    
    if (states.size > 0) {
      states.last.render(gl2);
      tbs.rendering.GameTextRenderer.drawGlyph(gl2, '#', (32, 32), (64, 64), (0.5, 0.5, 0))
    }
  }
  
  def getGLProfile : GLProfile = {
    awt.capabilities.getGLProfile
  }
  
  def getDrawableDimensions() : (Int, Int) = {
    (getDrawableWidth, getDrawableHeight)
  }
  
  def getDrawableWidth() : Int = {
    if (awt.canvas != null) awt.canvas.getWidth
    else 0
  }
  
  def getDrawableHeight() : Int = {
    if (awt.canvas != null) awt.canvas.getHeight
    else 0
  }
  
  /**
   * Logic and animation that is time dependent is applied here.
   * For example, character idle animations cycle through sprites during the update
   * loop. The current sprite state is then read by the render thread and used to 
   * blit to screen. 
   */
  def update() = {
    if (! states.isEmpty) {
      states.last.update()
    }
  }
  
  /**
   * Netty Channels used to communicate with clients during a network game (when the local
   * game is hosting)
   */
  private val clientChannels : ListBuffer[Channel] = new ListBuffer[Channel]
  
  def addClientChannel(ch : Channel) = {
    println("Adding channel: " + ch)
    clientChannels.append(ch)
    if (currentState.isInstanceOf[ServerScenarioGameState]) {
      currentState.asInstanceOf[ServerScenarioGameState].initClientChannel(ch)
    }
  }
  
  def getClientChannels() : Array[Channel] = {
    clientChannels.toArray[Channel]
  }
  
  def run() = {
    while (! states.isEmpty) {
      states.last.run()
    }
  }

  /**
   * Exit to desktop
   */
  def exit() : Unit = {
    System.exit(0)
  }
  
  def pause : Unit = {
  }
  
}