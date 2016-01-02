package tbs.tiles

import tbs.entity.Entity

import tbs.random.UsesRandom

class WallTile extends Tile with UsesRandom {
  glyph = '#'
  
  override def isPassable(mover : Entity) = false
}