package com.example.model

import java.util.UUID

/**
 * Subsystem B: Bone Hierarchies & Analytical IK
 */
data class Bone(
  val id: String = UUID.randomUUID().toString(),
  var name: String = "Bone",
  val parentId: String? = null,
  var length: Float = 100f,
  var localAngleDegrees: Float = 0f, // Angle relative to parent
  var worldAngleDegrees: Float = 0f, // Computed absolute world angle
  var startX: Float = 0f,
  var startY: Float = 0f,
  var endX: Float = 0f,
  var endY: Float = 0f,
  var color: Long = 0xFF00E5FF,
  // IK attributes
  var isIKTarget: Boolean = false,
  var ikTargetX: Float = 0f,
  var ikTargetY: Float = 0f
)

data class SkeletalRig(
  val id: String = UUID.randomUUID().toString(),
  var name: String = "Skeleton Rig",
  var rootX: Float = 300f,
  var rootY: Float = 400f,
  val bones: MutableList<Bone> = mutableListOf(),
  var isIKActive: Boolean = true
) {
  /**
   * Recalculates all bone world positions from the root down the hierarchy
   */
  fun updateHierarchy() {
    val boneMap = bones.associateBy { it.id }

    for (bone in bones) {
      if (bone.parentId == null) {
        // Root bone
        bone.startX = rootX
        bone.startY = rootY
        bone.worldAngleDegrees = bone.localAngleDegrees
      } else {
        val parent = boneMap[bone.parentId]
        if (parent != null) {
          bone.startX = parent.endX
          bone.startY = parent.endY
          bone.worldAngleDegrees = parent.worldAngleDegrees + bone.localAngleDegrees
        }
      }

      val rad = Math.toRadians(bone.worldAngleDegrees.toDouble())
      bone.endX = (bone.startX + Math.cos(rad) * bone.length).toFloat()
      bone.endY = (bone.startY + Math.sin(rad) * bone.length).toFloat()
    }
  }

  fun getBone(id: String): Bone? = bones.firstOrNull { it.id == id }
}
