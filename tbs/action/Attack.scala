package tbs.action

import tbs.entity._
import tbs.game.scenario.Scenario
import tbs.effect._
import tbs.random._

import scala.collection.immutable.List
  
// Offensive abilities with a chance of success and failure, and separate effects for each.
// (i.e. on hit, do damage, on miss, do half damage)
trait Attack extends EntityTargetingAction with HitAndMiss {
  var rollAttackF : () => Boolean = () => false

  protected var hitEffects : List[() => Effect] = Nil
  protected var missEffects : List[() => Effect] = Nil
  
  def addHitEffect(effect : () => Effect) {
    hitEffects = hitEffects ++ List(effect)
  }
  
  def removeHitEffect(effect : EntityEffect) {
    
  }
  
  override def execute() = {
    if (isValid()) {
      // map.onActionBefore(this).applyFunctions();
      println(getUser().get + " attacks " + getTarget().get)
      attemptHit()
      // user.map.onActionAfter(this).applyFunctions();
    }
  }
  
  override def resolveHit() = {
    (getTarget, getTarget.flatMap(_.scenario) ) match {
      case (Some(target), Some(sc)) => {
        hitEffects.foreach((e) => sc.enqueueEffect(e.apply()))
      }
      case _ =>
    }
  }
  
  override def resolveMiss() = {
    (getTarget, getTarget.flatMap(_.scenario) ) match {
      case (Some(target), Some(sc)) => {
        missEffects.foreach((e) => sc.enqueueEffect(e.apply()))
      }
      case _ =>
    }
  }
  
  override def checkIfHit () : Boolean = {
    rollAttackF()
  }
}

/**
 * Attacks that depend on weapon stats.
 */
class WeaponAttack extends Attack {
  
  override def isValidTarget(e : Entity) = {
    e != getUser().get && getUser().get.xyMapDistanceToEntity(e) == 1
  }
  
  rollAttackF = () => {
    (getUser(), target) match {
      case (Some(user), Some(tar)) => true
      case _ => false
    }
  }
  
}

class CounterAttack extends WeaponAttack with Reactive {

}

class BasicAttack extends WeaponAttack with Proactive {
  
}