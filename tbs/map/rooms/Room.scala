package tbs.map.rooms

import tbs.tiles.Tile
import tbs.map.GameMap

class Room {
  var floorTiles : Array[Tuple3[Int, Int, Tile]] = Array()
  
  var wallTiles : Array[Tuple3[Int, Int, Tile]] = Array()
  
  // Bottom left corner of room.
  protected var _mapCoord : (Int, Int) = (0, 0)
  
  def mapCoord = _mapCoord
  
  def mapCoord_= (x : Int, y : Int) = {
    val diff : (Int, Int) = (mapCoord._1 - x, mapCoord._2 - y)
    (floorTiles++wallTiles).transform((t) => new Tuple3(t._1 + diff._1, t._2 + diff._2, t._3))
    _mapCoord =(x, y)
  }
  
  def overlapsWith(room : Room) : Boolean = {
    // Check our floor tile doesn't replace another room's floor or wall tile. 
    // Intercepting wall tiles are ok (two rooms can share walls)
    ! floorTiles.intersect(room.floorTiles ++ room.wallTiles).isEmpty
  }
  
  def placeOn(map: GameMap) : Unit = {
    // (floorTiles++wallTiles).foreach(t => map.addTile(t._3, t._1, t._2))
  }
}