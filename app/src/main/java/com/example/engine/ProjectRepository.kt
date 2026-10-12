package com.example.engine

import com.example.model.AnimProperty
import com.example.model.AnimationProject
import com.example.model.Bone
import com.example.model.Layer
import com.example.model.LayerType
import com.example.model.MeshPuppet
import com.example.model.MeshTriangle
import com.example.model.MeshVertex
import com.example.model.ShapeType
import com.example.model.SkeletalRig
import com.example.model.VectorNode
import com.example.model.VectorShape
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object ProjectRepository {

  private val _projects = MutableStateFlow<List<AnimationProject>>(emptyList())
  val projects = _projects.asStateFlow()

  init {
    loadSampleProjects()
  }

  fun getProject(id: String): AnimationProject? {
    return _projects.value.firstOrNull { it.id == id }
  }

  fun addProject(project: AnimationProject) {
    _projects.value = listOf(project) + _projects.value
  }

  fun updateProject(project: AnimationProject) {
    project.lastModified = System.currentTimeMillis()
    _projects.value = _projects.value.map { if (it.id == project.id) project else it }
  }

  fun deleteProject(id: String) {
    _projects.value = _projects.value.filterNot { it.id == id }
  }

  fun duplicateProject(id: String): AnimationProject? {
    val src = getProject(id) ?: return null
    val copy = src.copy(
      id = UUID.randomUUID().toString(),
      name = "${src.name} (Copy)",
      lastModified = System.currentTimeMillis()
    )
    addProject(copy)
    return copy
  }

  private fun loadSampleProjects() {
    val list = mutableListOf<AnimationProject>()

    // 1. Manga Hero Rig (Full Skeletal & 2-Bone IK)
    list.add(createMangaHeroProject())

    // 2. Vector Fluid Morph (Matching screenshot)
    list.add(createVectorFluidMorphProject())

    // 3. PNG Puppet Mesh (Delaunay & LBS)
    list.add(createPuppetMeshProject())

    // 4. Kinetic Squash & Stretch (Curve easing)
    list.add(createBouncingBallProject())

    _projects.value = list
  }

  private fun createMangaHeroProject(): AnimationProject {
    val rig = SkeletalRig(
      name = "Manga Rig",
      rootX = 400f,
      rootY = 420f
    )

    // Bones hierarchy: Pelvis -> Torso -> Neck -> Head
    val pelvis = Bone(id = "pelvis", name = "Pelvis", parentId = null, length = 50f, localAngleDegrees = -90f, color = 0xFF00E676)
    val torso = Bone(id = "torso", name = "Torso", parentId = "pelvis", length = 90f, localAngleDegrees = 0f, color = 0xFF00E676)
    val head = Bone(id = "head", name = "Head", parentId = "torso", length = 60f, localAngleDegrees = 0f, color = 0xFFFFD600)

    // Left Arm (2-Bone IK: Upper Arm -> Forearm)
    val lUpperArm = Bone(id = "l_upper_arm", name = "L Upper Arm", parentId = "torso", length = 75f, localAngleDegrees = 110f, color = 0xFF00E5FF)
    val lForearm = Bone(id = "l_forearm", name = "L Forearm", parentId = "l_upper_arm", length = 70f, localAngleDegrees = -40f, color = 0xFF00E5FF, isIKTarget = true, ikTargetX = 520f, ikTargetY = 560f)

    // Right Arm (2-Bone IK)
    val rUpperArm = Bone(id = "r_upper_arm", name = "R Upper Arm", parentId = "torso", length = 75f, localAngleDegrees = -110f, color = 0xFF00E5FF)
    val rForearm = Bone(id = "r_forearm", name = "R Forearm", parentId = "r_upper_arm", length = 70f, localAngleDegrees = 40f, color = 0xFF00E5FF, isIKTarget = true, ikTargetX = 280f, ikTargetY = 560f)

    // Left Leg (2-Bone IK: Thigh -> Shin)
    val lThigh = Bone(id = "l_thigh", name = "L Thigh", parentId = "pelvis", length = 90f, localAngleDegrees = 85f, color = 0xFFFF4081)
    val lShin = Bone(id = "l_shin", name = "L Shin", parentId = "l_thigh", length = 85f, localAngleDegrees = 15f, color = 0xFFFF4081, isIKTarget = true, ikTargetX = 460f, ikTargetY = 680f)

    // Right Leg (2-Bone IK)
    val rThigh = Bone(id = "r_thigh", name = "R Thigh", parentId = "pelvis", length = 90f, localAngleDegrees = 95f, color = 0xFFFF4081)
    val rShin = Bone(id = "r_shin", name = "R Shin", parentId = "r_thigh", length = 85f, localAngleDegrees = -15f, color = 0xFFFF4081, isIKTarget = true, ikTargetX = 340f, ikTargetY = 680f)

    rig.bones.addAll(listOf(pelvis, torso, head, lUpperArm, lForearm, rUpperArm, rForearm, lThigh, lShin, rThigh, rShin))
    rig.updateHierarchy()

    val rigLayer = Layer(
      name = "Manga Skeleton (2-Bone IK)",
      type = LayerType.SKELETON,
      skeletalRig = rig
    )

    // Add idle/walk keyframes
    rigLayer.posX.addOrUpdateKeyframe(0, 0f)
    rigLayer.posX.addOrUpdateKeyframe(30, 15f)
    rigLayer.posX.addOrUpdateKeyframe(60, 0f)

    rigLayer.posY.addOrUpdateKeyframe(0, 0f)
    rigLayer.posY.addOrUpdateKeyframe(15, -12f, EasingCurve.EaseInOut)
    rigLayer.posY.addOrUpdateKeyframe(30, 0f, EasingCurve.EaseInOut)
    rigLayer.posY.addOrUpdateKeyframe(45, -12f, EasingCurve.EaseInOut)
    rigLayer.posY.addOrUpdateKeyframe(60, 0f, EasingCurve.EaseInOut)

    // Manga head/eyes shape layer
    val headShape = Layer(
      name = "Anime Face & Hair",
      type = LayerType.SHAPE,
      shapeType = ShapeType.CIRCLE,
      fillColor = 0xFFFFE0B2,
      strokeColor = 0xFF5D4037,
      strokeWidth = 3f
    )
    headShape.posX.defaultValue = 400f
    headShape.posY.defaultValue = 210f
    headShape.scaleX.defaultValue = 1.1f
    headShape.scaleY.defaultValue = 1.1f

    return AnimationProject(
      id = "manga-hero-rig",
      name = "Manga Hero Rig (2-Bone IK)",
      aspectRatio = "16:9",
      fps = 60,
      durationFrames = 90,
      layers = mutableListOf(headShape, rigLayer),
      selectedLayerId = rigLayer.id
    )
  }

  private fun createVectorFluidMorphProject(): AnimationProject {
    // Exactly mirrors user's screenshot (IMG_20261010_093037.jpg)
    val shape = VectorShape(
      name = "Bézier Morph Path",
      isClosed = false,
      strokeColor = 0xFF7986CB,
      strokeWidth = 5f
    )

    // Initial nodes with anchor and in/out handles
    val n1 = VectorNode(
      x = 180f, y = 480f,
      inHandleX = 140f, inHandleY = 420f,
      outHandleX = 220f, outHandleY = 540f
    )
    val n2 = VectorNode(
      x = 380f, y = 320f,
      inHandleX = 350f, inHandleY = 220f,
      outHandleX = 410f, outHandleY = 420f
    )
    val n3 = VectorNode(
      x = 580f, y = 450f,
      inHandleX = 540f, inHandleY = 380f,
      outHandleX = 620f, outHandleY = 520f
    )
    val n4 = VectorNode(
      x = 720f, y = 280f,
      inHandleX = 680f, inHandleY = 220f,
      outHandleX = 760f, outHandleY = 340f
    )
    shape.nodes.addAll(listOf(n1, n2, n3, n4))

    val vectorLayer = Layer(
      name = "Bézier Curve Track",
      type = LayerType.VECTOR,
      vectorShape = shape
    )

    vectorLayer.posY.addOrUpdateKeyframe(0, 0f)
    vectorLayer.posY.addOrUpdateKeyframe(30, -35f, EasingCurve.EaseInOut)
    vectorLayer.posY.addOrUpdateKeyframe(60, 0f, EasingCurve.EaseInOut)

    return AnimationProject(
      id = "vector-fluid-morph",
      name = "Vector Bézier Fluid Morph",
      aspectRatio = "16:9",
      fps = 60,
      durationFrames = 60,
      layers = mutableListOf(vectorLayer),
      selectedLayerId = vectorLayer.id
    )
  }

  private fun createPuppetMeshProject(): AnimationProject {
    // 2D Character silhouette puppet mesh with Delaunay triangles and bone skinning weights
    val vertices = mutableListOf(
      MeshVertex(0, 300f, 200f, weights = mapOf("bone_head" to 1.0f)),
      MeshVertex(1, 380f, 200f, weights = mapOf("bone_head" to 1.0f)),
      MeshVertex(2, 340f, 280f, weights = mapOf("bone_head" to 0.7f, "bone_spine" to 0.3f)),
      MeshVertex(3, 260f, 320f, weights = mapOf("bone_arm" to 0.9f, "bone_spine" to 0.1f)),
      MeshVertex(4, 420f, 320f, weights = mapOf("bone_spine" to 0.8f, "bone_head" to 0.2f)),
      MeshVertex(5, 300f, 440f, weights = mapOf("bone_spine" to 1.0f)),
      MeshVertex(6, 380f, 440f, weights = mapOf("bone_spine" to 1.0f)),
      MeshVertex(7, 220f, 420f, weights = mapOf("bone_arm" to 1.0f))
    )

    val triangles = mutableListOf(
      MeshTriangle(0, 1, 2),
      MeshTriangle(0, 2, 3),
      MeshTriangle(1, 4, 2),
      MeshTriangle(2, 4, 6),
      MeshTriangle(2, 6, 5),
      MeshTriangle(2, 5, 3),
      MeshTriangle(3, 5, 7)
    )

    val puppet = MeshPuppet(
      name = "Anime Puppet Mesh",
      vertices = vertices,
      triangles = triangles,
      boundRigId = "puppet_rig",
      fillColor = 0xFF1E88E5,
      wireframeColor = 0xFF00E676
    )

    // Accompanying rig
    val rig = SkeletalRig(name = "Puppet Rig", rootX = 340f, rootY = 440f)
    val bSpine = Bone(id = "bone_spine", name = "Spine", parentId = null, length = 120f, localAngleDegrees = -90f, color = 0xFF00E676)
    val bHead = Bone(id = "bone_head", name = "Head", parentId = "bone_spine", length = 80f, localAngleDegrees = 0f, color = 0xFFFFD600)
    val bArm = Bone(id = "bone_arm", name = "Arm", parentId = "bone_spine", length = 100f, localAngleDegrees = 120f, color = 0xFF00E5FF, isIKTarget = true, ikTargetX = 220f, ikTargetY = 420f)
    rig.bones.addAll(listOf(bSpine, bHead, bArm))
    rig.updateHierarchy()

    val puppetLayer = Layer(
      name = "Deformable Puppet Mesh (LBS)",
      type = LayerType.PUPPET_MESH,
      meshPuppet = puppet,
      skeletalRig = rig
    )

    puppetLayer.rotation.addOrUpdateKeyframe(0, 0f)
    puppetLayer.rotation.addOrUpdateKeyframe(25, 12f, EasingCurve.EaseInOut)
    puppetLayer.rotation.addOrUpdateKeyframe(50, -8f, EasingCurve.EaseInOut)
    puppetLayer.rotation.addOrUpdateKeyframe(75, 0f, EasingCurve.EaseInOut)

    return AnimationProject(
      id = "puppet-mesh-lbs",
      name = "Puppet Mesh & Delaunay Skinning",
      aspectRatio = "16:9",
      fps = 60,
      durationFrames = 75,
      layers = mutableListOf(puppetLayer),
      selectedLayerId = puppetLayer.id
    )
  }

  private fun createBouncingBallProject(): AnimationProject {
    val ballLayer = Layer(
      name = "Manga Squash & Stretch Ball",
      type = LayerType.SHAPE,
      shapeType = ShapeType.MANGA_BALL,
      fillColor = 0xFFFF7043,
      strokeColor = 0xFFFFFFFF,
      strokeWidth = 3f
    )

    ballLayer.posX.defaultValue = 400f
    ballLayer.posY.defaultValue = 200f

    // Bouncing keyframes with squash and stretch
    ballLayer.posY.addOrUpdateKeyframe(0, 200f, EasingCurve.EaseIn)
    ballLayer.posY.addOrUpdateKeyframe(20, 500f, EasingCurve.Overshoot)
    ballLayer.posY.addOrUpdateKeyframe(40, 200f, EasingCurve.EaseOut)

    ballLayer.scaleX.addOrUpdateKeyframe(0, 1f)
    ballLayer.scaleX.addOrUpdateKeyframe(18, 0.75f) // Stretch before impact
    ballLayer.scaleX.addOrUpdateKeyframe(20, 1.4f)  // Squash on floor
    ballLayer.scaleX.addOrUpdateKeyframe(24, 0.8f)  // Rebound stretch
    ballLayer.scaleX.addOrUpdateKeyframe(40, 1f)

    ballLayer.scaleY.addOrUpdateKeyframe(0, 1f)
    ballLayer.scaleY.addOrUpdateKeyframe(18, 1.35f)
    ballLayer.scaleY.addOrUpdateKeyframe(20, 0.6f)
    ballLayer.scaleY.addOrUpdateKeyframe(24, 1.25f)
    ballLayer.scaleY.addOrUpdateKeyframe(40, 1f)

    return AnimationProject(
      id = "bouncing-ball-squash",
      name = "Squash & Stretch (Easing Graph)",
      aspectRatio = "16:9",
      fps = 60,
      durationFrames = 40,
      layers = mutableListOf(ballLayer),
      selectedLayerId = ballLayer.id
    )
  }
}
