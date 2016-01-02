package tbs.effect

import tbs.effectqueue.EffectQueue
import tbs.game.scenario.Scenario
/**
 * Negate (Cancel) another effect that is about to be applied in the queue.
 */
class NegationEffect (val _scenario : Scenario, val effectFilter : (Effect) => Boolean) extends Effect {
  override def scenario : Option[Scenario] = Some(_scenario)
  private var negatedEffects : List[Effect] = Nil
  
  override def applyEffect(effectQueue : EffectQueue) : Unit = {
    effectQueue.effectList.filter(effectFilter).foreach((e) => {
      negatedEffects = e :: negatedEffects
      effectQueue.addNegation(e)
    })
  }
  
  override def unapplyEffect(effectQueue : EffectQueue) : Unit = {
    negatedEffects.foreach(effectQueue.removeNegation)
  }
  
  override def toString() : String = {
    super.toString() + "( " + negatedEffects + " )"
  }
}