package kengine.math

import kengine.math.vector.Vector3
import kengine.objects.mesh.Mesh

data class Ray(val origin: Vector3, val direction: Vector3) {
	/**
	 * Determines if this Ray intersects with any of the Triangles of a given Mesh.
	 *
	 * @param mesh The Mesh to test against. It must contain a number of Triangles.
	 * @return true if the Ray intersects the mesh, false otherwise.
	 */
	fun intersects(mesh: Mesh): Boolean {
		for (triangle in mesh.getTriangles()) {
			if (this.intersects(triangle)) {
				return true
			}
		}
		return false
	}
	
	/**
	 * Determines if this Ray intersects with a given Triangle in 3D space using the Möller–Trumbore intersection algorithm.
	 *
	 * @param triangle The Triangle to test for intersection. It must contain three vertices.
	 * @return true if the Ray intersects the Triangle, false otherwise.
	 */
	private fun intersects(triangle: Triangle): Boolean {
		val vertex0 = triangle.vertex0
		val vertex1 = triangle.vertex1
		val vertex2 = triangle.vertex2
		
		val edge1 = vertex1 - vertex0
		val edge2 = vertex2 - vertex0
		
		// Step 1: Calculate the determinant
		val h = direction cross edge2
		val a = edge1 dot h
		
		// If the determinant is close to 0, the ray lies in the plane of the triangle
		if (a > -EPSILON && a < EPSILON) {
			return false  // The ray is parallel to the triangle
		}
		
		val f = 1.0f / a
		val s = (origin - vertex0) as Vector3
		val u = f * (s dot h)
		
		// Check if the intersection is outside the triangle
		if (u < 0.0f || u > 1.0f) {
			return false
		}
		
		val q = s cross edge1
		val v = f * (direction dot q)
		
		// If the intersection lies outside the triangle
		if (v < 0.0f || u + v > 1.0f) {
			return false
		}
		
		// At this stage, we can compute t to find out where the intersection point is on the line
		val t = f * (edge2 dot q)
		
		// Check if the ray intersects the triangle (t > 0 indicates a valid intersection)
		return t > EPSILON
	}
	
	/**
	 * Intersect a ray with a plane.
	 *
	 * @param planePoint A point on the plane.
	 * @param planeNormal The normal vector of the plane.
	 *
	 * @return The intersection point, or null if the ray is parallel to the plane.
	 */
	private fun intersectWithPlane(planePoint: Vector3, planeNormal: Vector3): Vector3? {
		// Calculate the denominator of the intersection formula (dot product between ray direction and plane normal)
		val denominator = planeNormal dot direction
		
		// If denominator is close to 0, the ray is parallel to the plane and there is no intersection
		if (denominator > -EPSILON && denominator < EPSILON) {
			return null
		}
		
		// Compute the distance from the ray origin to the plane
		val t = (planePoint - origin) dot planeNormal / denominator
		
		// If t is negative, the intersection is behind the ray's origin
		if (t < 0) {
			return null
		}
		
		// Compute the intersection point as rayOrigin + t * rayDirection
		return origin + (direction * t)
	}
}
