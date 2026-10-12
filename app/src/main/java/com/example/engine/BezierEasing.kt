package com.example.engine

import kotlin.math.abs

/**
 * Subsystem D: Timeline & Property Keyframing Engine
 * Evaluates Cubic Bézier Easing curves:
 * Value(t) = Keyframe_A + (Keyframe_B - Keyframe_A) * BézierEasing(t)
 */
data class EasingCurve(
  val x1: Float = 0.25f,
  val y1: Float = 0.1f,
  val x2: Float = 0.25f,
  val y2: Float = 1.0f,
  val name: String = "Custom"
) {
  companion object {
    val Linear = EasingCurve(0f, 0f, 1f, 1f, "Linear")
    val EaseIn = EasingCurve(0.42f, 0.0f, 1.0f, 1.0f, "Ease In")
    val EaseOut = EasingCurve(0.0f, 0.0f, 0.58f, 1.0f, "Ease Out")
    val EaseInOut = EasingCurve(0.42f, 0.0f, 0.58f, 1.0f, "Ease In-Out")
    val Overshoot = EasingCurve(0.34f, 1.56f, 0.64f, 1.0f, "Overshoot")
    val Anticipate = EasingCurve(0.36f, 0.0f, 0.66f, -0.56f, "Anticipate")
  }

  /**
   * Solves for progress t in [0, 1] using binary search / Newton Raphson
   */
  fun evaluate(progress: Float): Float {
    if (progress <= 0f) return 0f
    if (progress >= 1f) return 1f
    if (x1 == y1 && x2 == y2) return progress

    var t = progress
    for (i in 0 until 8) {
      val x = sampleCurveX(t)
      val diff = x - progress
      if (abs(diff) < 0.001f) break
      val dx = sampleCurveDerivativeX(t)
      if (abs(dx) < 1e-6f) break
      t -= diff / dx
    }
    t = t.coerceIn(0f, 1f)
    return sampleCurveY(t)
  }

  private fun sampleCurveX(t: Float): Float {
    // 3*(1-t)^2*t*x1 + 3*(1-t)*t^2*x2 + t^3
    val oneMinusT = 1f - t
    return 3f * oneMinusT * oneMinusT * t * x1 +
           3f * oneMinusT * t * t * x2 +
           t * t * t
  }

  private fun sampleCurveDerivativeX(t: Float): Float {
    val oneMinusT = 1f - t
    return 3f * oneMinusT * oneMinusT * x1 +
           6f * oneMinusT * t * (x2 - x1) +
           3f * t * t * (1f - x2)
  }

  private fun sampleCurveY(t: Float): Float {
    val oneMinusT = 1f - t
    return 3f * oneMinusT * oneMinusT * t * y1 +
           3f * oneMinusT * t * t * y2 +
           t * t * t
  }
}
