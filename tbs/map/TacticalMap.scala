package tbs.map

import tbs.tiles.TacticalTile
import tbs.entity.Entity

class TacticalMap (override val xSize : Int, override val ySize : Int) extends GameMap (xSize, ySize : Int) {
  def addTile(tile : TacticalTile, x : Int, y : Int) = {
    super.addTile(tile, x, y)
  }
  
  def tacticalTiles : List[TacticalTile] = {
    var tacTiles : List[TacticalTile] = Nil
    for (t <- getTiles) t match {
      case (tile : TacticalTile) => tacTiles = tacTiles :+ tile
      case _ =>
    }
    tacTiles
  }
}