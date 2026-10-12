package com.example.engine

import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Analytical 2-Bone Inverse Kinematics (IK) Solver
 * Based on the Law of Cosines:
 * cos(Angle_Joint) = (L1^2 + L2^2 - D^2) / (2 * L1 * L2)
 */
object InverseKinematics {

  data class IKResult(
    val bone1AngleDeg: Float,
    val bone2LocalAngleDeg: Float,
    val jointX: Float,
    val jointY: Float,
    val endX: Float,
    val endY: Float
  )

  /**
   * Solves 2-bone chain from root (x0, y0) to target (targetX, targetY).
   * @param x0 Root origin X
   * @param y0 Root origin Y
   * @param targetX IK target X
   * @param targetY IK target Y
   * @param l1 Upper bone length (e.g. Upper Arm / Thigh)
   * @param l2 Lower bone length (e.g. Forearm / Shin)
   * @param flipBend If true, bends inward/knee-forward; if false, bends outward
   */
  fun solve2Bone(
    x0: Float,
    y0: Float,
    targetX: Float,
    targetY: Float,
    l1: Float,
    l2: Float,
    flipBend: Boolean = false
  ): IKResult {
    val dx = targetX - x0
    val dy = targetY - y0
    var d = sqrt(dx * dx + dy * dy)

    // Clamp distance to avoid triangle inequality breakdown
    val maxReach = l1 + l2 - 0.001f
    val minReach = kotlin.math.abs(l1 - l2) + 0.001f

    if (d > maxReach) {
      d = maxReach
    } else if (d < minReach) {
      d = minReach
    }

    // Angle to target in radians
    val alpha = atan2(dy, dx)

    // Law of cosines for angle at bone 1:
    // cos(theta1) = (L1^2 + D^2 - L2^2) / (2 * L1 * D)
    val cosTheta1 = ((l1 * l1 + d * d - l2 * l2) / (2f * l1 * d)).coerceIn(-1f, 1f)
    val theta1 = acos(cosTheta1)

    // Law of cosines for interior joint angle:
    // cos(theta2) = (L1^2 + L2^2 - D^2) / (2 * L1 * L2)
    val cosTheta2 = ((l1 * l1 + l2 * l2 - d * d) / (2f * l1 * l2)).coerceIn(-1f, 1f)
    val theta2 = acos(cosTheta2)

    val bendSign = if (flipBend) -1.0 else 1.0

    // Absolute world angle for bone 1
    val bone1AngleRad = alpha + bendSign * theta1
    // Local angle for bone 2 relative to bone 1 (interior angle subtracted from 180 deg)
    val bone2LocalAngleRad = -bendSign * (Math.PI - theta2)

    val b1AngleDeg = Math.toDegrees(bone1AngleRad).toFloat()
    val b2LocalDeg = Math.toDegrees(bone2LocalAngleRad).toFloat()

    // Intermediate joint (elbow/knee)
    val jX = x0 + cos(bone1AngleRad).toFloat() * l1
    val jY = y0 + sin(bone1AngleRad).toFloat() * l1

    // End effector (wrist/ankle)
    val totalAngleRad = bone1AngleRad + bone2LocalAngleRad
    val eX = jX + cos(totalAngleRad).toFloat() * l2
    val eY = jY + sin(totalAngleRad).toFloat() * l2

    return IKResult(
      bone1AngleDeg = b1AngleDeg,
      bone2LocalAngleDeg = b2LocalDeg,
      jointX = jX,
      jointY = jY,
      endX = eX,
      endY = eY
    )
  }
}
