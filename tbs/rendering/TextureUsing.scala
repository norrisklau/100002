package tbs.rendering

/**
 * Describes objects that require textures to be loaded, really.
 * 
 * Much of the rendering and texture storing specifics would be class
 * specific. This trait exists so the gamestate knows if it has to call
 * initTextures or freeTextures when adding / removing parts. 
 */
trait TextureUsing {
  /**
   * Initialise Textures, loading them in. 
   */
  def initTextures(table: TextureTable) = {
    // Do table.requestTexture(path) here
  }
  
  /**
   * Call when the parent class will no longer be Renderable, to free
   * up textures in the texture table. Clean up, guys!
   */
  def freeTextures(table: TextureTable) = {
    // A bunch of table.releaseTexture(path) calls, no doubt
  }
}
