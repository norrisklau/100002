package tbs.map
import tbs.tiles._
import tbs.map.rooms._
import Array._

/**
 * Generates all the random levels in the tower (so not the special levels). Populates the map
 * with items and monsters during the process.
 */
object TowerMapGenerator {
  // V 0.1, generate box rooms, connect box rooms with corridors. 
  
  def createRoom(minSize : Int, maxSize : Int) : Room = {
    var room : Room = new RectangleRoom(4, 3)
    room
  }
  
  import tbs.rng.RNG
  
  def generateMaze(width : Int, height : Int) : OverworldMap = {
    var map = new OverworldMap(width, height)
    for (x <- 0 until width)
      for (y <- 0 until height)
        map.addTile(new OwWallTile, x, y)
        
    var candidates : Array[(Int, Int)] = Array( (RNG.nextInt(width), RNG.nextInt(height)) )
    while (! candidates.isEmpty) {
      val nextCandidate = candidates(RNG.nextInt(candidates.size))
      if (validCandidate(map.getTileAt(nextCandidate._1, nextCandidate._2))) {
        map.addTile(new OwFloorTile, nextCandidate._1, nextCandidate._2)
        val other = map.getTileAt(nextCandidate._1, nextCandidate._2).get.connectedTiles.filter(t => validCandidate(Some(t)))
        for (t <- other) {
          candidates = candidates :+ (t.mapX, t.mapY)
        }
      }
      candidates = candidates.filterNot(_ == nextCandidate)
    }
    map
  }
  
  def validCandidate(tile : Option[Tile]) : Boolean = tile match {
    case None => true
    case Some (t) => t.connectedTiles.filter(_.isInstanceOf[OwFloorTile]).size <= 1
  }
}

private class candidateMap {
  
}

