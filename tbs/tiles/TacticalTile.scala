package tbs.tiles

import tbs.map.TacticalMap
import tbs.entity.Entity

/**
 * TacticalTiles can *only* be attached to tactical maps. There should be
 * no public method that lets you attach this to a non TacticalMap
 */
class TacticalTile extends Tile {
  def attachToMap(map : TacticalMap) = {
    attachedMap = Some(map)
  }
  
  override def currentMap : Option[TacticalMap] = {
    attachedMap.asInstanceOf[Option[TacticalMap]]
  }
}