package tbs.tiles

import tbs.entity.Entity
import tbs.effect._
import tbs.rendering._
import tbs.mouse._
import tbs.interactive._
import tbs.id.HasID
import tbs.random.UsesRandom
import tbs.game.scenario.{Scenario, PartOfScenario}
import scala.collection.mutable.ListBuffer
import javax.media.opengl.GL2
import scala.math.abs
import tbs.map._

object Tile {
  final val DefaultGLWidth : Double = 0.1
  final val DefaultGLHeight : Double = 0.1
 
  final val DefaultPixelW : Int = 64
  final val DefaultPixelH : Int = 64
}

/**
 * Map Tiles. Effects can be attached to them, entities can be placed on them, they can be clicked.
 */
class Tile extends Renderable with HasID // Tile Identifier to be used by Actions 
                              with MouseInteractive // You can click on tiles 
                              with TextureUsing 
                              with Zoomable
                              with Serializable
                              with UsesRandom
                              with EffectAttachable[Tile] 
                              with PartOfScenario {
  
  var attachedMap : Option[GameMap] = None
  var mapX : Int = -1
  var mapY : Int = -1 
  
  var isHighlighted = false
  var highlightMask = (0d, 1d, 0d, 0.8d)
  
  override def pixelW = (Tile.DefaultPixelW * getZoom()).toInt
  override def pixelH = (Tile.DefaultPixelH * getZoom()).toInt
  
  protected var glyph : Char = '?'
  protected var glyphColor : (Double, Double, Double) = (1, 1, 1)
  // entity sitting on this tile
  var entity: Option[Entity] = None
  
  def setHighlighted(isHighlighted : Boolean) : Unit = {
    this.isHighlighted = isHighlighted
  }
  
  def attachToMap(map : GameMap) = {
    attachedMap = Some(map)
  }
  
  // Privatize
  def currentMap: Option[GameMap] = {
    attachedMap
  }
  
  // Move Out
  def isPassable(mover : Entity) : Boolean = {
    true
  }
  
  // Branch into two types, privatize
  def getOtherTilesInMap() : List[Tile] = currentMap match {
    case Some(map) => map.getTiles()
    case None => Nil
  }
  
  // Don't need these anymore
  override def scenario : Option[Scenario] = {
    getScenarioData().flatMap(_.getScenario)
  } 
  
  import tbs.game.scenario.ScenarioData
  def getScenarioData() : Option[ScenarioData] = {
    currentMap.flatMap(_.getScenarioData())
  }
  
  def isOnSameMapAsTile(tile : Tile) : Boolean = {
    (currentMap != None && tile.currentMap != None && currentMap.get == tile.currentMap.get)
  }
  
  /**
   * Tileset glyph stuff
   */
  
  override def initTextures(table: TextureTable) : Unit = {
    
  }
  
  /**
   * Coordinates on the map, starting with 0, 0 for lower left tile
   */
  def getMapCoordinates() : (Int, Int) = {
    (mapX, mapY)
  }
  
  def getMapX() : Int = {
    getMapCoordinates()._1
  }
  
  def getMapY() : Int = {
    getMapCoordinates()._2
  }
  
  /**
   * Return the x y distance of another tile from this one
   * 
   * @return
   * -1 if tiles are not on the same map
   * |this.x - other.x| + |this.y - other.y| otherwise
   */
  def xyMapDistanceTo(other : Tile) : Int = {
    var distance = -1
    if (isOnSameMapAsTile(other)) {
      import scala.math._
      distance = abs(getMapX() - other.getMapX()) + abs(getMapY() - other.getMapY())
    } 
    distance
  }
  
  /**
   * Get all the tiles which are connected to this one ; i.e. tiles which an 
   * entity on this one can reach in a single movement step. This may include
   * more than just adjacent tiles - for example, a teleport tile which you 
   * could get to from this one in one step would count as a 'connected tile'. 
   * 
   * @return
   * An array of connected Tiles
   */
  def connectedTiles : Array[Tile] = {
    val tiles : ListBuffer[Tile] = new ListBuffer
    // Ask out map 
    attachedMap match {
      case None => // If we're not on a map, we're not connected to anything
      case Some(m  : TacticalMap) => {
        // Get left, bottom, right and top neighbours
        for (xy <- Array( (mapX - 1 , mapY),
                  (mapX, mapY - 1),
                  (mapX + 1 , mapY),
                  (mapX, mapY + 1)) ){
          m.getTileAt(xy._1, xy._2) match {
            case Some(t) => tiles.append(t)
            case None  => {}
          }
        }
      }
      case Some(m  : OverworldMap) => {
        // Get left, bottom, right and top neighbours
        for (xy <- Array( (mapX - 1 , mapY),
                  (mapX, mapY - 1),
                  (mapX + 1 , mapY),
                  (mapX, mapY + 1)) ){
          m.getTileAt(xy._1, xy._2) match {
            case Some(t) => tiles.append(t)
            case None  => {}
          }
        }
      }
      case _ =>
    }
    tiles.toArray
  }
  
      /**
   * Add an entity to this map tile. If the same entity is to be placed on the tile
   * more than once, it is only added the first time.
   * 
   * @param entity the entity to be added to the tile
   */
  def placeEntity(entity : Entity) : Unit = {
    this.entity = Some(entity)
    entity.setCurrentTile(this)
    entity.setCurrentScenario(this.getScenarioData)
  }
  
  def getEntity() : Option[Entity] = {
    this.entity
  }
  
  /**
   * Remove an entity from the current tile.
   * 
   * @param entity The entity to be removed.
   */
  def removeEntity(entity : Entity) : Unit = {
    this.entity match {
      case None => // Huh
      case Some(e) if (e == entity) => { 
        e.currentTile = None
        this.entity = None
      }
      case Some(f) => System.err.println("Warning: Attempting to remove entity " + entity +
                                         "from tile: '" + this.toString + "', but entity " + 
                                         f.toString() + " was found instead.")
    }
  }
  
  /**
   * Use our map's zoom level (if we are attached to one). This means
   * zooming in and out of the map will zoom in and out of all its tiles.
   */
  override def getZoom() : Double = {
    var zoom : Double = 1
    attachedMap match {
      case None => zoom = zoomLevel
      case Some(m) => zoom = m.getZoom
    }
    zoom
  }
  
  def isGLCoordWithinTile(x : Double, y : Double) : Boolean = {
    import scala.math._
    val midPoint : (Double, Double) = (glX() + glWidth / 2, glY() + glHeight / 2)
    (abs(midPoint._1 - x) <= glWidth / 2) && 
    (abs(midPoint._2 - y) <= (1 - abs(midPoint._1 - x) / (glWidth / 2)) * (glHeight / 2))
  }
  
  // MOUSE INPUT FUNCS
  override def parseMousePress(mP : TBSMouseEvent) : Boolean = {
    isGLCoordWithinTile(mP.mX, mP.mY)
  }
  
  /**
   * Width and Height on screen depend on the zoom level of the map. (Users can zoom in and out 
   * of maps, increasing the width and height drawn on the screen). HMM.
   */
  override def glWidth() : Double = {
    attachedMap match {
      case None => Tile.DefaultGLWidth
      case Some(m) => Tile.DefaultGLWidth * getZoom()
    }
  }
  
  override def glHeight() : Double = {
    attachedMap match {
      case None => Tile.DefaultGLHeight
      case Some(m) => Tile.DefaultGLHeight * getZoom() 
    }
  }
  
  override def glX(): Double = {
    attachedMap match {
      case Some(m) => mapX * glWidth + m.glX
      case None => 0
    }
  }
  
  override def glY(): Double = {
    attachedMap match {
      case Some(m) =>  mapY * glHeight() + m.glY
      case None => 0
    }
  }
  
  override def pixelX() : Int = {
    attachedMap match {
      case Some(m) => mapX * pixelW + m.pixelX
      case None => 0
    }
  }
  
  override def pixelY() : Int = {
    attachedMap match {
      case Some(m) => mapY * pixelH + m.pixelY
      case None => 0
    }
  }
  
  /**
   * Draw a QUAD, applying a texture if necessary for the tile. 
   */
  override def render (gl : GL2) = {
    if (isHighlighted) renderHighlightMask(gl)
    renderGlyph(gl, glyph, glyphColor, (pixelX, pixelY), (pixelX + pixelW, pixelY + pixelH))
    entity match {
      case None =>
      case Some(e) => e.render(gl)
    }
    
    // Render any effects that animate
    for (effect <- attachedEffects) {
      effect match {
        case r : Renderable => r.render(gl)
        case _ =>
      }
    }
  }
  
  def renderRectangle(gl : GL2) = {
    gl.glVertex2d(glX, glY + glHeight)
    gl.glVertex2d(glX + glWidth, glY + glHeight)
    gl.glVertex2d(glX + glWidth, glY)
    gl.glVertex2d(glX, glY)
  }
  
  private def renderHighlightMask (gl : GL2) = {
    gl.glBegin(GL2.GL_POLYGON)
    gl.glColor4d(highlightMask._1, highlightMask._2, highlightMask._3, highlightMask._4)
    renderRectangle(gl)
    gl.glEnd()
  }
  
  override def update() = {
  }
  
  /**
   * Set current tile and centre map around it. 
   */
  override def centreOnScreen () = {
    attachedMap match {
      case Some(m)    => m.centreOnTile(this)
      // No map .. just stick this tile in the middle of the screen and call it a day
      case None       => super.centreOnScreen() 
    }
  }
  
  override def toString() : String = {
    "Tile (" + mapX + " , " + mapY + ")"
  }
}


