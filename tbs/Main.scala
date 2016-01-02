package tbs

import tbs.game.Game
import tbs.game.state._
import tbs.entity._
import tbs.effect._
import tbs.interactive._
import tbs.player._
import tbs.map._
import tbs.action._
import tbs.game.scenario._
import tbs.dialog._
import scala.collection.mutable._

object Main {
  
  def main(args: Array[String]) = {
    val game : Game = new Game 
    val cake = new Scenario
    val peanuts = new TitleScreen
     
    game.init()
    game.pushState(cake)
  }
}