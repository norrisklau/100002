package tbs.movement

import tbs.map._
import tbs.entity._
import tbs.game.scenario.ScenarioData

import scala.collection.mutable.ArrayBuffer
import tbs.tiles.Tile

/**
 * Represents the movement path taken by an entity during the execution of a movement action.
 * This is used instead merely using a start point and end point system for a number of reasons:
 * 
 * - The player can choose to take several paths to the same tile, offering a little more choice
 * - Some traps and effects trigger en route when an entity moves through a square.
 * 
 * Downside is that choosing a path will take some extra effort in player selection logic.
 */
class MovementData (private val _mover : Entity, 
                    private var path : List[Tile], 
                    private var movementHistory : List[Tile] = Nil) extends Serializable {
	/**
	 * Remove tiles from the path after a certain tile. Call when an entity
	 * is to be stopped midway during movement phase.
	 * 
	 * @param tile
	 * The tile for which all tiles indexed after it are to be removed from the movement tile list.
	 * After invoking removeTilesAfter(t1), t1 should be the new last tile (final destination), IFF
	 * the movement path contains the tile t1.
	 * 
	 * @return
	 * The length of the new tile movement list
	 */
	def clearPathAfterTile(tile : Tile) : Int = {
	  if (path.contains(tile)) {
	    path = path.dropRight(path.indexOf(tile))
	  }
	  movementLength
	}
  
  def clearPath() : Unit = {
    path = Nil
  }
	
  /**
   * Move forward 1 tile along the path (if possible)
   * 
   * @return
   * 
   */
  def increment : MovementData = path match {
    case (next : Tile) :: (rest : List[Tile]) => 
      new MovementData(_mover, path.drop(1), movementHistory ::: List(next))
    case _ => this
  }
  
  /**
   * @return
   * The entity that is moving.
   */
  def mover: Entity = {
    _mover
  }
  
  def getScenarioData () : Option[ScenarioData] = {
    _mover.getScenarioData
  }
  
	/**
	 * Get the path taken up to the current point. 
	 * For example, an entity that originally had a movement path of [t1, t2, t3, t4] and is now 
	 * current in t3 (and about to move to t4) would have a movementHistory of [t1, t2, t3] 
	 * 
	 * @return
	 * An array list of the tile path.
	 */
	def getMovementHistory() : List[Tile] = {
	  movementHistory
	}
	
	/**
	 * Append a tile to the end of the movement path. Tile should be in the same
	 * map as the others in the path, and should not be null.
	 * 
	 * @param tile Tile to add to the path
	 * @return The length of the path after adding the tile
	 * 
	 */
	def addTileToPath(tile : Tile) : Int = {
	  assert(tile != null)
	  path = path ::: List(tile)
	  movementLength
	}
	
	/**
	 * Look at first tile on the list
	 */
	def peekTile() : Tile = {
	  if (! path.isEmpty) {
	    path(0)
	  } else {
	    null
	  }
	}
	
	/**
	 * Remove the next (first) tile from the movement path. 
	 * 
	 * @return
	 * null if the path is empty, the tile that was popped from the path otherwise.
	 */
	def popTile() : Option[Tile] = path match {
    case (tile : Tile) :: (rest : List[Tile]) => {
      path = rest
      Some(tile)
    }
    case Nil => {
      None
    }
	}
	
	/**
	 * Append multiple tiles to the end of this path. Should not contain null tiles.
	 * 
	 * @param tile An Array[Tile] to append to the end of the path
	 * @return The length of the path after adding tiles
	 */
	def addTilesToPath(tiles : List[Tile]) : Int = {
	  path = path ::: tiles
	  movementLength
	}
	
	def movementLength: Int = {
	  path.size
	}
	
	def getPath() : List[Tile] = {
	  path
	}
	
	override def toString : String = {
	  var str : String = "Path: " + mover
	  for (t <- path) {
	    str += "->" + t
	  }
	  str
	}
}