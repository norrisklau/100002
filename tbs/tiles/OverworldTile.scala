package tbs.tiles

import tbs.party.Party
import javax.media.opengl.GL2
import tbs.entity.Entity

class OverworldTile extends Tile {
  protected var attachedParty : Option[Party] = None
  
  def addParty(party : Party) : Unit = {
    if (attachedParty != Some(party)) {
      attachedParty = Some(party)
    } else {
      System.err.println("Attempting to add party : " + party + " at " + this)
      System.err.println("But party was not placed, as it already exists!")
    }
  }
  
  def removeParty() : Unit = {
    attachedParty = None
  }
  
  def Party : Option[Party] = {
    attachedParty
  }
  
  def hasParty : Boolean = attachedParty != None
  
  override def render(gl : GL2) = Party match {
    case None => super.render(gl)
    case Some(party) => party.renderAtRect(gl, (pixelX, pixelY), (pixelX + pixelW, pixelY + pixelH))
  }
  
  def isPassable(mvr : Party) : Boolean = true
}

class OwWallTile extends OverworldTile {
  glyph = '#'
  
  override def isPassable(mvr : Party) : Boolean = false
}

class OwFloorTile extends OverworldTile {
  glyph = '.'

  override def isPassable(mvr : Party) : Boolean = true
}