package kengine.math.vector

import kotlin.math.sqrt

class Vector4(var x: Float, var y: Float, var z: Float, var w: Float) : Vector {
	companion object {
		val ZERO = Vector4(0f, 0f, 0f, 0f)
	}
	
	override fun toString() = "$x, $y, $z, $w"
	
	override fun get(index: Int): Float {
		return when (index) {
			0 -> x
			1 -> y
			2 -> z
			3 -> w
			else -> throw IndexOutOfBoundsException("Index out of bounds for 4D vector")
		}
	}
	
	override fun plus(other: Vector): Vector4 {
		if (other !is Vector4) throw IllegalArgumentException("Vector must be of type Vector4")
		return Vector4(
			this.x + other.x,
			this.y + other.y,
			this.z + other.z,
			this.w + other.w
		)
	}
	
	override fun minus(other: Vector): Vector4 {
		if (other !is Vector4) throw IllegalArgumentException("Vector must be of type Vector4")
		return Vector4(
			this.x - other.x,
			this.y - other.y,
			this.z - other.z,
			this.w - other.w
		)
	}
	
	override fun times(scalar: Float): Vector4 {
		return Vector4(
			x * scalar,
			y * scalar,
			z * scalar,
			w * scalar
		)
	}
	
	override fun times(other: Vector): Vector4 {
		if (other !is Vector4) throw IllegalArgumentException("Vector must be of type Vector4")
		return Vector4(
			this.x * other.x,
			this.y * other.y,
			this.z * other.z,
			this.w * other.w
		)
	}
	
	override fun div(scalar: Float): Vector4 {
		return Vector4(
			x / scalar,
			y / scalar,
			z / scalar,
			w / scalar
		)
	}
	
	override fun div(other: Vector): Vector4 {
		if (other !is Vector4) throw IllegalArgumentException("Vector must be of type Vector4")
		return Vector4(
			this.x / other.x,
			this.y / other.y,
			this.z / other.z,
			this.w / other.w
		)
	}
	
	override fun unaryMinus(): Vector4 {
		return Vector4(
			-x,
			-y,
			-z,
			-w
		)
	}
	
	override fun length(): Float {
		return sqrt(x * x + y * y + z * z + w * w)
	}
	
	override fun normalize(): Vector4 {
		val len = length()
		if (len == 0f) return this
		return this / len
	}
	
	override fun distance(other: Vector): Float {
		if (other !is Vector4) throw IllegalArgumentException("Vector must be of type Vector4")
		return sqrt(
		 (this.x - other.x) * (this.x - other.x) +
			(this.y - other.y) * (this.y - other.y) +
			(this.z - other.z) * (this.z - other.z) +
			(this.w - other.w) * (this.w - other.w)
		)
	}
	
	override infix fun dot(other: Vector): Float {
		if (other !is Vector4) throw IllegalArgumentException("Vector must be of type Vector4")
		return (this.x * other.x) + (this.y * other.y) + (this.z * other.z) + (this.w * other.w)
	}
	
	
	fun toVector3() = Vector3(x, y, z)
	
	fun divideByW() {
		if (w != 0f) {
			x /= w
			y /= w
			z /= w
			w = 1.0f
		}
	}
}