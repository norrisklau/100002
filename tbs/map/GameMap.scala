package tbs.map

import Array._
import java.awt.event.MouseEvent
import java.awt.event.KeyEvent
import java.awt.event.InputEvent
import scala.collection.mutable.ListBuffer
import java.awt.event.MouseWheelEvent
import tbs.entity._
import tbs.rendering.Renderable
import tbs.rendering.Zoomable
import tbs.interactive._
import tbs.game.scenario._
import javax.media.opengl.GL2
import tbs.mouse._
import tbs.rendering.TextureUsing
import tbs.tiles._

/**
 * The Map used as part of our TBS Scenario (for the most part). Combat
 * happens on this map.
 * 
 * The glX and glY coordinates represent the map offset, where
 * (0.0, 0.0) is no offset and has the screen rendering the bottom
 * left tile at glCoordinates (-1, -1) (The bottom left of the screen)
 */
class GameMap (val xSize : Int, val ySize : Int) extends Renderable 
                                                            with TextureUsing
                                                            with Zoomable 
                                                            with MouseInteractive
                                                            with KeyboardInteractive
                                                            with Serializable
                                                            with PartOfScenario {
  // 2d array of tiles
  var tileDimensions : Double = 0.01
  // Not every coordinate may have a tile: this allows for more interesting, non rectangular maps
  protected var tiles : Array[Array[Option[Tile]]] = ofDim[Option[Tile]](ySize, xSize)
  
  @transient private var scenarioData : Option[ScenarioData] = None
  
  setZoom(1.0)
  
  // Coords start at 0, 0 on the bottom left
  // Initialize tiles 
  for (y <- 0 to tiles.length - 1) {
    for (x <- 0 to tiles(y).length - 1) {
      tiles(y)(x) = None
    }
  }
  
  override def scenario : Option[Scenario] = {
    getScenarioData.flatMap(_.getScenario)
  }
  
  def getScenarioData() : Option[ScenarioData] = {
    scenarioData
  }
  
  def setScenarioData(scData : ScenarioData) : Unit = {
    scenarioData = Some(scData)
  }
  
  /**
   * Get the tile at the indicated position on this map.
   * 
   * @param x horizontal coordinate of the tile, starting from 0
   * @param y horizontal map coordinate of the tile. Starts from 0
   * 
   * @return
   * Some(tile) if a tile exists at the specified coordinates.
   * None if no tile is at the coordinates, or the coordinates are
   * outside the map bounds.
   */
  def getTileAt(x : Int, y : Int) : Option[Tile] = {
    var tile : Option[Tile] = None
    if (y >= 0 && y < tiles.length && x >= 0 && x < tiles(y).length) {
      tile = tiles(y)(x)
    }
    tile
  }
  
  /**
   * Get all non 'None' tiles on this map
   * 
   * @return
   * An array of tiles on this map. 
   */
  def getTiles() : List[Tile] = {
    val ts : ListBuffer[Tile] = new ListBuffer()
    for (tile <- tiles.flatten) {
      tile match {
        case None =>
        case Some(t) => ts.append(t)
      }
    }
    ts.toList
  }
  
  /**
   * Returns a tile with a specified ID field.
   * 
   * @return
   * Some(tile) if tile.getID() equals id
   * None       if there is no such tile on the map
   */
  def getTileByID(id : String) : Option[Tile] = { 
    var tile : Option[Tile] = None
    for (t <- getTiles) {
      if (t.getID().matches(id)) tile = Some(t)
    }
    tile
  }
  
  /**
   * Add a tile to the map at the specified coordinates, overwriting any current tile if 
   * needed. 0, 0 is the bottom left of the map
   * 
   * @param x x map coordinate to place the tile 
   * @param y y map coordinate to place the tile
   */
  def addTile (tile: Tile, x : Int, y : Int) : Unit = {
    // Check if x / y are 
    if (y < 0 || y >= tiles.length) {
      System.err.println("Error: y coordinate " + y + " for " + this + " is set outside " + this + "'s boundaries.")
    } else if (x < 0 || x >= tiles(0).length) {
      System.err.println("Error: x coordinate " + y + " for " + this + " is set outside " + this + "'s boundaries.")
    } else {
      if (tiles(y)(x) != None) System.err.println("Warning: Overwriting tile at " + x + "," + y)
      tile.attachToMap(this)
      tile.mapX = x
      tile.mapY = y
      removeTile(x, y)
      tiles(y)(x) = Some(tile)
    }
  }
  
      /**
   *  ENTITY METHODS
   */
  
  /**
   * Add entity to a tile at the requested coordinates. Calls tile.addEntity if a tile 
   * exists at the requested coordinates, otherwise does nothing. 
   */
  def addEntity(entity: Entity, x : Int, y : Int) : Unit = {
    getTileAt(x, y) match {
      case None => {System.err.println("Error: No tile exists at " + x + "," + y + " to add " + entity + "to")}
      case Some(t) => {
       t.placeEntity(entity)
      }
    }
  }
  
  def moveEntity(entity : Entity, x : Int, y : Int) : Unit = {
    removeEntities(_ == entity)
    addEntity(entity, x, y)
  }
  
  def moveEntity(entity : Entity, tile : Tile) : Unit = {
    if (tile.currentMap == Some(this)) {
      moveEntity(entity, tile.getMapX(), tile.getMapY())
    } else {
      assert (false)
    }
  }
  
  /**
   * Return a list of all entities on this map for which the filter function returns true.
   * Passing in no filter results in all entities being returned. 
   */
  def getEntities(filter : (Entity) => Boolean = (_) => true) : List[Entity] = {
    var entities : List[Entity] = Nil
    getTiles().foreach( _.asInstanceOf[TacticalTile].getEntity match {
      case None =>
      // Run entity against filter to see if we want to this one
      case Some(e) => if (filter(e)) entities = entities ::: List(e)
    } )
    entities
  }
  
  /**
   * Remove an entity from this map, running through a filter. 
   * With no explicit argument, removes all creatures from map.
   * 
   * @param filter Entity -> boolean function which returns true for 
   *               entities that should be removed, and false otherwise.
   *               
   * @
   * Number of entities removed.
   */
  def removeEntities(filter: (Entity) => Boolean = (_) => true) : Int = {
    for (e <- getEntities() ) {
      if (filter(e)) {
        e.currentTile match {
          // This should never happen, as we cycled through tiles to get the entities
          case None => assert(false) 
          case Some(t) => t.removeEntity(e);
        }
      }
    }
    0
  }
  
  /**
   * Remove a tile from the map, leaving an empty space at the coordinates.
   * 
   * @param x x map coordinate of tile to remove
   * @param y y map coordinate of tile to remove
   * 
   * @return
   * Some(Tile) if a tile existed and was removed. 
   * None, if no tile existed at the requested coordinates
   */
  def removeTile(x : Int, y : Int) : Option[Tile] = {
    var removedTile : Option[Tile] = None
    tiles(y)(x) match {
      case None => // No tile to remove
      case Some(t) => {
        removedTile = tiles(y)(x)
        tiles(y)(x) = None
      }
    }
    removedTile
  }
  
  /**
   * Get the dimensions of the map (the number of rows and columns of tiles)
   * 
   * @return
   * A tuple containing (# Columns, # Rows) as an 2-tuple of integers
   */
  def getDimensions() : (Int, Int) = {
    var columns = tiles.length
    var rows = 0
    if (columns > 0) rows = tiles(0).length
    (columns, rows)
  }
  
  def getNumberOfRows() : Int = {
    getDimensions()._2
  }
  
  def getNumberOfColumns() : Int = {
    getDimensions()._1
  }
  
  /**
   * MOUSE / KEYBOARD INPUT METHODS
   */
  
  private var tileSelectionF : (Tile) => Boolean = (t) => false
    /**
   * Set the filter function that determines which of the map's tiles 
   * are valid selections during Tile Selection mode. This function may be called
   * on the fly at any time during the tile selection process, or prior to it.
   */
  def setTileSelectionFilter(filter: (Tile) => Boolean) : Unit = {
    tileSelectionF = filter
  }
    
  
  /**
   * Change the map's mouse and keyboard input to tile selection mode. Selectable tiles
   * are highlighted, and mouse clicks on these tiles are considered to be selections. 
   * When a tile is selected this way, a selection function is called on that tile.
   * 
   * An example of how this would be used would be during target selection for abilities - 
   * the selection function would set the ability target field to the entity on the 
   * selected tile.
   * 
   * Cancelling selection will call a cancel function, which serves to tell whatever 
   * called this method that we aborted the selection process.
   * 
   * @param filter         a tile function that indicates which of the map's tiles are valid 
   *                       selections
   * @param selectionF     the function that is to be run with the selected tile as a parameter,
   *                       when a selection is made
   * @param cancelF        the function to be run when selection is cancelled without choosing 
   *                       a tile
   */

  
  def enterTileSelection(filter: (Tile) => Boolean, 
                         selectionF : (Tile) => Unit,
                         cancelF : () => Unit = () => {}) {
    setTileSelectionFilter(filter)
    val prv = parseMousePressF
    val prvParseKeyEvent = parseKeyEventF
    
    parseMousePressF = (me) => {
      var tilePressed = false
      for (tile <- tiles.flatten if ! tilePressed) {
        tile match {
          case Some(t) if (t.isGLCoordWithinTile(me.mX, me.mY))  => {
            tilePressed = true
            // If tile is a valid selection, run the selection func on it
            if (tileSelectionF(t)) {
              selectionF(t)
            }
          }
          case _ => 
        }
      }
      true
    }
    
    /**
     * Press escape to cancel tile selection
     */
    parseKeyEventF = (kE) => {
      if (kE.getKeyCode() == KeyEvent.VK_ESCAPE) {
        cancelF()
        exitTileSelection()
      }
      true
    }
  }
  
  /**
   * Return to default mouse and keyboard input behaviour.
   */
  def exitTileSelection() : Unit = {
    parseMousePressF = defaultParseMousePressF
    parseKeyEventF = defaultParseKeyEventF
  }
  
  /**
   * Default mouse pressing on the map, when it's not your turn to move or anything. 
   */
  var defaultParseMousePressF : (TBSMouseEvent) => Boolean = (me : TBSMouseEvent) => {
    var tilePressed = false
    for (t <- tiles.flatten if ! tilePressed) {
      t match {
        case None =>
        case Some(tile) => tile.parseMouseEvent(me)
        case _ =>
      }
    }
    tilePressed
  }
  
  var parseMousePressF = defaultParseMousePressF
  
  /**
   * The only mouse pressable parts of a map are the tiles, really.
   */
  override def parseMousePress(me: TBSMouseEvent) : Boolean = {
    parseMousePressF(me)
  }
  
  /**
   * Dragging the map moves the viewpoint around
   */
  override def parseMouseDrag(me: TBSMouseEvent) : Boolean = {
    val dragVector : (Double, Double) = getDragVector(me)
    // Adjust offset
    // Move map in the same direction as the dragging
    setGlCoords(glX + dragVector._1, glY + dragVector._2)
    previousMouseDragEvent = Some(me)
    true
  }
  
  var defaultParseKeyEventF : (KeyEvent) => Boolean = (ke) => true
  
  var parseKeyEventF = defaultParseKeyEventF
  
  override def parseKeyEvent(kE: KeyEvent) : Boolean = {
    var keyConsumed = false
    // parseKeyEventF(kE)
    if (kE.getKeyCode() == KeyEvent.VK_PLUS && kE.isControlDown()) {
      if (getZoom() < 8d) setZoom(getZoom() * 2d)
      keyConsumed = true
    } else if (kE.getKeyCode() == KeyEvent.VK_MINUS && kE.isControlDown()) {
      if (getZoom() > 0.5d ) setZoom(getZoom() / 0.5d)
    }
    keyConsumed
  }
  
  /**
   * Scrolling zooms in and out of the map
   */
  override def parseMouseScroll(scrollEvent : MouseWheelEvent) : Boolean = {
    val rotation : Int = scrollEvent.getWheelRotation()
    if (rotation > 0) {
      setZoom(getZoom * rotation * 1.5)
    } else if (rotation < 0) {
      setZoom(getZoom / (-rotation * 1.5))
    }
    true
  }
  
  /**
   * RENDERING METHODS
   */
  
  /**
   * @TODO
   * Snap map view so that tile is in the centre of the screen.
   * 
   * @param tile the tile to centre the map view on.
   */
  def centreOnTile(tile : Tile) : Unit = {
    // Set(x, y) 
  }
  
  import tbs.glcoord.Pixel2GL
  
  override def pixelX : Int = Pixel2GL.xGLToPixel(glX)
  
  override def pixelY : Int = Pixel2GL.yGLToPixel(glY)
  
  override def render (gl : GL2) = {
    // Draw Background
    // Draw Tiles
    for (t <- tiles.flatten.reverse) {
      t match {
        case Some(t) => t.render(gl)
        case _ => 
      }
    }
  }
  
  // Call update method of tile, which also calls the update methods of any entities on them.
  // When global map effects etc. are implemented, they will also be called here (non tile entities)
  override def update() = {
    for (t <- getTiles()) {
      t.update()
    }
  }
}