package tbs.game.state

import tbs.dialog._
import java.util.Date

/**
 * The starting menu is a single dialogue displayed over a background image. 
 * Current buttons should consist of 
 * 
 * [New Game]
 * [Continue]
 * [Join Game]
 * [Settings]
 * [Exit]
 */
class TitleScreen extends GameState {
  val titleMenu = new TitleMenu
  // this.addDialog(titleMenu)
  
  var time = new Date().getTime()
  
  override def update() = {

  }
}

class TitleMenu extends DialogMenu {
  addComponent(new DialogButton(() => println("Connecting to server ..."),
      0.1,
      0.1,
      0.2,
      0.8
      
  ))
  setGlCoords(-0.9, -0.9)
  setGlDimensions(1.8, 1.8)
}