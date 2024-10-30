package kengine.scene

enum class Mode(val type: ModeType, var subMode: SubMode = SubMode.NONE) {
	NONE(ModeType.NONE),
	OBJECT(ModeType.VIEW),
	EDIT(ModeType.VIEW),
	GRAB(ModeType.TRANSFORM),
	SCALE(ModeType.TRANSFORM),
	ROTATE(ModeType.TRANSFORM),
	EXTRUDE(ModeType.MESHDATA),
	FILL(ModeType.MESHDATA),
	MERGE(ModeType.MESHDATA)
}

enum class SubMode {
    NONE,
	X,
	Y,
	Z
}

enum class ModeType {
	NONE,
	VIEW,       // Select (Object & Edit Mode)
	TRANSFORM,  // Grab, Scale, Rotate
	MESHDATA    // Extrude, Fill
}