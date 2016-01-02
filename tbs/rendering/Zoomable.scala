package tbs.rendering

import scala.math._

/**
 * Objects you can zoom in on. For example, the map is zoomable, in that the user can
 * zoom in and out so more or less tiles are visible on the screen at one time
 */
trait Zoomable {
  @transient protected var zoomLevel : Double = 1
  final val minimumZoomLevel : Double = 1.0/16.0
  final val maximumZoomLevel : Double = 16d // Maximum of 16 x magnification
  
  def setZoom (zoom : Double) : Unit = {
    zoomLevel = max(zoom, minimumZoomLevel)
    zoomLevel = min(zoomLevel, maximumZoomLevel)
  }
  
  def getZoom() : Double = zoomLevel
}