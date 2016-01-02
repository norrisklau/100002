package tbs.id

import java.util.UUID

trait HasID {
	private var id : String = this.getClass() + " : " + UUID.randomUUID()
	
	def copyID(other : HasID) : Unit = {
	  this.id = other.id
	}
	
	def getID() : String = {
	  id
	}
  
  def hasID(otherID: String) : Boolean = {
    otherID == getID()
  }
  
  def sameIdAs (otherObj: HasID): Boolean = {
    hasID( otherObj.getID() )
  }
}