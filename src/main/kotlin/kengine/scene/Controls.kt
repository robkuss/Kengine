package kengine.scene

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWCharCallback
import kengine.scene.graphics.Viewport
import kengine.window

/**
 * Initialization function to set callbacks for events:
 *   - Window resizing
 *   - Mouse:
 *      - Left mouse button:
 *          - Select object
 *      - Middle mouse button:
 *          - Hold and drag: Rotate Viewport around the origin
 *          - Scroll: Zoom in and out of the Viewport
 *   - Keys:
 *      - Number keys:
 *          - Toggle perspective
 *      - TAB
 *          - Toggle Object/Edit Mode
 *      - Object transformation:
 *          - G: Grab
 *          - S: Scale
 *          - R: Rotate
 *          - E: Extrude
 *          - F: Fill
 *          - M: Merge
 */
fun setCallbacks(viewport: Viewport) {
	// Window resize callback
	GLFW.glfwSetFramebufferSizeCallback(window) { _, width, height ->
		viewport.windowResize(width, height)
	}
	
	// Mouse button callbacks
	GLFW.glfwSetMouseButtonCallback(window) { _, button, action, _ ->
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
			viewport.select()
		}
		if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
			viewport.initRotation(action == GLFW.GLFW_PRESS)
		}
	}
	
	// Cursor position callback
	GLFW.glfwSetCursorPosCallback(window) { _, xpos, ypos ->
		viewport.transform(xpos, ypos)
	}
	
	// Mouse scroll callback
	GLFW.glfwSetScrollCallback(window) { _, _, yoffset ->
		viewport.zoom(yoffset)
	}
	
	// Keys callbacks: Non-printable characters
	GLFW.glfwSetKeyCallback(window) { _, key, _, action, _ ->
		if (action == GLFW.GLFW_PRESS) {
			when (key) {
				// Change Viewport Mode
				GLFW.GLFW_KEY_TAB -> viewport.toggleViewportMode()     // TAB -> Toggle Object/Edit Mode
			}
		}
	}
	
	// Key callbacks: Printable characters (using char callbacks to work for different keyboard layouts)
	GLFW.glfwSetCharCallback(window, object : GLFWCharCallback() {
		override fun invoke(window: Long, codepoint: Int) {
			when (codepoint.toChar()) {
				// Number keys for perspective toggling
				'1' -> viewport.togglePerspective(  0f,  0f)     // 1 -> Front View  (towards negative X)
				'2' -> viewport.togglePerspective(-90f,  0f)     // 2 -> Right View  (towards negative Y)
				'3' -> viewport.togglePerspective(  0f, 90f)     // 3 -> Top View    (towards negative Z)
				'4' -> viewport.togglePerspective(180f,  0f)     // 4 -> Back View   (towards positive X)
				'5' -> viewport.togglePerspective(90f,   0f)     // 5 -> Left View   (towards positive Y)
				'6' -> viewport.togglePerspective( 0f, -90f)     // 6 -> Bottom View (towards positive Z)
				
				// Change Transform Mode
				'g' -> viewport.changeTransformMode(Mode.GRAB)         // G -> Grab
				's' -> viewport.changeTransformMode(Mode.SCALE)        // S -> Scale
				'r' -> viewport.changeTransformMode(Mode.ROTATE)       // R -> Rotate
				'e' -> viewport.changeTransformMode(Mode.EXTRUDE)      // E -> Extrude
				'f' -> viewport.changeTransformMode(Mode.FILL)         // F -> Fill
				'm' -> viewport.changeTransformMode(Mode.MERGE)        // M -> Merge
				
				// Change Transform SubMode
				'x' -> viewport.changeTransformSubMode(SubMode.X)      // X -> Snap transformation to X direction
				'y' -> viewport.changeTransformSubMode(SubMode.Y)      // Y -> Snap transformation to Y direction
				'z' -> viewport.changeTransformSubMode(SubMode.Z)      // Z -> Snap transformation to Z direction
			}
		}
	})
}