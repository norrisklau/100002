package tbs.action

import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import scala.math._
import Array._

import tbs.entity._
import tbs.effect._
import tbs.map._
import tbs.movement._
import tbs.tiles._


/**
 * MovementAction describes the set of actions that get a unit from point A to point B.
 * 
 */
abstract class MovementAction extends TileTargetingAction with Proactive with Ranged {
  var _range : Int = 1
  
  def validTilesInRange (tile: TacticalTile, r : Int) : Array[TacticalTile]
  
  def canReachTile : (TacticalTile) => Boolean = (t) => true
  
  /**
   * Check if an entity can use a certain tile as part of its movement. 
   * This may not necessarily be the same as a valid tile in range. 
   * 
   * For example, a tile which an ally occupies may be passed through, but clearly
   * not a valid destination for movement.
   * 
   * @param tile The tile through which you are checking for valid movement
   * 
   * @return
   * true if movement can incorporate this tile as part of its path, false otherwise.
   */
  def canPassTile (tile : TacticalTile) : Boolean = {
    true
  }
  
  /**
   * Get the distance between user and a target tile.
   * 
   * @param tile the tile to which the distance from this actor is being calculated.
   * 
   * @return If the actor can reach the tile, the distance to it is returned. Otherwise, -1
   */
  def movementDistanceTo(tile : TacticalTile) : Int = {
    assert(getUser() != None && getUser().get != null)
    var distance : Int = -1
    // BFS starting from the creature's current tile 
    ( getUser().flatMap(_.getCurrentMap()) , getUser().flatMap(_.getCurrentTile()) ) match {
      case (Some(map), Some(currentTile : TacticalTile)) if (currentTile.currentMap == Some(map)) => {
	      val toSearch : ArrayBuffer[(TacticalTile, Int)] = new ArrayBuffer()
	      toSearch.append((currentTile, 0))
	      
	      var searched : Array[Array[Boolean]] = ofDim[Boolean](map.getNumberOfColumns(),
	                                                            map.getNumberOfRows())
	          
	      for (y <- 0 to searched.length - 1)
	        for (x <- 0 to searched(y).length - 1)
	          searched(y)(x) = false
	      // Mark searched locations
	      searched(currentTile.mapY)(currentTile.mapX) = true
	      while (! toSearch.isEmpty ) {
	        val nextTile = toSearch.head._1
	        val currDistance = toSearch.head._2
	        if (nextTile == tile) distance = currDistance
	        else {
	          for (t <- nextTile.connectedTiles) {
	            if (isValidTarget(t.asInstanceOf[TacticalTile]) && ! searched(t.mapY)(t.mapX)) {
	              searched(t.mapY)(t.mapX) = true
	              toSearch.append((t.asInstanceOf[TacticalTile], currDistance + 1))
	            }
	          }
	        }
	        // Dequeue
	        toSearch.remove(0)
	      }
      }
      case _ => 
    }
    distance
  }
  
  // Flood filling
  def movementDistanceBetween(from : TacticalTile, to : TacticalTile) = {
    var distance = -1
     ( from.currentMap , to.currentMap ) match {
      case (Some(map1), Some(map2)) if (map1 == map2) => {
	      val toSearch : ArrayBuffer[(TacticalTile, Int)] = new ArrayBuffer()
	      toSearch.append((from, 0))
	      
	      var searched : Array[Array[Boolean]] = ofDim[Boolean](map1.getNumberOfColumns(),
	                                                            map1.getNumberOfRows())
	          
	      for (y <- 0 to searched.length - 1)
	        for (x <- 0 to searched(y).length - 1)
	          searched(y)(x) = false
	      // Mark searched locations
	      searched(from.mapY)(from.mapX) = true
	      while (! toSearch.isEmpty ) {
	        val nextTile = toSearch.head._1
	        val currDistance = toSearch.head._2
	        if (nextTile == to) distance = currDistance
	        else {
	          for (tile <- nextTile.connectedTiles) tile match {
              case (t : TacticalTile) => {
                if (canPassTile(t) && ! searched(t.mapY)(t.mapX)) {
                  searched(t.mapY)(t.mapX) = true
                  toSearch.append((t, currDistance + 1))
                }
	            }
              case _ => 
	          }
	        }
	        // Dequeue
	        toSearch.remove(0)
	      }
      }
      case _ => 
    }
    distance
  }
  
  /**
   * Get the (or a) shortest path between two tiles using this movement action. BFS.
   */
  def getShortestPath(from : TacticalTile, dest : TacticalTile, searched : Array[Array[Int]] = null, d : Int = 1) : List[TacticalTile] = {
    var path : List[TacticalTile] = Nil
    var searchedMap = searched
    // Check if they're on the same map
    if (! from.isOnSameMapAsTile(dest)) {
      
    } else if (from == dest) { // We found the target
      path = path ::: List(dest)
    } else {
      // Make a new searched array if we have none 
      if (searchedMap == null) {
        val dim = from.currentMap.get.getDimensions
        searchedMap = ofDim[Int](dim._1, dim._2)
        for (y <- 0 to searchedMap.length - 1)
	        for (x <- 0 to searchedMap(y).length - 1)
	          searchedMap(y)(x) = -1
      }
      var shortestPath : List[TacticalTile] = Nil
      for (tile <- from.connectedTiles) tile match {
        case (t : TacticalTile) => {
          if (canPassTile(t) && (searchedMap (t.getMapX())(t.getMapY()) == -1 || 
              									 searchedMap (t.getMapX())(t.getMapY()) > d )) {
            searchedMap (t.getMapX())(t.getMapY()) = d
            val candidatePath = getShortestPath(t, dest, searchedMap, d+1)
            if (candidatePath.length != 0 && (shortestPath.length == 0 || shortestPath.length > candidatePath.length)) {
              shortestPath = candidatePath
            }
          }
        }
        case _ => 
      }
      if (shortestPath.length > 0) {
        path = path ::: List(from)
        path = path ::: shortestPath
      }
    }
    path
  }
  
    /**
   * Wrapper for movementDistanceTo(TacticalTile) with x y map coordinates instead.
   * 
   * @param x x coordinates of the tile for which the distance to is calculated
   * @param y y coordinates of the tile for which the distance to is calculated
   * 
   * @return
   * If the tile is not reachable (or does not exist), returns -1
   * Else, the distance to it in movement units.
   */
  def movementDistanceTo(x : Int, y : Int) : Int = {
    assert(getUser() != None)
    var d : Int = -1
    getUser().flatMap(_.currentMap) match {
      case Some(map) => map.getTileAt(x, y) match {
        case Some(t : TacticalTile) => {d = movementDistanceTo(t)}
        case _ => 
      }
      case None =>
    }
    d
  }
}

/**
 * 'Classic' movement which involves a unit from one point to another over tiles on a map. 
 * This is contrasted with teleportation spells which do not involve the entity moving
 * through tiles between the starting and end points, and which also usually do not
 * consider the terrain and intermediate tiles when deciding whether the entity can
 * reach the destination.
 */
class BasicMovementAction (movementRange : Int = 4) extends MovementAction {
  _range = movementRange
  
  override def validTilesInRange (tile: TacticalTile, r : Int = _range) : Array[TacticalTile] = {
    var validTiles : HashSet[TacticalTile] = new HashSet
    if (r > 0) {
      for (t1 <- tile.connectedTiles) {
        if (canPassTile(t1.asInstanceOf[TacticalTile])) {
          if (t1.entity == None) validTiles.add(t1.asInstanceOf[TacticalTile]) // We can't actually move to occupied squares
          for (t2 <- validTilesInRange(t1.asInstanceOf[TacticalTile], r-1)) validTiles.add(t2.asInstanceOf[TacticalTile])
        }
      }
    }
    validTiles.toArray
  }
      
  override def isValidTarget(tile : TacticalTile) : Boolean = {
    getUser().flatMap(_.getCurrentTile()) match {
      case Some(usrTile) => usrTile.xyMapDistanceTo(tile) <= _range && tile.getEntity() == None
      case _ => false
    }
  }
  
  /**
   * Movement validity is based on whether the user has enough range to reach the targeted tile,
   * has enough AP to carry out the action, and whether the tile is reachable and unoccupied.
   */
  override def isValid() : Boolean = {
    var isValid = false;
    // Check source entity's current tile
    (getUser().flatMap(_.getCurrentTile()), getTarget()) match {
      case (Some(currTile), Some(target : TacticalTile)) => {
        isValid = isValidTarget(target)
      }
      case _ => // Either current tile or target tile are None. This is invalid.
    }
    isValid;
  }
  

  
  /**
   * Get all tiles that can be reached by the user with this movement action.
   * 
   * @return
   * An array of tiles that the entity can move to (and select as targets for) with
   * this basic movement
   */
  override def getValidTargets() : List[TacticalTile] = {
    var targets : List[TacticalTile] = Nil
    getUser().flatMap(_.getCurrentMap) match {
      case Some(map : TacticalMap) => {
        for (tile <- map.getTiles()) {
          if (isValidTarget(tile.asInstanceOf[TacticalTile])) {
            targets = tile.asInstanceOf[TacticalTile] :: targets
          }
        }
      }
      case _ => 
    }
    targets
  }
  
  override def toString() : String = {
    super.toString() + " RANGE :" + _range
  }
  
  override def animate() : Unit = {
    
  }
  
  override def execute() : Unit = {
    super.execute()
    (getUser(), getUser().flatMap(_.scenario), getUser().flatMap(_.getCurrentTile()), getTarget()) match {
      case (Some(user), Some(scenario), Some(from : TacticalTile), Some(dest : TacticalTile)) => {
        val path = this.getShortestPath(from, dest)
        val movementPath = new MovementData(user, path)
        scenario.enqueueEffect(new MovementEffect(movementPath))
        scenario.resolveEffects()
      }
      case _ => 
    }
  }
}

class RookMovementAction extends BasicMovementAction {
  override def isValidTarget(tile: TacticalTile) : Boolean = {
    var isValid : Boolean = false
    getUser() flatMap(_.getCurrentTile() ) match {
      case None =>
      case Some(t) => {
        isValid = (t.mapX == tile.mapX || t.mapY == tile.mapY) && (tile.getEntity() == None)
      }
    }
    isValid
  }
}

class BishopMovementAction extends BasicMovementAction {
  override def isValidTarget(tile: TacticalTile) : Boolean = {
    import scala.math._;
    var isValid : Boolean = false
    getUser() flatMap(_.getCurrentTile() ) match {
      case None => 
      case Some(t) => isValid = (abs(t.mapX - tile.mapX) == abs(t.mapY - tile.mapY)) && (tile.getEntity == None)
    }
    isValid
  }
}