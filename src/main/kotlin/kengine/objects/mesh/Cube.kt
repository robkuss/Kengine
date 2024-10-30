package kengine.objects.mesh

import kengine.math.vector.Vector3

/**
 * Mesh data for a Cube
 *
 * @param s side length
 */
class Cube(override var name: String, s: Float, override var position: Vector3) : Mesh {
	override var scale    = Vector3(1f, 1f, 1f)     // Default scale is 1 (no scaling)
	override var rotation = Vector3(0f, 0f, 0f)     // Default rotation (no rotation)
	
	override val vertices = mutableListOf(
		// Front face
		Vector3(-s/2 + position.x, -s/2 + position.y,  s/2 + position.z),  // Bottom-left
		Vector3( s/2 + position.x, -s/2 + position.y,  s/2 + position.z),  // Bottom-right
		Vector3( s/2 + position.x,  s/2 + position.y,  s/2 + position.z),  // Top-right
		Vector3(-s/2 + position.x,  s/2 + position.y,  s/2 + position.z),  // Top-left
		
		// Back face
		Vector3(-s/2 + position.x, -s/2 + position.y, -s/2 + position.z),  // Bottom-left
		Vector3( s/2 + position.x, -s/2 + position.y, -s/2 + position.z),  // Bottom-right
		Vector3( s/2 + position.x,  s/2 + position.y, -s/2 + position.z),  // Top-right
		Vector3(-s/2 + position.x,  s/2 + position.y, -s/2 + position.z),  // Top-left
	)
	
	override val faceIndices = intArrayOf(
		// Front face
		0, 1, 2,  2, 3, 0,
		// Right face
		1, 5, 6,  6, 2, 1,
		// Back face
		5, 4, 7,  7, 6, 5,
		// Left face
		4, 0, 3,  3, 7, 4,
		// Top face
		3, 2, 6,  6, 7, 3,
		// Bottom face
		4, 5, 1,  1, 0, 4
	)
	
	override val edgeIndices = intArrayOf(
		// Front face edges
		0, 1, 1, 2, 2, 3, 3, 0,
		// Back face edges
		4, 5, 5, 6, 6, 7, 7, 4,
		// Connect front and back faces
		0, 4, 1, 5, 2, 6, 3, 7
	)
	
	override val edgeToFaceMap: MutableMap<Pair<Int, Int>, MutableList<Int>> = mutableMapOf()
	
	init {
		buildEdgeToFaceMap()
	}
}