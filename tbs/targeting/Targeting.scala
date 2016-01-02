package tbs.targeting

import tbs.id.HasID

/**
 * Actions that target *something*. This includes Tiles, other Entities, 
 * or Items ... conceivably. Some actions (such as self healing actions) do not
 * require a targeting field, but can conceivably use one. 
 */
trait Targeting[T <: HasID] {
  @transient protected var target : Option[T] = None
  protected var targetID : Option[String] = None
  
  def getValidTargets() : List[T] = Nil 
  
  def setTarget(tar : T) : Unit = {
    assert(tar != null)
    target = Some(tar)
    targetID = Some(tar.getID())
  }
  
  def getTarget() : Option[T] = {
    target
  }
  
  def getTargetID() : Option[String] = {
    targetID
  }
  
  def isValidTarget(tar : T) : Boolean = {
    false
  }
  
  override def toString() : String = {
    super.toString() + " with target " + target.toString()
  }
} 