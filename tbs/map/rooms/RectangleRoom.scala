package tbs.map.rooms

import tbs.tiles._

// Width does not include wall tiles! 
class RectangleRoom (width : Int, height : Int) extends Room {
  
  wallTiles = Array()
  floorTiles = Array()
  
  for (x <- 0 to width + 1)
    for (y <- 0 to height + 1)
      if (x == 0 || x == width + 1 || y ==0 || y == height + 1)
        wallTiles = wallTiles :+ (x, y, new WallTile)
      
  for (x <- 1 to width)
    for (y <- 1 to height)
      floorTiles = floorTiles :+ (x, y, new FloorTile)
}