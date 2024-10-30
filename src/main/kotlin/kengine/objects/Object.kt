package kengine.objects

import kengine.math.vector.Vector3

interface Object {
	var name: String
	
	var position: Vector3
	var scale: Vector3
	var rotation: Vector3
}