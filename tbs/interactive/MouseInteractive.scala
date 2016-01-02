package tbs.interactive

import tbs.mouse.TBSMouseEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent

/**
 * Describes interactive elements that can be added to the game. Basically, any stand-alone object that 
 * takes mouse and/or key input can be derived from interactive.
 */
trait MouseInteractive extends MousePressable 
                       with    MouseScrollable
                       with    MouseDraggable {
  // Returns true if mouse events triggered anything from this element
  def parseMouseEvent(mEvent: TBSMouseEvent) : Boolean = {
    var respondedToMouse : Boolean = false
    if (mEvent.eventType == MouseEvent.MOUSE_PRESSED) {
      respondedToMouse = parseMousePress(mEvent)
    } 
    
    if (mEvent.eventType == MouseEvent.MOUSE_DRAGGED) {
      respondedToMouse = parseMouseDrag(mEvent)
      if (respondedToMouse) {
        previousMouseDragEvent = Some(mEvent)
      } else {
        previousMouseDragEvent = None
      }
    } else {
        previousMouseDragEvent = None
    }
    
    if (mEvent.eventType == MouseEvent.MOUSE_MOVED) {

    }
    respondedToMouse
  }
}

/**
 * Describes classes that respond to mouse dragging over them.
 */
abstract trait MouseDraggable {
  @transient var previousMouseDragEvent : Option[TBSMouseEvent] = None
  
  protected def previousDragCoord() : Option[(Double, Double)] = {
    var prvCoord : Option[(Double, Double)]= None
    previousMouseDragEvent match {
      case None => 
      case Some(prv) => prvCoord = Some (prv.mX, prv.mY)
    }
    prvCoord
  }
  
  protected def getDragVector(mEvent: TBSMouseEvent) : (Double, Double) = {
    var vector = (0d, 0d)
    previousDragCoord() match {
      case None => 
      case Some(coords) => vector = (mEvent.mX - coords._1, mEvent.mY - coords._2)
    }
    vector
  }
   
  /**
   * Clear previous drag information. Called when user releases mouse after dragging.
   */
  def resetDrag() : Unit = {
    previousMouseDragEvent = None
  }
  
  protected def parseMouseDrag(mEvent: TBSMouseEvent) : Boolean = false;
}

abstract trait MouseScrollable {
  def parseMouseScroll(mW : MouseWheelEvent) : Boolean = false;
}

/**
 * Classes that handle mouse presses
 */
abstract trait MousePressable {
  protected def parseMousePress(mEvent: TBSMouseEvent) : Boolean = false;
}