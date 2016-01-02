package tbs.game.state

import tbs.map.OverworldMap
import tbs.map.TowerMapGenerator
import tbs.party.Party

import java.awt.event.KeyEvent

/**
 * The Tower Overworld is where players move the party through the party.
 * When the party runs into monsters, then the game enters the Tower Combat state.
 */
class TowerOverworld extends GameState {
  var currLevel : Int = _
  var currMap : OverworldMap = TowerMapGenerator.generateMaze(15, 15)
  
  /**
   * Party being controlled
   */
  var party : Party = new Party
  var controller : Int = _
  
  def populateMap(map : OverworldMap) : Unit = {
    // currMap.getTiles.filter(_.isPassable())
  }
  
  /**
   * Keyput Input Parsing
   */
  val partyMovemenInput : (KeyEvent) => Boolean = (ke : KeyEvent) => {
    var keyParsed : Boolean = false
    if (ke.getModifiers == 0) ke.getKeyCode() match {
      case KeyEvent.VK_UP => {
        
      }
      case KeyEvent.VK_DOWN => {
        
      }
      case KeyEvent.VK_LEFT => {
        
      }
      case KeyEvent.VK_RIGHT => {
        
      }
      case _ => 
    } else {
    }
    true
  }

}