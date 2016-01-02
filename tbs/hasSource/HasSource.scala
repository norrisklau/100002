package tbs.hasSource

import tbs.id.HasID

/**
 * Tags effects, abilities, entities that have a 'source' from which it came from. 
 * Useful when applying effects that only trigger on damage that comes from meleeattacks, 
 * for example, as the damage would be sourced from 'MeleeAttack'
 */
trait HasSource {
  private var _source : Option[HasID] = None
  
  def source : Option[HasID] = _source
  
  def source_= (src : Option[HasID]) : Unit = {_source = src}
  
  /**
   * Calls source_=(Some(src)). Just saves typing Some(...) if you're lazy.
   */
  def source_= (src : HasID) : Unit = source_=(Some(src))
  
  /**
   * Recursively check if our sources match a specific criteria.
   * For example, a damage effect may originate from a trapeffect that originated
   * from a TrapAction. In this case, isSourcedFrom will let you check for
   * the original action source using 'isSourcedFrom(_.isInstanceOf[TrapAction])
   * (Assuming there is a link of sources from this object back up to the generating effect)
   * 
   */
  def isSourcedFrom(matchFunc : (HasID) => Boolean) : Boolean = source match {
    case Some(s) => s match {
      case hSrc : HasSource => matchFunc(s) || hSrc.isSourcedFrom(matchFunc)
      case _ => matchFunc(s)
    }
    case None => false
  }
}