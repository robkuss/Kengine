package kengine.scene.graphics

import kengine.scene.graphics.color.*
import kengine.objects.mesh.*

import org.lwjgl.opengl.GL20.*
import kengine.math.vector.Vector3

class MeshRenderer {
	fun render(mesh: Mesh, isSelected: Boolean, isEditMode: Boolean, cameraPosition: Vector3) {
		// Draw the faces with one color
		color3f(MESH_FACE_COLOR)
		glBegin(GL_TRIANGLES)
		for (i in mesh.getTriangles()) {
			glVertex3f(i.vertex0.x, i.vertex0.y, i.vertex0.z)
			glVertex3f(i.vertex1.x, i.vertex1.y, i.vertex1.z)
			glVertex3f(i.vertex2.x, i.vertex2.y, i.vertex2.z)
		}
		glEnd()
		
		if (isEditMode) {
			// Draw the edges with a different color
			val edgeColor = if (isSelected) MESH_SELECT_COLOR else MESH_EDGE_COLOR
			color3f(edgeColor)
			glBegin(GL_LINES)
			for (i in mesh.edgeIndices.indices step 2) {
				val vertex0 = mesh.vertices[mesh.edgeIndices[i]]
				val vertex1 = mesh.vertices[mesh.edgeIndices[i + 1]]
				glVertex3f(vertex0.x, vertex0.y, vertex0.z)
				glVertex3f(vertex1.x, vertex1.y, vertex1.z)
			}
			glEnd()
			
			// Draw the vertices with another color
			val vertColor = if (isSelected) MESH_SELECT_COLOR else MESH_VERT_COLOR
			color3f(vertColor)
			glPointSize(4.0f)
			glBegin(GL_POINTS)
			for (i in mesh.vertices.indices) {
				glVertex3f(mesh.vertices[i].x, mesh.vertices[i].y, mesh.vertices[i].z)
			}
			glEnd()
		} else if (isSelected) {
			// Only highlight outline of the mesh in Object Mode
			val outlineColor = MESH_SELECT_COLOR
			color3f(outlineColor)
			glLineWidth(4.0f)  // Set a thicker line width for the outline
			
			// Draw only the silhouette edges
			for ((edge, faces) in mesh.edgeToFaceMap) {
				if (isSilhouetteEdge(mesh, faces, cameraPosition)) {
					glBegin(GL_LINES)
					val vertex0 = mesh.vertices[edge.first]
					val vertex1 = mesh.vertices[edge.second]
					glVertex3f(vertex0.x, vertex0.y, vertex0.z)
					glVertex3f(vertex1.x, vertex1.y, vertex1.z)
					glEnd()
					
					// Also highlight the vertices of silhouette edges
					glPointSize(3.0f)
					glBegin(GL_POINTS)
					glVertex3f(vertex0.x, vertex0.y, vertex0.z)
					glVertex3f(vertex1.x, vertex1.y, vertex1.z)
					glEnd()
				}
			}
			
			// Reset line width back to default
			glLineWidth(1.0f)
		}
	}
	
	// Determine if an edge is part of the silhouette based on face adjacency
	private fun isSilhouetteEdge(mesh: Mesh, faces: List<Int>, cameraPosition: Vector3): Boolean {
		if (faces.size == 1) return true  // If only one face shares this edge, it's on the silhouette
		
		// Get the normals of the two faces
		val normal1 = calculateFaceNormal(mesh, faces[0])
		val normal2 = calculateFaceNormal(mesh, faces[1])
		
		// Use any point from the first face to compute the direction to the camera
		val pointOnFace = mesh.vertices[mesh.faceIndices[faces[0] * 3]]
		val cameraDirection = (pointOnFace - cameraPosition).normalize()
		
		// Compute the dot products of the camera direction with the face normals
		val dot1 = normal1 dot cameraDirection
		val dot2 = normal2 dot cameraDirection
		
		// If one face is front-facing and the other is back-facing, the edge is part of the silhouette
		return (dot1 > 0f && dot2 < 0f) || (dot1 < 0f && dot2 > 0f)
	}
	
	// Calculate the normal for a face
	private fun calculateFaceNormal(mesh: Mesh, faceIndex: Int): Vector3 {
		val v0 = mesh.vertices[mesh.faceIndices[faceIndex * 3]]
		val v1 = mesh.vertices[mesh.faceIndices[faceIndex * 3 + 1]]
		val v2 = mesh.vertices[mesh.faceIndices[faceIndex * 3 + 2]]
		
		// Compute the two edge vectors
		val edge1 = v1 - v0
		val edge2 = v2 - v0
		
		// Compute the normal using the cross product
		return (edge1 cross edge2).normalize()
	}
}