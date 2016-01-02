package tbs.event

import tbs.id.HasID

/**
 * Triggers have two components, an EventType, and a function that fires each time the 
 * component is met.
 */
class Trigger (val func: (TBSEvent) => Unit) extends HasID {
  def fire (event: TBSEvent) : Unit = {
    func(event)
  }
  
  def hasHigherFiringPriorityThan (other: Trigger) : Boolean = {
    true
  }

}