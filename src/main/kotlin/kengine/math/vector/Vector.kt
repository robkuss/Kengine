package kengine.math.vector

interface Vector {
	// Access a component by index
	operator fun get(index: Int): Float
	
	// Add another vector
	operator fun plus(other: Vector): Vector
	
	// Subtract another vector
	operator fun minus(other: Vector): Vector
	
	// Scale the vector by a scalar
	operator fun times(scalar: Float): Vector
	
	// Element-wise multiplication (Hadamard product)
	operator fun times(other: Vector): Vector
	
	// Divide the vector by a scalar
	operator fun div(scalar: Float): Vector
	
	// Element-wise division
	operator fun div(other: Vector): Vector
	
	// Unary minus (negation)
	operator fun unaryMinus(): Vector
	
	// Compute the length (magnitude) of the vector
	fun length(): Float
	
	// Normalize the vector to a unit vector
	fun normalize(): Vector
	
	// Compute the distance between two vectors
	fun distance(other: Vector): Float
	
	// Compute the dot product with another vector
	infix fun dot(other: Vector): Float
}