package tbs.map

import tbs.party.Party
import tbs.tiles.OverworldTile

/**
 * The overworld map is the 'main' map in the tower where the player party explores
 * the level and monster parties rove around. There are no 'entities' on this level, 
 * only parties of entities.
 * 
 * Think of this as an abstracted, shrunk version of what would be a massive tower floor.
 * Each tile of movement takes maybe 10 minutes of real time, so traversing a level would
 * take hours in real life.
 */
class OverworldMap (override val xSize : Int, override val ySize : Int) extends GameMap (xSize, ySize : Int) {
  def addTile(tile : OverworldTile, x : Int, y : Int) = {
    super.addTile(tile, x, y)
  }

  def addParty() = {
    
  }
}