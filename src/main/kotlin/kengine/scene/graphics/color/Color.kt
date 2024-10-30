package kengine.scene.graphics.color

import org.lwjgl.opengl.GL20.*

// Predefined colors
val RED                 = Color(255,   0,   0)
val GREEN               = Color(  0, 255,   0)
val BLUE                = Color(  0,   0, 255)
val BG_COLOR            = Color( 60,  60,  60)
val GRID_COLOR          = Color( 80,  80,  80)
val MESH_FACE_COLOR     = Color(150, 150, 150)
val MESH_EDGE_COLOR     = Color( 50,  50,  50)
val MESH_VERT_COLOR     = Color( 25,  25,  25)
val MESH_SELECT_COLOR   = Color(240, 150,  60)
val RAY_COLOR           = Color(255, 255,   0)
val TEXT_COLOR          = Color(192, 192, 192)

data class Color(val red: Int, val green: Int, val blue: Int, val alpha: Int? = 255) {
	private val scaler = 255.0f
	
	fun red()   = red.toFloat() / scaler
	fun green() = green.toFloat() / scaler
	fun blue()  = blue.toFloat() / scaler
	fun alpha() = (alpha?.toFloat() ?: throw NullPointerException("RGB color has no alpha value")) / scaler
}

fun clearColor(color: Color) {
	glClearColor(color.red(), color.green(), color.blue(), color.alpha())
}

fun color3f(color: Color) {
	glColor3f(color.red(), color.green(), color.blue())
}