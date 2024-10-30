package kengine.objects.mesh

import kengine.math.Triangle
import kengine.math.vector.Vector3
import kengine.objects.Object
import kengine.scene.Mode
import kotlin.math.cos
import kotlin.math.sin

interface Mesh : Object {
	override var position: Vector3
	override var scale: Vector3
	override var rotation: Vector3
	
	val vertices: MutableList<Vector3>
	val faceIndices: IntArray
	val edgeIndices: IntArray
	val edgeToFaceMap: MutableMap<Pair<Int, Int>, MutableList<Int>>  // edge-to-face adjacency information
	
	// Apply transformations to the object
	fun applyTransformation(mode: Mode, transformation: Vector3) {
		when (mode) {
			Mode.GRAB  -> {
				position += transformation
				
				// Translate each vertex by the same transformation
				for (i in vertices.indices) {
					vertices[i] += transformation
				}
			}
			Mode.SCALE -> {
				val oldScale = scale
				scale *= transformation
				
				// Apply scaling to each vertex based on the new scale
				for (i in vertices.indices) {
					vertices[i] = (vertices[i] - position) * (scale / oldScale) + position
				}
			}
			Mode.ROTATE -> {
				rotation += transformation
				
				// Get the rotation angles in degrees from the transformation vector
				val rotationRadians = transformation * (Math.PI / 180).toFloat() // Convert to radians
				
				// Calculate sine and cosine for the rotation angles
				val cosX = cos(rotationRadians.x)
				val sinX = sin(rotationRadians.x)
				val cosY = cos(rotationRadians.y)
				val sinY = sin(rotationRadians.y)
				val cosZ = cos(rotationRadians.z)
				val sinZ = sin(rotationRadians.z)
				
				// Apply the rotation to each vertex
				for (i in vertices.indices) {
					// Translate vertex to origin
					var vertex = vertices[i] - position
					
					// Rotate around x-axis
					val newY1 = vertex.y * cosX - vertex.z * sinX
					val newZ1 = vertex.y * sinX + vertex.z * cosX
					vertex = Vector3(vertex.x, newY1, newZ1)
					
					// Rotate around y-axis
					val newX2 = vertex.x * cosY + vertex.z * sinY
					val newZ2 = -vertex.x * sinY + vertex.z * cosY
					vertex = Vector3(newX2, vertex.y, newZ2)
					
					// Rotate around z-axis
					val newX3 = vertex.x * cosZ - vertex.y * sinZ
					val newY3 = vertex.x * sinZ + vertex.y * cosZ
					vertex = Vector3(newX3, newY3, vertex.z)
					
					// Translate back to the original position
					vertices[i] = vertex + position
				}
			}
			else -> TODO("Not yet implemented")   // TODO("For MESHDATA operations, make sure to call buildEdgeToFaceMap() again after applying changes")
		}
	}
	
	fun getTriangles(): List<Triangle> {
		val triangles = mutableListOf<Triangle>()
		for (i in faceIndices.indices step 3) {
			if (i + 2 < faceIndices.size) {     // Ensure we are not going out of bounds
				triangles.add(
					Triangle(
						vertices[faceIndices[i]],
						vertices[faceIndices[i + 1]],
						vertices[faceIndices[i + 2]]
					)
				)
			}
		}
		return triangles
	}
	
	// Build the edge-to-face adjacency map for the mesh
	fun buildEdgeToFaceMap() {
		edgeToFaceMap.clear()
		for (i in faceIndices.indices step 3) {
			val faceIndex = i / 3
			
			// Get the 3 vertices that form a face
			val v0 = faceIndices[i]
			val v1 = faceIndices[i + 1]
			val v2 = faceIndices[i + 2]
			
			// Add edges to the adjacency map (ensure that smaller index comes first for consistency)
			addEdgeToMap(v0, v1, faceIndex)
			addEdgeToMap(v1, v2, faceIndex)
			addEdgeToMap(v2, v0, faceIndex)
		}
	}
	
	// Helper function to add an edge to the map
	private fun addEdgeToMap(v0: Int, v1: Int, faceIndex: Int) {
		// Ensure that the order of the vertices is consistent
		val edge = if (v0 < v1) Pair(v0, v1) else Pair(v1, v0)
		
		if (!edgeToFaceMap.containsKey(edge)) {
			edgeToFaceMap[edge] = mutableListOf(faceIndex)
		} else {
			edgeToFaceMap[edge]!!.add(faceIndex)
		}
	}
}