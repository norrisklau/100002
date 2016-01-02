package tbs.rendering

import javax.media.opengl.GL
import javax.media.opengl.GL2
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import com.jogamp.opengl.util.texture.Texture
import com.jogamp.opengl.util.texture.awt.AWTTextureIO
import javax.media.opengl.GLProfile
import javax.media.opengl.GLDrawable
import com.jogamp.opengl.util.awt.ImageUtil

import tbs.glcoord.Pixel2GL


/**
 * Renders the glyphs for our game. Reads a bitmap file with the Unicode font (characters)
 * 
 * When asked to render a unicode character, checks if it's in the bounds of the 
 * font sheet (for example, a sheet with 256 characters can't render any glyphs beyond that.
 * 
 * If it is, it does a basic texture rendering using the location of said glyph.
 */
object GameTextRenderer {
  
  // Associated GL Drawable surface
  var _glDrawable : GLDrawable = _
  
  // Glyph dimensions (in pixel)
  private var glyphWidth : Int = 0
  private var glyphHeight : Int = 0
  
  private var _fontTexture : Texture = _
  
  def fontTexture : Texture = _fontTexture
  
  private def fontTexture_=(fontTexture : Texture) = {
    _fontTexture = fontTexture
  }
  
  def validFontTexture : Boolean = {
    fontTexture != null && 
    fontTexture.getImageWidth() > glyphWidth &&
    fontTexture.getImageHeight() > glyphHeight
  }
  
  /**
   * Number of characters or glyphs that make up one line in the font image.
   */
  def glyphsPerRow : Int = {
    var count = 0
    if (fontTexture != null) {
      if (glyphWidth > 0) 
        count = fontTexture.getImageWidth() / glyphWidth
    }
    count
  }
  
  def glyphsPerColumn : Int = {
    var count = 0
    if (fontTexture != null) {
      if (glyphHeight > 0) 
        count = fontTexture.getImageHeight / glyphHeight
    }
    count
  }
  
  def numGlyphs : Int = {
    glyphsPerRow * glyphsPerColumn
  }
  
  def glDrawable_=(glDrawable : GLDrawable) = {
    _glDrawable = glDrawable
  }
  
  def glDrawable : GLDrawable = _glDrawable
  
  /**
   * Load the texture to use as a font sheet.
   * 
   * @precondition
   *  Assumes the sheet is organised in basic unicode mapping order with no gaps 
   *  between characters. Does not need to have all unicode characters.
   *  
   *  Must be BMP, PNG, JPG, GIF or WBMP file.
   */
  def loadFontTexture(filename : String, chWidth : Int, chHeight : Int) : Unit = {
    try {
      val image : BufferedImage = ImageIO.read(new File(filename))
      // Images are 'upside down' : 0, 0 should be bottom left, but is top left when loaded by Java
      // Therefore, we need to flip the image before use
      ImageUtil.flipImageVertically(image)
      fontTexture = AWTTextureIO.newTexture(glDrawable.getGLProfile, image, true)
      glyphWidth = chWidth
      glyphHeight = chHeight
      
    } catch {
      case e : Throwable => {
        System.err.println("Exception while loading: " + new File(filename).getCanonicalPath)
        e.printStackTrace()
      }
    }
  }
  
  var print = false
  
  def drawGlyph(gl2 : GL2, glyph : Char, 
                           bottomLeft : (Int, Int), 
                           topRight : (Int, Int),
                           color : (Double, Double, Double) = (1, 1, 1)) : Unit = {
    if (validFontTexture) {
      // Unicode character to number code
      var unicode = glyph.toInt % numGlyphs
      /**
       * Calculate coordinates to use on the texture file. 
       * 0, 0 is the bottom of the texture file
       */
      val glyphGLWidth = glyphWidth.toDouble / fontTexture.getWidth()
      val glyphGLHeight = glyphHeight.toDouble / fontTexture.getHeight()
      val tilePos : (Double, Double) = (unicode % glyphsPerRow, unicode / glyphsPerRow)
      /**
       * Left is x position * glWidth of glyph
       * Bottom is top of tile - (y + 1) * glHeight of glyph
       */
      val texLeft = tilePos._1 * glyphGLWidth
      val texBottom = (glyphsPerColumn - 1 - tilePos._2) * glyphGLHeight
      val texRight = texLeft + glyphGLWidth
      val texTop = texBottom + glyphGLHeight
      
      /**
       * Calculate coordinates to use on the screen (to place the texture)
       * Convert pixel coords to GLCoords. -1, -1 is bottom of the screen!
       */
      val windowWidth = glDrawable.getWidth
      val windowHeight = glDrawable.getHeight
      val destLeft = Pixel2GL.xPixelToGL(bottomLeft._1)
      val destRight = Pixel2GL.xPixelToGL(topRight._1)
      
      val destBottom = Pixel2GL.yPixelToGL(bottomLeft._2)
      val destTop = Pixel2GL.yPixelToGL(topRight._2)

      fontTexture.bind(gl2)
      fontTexture.enable(gl2)
      // Start from bottom left corner, going clockwise
      fontTexture.setTexParameteri(gl2, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST_MIPMAP_NEAREST)
      fontTexture.setTexParameteri(gl2, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST)
      
      gl2.glBegin(GL2.GL_POLYGON) 
      gl2.glColor3d(color._1, color._2, color._3)
      gl2.glTexCoord2d(texLeft, texBottom)
      gl2.glVertex2d(destLeft, destBottom)
      gl2.glTexCoord2d(texLeft, texTop)
      gl2.glVertex2d(destLeft, destTop)
      gl2.glTexCoord2d(texRight, texTop)
      gl2.glVertex2d(destRight, destTop)
      gl2.glTexCoord2d(texRight, texBottom)
      gl2.glVertex2d(destRight, destBottom)

      gl2.glEnd()
      fontTexture.disable(gl2)
    }
  }
}