package tbs.effectqueue

import tbs.effect.Effect
import tbs.event.{TBSEventListener, BeforeEffectApplyEvent, AfterEffectApplyEvent}

class EffectQueue (private var _effectList : List[Effect] = Nil) {
  def enqueue(effect : Effect) : EffectQueue = {
    _effectList =  List(effect) ++ _effectList
    this
  }
  
  private var negatedEffects : List[Effect] = Nil
  
  /**
   * Mark an effect as inactive. This is the method called when say, another
   * effect triggers on preEffectApply and wants that effect to be cancelled.
   * 
   * Negated (Inactive) effects will not be applied, nor will they fire 
   * after application triggers before leaving the queue.
   * 
   * Note: Additional negations do not do anything
   */
  def addNegation(effect : Effect) : Unit = {
    if (! existsNegation(effect))
      negatedEffects = effect :: negatedEffects
  }
  
  def existsNegation(effect : Effect) : Boolean = {
    negatedEffects.contains(effect)
  }
  
  def effectList : List[Effect] = _effectList
  
  /**
   * Removes a negation from an effect. Doesn't do anything if the effect isn't
   * negated.
   */
  def removeNegation(effect : Effect) : Unit = {
    negatedEffects = negatedEffects.filterNot(_ == effect)
  }
  
  /**
   * Attempt to:
   * - Notify Listeners of the next effect on the queue
   * - Pop the next effect on the queue.
   * - Apply the effect
   * - Notify Listeners that the effect has been applied
   * 
   * @return 
   * True if there was an effect to be resolved, even if it was negated during resolution.
   * False if there was no effect to be resolved.
   * 
   */
  private def resolveNext(evL : TBSEventListener) : Boolean = peek match {
    case Some(effect) => {
      dequeue()  
      evL.fireTriggers(new BeforeEffectApplyEvent(effect))
      // Do not apply negated effects
      if (! existsNegation(effect)) {
        println("Resolving " + effect)
        effect.applyEffect(this)
        evL.fireTriggers(new AfterEffectApplyEvent(effect))
      } else {
        println("Negated! : " + effect)
      }
      removeNegation(effect)
      true
    }
    case _ => false
  }
  
  private def peek : Option[Effect] = _effectList match {
    case (nextEffect : Effect) :: (rest : List[Effect]) => {
      Some(nextEffect)
    }
    case Nil => None
  }
  
  private def dequeue() : Option[Effect] = _effectList match {
    case (nextEffect : Effect) :: (rest : List[Effect]) => {
      _effectList = rest
      Some(nextEffect)
    }
    case Nil => None
  }
  
  /** 
   *  Resolve all effects currently in the queue. 
   */
  def resolve(evl : TBSEventListener) : Unit = {
    while (resolveNext(evl)) {
      
    }
  }
}