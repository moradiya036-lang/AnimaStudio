package com.example.model

import java.util.UUID

/**
 * Subsystem C: Deformable Mesh & Skinning (PNG Puppet Animation)
 * Mesh vertex bound to bones via Linear Blend Skinning weights (0.0 to 1.0).
 */
data class MeshVertex(
  val index: Int,
  val bindX: Float,
  val bindY: Float,
  var deformedX: Float = bindX,
  var deformedY: Float = bindY,
  // Mapping of Bone ID to skin weight (sum = 1.0)
  val weights: Map<String, Float> = emptyMap()
)

data class MeshTriangle(
  val i1: Int,
  val i2: Int,
  val i3: Int
)

data class MeshPuppet(
  val id: String = UUID.randomUUID().toString(),
  var name: String = "Puppet Mesh",
  val vertices: MutableList<MeshVertex> = mutableListOf(),
  val triangles: MutableList<MeshTriangle> = mutableListOf(),
  var boundRigId: String? = null,
  var showWireframe: Boolean = true,
  var fillColor: Long = 0xFF2A3B4D,
  var wireframeColor: Long = 0xAA00E676
)
