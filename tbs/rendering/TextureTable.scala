package tbs.rendering

import javax.media.opengl.GL2
import java.io.File
import scala.collection.mutable.HashMap
import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.TextureIO

class TextureTable {
  private val textures : HashMap[String, Texture] = new HashMap
  private val numUsers : HashMap[String, Int] = new HashMap
  /**
   * Call to ask for a texture to be loaded from file. If it does not exist in mapping, 
   * loads it in, and sets the number of users to 1. Otherwise, 
   * increments the number of users of that texture.
   * 
   * @param fileName the directory filepath of the texture image to be loaded.
   * 
   * @return
   * Returns the number of users currently using this texture (including the 
   * one that just called this method). A value of 0 indicates the texture 
   * was not loaded properly.
   */
  def loadTexture(fileName: String) : Int = {
    var users : Int = 0
    // Load from file if we have to
    if (! textures.contains(fileName)) {
      try {
        val texture : Texture = TextureIO.newTexture(new File(fileName), true)
        textures(fileName) = texture
        numUsers(fileName) = 1
      } catch {
        case e : Throwable => e.printStackTrace()
      }
    // If texture is already loaded, increment the number of users of this texture
    } else {
      numUsers(fileName) += 1
    }
    if (numUsers.contains(fileName)) users = numUsers(fileName)
    users
  }
  
  
  /**
   * Return the texture object that was loaded into memory from filepath. Generally, 
   * we would call the renderTo methods instead of calling this for rendering.
   * 
   * @param filepath The filepath we used to load the texture during a previous 
   *                 loadTexture call
   *                 
   * @
   * A Texture object if it exists, otherwise, null if no such texture had been
   * previously loaded.
   */
  def getTexture(filepath : String) : Texture = {
    var tex : Texture = null
    if (textures.contains(filepath)) {
      tex = textures(filepath)
    }
    tex
  }
  
  /**
   * Tell the texture table we're no longer using a texture, and that they can
   * free it up if required. Decrements the user count tied to a texture. The
   * texture is freed if the count reaches 0 (i.e. no objects are using the 
   * texture).
   * 
   * Prints an error is the texture does not exist in the texture table. 
   * 
   * @param fileName file path of the texture we wish to release
   * 
   * @The number of users of the texture. A value of 0 means no more users remain, 
   *         and the texture has been freed.
   */
  def releaseTexture(fileName: String) : Int = {
    var usersLeft = 0
    if (! textures.contains(fileName)) {
      System.err.println("Error: Trying to free texture " + fileName + " which is not loaded.")
    } else {
      // Reduce the user count for this texture by 1
      numUsers(fileName) -= 1
      usersLeft = numUsers(fileName)
      // If no one else is using the texture, remove it and free up memory
      if (usersLeft <= 0) {
        textures.remove(fileName)
      }
    }
    usersLeft
  }
  
  // Render using pixel coords.
  def renderAtPixelCoords(gl2 : GL2, id : String, srcX : Int, srcY : Int,
                                                  srcW : Int, srcH : Int,
                                                  destLeft : Int, destBottom : Int, 
                                                  destRight : Int, destTop : Int,
                                                  windowWidth : Int, windowHeight : Int) : Unit = {
    if (textures.contains(id)) {
      val tHeight = textures(id).getImageHeight()
      val tWidth = textures(id).getImageWidth()
      import tbs.glcoord.Pixel2GL
      
      renderTo(gl2, id, srcX/tHeight, srcY/tWidth, srcW/tWidth, srcH/tHeight,
                        Pixel2GL.xPixelToGL(destLeft), Pixel2GL.yPixelToGL(destBottom), 
                        Pixel2GL.xPixelToGL(destRight), Pixel2GL.yPixelToGL(destTop))
    }
  }
  
  // Render to rectangle with lower left: (destLeft, destBottom) and upper right (destRight, destTop)
  def renderTo(gl2 : GL2, id : String, destLeft : Double, destBottom : Double, 
                                       destRight : Double, destTop : Double) : Unit = {
    renderTo(gl2, id, 0, 0, 1, 1, destLeft, destBottom, destRight, destTop)
  }
  
  // Render texture to a rectangle on screen. 
  def renderTo(gl2 : GL2, id: String, srcX : Double, srcY : Double,
                                      srcW : Double, srcH : Double,
                                      destLeft : Double, destBottom : Double, 
                                      destRight : Double, destTop : Double) : Unit = {
    if (textures.contains(id)) {
      textures(id).bind(gl2)
      textures(id).enable(gl2)
      // Start from bottom left corner, going clockwise
  
      gl2.glBegin(GL2.GL_POLYGON) 
      gl2.glColor3d(1, 1, 1) // ???
      gl2.glTexCoord2d(srcX, srcY)
      gl2.glVertex2d(destLeft, destBottom)
      gl2.glTexCoord2d(srcX, srcY + srcH)
      gl2.glVertex2d(destLeft, destTop)
      gl2.glTexCoord2d(srcX + srcW, srcY + srcH)
      gl2.glVertex2d(destRight, destTop)
      gl2.glTexCoord2d(srcX + srcW, srcY)
      gl2.glVertex2d(destRight, destBottom)
      gl2.glEnd()
      textures(id).disable(gl2)
      
    } else {
      
    }
  }
  
  /**
   * Remove all textures from the texture table. It can be assumed that all memory used
   * by storing these textures is freed up after this call. 
   * 
   * Generally, the only time this is called is when the texture table itself is about to 
   * go out of rendering context/scope.
   */
  def clear() = {
    for (k <- textures.keySet) {
      textures.remove(k)
    }
  }
}