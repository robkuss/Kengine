package kengine.ui.text

import org.lwjgl.PointerBuffer
import kengine.math.vector.Vector2

import org.lwjgl.opengl.GL20.*
import org.lwjgl.util.freetype.*
import org.lwjgl.util.freetype.FreeType.*
import org.lwjgl.system.MemoryUtil.*
import kengine.scene.graphics.color.Color
import kengine.scene.graphics.color.color3f
import kengine.windowHeight
import kengine.windowWidth

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files


const val firstLineX = 10f
const val firstLineY = 45f
const val lineSpacing = 1.2f

private const val fontPath = "resources/fonts/cour.ttf"
private const val fontSize = 48

private var font: FT_Face? = null
private var fontTexture: Int = 0
private val characters = HashMap<Char, Character>()
private var library: Long = 0


private data class Character(
	val textureID: Int,
	val size: Vector2,
	val bearing: Vector2,
	val advance: Int
)

fun initFreeType() {
	// Load the font file into a byte buffer
	val fontFile = File(fontPath)
	val fontBytes = Files.readAllBytes(fontFile.toPath())
	val fontBuffer: ByteBuffer = memAllocDirect(fontBytes.size.toLong()).apply {
		put(fontBytes)
		flip()
	}

	// Initialize FreeType library
	val libraryPointer = allocPointer()
	if (FT_Init_FreeType(libraryPointer) != 0) {
		println("FT_Init_FreeType failed")
		return
	}
	library = libraryPointer.get(0)
	
	// Create a new face from the font buffer
	val facePointer = allocPointer()
	if (FT_New_Memory_Face(library, fontBuffer, 0, facePointer) != 0) {
		println("FT_New_Memory_Face failed")
		return
	}
	
	val face = facePointer.get(0)
	font = FT_Face.create(face)
	
	if (FT_Set_Pixel_Sizes(font!!, 0, fontSize) != 0) {
		println("FT_Set_Pixel_Sizes failed")
		return
	}
	
	loadFontTexture()
}

private fun allocPointer(): PointerBuffer {
	return memAllocPointer(1)
}

fun memAllocDirect(size: Long): ByteBuffer {
	if (size < 0) throw IllegalArgumentException("Size must be non-negative")
	return ByteBuffer.allocateDirect(size.toInt()).order(ByteOrder.nativeOrder())
}

private fun loadFontTexture() {
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1)  // Disable byte-alignment restriction
	
	for (charCode in 32..126) {
		if (FT_Load_Char(font!!, charCode.toLong(), FT_LOAD_RENDER) != 0) {
			println("Failed to load Glyph: $charCode (character: ${charCode.toChar()})")
			continue
		}
		
		val glyph = font!!.glyph()!!
		val bitmap = glyph.bitmap()
		
		val width = bitmap.width()
		val height = bitmap.rows()
		val left = glyph.bitmap_left()
		val top = glyph.bitmap_top()
		
		val buffer = bitmap.buffer(width * height)
		
		// Generate texture
		val textureID = glGenTextures()
		glBindTexture(GL_TEXTURE_2D, textureID)
		glTexImage2D(
			GL_TEXTURE_2D,
			0,
			GL_ALPHA,                                  // Use ALPHA because the bitmap is grayscale
			bitmap.width(),
			bitmap.rows(),
			0,
			GL_ALPHA,                                  // The format of the pixel data
			GL_UNSIGNED_BYTE,
			buffer
		)
		
		// Set texture options
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
		
		// Store character info
		val character = Character(
			textureID,
			Vector2(width.toFloat(), height.toFloat()),
			Vector2(left.toFloat(), top.toFloat()),
			glyph.advance().x().toInt()
		)
		characters[charCode.toChar()] = character
		
		// Debug prints
		/*println("${charCode.toChar()}:")
		println("Buffer: $buffer")
		println("\tTexture ID: $textureID")
		println("\tWidth: $width, Height: $height")
		println("\tBearing Left: ${glyph.bitmap_left()}, Bearing Top: ${glyph.bitmap_top()}")
		println("\tAdvance: ${glyph.advance().x().toInt()}")
		println()*/
	}
}

/**
 * Draw UI Text somewhere on the screen
 * @param text the String that will be rendered to the screen
 * @param x the x position in screen coordinates where the text will be drawn
 * @param line the y position, represented as an Integer line number
 * @param scale scaling factor for the text. It will be drawn with font size [fontSize] * [scale]
 */
fun renderText(text: String, x: Float, line: Int, scale: Float, textColor: Color) {
	// Save the current matrix state (3D perspective matrix)
	glMatrixMode(GL_PROJECTION)
	glPushMatrix()
	glLoadIdentity()
	
	// Set up an orthographic projection
	val windowWidth = windowWidth
	val windowHeight = windowHeight
	
	glOrtho(0.0, windowWidth.toDouble(), windowHeight.toDouble(), 0.0, -1.0, 1.0)
	
	// Switch to the model view matrix to render the text
	glMatrixMode(GL_MODELVIEW)
	glPushMatrix()
	glLoadIdentity()
	
	// Enable necessary settings for text rendering
	glEnable(GL_TEXTURE_2D)
	glBindTexture(GL_TEXTURE_2D, fontTexture)
	glEnable(GL_BLEND)
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
	color3f(textColor)
	
	// Calculate y position as screen coordinates
	val y = line(line, scale)
	
	// Move to the position where the text should be drawn
	glTranslatef(x, y, 0f)
	glScalef(scale, scale, 1f)
	
	// Render the text character by character
	for (c in text) {
		val character = characters[c] ?: continue
		
		// Correct kerning by considering the glyph's bearing.x
		val xpos = character.bearing.x
		val ypos = if (c == '-') -character.bearing.y else 0f
		
		// Bind the character's texture
		glBindTexture(GL_TEXTURE_2D, character.textureID)
		
		// Render the character quad with the glyph texture
		glBegin(GL_QUADS)

		glTexCoord2f(0f, 0f)
		glVertex2f(xpos, ypos - character.size.y)
		
		glTexCoord2f(1f, 0f)
		glVertex2f(xpos + character.size.x, ypos - character.size.y)
		
		glTexCoord2f(1f, 1f)
		glVertex2f(xpos + character.size.x, ypos)

		glTexCoord2f(0f, 1f)
		glVertex2f(xpos, ypos)
		
		glEnd()
		
		// Move the cursor to the next position (advance by glyph's advance value)
		glTranslatef((character.advance shr 6).toFloat(), 0f, 0f)  // shr 6 to divide by 64
	}
	
	// Restore settings
	glDisable(GL_BLEND)
	glDisable(GL_TEXTURE_2D)
	
	// Restore the model view matrix
	glPopMatrix()
	
	// Restore the projection matrix (back to 3D perspective)
	glMatrixMode(GL_PROJECTION)
	glPopMatrix()
	
	// Switch back to the model view matrix
	glMatrixMode(GL_MODELVIEW)
}

fun line(lineNumber: Int, fontScale: Float) = (firstLineY * fontScale) + (lineNumber * fontSize * fontScale * lineSpacing)


fun fontCleanup() {
	if (font != null) {
		FT_Done_Face(font!!)
	}
	// Free the library handle if it was initialized
	FT_Done_FreeType(library)
	glDeleteTextures(fontTexture)
}
