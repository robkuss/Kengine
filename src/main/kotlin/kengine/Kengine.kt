package kengine

import kengine.scene.SceneManager
import kengine.scene.graphics.*
import kengine.scene.setCallbacks

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import kengine.ui.text.fontCleanup


// Constants
const val WIDTH_INIT = 1920
const val HEIGHT_INIT = 1080

const val ANTIALIASING_SAMPLES = 10

// Initial values
var window: Long = 0L
var windowWidth: Int = WIDTH_INIT
var windowHeight: Int = HEIGHT_INIT
var aspect = windowWidth.toFloat() / windowHeight.toFloat()     // Aspect ratio

var lastFPS                     = 0                             // Calculated FPS value for performance test
private var runtimer            = 0                             // Counter variable for performance tests
private var lastResetTime       = System.currentTimeMillis()    // Last time the runtimer has been reset


fun main() {
	// Initialize GLFW
	GLFWErrorCallback.createPrint(System.err).set()
	
	if (!glfwInit()) {
		throw IllegalStateException("Unable to initialize GLFW")
	}
	
	glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE)
	glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
	glfwWindowHint(GLFW_SAMPLES, ANTIALIASING_SAMPLES)
	
	// Create the window
	window = glfwCreateWindow(WIDTH_INIT, HEIGHT_INIT, "Qengine", 0, 0)
	if (window == 0L) {
		throw RuntimeException("Failed to create the GLFW window")
	}
	
	// Center the window
	centerWindow()
	
	// Make the OpenGL context current
	glfwMakeContextCurrent(window)
	glfwSwapInterval(1)      // Enable v-sync
	glfwShowWindow(window)
	
	GL.createCapabilities()         // Initialize OpenGL capabilities
	
	// Initialize Mesh Renderer, Scene Manager and Viewport
	val meshRenderer = MeshRenderer()
	val sceneManager = SceneManager(meshRenderer)
	val viewport     = Viewport(sceneManager)
	
	setCallbacks(viewport)          // Set callbacks for mouse and keyboard input
	
	// Render window every frame, capped to 60 FPS due to v-sync
	while (!glfwWindowShouldClose(window)) {
		viewport.render()
		getFPS()
	}
	
	// Cleanup
	glfwDestroyWindow(window)
	glfwTerminate()
	glfwSetErrorCallback(null)?.free()
	fontCleanup()
}

/** Centers the application's window to the middle of the screen. */
private fun centerWindow() {
	val monitor = glfwGetPrimaryMonitor()
	val vidMode = glfwGetVideoMode(monitor)!!
	val monitorWidth = vidMode.width()
	val monitorHeight = vidMode.height()
	val windowPosX = (monitorWidth - WIDTH_INIT) / 2
	val windowPosY = (monitorHeight - HEIGHT_INIT) / 2
	glfwSetWindowPos(window, windowPosX, windowPosY)
}

/**
 * Calculate the frame rate per second to monitor the Qengine's performance
 */
private fun getFPS() {
	val currentTime = System.currentTimeMillis()
	
	// Check if 1 second has passed
	if (currentTime - lastResetTime >= 1000) {
		lastFPS = runtimer              // set FPS to amount of frames in the last seconds
		runtimer = 0                    // Reset runtimer
		lastResetTime = currentTime     // Update the last reset time
	}
	
	runtimer++
}
