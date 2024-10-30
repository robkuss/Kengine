package kengine.math.matrix

import kengine.math.EPSILON
import kengine.math.vector.Vector4
import kotlin.math.abs

class Matrix4x4(private val matrix: FloatArray) {
	init {
		// Ensure the matrix is a 4x4 matrix (16 elements)
		require(matrix.size == 16) { "Matrix must be 4x4 (16 elements)" }
	}
	
	fun get() = this.matrix
	operator fun get(i: Int) = this.matrix[i]
	
	/** Matrix times vector */
	operator fun times(vec: Vector4) = Vector4(
		matrix[0] * vec.x + matrix[4] * vec.y + matrix[8]  * vec.z + matrix[12] * vec.w,
		matrix[1] * vec.x + matrix[5] * vec.y + matrix[9]  * vec.z + matrix[13] * vec.w,
		matrix[2] * vec.x + matrix[6] * vec.y + matrix[10] * vec.z + matrix[14] * vec.w,
		matrix[3] * vec.x + matrix[7] * vec.y + matrix[11] * vec.z + matrix[15] * vec.w
	)
	
	/** Matrix times matrix */
	operator fun times(other: Matrix4x4): Matrix4x4 {
		val result = FloatArray(16)
		
		// Loop through rows of the first matrix (this)
		for (i in 0 until 4) {
			// Loop through columns of the second matrix (other)
			for (j in 0 until 4) {
				// Calculate the dot product for element (i, j)
				var sum = 0f
				for (k in 0 until 4) {
					sum += this[i * 4 + k] * other[k * 4 + j]
				}
				result[i * 4 + j] = sum
			}
		}
		
		return Matrix4x4(result)
	}
	
	/**
	 * Invert a 4x4 Matrix
	 */
	fun invert(): Matrix4x4 {
		// Helper function to access matrix elements in row-major order
		fun get(m: FloatArray, row: Int, col: Int) = m[row * 4 + col]
		fun set(m: FloatArray, row: Int, col: Int, value: Float) { m[row * 4 + col] = value }
		
		// Initialize the identity matrix for the inverse
		val invOut = FloatArray(16) { if (it % 5 == 0) 1f else 0f }
		
		// Create a copy of the matrix to perform operations on
		val temp = this.matrix.copyOf()
		
		for (i in 0 until 4) {
			// Find the pivot element
			var maxRow = i
			var maxVal = abs(get(temp, i, i))
			for (k in i + 1 until 4) {
				val valK = abs(get(temp, k, i))
				if (valK > maxVal) {
					maxVal = valK
					maxRow = k
				}
			}
			
			// Swap rows in temp and invOut if necessary
			if (maxRow != i) {
				for (j in 0 until 4) {
					// Swap in temp
					val tempVal = get(temp, i, j)
					set(temp, i, j, get(temp, maxRow, j))
					set(temp, maxRow, j, tempVal)
					
					// Swap in invOut
					val invVal = get(invOut, i, j)
					set(invOut, i, j, get(invOut, maxRow, j))
					set(invOut, maxRow, j, invVal)
				}
			}
			
			// Check for singular matrix (pivot should not be zero)
			if (abs(get(temp, i, i)) < EPSILON) {
				println("Pivot is zero at row $i")
				throw IllegalArgumentException("Matrix is singular and cannot be inverted")
			}
			
			// Normalize the pivot row
			val pivot = get(temp, i, i)
			for (j in 0 until 4) {
				set(temp, i, j, get(temp, i, j) / pivot)
				set(invOut, i, j, get(invOut, i, j) / pivot)
			}
			
			// Eliminate the current column in other rows
			for (k in 0 until 4) {
				if (k != i) {
					val factor = get(temp, k, i)
					for (j in 0 until 4) {
						set(temp, k, j, get(temp, k, j) - factor * get(temp, i, j))
						set(invOut, k, j, get(invOut, k, j) - factor * get(invOut, i, j))
					}
				}
			}
		}
		
		return Matrix4x4(invOut)
	}
}
