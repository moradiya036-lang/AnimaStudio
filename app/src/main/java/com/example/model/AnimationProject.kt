package com.example.model

import java.util.UUID

data class AnimationProject(
  val id: String = UUID.randomUUID().toString(),
  var name: String = "Untitled Project",
  var aspectRatio: String = "16:9",
  var canvasWidth: Int = 1920,
  var canvasHeight: Int = 1080,
  var fps: Int = 30,
  var durationFrames: Int = 120,
  var currentFrame: Int = 0,
  var isPlaying: Boolean = false,
  var backgroundColor: Long = 0xFF14161B,
  val layers: MutableList<Layer> = mutableListOf(),
  var selectedLayerId: String? = null,
  var lastModified: Long = System.currentTimeMillis()
) {
  fun getSelectedLayer(): Layer? = layers.firstOrNull { it.id == selectedLayerId } ?: layers.firstOrNull()

  fun duplicateLayer(layerId: String): Layer? {
    val source = layers.firstOrNull { it.id == layerId } ?: return null
    val copy = source.copy(
      id = UUID.randomUUID().toString(),
      name = "${source.name} Copy"
    )
    val index = layers.indexOf(source)
    layers.add(index + 1, copy)
    return copy
  }

  fun removeLayer(layerId: String) {
    layers.removeAll { it.id == layerId }
    if (selectedLayerId == layerId) {
      selectedLayerId = layers.firstOrNull()?.id
    }
  }

  fun moveLayerUp(layerId: String) {
    val index = layers.indexOfFirst { it.id == layerId }
    if (index > 0) {
      val item = layers.removeAt(index)
      layers.add(index - 1, item)
    }
  }

  fun moveLayerDown(layerId: String) {
    val index = layers.indexOfFirst { it.id == layerId }
    if (index != -1 && index < layers.size - 1) {
      val item = layers.removeAt(index)
      layers.add(index + 1, item)
    }
  }
}
