package tbs.event

import tbs.movement.MovementData
import tbs.effect.Effect

abstract class TBSEvent {
  val typeString : String = ""
}

abstract class EffectEvent extends TBSEvent {
  val effect : Effect
}

case class BeforeEffectApplyEvent (override val effect : Effect) extends EffectEvent {
  
}

case class AfterEffectApplyEvent (override val effect : Effect) extends EffectEvent {
  
}

// Movement Events
abstract class MovementEvent extends TBSEvent {
  val movementData : MovementData
}

class AfterMovementEvent (override val movementData: MovementData) extends MovementEvent {
  
}
