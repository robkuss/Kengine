package kengine.math.vector

import kotlin.math.sqrt

class Vector2(var x: Float, var y: Float) : Vector {
	companion object {
		val ZERO = Vector2(0f, 0f)
	}
	
	override fun toString() = "$x, $y"
	
	override fun get(index: Int): Float {
		return when (index) {
			0 -> x
			1 -> y
			else -> throw IndexOutOfBoundsException("Index out of bounds for 2D vector")
		}
	}
	
	override fun plus(other: Vector): Vector2 {
		if (other !is Vector2) throw IllegalArgumentException("Vector must be of type Vector2")
		return Vector2(
			this.x + other.x,
			this.y + other.y
		)
	}
	
	override fun minus(other: Vector): Vector2 {
		if (other !is Vector2) throw IllegalArgumentException("Vector must be of type Vector2")
		return Vector2(
			this.x - other.x,
			this.y - other.y
		)
	}
	
	override fun times(scalar: Float): Vector2 {
		return Vector2(
			x * scalar,
			y * scalar
		)
	}
	
	override fun times(other: Vector): Vector2 {
		if (other !is Vector2) throw IllegalArgumentException("Vector must be of type Vector2")
		return Vector2(
			this.x * other.x,
			this.y * other.y
		)
	}
	
	override fun div(scalar: Float): Vector2 {
		return Vector2(
			x / scalar,
			y / scalar
		)
	}
	
	override fun div(other: Vector): Vector2 {
		if (other !is Vector2) throw IllegalArgumentException("Vector must be of type Vector2")
		return Vector2(
			this.x / other.x,
			this.y / other.y
		)
	}
	
	override fun unaryMinus(): Vector2 {
		return Vector2(
			-x,
			-y
		)
	}
	
	override fun length(): Float {
		return sqrt(x * x + y * y)
	}
	
	override fun normalize(): Vector2 {
		val len = length()
		if (len == 0f) return this
		return this / len
	}
	
	override fun distance(other: Vector): Float {
		if (other !is Vector2) throw IllegalArgumentException("Vector must be of type Vector2")
		return sqrt(
		 (this.x - other.x) * (this.x - other.x) +
			(this.y - other.y) * (this.y - other.y)
		)
	}
	
	override infix fun dot(other: Vector): Float {
		if (other !is Vector2) throw IllegalArgumentException("Vector must be of type Vector2")
		return (this.x * other.x) + (this.y * other.y)
	}
}