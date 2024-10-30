package kengine.scene.graphics

import kengine.*
import kengine.scene.graphics.color.*
import kengine.math.*
import kengine.math.vector.Vector3
import kengine.math.vector.Vector4
import kengine.math.matrix.Matrix4x4
import kengine.objects.mesh.Mesh
import kengine.objects.Object
import kengine.scene.Mode
import kengine.scene.SceneManager

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL20.*
import kengine.scene.ModeType
import kengine.scene.SubMode
import kengine.ui.text.firstLineX
import kengine.ui.text.initFreeType
import kengine.ui.text.renderText

import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.tan

class Viewport(private val sceneManager: SceneManager) {
	private var viewportMode        = Mode.OBJECT                   // Mode for selection and rendering
	private var transformMode       = Mode.NONE                     // Mode for Object transformation
	
	// Initial values
	private var cameraPosition      = CAMERA_POSITION_INIT
	private var cameraDistance      = CAMERA_DISTANCE_INIT
	private var lookAtPoint         = LOOK_AT_POINT_INIT            // Target look-at point for the camera
	private var upVector            = UP_VECTOR_INIT
	private var zoomSpeedScalar     = 1f                            // Will change based on the camera distance to allow for smoother zooming
	
	private var fontScale           = 0.5f
	
	// Variables for camera rotation
	private var rotating            = false
	private var rotationSensitivity = 0.5f
	private var rotH                = 0f              // horizontal rotation
	private var rotV                = 0f              // vertical rotation
	private var lastH               = 0.0
	private var lastV               = 0.0
	
	// Variables for object transformation
	private var transformation      = Vector3.ZERO    // General Vector for all object transformations in 3D space
	private var lastTransformation  = Vector3.ZERO    // Used to calculate differences from last transformation operation
	
	// Mouse Ray
	//private var rayStart          = Vector3(-1f, -1f, -1f)
	//private var rayEnd            = Vector3( 1f,  1f,  1f)
	
	// "Pointers" - Shared resources for viewport state used mainly by the OpenGL library
	private val viewport            = IntArray(4)
	private var projectionMatrix    = FloatArray(16)
	private var viewMatrix          = FloatArray(16)
	private val mouseX              = DoubleArray(1)
	private val mouseY              = DoubleArray(1)
	
	companion object {
		// Constants
		private val CAMERA_POSITION_INIT    = Vector3(10f, 0f, 0f)     // Default camera position
		private val CAMERA_DISTANCE_INIT    = CAMERA_POSITION_INIT.length()     // Camera distance from the origin
		private val LOOK_AT_POINT_INIT      = Vector3.ZERO                      // Default: Looking at origin
		private val UP_VECTOR_INIT          = Vector3(0f, 0f, 1f)      // Default: Up direction is positive Z
		
		const val CAMERA_DISTANCE_MIN       = 0.02f
		const val CAMERA_DISTANCE_MAX       = 10000f
		const val Z_NEAR                    = CAMERA_DISTANCE_MIN / 2           // Near Clipping Plane: The distance from the camera to the near clipping plane. Objects closer than this distance are not rendered.
		const val Z_FAR                     = CAMERA_DISTANCE_MAX * 2           //  Far Clipping Plane: The distance from the camera to the far clipping plane. Objects further than this distance are not rendered.
		const val AXES_LENGTH               = 100f
		//const val MOUSE_RAY_LENGTH        = 1000f
		
		const val ZOOM_SENSITIVITY          = 2f
		const val FOV_Y                     = 45.0                              // Field of View in Y dimension
	}
	
	init {
		// OpenGL setup
		glEnable(GL_DEPTH_TEST)         // Enable depth testing
		glEnable(GL_MULTISAMPLE)        // Enable multi-sampling (antialiasing)
		
		gluPerspective()                // Initialize projection matrix
		gluLookAt(                      // Initialize modelview matrix
			cameraPosition,
			lookAtPoint,
			upVector
		)
		
		initFreeType()                  // Initialize FreeType for on-screen text
	}
	
	
	/** Viewport rendering loop called every frame (capped at 60 FPS). */
	fun render() {
		clearColor(BG_COLOR)                                         // Background color
		glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)    // Clear the framebuffer
		
		// Draw the coordinate system
		drawAxes()
		drawGrid()
		
		// Render scene objects
		sceneManager.render(viewportMode, cameraPosition)
		
		// Draw the mouse ray
		//drawMouseRay()
		
		drawOnScreenText()
		
		glfwSwapBuffers(window)
		glfwPollEvents()
	}
	
	/**
	 * Draw on-screen debug text using FreeType
	 */
	private fun drawOnScreenText() {
		val cube = sceneManager.getSceneObjects()[0]
		val mouseWorld = screenToWorld(mouseX[0], mouseY[0], 0f)
		for (i in 0 .. 11) {
			renderText(
				when (i) {
					0    -> "FPS: $lastFPS"
					1    -> "Camera Pos: ${"%.3f".format(cameraPosition.x)} ${"%.3f".format(cameraPosition.y)} ${"%.3f".format(cameraPosition.z)}"
					2    -> "Camera Rot: ${"%.1f".format(rotH)} / ${"%.1f".format(rotV)}"
					3    -> "Zoom: ${"%.3f".format(cameraDistance)}"
					4    -> "Mouse Screen: ${mouseX[0]} / ${mouseY[0]}"
					5    -> "Mouse World: ${"%.3f".format(mouseWorld.x)} ${"%.3f".format(mouseWorld.y)} ${"%.3f".format(mouseWorld.z)}"
					6    -> "Mode: $viewportMode${if (transformMode != Mode.NONE) " $transformMode" else ""}${if (transformMode.subMode != SubMode.NONE) " ${transformMode.subMode}" else ""}"
					7    -> "Transform: ${"%.3f".format(transformation.x)} ${"%.3f".format(transformation.y)} ${"%.3f".format(transformation.z)}"
					8    -> "Cube:"
					9    -> "    Pos: ${"%.3f".format(cube.position.x)} ${"%.3f".format(cube.position.y)} ${"%.3f".format(cube.position.z)}"
					10   -> "    Scale: ${"%.3f".format(cube.scale.x)} ${"%.3f".format(cube.scale.y)} ${"%.3f".format(cube.scale.z)}"
					else -> "    Rot: ${"%.3f".format(cube.rotation.x)} ${"%.3f".format(cube.rotation.y)} ${"%.3f".format(cube.rotation.z)}"
				},
				firstLineX,
				i,
				fontScale,
				TEXT_COLOR
			)
		}
	}
	
	
	/**
	 * Function to set up a perspective projection matrix, which is essential for rendering 3D scenes
	 * in a way that simulates human vision, where objects further away appear smaller than those closer.
	 */
	private fun gluPerspective() {
		glMatrixMode(GL_PROJECTION)     // Subsequent matrix operations will affect the projection matrix
		
		val fH = tan(Math.toRadians(FOV_Y) / 2.0) * Z_NEAR  // Height of the Near Clipping Plane
		val fW = fH * aspect                                   //  Width of the Near Clipping Plane
		
		// Update the perspective projection matrix based on the calculated dimensions
		glLoadIdentity()
		glFrustum(-fW, fW, -fH, fH, Z_NEAR.toDouble(), Z_FAR.toDouble())
	}
	
	/**
	 * Defines a viewing transformation by specifying an eye point, a reference point indicating.
	 * the center of the scene, and an up vector.
	 *
	 * @param eye    Coordinates of the eye point (camera position).
	 * @param center Coordinates of the reference point (look-at point).
	 * @param up     Coordinates of the up vector, which defines the upwards direction relative to the camera.
	 *               It typically points upwards in the world coordinate system but can be adjusted to tilt the camera.
	 */
	private fun gluLookAt(eye: Vector3, center: Vector3, up: Vector3) {
		glMatrixMode(GL_MODELVIEW)      // Subsequent matrix operations will affect the modelview matrix
		
		// Calculate the forward vector (direction from eye to center)
		val forward = (center - eye).normalize()
		
		// Calculate the side vector (perpendicular to both forward and up vectors)
		val side = (forward cross up).normalize()
		
		// Recalculate the actual up vector to ensure orthogonality
		val zUp = (side cross forward)
		
		// Construct the view matrix, which is used to transform coordinates from world space to camera space
		viewMatrix = floatArrayOf(
			 side.x,         zUp.x,       -forward.x,        0f,
			 side.y,         zUp.y,       -forward.y,        0f,
			 side.z,         zUp.z,       -forward.z,        0f,
			-side.dot(eye), -zUp.dot(eye), forward.dot(eye), 1f
		)
		
		// Update the view matrix, which sets up the camera's orientation and position
		glLoadIdentity()
		glMultMatrixf(viewMatrix)
	}
	
	/** Update camera position based on spherical coordinates. */
	private fun updateCameraPosition() {
		val radiansH = Math.toRadians(rotH.toDouble()).toFloat()
		val radiansV = Math.toRadians(rotV.toDouble()).toFloat()
		
		val cosV = cos(radiansV)
		val sinV = sin(radiansV)
		val cosH = cos(radiansH)
		val sinH = sin(radiansH)
		
		cameraPosition = Vector3(
			cameraDistance * cosV * cosH,
			cameraDistance * cosV * -sinH,   // -sinH because of Z-up
			cameraDistance * sinV
		)
	}
	
	
	// User input processing -> see Controls.kt for callbacks
	
	fun windowResize(width: Int, height: Int) {
		glViewport(0, 0, width, height)
		windowWidth = width
		windowHeight = height
		aspect = width.toFloat() / height.toFloat()
		gluPerspective()
		render()    // Update viewport
	}
	
	fun select() {
		handleSelection(mouseX[0], mouseY[0])
		
		if (transformMode != Mode.NONE) {
			// Applied transformations
			transformation          = Vector3.ZERO      // Reset transformation vector
			lastTransformation      = Vector3.ZERO
			transformMode           = Mode.NONE         // Go back to View Mode
			transformMode.subMode   = SubMode.NONE
		}
	}
	
	fun initRotation(isRotating: Boolean) {
		rotating = isRotating
		if (rotating) {
			lastH = mouseX[0]
			lastV = mouseY[0]
		}
	}
	
	fun transform(mouseX: Double, mouseY: Double) {
		glfwGetCursorPos(window, this.mouseX, this.mouseY)
		
		when (transformMode) {
			Mode.NONE -> {  // View Mode
				if (rotating) {
					val dx = mouseX - lastH
					val dy = mouseY - lastV
					rotH = ((rotH + dx * rotationSensitivity) % 360).toFloat()  // Adjust horizontal rotation
					rotV = ((rotV + dy * rotationSensitivity) % 360).toFloat()  // Adjust vertical rotation
					lastH = mouseX
					lastV = mouseY
					
					// Apply camera rotation
					updateCameraPosition()
					gluLookAt(cameraPosition, lookAtPoint, upVector)
				}
			}
			Mode.GRAB -> {
				val worldPosition = screenToWorld(mouseX, mouseY, 0f)                             // Ray from the mouse position
				
				// Truncate world position vector based on the selected direction (Transformation Sub Mode)
				val wpDirectional = when (transformMode.subMode) {
					SubMode.NONE -> worldPosition                                                               // Omnidirectional (freehand) translation
					// TODO: Make the unidirectional translation also follow the cursor
					SubMode.X    -> Vector3(worldPosition.x, 0f, 0f)                                      // X-directional translation
					SubMode.Y    -> Vector3(0f, worldPosition.y, 0f)                                      // Y-directional translation
					SubMode.Z    -> Vector3(0f, 0f, worldPosition.z)                                      // Z-directional translation
				}
				
				val grabZ = (sceneManager.selectedObject!!.position - cameraPosition).length()            // Distance of the object from the camera
				lastTransformation = if (lastTransformation == Vector3.ZERO) wpDirectional                      // Ensure last transformation is non-zero
									 else lastTransformation
				transformation = (wpDirectional - lastTransformation) * grabZ                                   // Calculate transformation vector
				(sceneManager.selectedObject as? Mesh)!!.applyTransformation(transformMode, transformation)     // Apply translation
				lastTransformation = wpDirectional                                                              // Save translation difference
			}
			Mode.SCALE -> {
				TODO("Scaling not yet implemented")
			}
			Mode.ROTATE -> {
				TODO("Rotation not yet implemented")
			}
			Mode.EXTRUDE -> {
				TODO("Extrusion not yet implemented")
			}
			Mode.FILL -> {
				TODO("Filling not yet implemented")
			}
			Mode.MERGE -> {
				TODO("Merging not yet implemented")
			}
			else -> throw IllegalStateException("Wrong transform mode")
		}
	}
	
	/** Handle the mouse scroll event to zoom in and out. */
	fun zoom(yoffset: Double) {
		// Calculate the zoom speed scalar based on the current camera distance
		zoomSpeedScalar = (cameraDistance / CAMERA_DISTANCE_INIT) * ZOOM_SENSITIVITY
		
		// Update the camera distance with the scroll input
		cameraDistance -= yoffset.toFloat() * zoomSpeedScalar
		cameraDistance = cameraDistance.coerceIn(CAMERA_DISTANCE_MIN, CAMERA_DISTANCE_MAX)
		
		// Apply zoom
		updateCameraPosition()
		gluLookAt(cameraPosition, lookAtPoint, upVector)
	}
	
	fun togglePerspective(h: Float, v: Float) {
		rotH = h
		rotV = v
		updateCameraPosition()    // Apply camera rotation
		gluLookAt(cameraPosition, lookAtPoint, upVector)
	}
	
	fun toggleViewportMode() {
		viewportMode = when (viewportMode) {
			Mode.OBJECT -> Mode.EDIT
			Mode.EDIT -> Mode.OBJECT
			else -> viewportMode
		}
	}
	
	fun changeTransformMode(mode: Mode) {
		sceneManager.selectedObject ?: return       // Don't change mode if no Object is selected
		transformMode = mode
		transformMode.subMode = SubMode.NONE        // Reset direction
	}
	
	fun changeTransformSubMode(subMode: SubMode) {
		sceneManager.selectedObject ?: return       // Don't change mode if no Object is selected
		if (transformMode.type == ModeType.TRANSFORM) {
			transformMode.subMode = subMode
		}
	}
	
	
	// Quick maths
	
	/**
	 * This function maps screen space (2D mouse coordinates) to world space (3D).
	 *
	 * @param mouseX The x-coordinate of the mouse position in screen space.
	 * @param mouseY The y-coordinate of the mouse position in screen space.
	 * @param depth A Z-value in normalized device coordinates.
	 *              Use 1f for a ray (far plane),
	 *              and 0f for the near plane or exact position grabbing.
	 *
	 * @return the world space coordinates for the given mouse position as a Vector3
	 */
	private fun screenToWorld(mouseX: Double, mouseY: Double, depth: Float): Vector3 {
		// Get viewport, projection, and modelview matrices
		glGetIntegerv(GL_VIEWPORT, viewport)
		glGetFloatv(GL_PROJECTION_MATRIX, projectionMatrix)
		glGetFloatv(GL_MODELVIEW_MATRIX, viewMatrix)
		
		// Convert mouse coordinates to normalized device coordinates (NDC)
		val x = (2f * mouseX) / viewport[2] - 1f
		val y = 1f - (2f * mouseY / viewport[3])
		
		// Create a vector in clip space
		val viewSpace = Vector4(x.toFloat(), y.toFloat(), depth, 1f)
		
		// Transform from clip space to view space by applying the inverse of the projection matrix
		val clipSpace = Matrix4x4(projectionMatrix).invert() * viewSpace
		
		// Set the Z to -1 for proper unprojection and W to 0 for direction vector in the case of a ray
		val unprojectedClipSpace = Vector4(clipSpace.x, clipSpace.y, -1f, 0f)
		
		// Transform from clip space to world space by applying the inverse of the view matrix
		val worldSpace = Matrix4x4(viewMatrix).invert() * unprojectedClipSpace
		
		// Return the world space as a Vector3
		return Vector3(worldSpace.x, worldSpace.y, worldSpace.z)
	}
	
	/**
	 * Computes a [Ray] from the camera through the specified mouse coordinates in the window.
	 *
	 * This function converts the mouse screen coordinates to normalized device coordinates (NDC),
	 * then transforms these coordinates into world space to generate a Ray. The Ray is defined by
	 * its origin (the camera position) and its direction (the computed direction in world space).
	 *
	 * @param mouseX The x-coordinate of the mouse position in screen space.
	 * @param mouseY The y-coordinate of the mouse position in screen space.
	 *
	 * @return A [Ray] object representing the origin and direction of the ray in world space.
	 */
	private fun getMouseRay(mouseX: Double, mouseY: Double): Ray {
		// Get the Ray's direction based on the mouse screen coordinates
		val direction = screenToWorld(mouseX, mouseY, 1f).normalize()
		
		// Construct the ray using the camera's position and the direction
		return Ray(cameraPosition, direction)
	}
	
	/**
	 * Handles the selection of [Object]s in the scene based on the current mouse position.
	 *
	 * This function retrieves the mouse Ray using the current mouse coordinates and checks for
	 * intersections with any [Mesh] in the scene. If a Mesh is intersected, it is selected
	 * by the [SceneManager]. Additionally, the Ray's origin and calculated endpoint are logged
	 * for debugging purposes.
	 *
	 * @see getMouseRay for the ray computation logic.
	 */
	private fun handleSelection(selectX: Double, selectY: Double) {
		val ray = getMouseRay(selectX, selectY)
		
		//val directionScaled = (ray.direction * MOUSE_RAY_LENGTH)
		//rayStart = ray.origin
		//rayEnd   = (ray.origin + directionScaled)
		
		val intersectingObjects = mutableListOf<Object>()
		for (obj in sceneManager.getSceneObjects()) {
			if (obj is Mesh && ray.intersects(obj)) {
				intersectingObjects.add(obj)
			}
		}
		if (intersectingObjects.isEmpty()) sceneManager.selectObject(null)
		else {
			val closestIntersectingObject = intersectingObjects
				.minBy { ((it as Mesh).position - cameraPosition).length() }
			sceneManager.selectObject(closestIntersectingObject)
		}
	}
	
	
	// Drawing functions
	
	/** Draw the coordinate system axes. */
	private fun drawAxes() {
		glBegin(GL_LINES)
		
		// X-axis in red
		color3f(RED)
		glVertex3f(-AXES_LENGTH, 0f, 0f)    // Start point
		glVertex3f( AXES_LENGTH, 0f, 0f)    // End point
		
		// Y-axis in green
		color3f(GREEN)
		glVertex3f(0f, -AXES_LENGTH, 0f)
		glVertex3f(0f,  AXES_LENGTH, 0f)
		
		// Z-axis in blue
		color3f(BLUE)
		glVertex3f(0f, 0f, -AXES_LENGTH)
		glVertex3f(0f, 0f,  AXES_LENGTH)
		
		glEnd()
	}
	
	/** Draw a grid in the xy-plane to visualize the coordinate system. */
	private fun drawGrid() {
		glBegin(GL_LINES)
		
		// Grid lines in the x direction (front-back lines)
		color3f(GRID_COLOR)
		for (x in -AXES_LENGTH.toInt() .. AXES_LENGTH.toInt()) {
			// Horizontal line at y = constant, spanning x-axis
			glVertex3f(x.toFloat(), -AXES_LENGTH, 0f)
			glVertex3f(x.toFloat(),  AXES_LENGTH, 0f)
		}
		
		// Grid lines in the y direction (left-right lines)
		for (y in -AXES_LENGTH.toInt() .. AXES_LENGTH.toInt()) {
			// Vertical line at x = constant, spanning y-axis
			glVertex3f(-AXES_LENGTH, y.toFloat(), 0f)
			glVertex3f( AXES_LENGTH, y.toFloat(), 0f)
		}
		
		glEnd()
	}
	
	/**
	 * Draw the [Ray] that's created when the user clicks anywhere in the Viewport
	 */
	/*private fun drawMouseRay() {
		glPointSize(5f)
		glBegin(GL_POINTS)
		color3f(RED)
		glVertex3f(rayStart.x, rayStart.y, rayStart.z)
		glVertex3f(rayEnd.x, rayEnd.y, rayEnd.z)
		glEnd()
		
		glBegin(GL_LINES)
		color3f(RAY_COLOR)
		glVertex3f(rayStart.x, rayStart.y, rayStart.z)
		glVertex3f(rayEnd.x, rayEnd.y, rayEnd.z)
		glEnd()
	}*/
}
