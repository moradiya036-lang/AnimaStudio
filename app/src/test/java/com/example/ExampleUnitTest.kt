package com.example

import com.example.engine.EasingCurve
import com.example.engine.InverseKinematics
import com.example.model.AnimProperty
import com.example.model.Bone
import com.example.model.MeshPuppet
import com.example.model.MeshVertex
import com.example.model.SkeletalRig
import com.example.engine.LinearBlendSkinning
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

class ExampleUnitTest {

  @Test
  fun testAnalytical2BoneIKSolver() {
    // Test arm: Upper arm L1=100, Forearm L2=100
    // Root at (0, 0), Target at (100, 100) -> D = sqrt(20000) ~= 141.42
    val result = InverseKinematics.solve2Bone(
      x0 = 0f,
      y0 = 0f,
      targetX = 100f,
      targetY = 100f,
      l1 = 100f,
      l2 = 100f,
      flipBend = false
    )

    // Distance from root to joint should equal L1 (100)
    val distToJoint = sqrt(result.jointX * result.jointX + result.jointY * result.jointY)
    assertTrue("Dist to joint should be ~100", abs(distToJoint - 100f) < 0.1f)

    // Distance from joint to end effector should equal L2 (100)
    val dxJointEnd = result.endX - result.jointX
    val dyJointEnd = result.endY - result.jointY
    val distToEnd = sqrt(dxJointEnd * dxJointEnd + dyJointEnd * dyJointEnd)
    assertTrue("Dist from joint to end should be ~100", abs(distToEnd - 100f) < 0.1f)

    // End effector should match target within tolerance
    assertTrue("End effector X should reach target", abs(result.endX - 100f) < 0.5f)
    assertTrue("End effector Y should reach target", abs(result.endY - 100f) < 0.5f)
  }

  @Test
  fun testBezierEasingBoundaries() {
    val curve = EasingCurve.EaseInOut

    // At progress = 0, value must be 0
    assertEquals(0f, curve.evaluate(0f), 0.001f)

    // At progress = 1, value must be 1
    assertEquals(1f, curve.evaluate(1f), 0.001f)

    // Linear curve
    val linear = EasingCurve.Linear
    assertEquals(0.5f, linear.evaluate(0.5f), 0.001f)
  }

  @Test
  fun testKeyframeInterpolation() {
    val track = AnimProperty("Pos X", 0f)
    track.addOrUpdateKeyframe(0, 100f, EasingCurve.Linear)
    track.addOrUpdateKeyframe(60, 200f, EasingCurve.Linear)

    // Midpoint at frame 30 should be 150
    assertEquals(150f, track.getValueAt(30f), 0.1f)
    assertEquals(100f, track.getValueAt(0f), 0.1f)
    assertEquals(200f, track.getValueAt(60f), 0.1f)
  }

  @Test
  fun testLinearBlendSkinning() {
    val rig = SkeletalRig(name = "Test Rig", rootX = 0f, rootY = 0f)
    val bone = Bone(id = "bone1", name = "Bone 1", parentId = null, length = 100f, localAngleDegrees = 0f)
    rig.bones.add(bone)
    rig.updateHierarchy()

    val v = MeshVertex(index = 0, bindX = 50f, bindY = 0f, weights = mapOf("bone1" to 1.0f))
    val puppet = MeshPuppet(vertices = mutableListOf(v))

    LinearBlendSkinning.applySkinning(puppet, rig)
    assertEquals(50f, v.deformedX, 0.1f)
    assertEquals(0f, v.deformedY, 0.1f)
  }
}
