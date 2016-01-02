package tbs.dialog

import tbs.entity._
import tbs.action._

/**
 * A menu attached to an actor, which lists all the possible actions the actor can take. 
 * Upon selecting an action, the selectionF is run on the selected action.
 */
class EntityActionSelectionDialog(actor: Entity, selectionF : (Action) => Unit) extends DialogMenu {
  val actions = actor.getActions()
  
  // setGLCoordinates(actor.glX, actor.glY)
  
  setGlDimensions(0.3, 0.5)
  val numActions = actions.size
  for (i <- 0 to actions.size - 1) {
    addComponent(new DialogButton(() => selectionF(actions(i)), 
                                  0, (numActions - i - 1) * 1.0 / numActions.toDouble,
                                  1d, 1.0 / numActions))
  }
}