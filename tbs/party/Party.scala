package tbs.party

import scala.collection.mutable.ListBuffer

import tbs.entity._
import tbs.rendering.Renderable
import javax.media.opengl.GL2

import tbs.entity.monster.GoblinShaman
import tbs.tiles.OverworldTile

/**
 * Party of entities.
 * Includes the party members, and controller(s).
 */
class Party extends Serializable with Renderable {
  var money : Int = 0
  var members : Array[Entity] = Array(new GoblinShaman)
  
  // var currentTile : Option[OverworldTile] = 
  override def renderAtRect(gl : GL2, bottomLeft : (Int, Int), topRight : (Int, Int)) = {
    if (! members.isEmpty) members(0).renderAtRect(gl, bottomLeft, topRight)
  }
  
  override def render(gl : GL2) = {
    // if (! members.isEmpty) members(0).render(gl)
  }
}