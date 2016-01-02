package tbs.game

import java.awt.Frame
import java.awt.Font
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseMotionAdapter
import javax.media.opengl.awt.GLCanvas
import javax.media.opengl._

import com.jogamp.opengl.util.FPSAnimator

import tbs.mouse.MouseCoord
import tbs.mouse.TBSMouseEvent
import tbs.glcoord.WindowSizeInfo
import com.jogamp.opengl.util.awt.TextRenderer

class GameAWT (val runningGame: Game,
               val windowText: String = "Game", 
               val defaultWidth: Int = 640, 
               val defaultHeight: Int = 640) {
  
  var glp : GLProfile = null
  var capabilities : GLCapabilities = null
  var canvas :GLCanvas = null
  var wsi : WindowSizeInfo = null
  
  def init: Unit = {
    // Initialize GL settings / context
    glp = GLProfile.getDefault()
    capabilities = new GLCapabilities(glp)
    canvas = new GLCanvas(capabilities)
    canvas.setFocusTraversalKeysEnabled(false)
    canvas.setFocusable(true)
    canvas.setVisible(true)
    
    // Add listeners
    canvas.addGLEventListener(new GLEventListener() {
      // Called whenever the window frame is resized
      override def reshape (aDraw: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int): Unit = {
        // Do any scaling we need for the game elements : >
        println("Window resized to : " + canvas.getWidth + ", " + canvas.getHeight)
      }
      
      override def init (aDraw: GLAutoDrawable): Unit = {
        aDraw.getGL().glEnable(GL.GL_BLEND)
        aDraw.getGL().glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA)
        // Window initialization 
        canvas.getContext().makeCurrent()
        
        // Initialise GL Info 
        tbs.glcoord.Pixel2GL.init(aDraw)
        // Load textures
        tbs.rendering.GameTextRenderer.glDrawable_=(aDraw)
        val fontPath = "src/art/unifont.bmp"
        // Character dimensions in pixels
        val CHAR_WIDTH = 16
        val CHAR_HEIGHT = 16
        tbs.rendering.GameTextRenderer.loadFontTexture("src/art/unifont.bmp", CHAR_WIDTH, CHAR_HEIGHT)
      }
      
      // Notifies the listener to perform the release of all OpenGL resources
      override def dispose(aDraw: GLAutoDrawable): Unit = {
        
      }
      
      override def display(aDraw: GLAutoDrawable): Unit = {
        runningGame.render(canvas.getGL.getGL2);
      }
    } );

    
    canvas.addKeyListener(new KeyAdapter() {
      override def keyPressed(e: KeyEvent) = {
        runningGame.parseKeyEvent(e);
      }
    } );
    
    canvas.addMouseListener(new MouseAdapter() {
      override def mousePressed(mEvent: MouseEvent) = {
        val gameMouseEvent = new TBSMouseEvent(mEvent, canvas, MouseEvent.MOUSE_PRESSED);
        runningGame.parseMouseEvent(gameMouseEvent);
      }
      
      override def mouseReleased(mEvent: MouseEvent) = {
        val gameMouseEvent = new TBSMouseEvent(mEvent, canvas, MouseEvent.MOUSE_RELEASED);
        runningGame.parseMouseEvent(gameMouseEvent);
      }
    } );
    
    // Dragging and moving the mouse
    canvas.addMouseMotionListener(new MouseMotionAdapter() {
      override def mouseDragged(mEvent: MouseEvent) = {
        val gameMouseEvent = new TBSMouseEvent(mEvent, canvas, MouseEvent.MOUSE_DRAGGED);
        runningGame.parseMouseEvent(gameMouseEvent);
      }
      
      override def mouseMoved(mEvent: MouseEvent) = {
        val gameMouseEvent = new TBSMouseEvent(mEvent, canvas, MouseEvent.MOUSE_MOVED);
      }
    } );
    
    canvas.addMouseWheelListener(new MouseWheelListener() {
      override def mouseWheelMoved(mwEvent: MouseWheelEvent) = {
        runningGame.parseMouseWheelEvent(mwEvent)
      }
    } );
    
    val animator: FPSAnimator = new FPSAnimator(canvas, 60)
    animator.start();
    
    // Window frame
    val frame: Frame = new Frame(windowText)
    frame.setSize(defaultWidth, defaultHeight);
    frame.add(canvas)
    frame.setVisible(true)
    
    frame.addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent) = {
        System.exit(0);
      }
    } );
  }
}