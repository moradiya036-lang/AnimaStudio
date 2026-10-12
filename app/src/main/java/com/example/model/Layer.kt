package com.example.model

import java.util.UUID

enum class LayerType {
  VECTOR,
  SKELETON,
  PUPPET_MESH,
  SHAPE
}

enum class ShapeType {
  RECTANGLE,
  CIRCLE,
  STAR,
  MANGA_BALL
}

data class Layer(
  val id: String = UUID.randomUUID().toString(),
  var name: String,
  val type: LayerType,
  var isVisible: Boolean = true,
  var isLocked: Boolean = false,
  // Transform Properties
  val posX: AnimProperty = AnimProperty("Pos X", 0f),
  val posY: AnimProperty = AnimProperty("Pos Y", 0f),
  val scaleX: AnimProperty = AnimProperty("Scale X", 1f),
  val scaleY: AnimProperty = AnimProperty("Scale Y", 1f),
  val rotation: AnimProperty = AnimProperty("Rotation", 0f),
  val opacity: AnimProperty = AnimProperty("Opacity", 1f),

  // Specific payloads
  var vectorShape: VectorShape? = null,
  var skeletalRig: SkeletalRig? = null,
  var meshPuppet: MeshPuppet? = null,
  var shapeType: ShapeType? = null,

  var fillColor: Long = 0xFF00E676,
  var strokeColor: Long = 0xFF00E5FF,
  var strokeWidth: Float = 4f,

  // Bone tracks for skeletal rigs: map of boneId -> AnimProperty(localAngle)
  val boneAngleTracks: MutableMap<String, AnimProperty> = mutableMapOf()
) {
  fun hasKeyframeAt(frame: Int): Boolean {
    return posX.hasKeyframeAt(frame) ||
           posY.hasKeyframeAt(frame) ||
           scaleX.hasKeyframeAt(frame) ||
           scaleY.hasKeyframeAt(frame) ||
           rotation.hasKeyframeAt(frame) ||
           opacity.hasKeyframeAt(frame) ||
           boneAngleTracks.values.any { it.hasKeyframeAt(frame) }
  }

  fun removeKeyframeAt(frame: Int) {
    posX.removeKeyframe(frame)
    posY.removeKeyframe(frame)
    scaleX.removeKeyframe(frame)
    scaleY.removeKeyframe(frame)
    rotation.removeKeyframe(frame)
    opacity.removeKeyframe(frame)
    boneAngleTracks.values.forEach { it.removeKeyframe(frame) }
  }
}
