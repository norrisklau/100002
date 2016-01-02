package tbs.event

import scala.collection.mutable.{Map, HashMap}
import scala.collection.mutable.ArrayBuffer

trait TBSEventObserver extends Serializable {
  val listenerMap = new HashMap[String, ArrayBuffer[TBSEventListener]] 
  
  /**
   * Add listener to the map, at all the keys associated with the passed
   * event (types)
   */
  def addListener (listener : TBSEventListener, events: TBSEvent*) = {
    for (ev <- events) {
      if (! listenerMap(ev.typeString).contains(listener) ) {
        listenerMap(ev.typeString).append(listener)
      }
    }
  }
  
  def removeListener (listener : TBSEventListener, events : TBSEvent*) = {
    // If no event type is passed, then remove listener for all events
    if (events.isEmpty) {
      for (lList <- listenerMap.values) {
        if (lList.contains(listener)) {
          lList.remove(lList.indexOf(listener))
        }
      }
    } else {
      for (ev <- events) {
        val lList = listenerMap(ev.typeString)
        if (lList.contains(listener)) lList.remove(lList.indexOf(listener))
      }
    }
  }
  
  def notifyListeners (event : TBSEvent) = {
    event.typeString
  }
}