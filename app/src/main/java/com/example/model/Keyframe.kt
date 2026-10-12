package com.example.model

import com.example.engine.EasingCurve
import java.util.UUID

/**
 * Keyframe on a timeline track
 */
data class Keyframe(
  val id: String = UUID.randomUUID().toString(),
  val frame: Int,
  var value: Float,
  var easing: EasingCurve = EasingCurve.EaseInOut
)

/**
 * Animated property with keyframes (Position X, Position Y, Scale, Rotation, Opacity, Bone Angle, etc.)
 */
data class AnimProperty(
  val name: String,
  var defaultValue: Float,
  val keyframes: MutableList<Keyframe> = mutableListOf()
) {
  fun addOrUpdateKeyframe(frame: Int, value: Float, easing: EasingCurve = EasingCurve.EaseInOut) {
    val existing = keyframes.firstOrNull { it.frame == frame }
    if (existing != null) {
      existing.value = value
      existing.easing = easing
    } else {
      keyframes.add(Keyframe(frame = frame, value = value, easing = easing))
      keyframes.sortBy { it.frame }
    }
  }

  fun removeKeyframe(frame: Int) {
    keyframes.removeAll { it.frame == frame }
  }

  fun hasKeyframeAt(frame: Int): Boolean = keyframes.any { it.frame == frame }

  fun getValueAt(frame: Float): Float {
    if (keyframes.isEmpty()) return defaultValue
    if (frame <= keyframes.first().frame) return keyframes.first().value
    if (frame >= keyframes.last().frame) return keyframes.last().value

    // Find bounding keyframes
    var prevIndex = 0
    for (i in 0 until keyframes.size - 1) {
      if (keyframes[i].frame <= frame && keyframes[i + 1].frame >= frame) {
        prevIndex = i
        break
      }
    }

    val kA = keyframes[prevIndex]
    val kB = keyframes[prevIndex + 1]
    val frameDelta = (kB.frame - kA.frame).toFloat()
    if (frameDelta <= 0f) return kA.value

    val linearProgress = ((frame - kA.frame) / frameDelta).coerceIn(0f, 1f)
    val easedProgress = kA.easing.evaluate(linearProgress)

    return kA.value + (kB.value - kA.value) * easedProgress
  }
}
