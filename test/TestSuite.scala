package test

import tbs.game.scenario._
import tbs.action._
import tbs.map._

object TestSuite {
	def main (args: Array[String]) {
	  println("Running entity test ...")
	  val g = new ServerScenarioGameState(new ScenarioData)
	  if (EntityTest.run()) {
	    println("EntityTest is a success")
	  }
	  EntityTest.testSerializability(g.scenarioData)
	  
	  val s = new ScenarioData
	  val m : GameMap = s.getMap()
	  val a = new RookMovementAction()
	  a.maximumRange_=(6)
    EntityTest.testSerializability(s)
	}
}