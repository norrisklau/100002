package tbs.event

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.PriorityQueue
import scala.collection.immutable.Map

import tbs.action.Action
import tbs.damage.DamageData
import tbs.movement._

trait TBSEventListener extends Serializable {
  /**
   * EVENT STUFF. Fires functions on notification.
   */
  // Base class of onEvent triggering.
  var triggers : List[Trigger] = Nil
  
  class OnEvent [T] extends Serializable {
    type eventTuple = ((T) => Unit, Int, String)
    
    private var functions = new ArrayBuffer[eventTuple]()
    
    def addFunction(function: (T) => Unit, priority : Int = 0, description : String = "Nondescript trigger") = {
      functions.append((function, priority, description))
      functions = functions.sortWith( _._2 < _._2) 
    }
    
    def removeFunction(f: (T) => Unit) = {
      functions.find (_._1 == f) match {
        case Some(f) => functions.remove(functions.indexOf(f))
        case _ => System.err.println(f + " not found.")
      }
    }
    
    def fireFunctions(arg: T) = {
      // Order (function, priority) tuples by the priority (second) value
      functions.sortWith( (t1 : eventTuple, t2 : eventTuple) => t1._2 < t2._2).foreach(_._1(arg))
    }
  }
  
  def addTrigger(trigger : Trigger) : Unit = {
    triggers = triggers ::: List(trigger)
  }
  
  def fireTriggers(event: TBSEvent) : Unit = {
    triggers.sortWith(_.hasHigherFiringPriorityThan(_)).foreach(_.fire(event))
  }
  
  def removeTrigger(trigger : Trigger) : Unit = {
    triggers = triggers.filterNot(_.sameIdAs(trigger))
  }
  
  import tbs.entity.Entity
  val onBeforeTurnStart = new OnEvent[Entity]
  val onAfterTurnStart = new OnEvent[Entity]
  
  val onBeforeAction = new OnEvent[Action]
  val onAfterAction = new OnEvent[Action]
  
  val onBeforeDamage = new OnEvent[DamageData]
  val onAfterDamage = new OnEvent[DamageData]
  
  import tbs.heal.HealData
  val onBeforeHeal = new OnEvent[HealData]
  val onAfterHeal = new OnEvent[HealData]
  
  val onBeforeMovement = new OnEvent[MovementData]
  val onAfterMovement = new OnEvent[MovementData]
}