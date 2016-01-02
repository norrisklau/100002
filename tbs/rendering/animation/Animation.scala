package tbs.rendering.animation

import tbs.rendering.Renderable
import javax.media.opengl.GL2

/**
 * Animated is an extension of the Renderable trait, with an extra 'animate' method that
 * may block until the animation finishes. 
 * 
 * Animations may be either added to things like entities (with an attack animation, a walk cycle), or
 * their own effect which is not attached to a concrete in-game entity (such as explosions
 * and particle effects). 
 * 
 */
trait Animated extends Renderable {
  var animFunc : () => Unit = () => {
    // Do nothing
  }
  
  def setAnimation (aFunc : () => Unit) = {
    animFunc = aFunc
  }
}