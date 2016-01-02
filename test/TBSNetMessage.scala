package test

class TBSNetMessage extends Serializable {

}

class JoinRequestMessage extends TBSNetMessage {
  var requestedName : String = "Bob"
}

import tbs.game.scenario.ScenarioData
class LoadScenarioMessage (private val scenarioInfo : ScenarioData) extends TBSNetMessage {
	def getScenarioInformation() : ScenarioData = {
	  scenarioInfo
	}
}

import tbs.action.Action
class ActionMessage (private val action: Action) extends TBSNetMessage {
  def getAction() : Action = {
    action
  }
}

import scala.util.Random
class RandomSyncMessage (private val random : Random) extends TBSNetMessage {
  def getRandom() : Random = {
    random
  }
}