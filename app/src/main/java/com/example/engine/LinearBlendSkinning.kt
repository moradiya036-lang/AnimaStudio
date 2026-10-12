package com.example.engine

import com.example.model.Bone
import com.example.model.MeshPuppet
import com.example.model.SkeletalRig
import kotlin.math.cos
import kotlin.math.sin

/**
 * Linear Blend Skinning (LBS) Engine
 * V_final = Sum(Weight_i * (M_bone_i * V_initial))
 */
object LinearBlendSkinning {

  fun applySkinning(puppet: MeshPuppet, rig: SkeletalRig) {
    if (puppet.vertices.isEmpty() || rig.bones.isEmpty()) return

    val boneMap = rig.bones.associateBy { it.id }

    for (vertex in puppet.vertices) {
      if (vertex.weights.isEmpty()) {
        vertex.deformedX = vertex.bindX
        vertex.deformedY = vertex.bindY
        continue
      }

      var sumX = 0f
      var sumY = 0f
      var totalWeight = 0f

      for ((boneId, weight) in vertex.weights) {
        val bone = boneMap[boneId] ?: continue
        if (weight <= 0.0001f) continue

        // Vector from bone origin to bind vertex
        val relX = vertex.bindX - bone.startX
        val relY = vertex.bindY - bone.startY

        // Bone rotation delta in radians
        val rad = Math.toRadians(bone.worldAngleDegrees.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()

        // Rotate and translate by current bone world transform
        val transformedX = bone.startX + (relX * cosA - relY * sinA)
        val transformedY = bone.startY + (relX * sinA + relY * cosA)

        sumX += transformedX * weight
        sumY += transformedY * weight
        totalWeight += weight
      }

      if (totalWeight > 0.0001f) {
        vertex.deformedX = sumX / totalWeight
        vertex.deformedY = sumY / totalWeight
      } else {
        vertex.deformedX = vertex.bindX
        vertex.deformedY = vertex.bindY
      }
    }
  }
}
