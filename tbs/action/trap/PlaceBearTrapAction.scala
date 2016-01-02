package tbs.action.trap

import tbs.effect._
import tbs.entity._

class PlaceBearTrapAction (dmgFunc : (Entity) => Int, stopDuration : Int) 
                          extends PlaceTileTrapAction (() => new BearTrapEffect (dmgFunc, stopDuration)) {
  
  override def execute() = {
    super.execute()
	}
}