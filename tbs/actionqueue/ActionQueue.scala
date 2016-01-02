package tbs.actionqueue

import tbs.action.Action
import scala.collection.mutable.ListBuffer

import tbs.event.TBSEventListener

/**
 * When an actor chooses to use an action, the action goes into the 
 * ActionQueue for resolution. 
 */
class ActionQueue (private var actionList : List[Action] = Nil) {

  
  def enqueueAction(action: Action) : Int = {
    1
  }
  
  def resolveNext(listener : TBSEventListener) : Boolean = actionList match {
    case (action : Action) :: (rest : List[Action]) => {
      // Trigger Action
      action.resolve()
      // Trigger Action
      true
    }
    case Nil => false
  }
  
  def resolve(listener : TBSEventListener) : Int = {
    // While action queue is empty, keep resolving the next action
    
    1
  }
}