package kengine.math.vector

import kotlin.math.sqrt

class Vector3(var x: Float, var y: Float, var z: Float) : Vector {
	companion object {
		val ZERO = Vector3(0f, 0f, 0f)
	}
	
	override fun toString() = "$x, $y, $z"
	
	override fun get(index: Int): Float {
		return when (index) {
			0 -> x
			1 -> y
			2 -> z
			else -> throw IndexOutOfBoundsException("Index out of bounds for 3D vector")
		}
	}
	
	override fun plus(other: Vector): Vector3 {
		if (other !is Vector3) throw IllegalArgumentException("Vector must be of type Vector3")
		return Vector3(
			this.x + other.x,
			this.y + other.y,
			this.z + other.z
		)
	}
	
	override fun minus(other: Vector): Vector3 {
		if (other !is Vector3) throw IllegalArgumentException("Vector must be of type Vector3")
		return Vector3(
			this.x - other.x,
			this.y - other.y,
			this.z - other.z
		)
	}
	
	override fun times(scalar: Float): Vector3 {
		return Vector3(
			x * scalar,
			y * scalar,
			z * scalar
		)
	}
	
	override fun times(other: Vector): Vector3 {
		if (other !is Vector3) throw IllegalArgumentException("Vector must be of type Vector3")
		return Vector3(
			this.x * other.x,
			this.y * other.y,
			this.z * other.z
		)
	}
	
	override fun div(scalar: Float): Vector3 {
		return Vector3(
			x / scalar,
			y / scalar,
			z / scalar
		)
	}
	
	override fun div(other: Vector): Vector3 {
		if (other !is Vector3) throw IllegalArgumentException("Vector must be of type Vector3")
		return Vector3(
			this.x / other.x,
			this.y / other.y,
			this.z / other.z
		)
	}
	
	override fun unaryMinus(): Vector3 {
		return Vector3(
			-x,
			-y,
			-z
		)
	}
	
	override fun length(): Float {
		return sqrt(x * x + y * y + z * z)
	}
	
	override fun normalize(): Vector3 {
		val len = length()
		if (len == 0f) return this
		return this / len
	}
	
	override fun distance(other: Vector): Float {
		if (other !is Vector3) throw IllegalArgumentException("Vector must be of type Vector3")
		return sqrt(
			 (this.x - other.x) * (this.x - other.x) +
				(this.y - other.y) * (this.y - other.y) +
				(this.z - other.z) * (this.z - other.z)
		)
	}
	
	override infix fun dot(other: Vector): Float {
		if (other !is Vector3) throw IllegalArgumentException("Vector must be of type Vector3")
		return this.x * other.x + this.y * other.y + this.z * other.z
	}
	
	// Cross product
	infix fun cross(other: Vector): Vector3 {
		if (other !is Vector3) throw IllegalArgumentException("Vector must be of type Vector3")
		return Vector3(
			this.y * other.z - this.z * other.y,
			this.z * other.x - this.x * other.z,
			this.x * other.y - this.y * other.x
		)
	}
}