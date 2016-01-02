package tbs.effect

import scala.collection.mutable.ArrayBuffer
import tbs.id.HasID

/**
 * Classes to which we can attach effects. 
 * Basically, this means entities, tiles and items (which are yet to be implemented)
 */
trait EffectAttachable [T <: HasID] {
  private var _attachedEffects : ArrayBuffer[TargetingEffect[T]] = new ArrayBuffer[TargetingEffect[T]]
  
  def attachEffect(effect : TargetingEffect[T]) : Int = {
    _attachedEffects.append(effect)
    _attachedEffects.size
  }
  
  def attachedEffects: Array[TargetingEffect[T]] = {
    _attachedEffects.toArray[TargetingEffect[T]]
  }
  
  def removeEffect(effect : TargetingEffect[T]) : Unit = {
    removeEffect( (e) => {e == effect } ) 
  } 
  
  def removeEffect(filter : (TargetingEffect[T]) => Boolean) : Unit = {
  	_attachedEffects = _attachedEffects.filterNot(filter)
  }
}