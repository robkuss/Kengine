package kengine.scene

import kengine.math.vector.Vector3
import kengine.objects.Object
import kengine.objects.mesh.*
import kengine.scene.graphics.MeshRenderer

class SceneManager(private val meshRenderer: MeshRenderer) {
	private val sceneObjects = mutableListOf<Object>()
	var selectedObject: Object? = null
	
	init {
		// Add Default Cube to scene
		addObject(
			Cube("Cube", 1f, Vector3(0.5f, 0.5f, 0.5f))
		)
	}
	
	fun render(mode: Mode, cameraPosition: Vector3) {
		for (obj in sceneObjects.filterIsInstance<Mesh>()) {
			meshRenderer.render(obj, obj == selectedObject, mode == Mode.EDIT, cameraPosition)
		}
	}
	
	fun getSceneObjects() = sceneObjects
	
	private fun addObject(obj: Object) {
		sceneObjects.add(obj)
	}
	
	private fun removeObject(obj: Object) {
		sceneObjects.remove(obj)
	}
	
	fun selectObject(obj: Object?) {
		selectedObject = obj
	}
}