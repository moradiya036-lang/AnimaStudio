package com.example.model

import androidx.compose.ui.graphics.Path
import java.util.UUID

/**
 * Subsystem A: Bézier Path Architecture
 * Anchor point (x, y) with explicit In-Handle and Out-Handle control vectors.
 */
data class VectorNode(
  val id: String = UUID.randomUUID().toString(),
  var x: Float,
  var y: Float,
  var inHandleX: Float = x,
  var inHandleY: Float = y,
  var outHandleX: Float = x,
  var outHandleY: Float = y,
  var isSmooth: Boolean = true
) {
  fun moveBy(dx: Float, dy: Float) {
    x += dx
    y += dy
    inHandleX += dx
    inHandleY += dy
    outHandleX += dx
    outHandleY += dy
  }

  fun setInHandle(newX: Float, newY: Float) {
    inHandleX = newX
    inHandleY = newY
    if (isSmooth) {
      // Mirror out-handle smoothly opposite anchor
      val dx = x - newX
      val dy = y - newY
      outHandleX = x + dx
      outHandleY = y + dy
    }
  }

  fun setOutHandle(newX: Float, newY: Float) {
    outHandleX = newX
    outHandleY = newY
    if (isSmooth) {
      // Mirror in-handle smoothly opposite anchor
      val dx = x - newX
      val dy = y - newY
      inHandleX = x + dx
      inHandleY = y + dy
    }
  }

  fun copyNode(): VectorNode = copy()
}

data class VectorShape(
  val id: String = UUID.randomUUID().toString(),
  var name: String = "Vector Path",
  val nodes: MutableList<VectorNode> = mutableListOf(),
  var isClosed: Boolean = false,
  var strokeColor: Long = 0xFF00E5FF, // Cyan default
  var strokeWidth: Float = 4f,
  var fillColor: Long? = null,
  var isVisible: Boolean = true
) {
  fun toComposePath(): Path {
    val path = Path()
    if (nodes.isEmpty()) return path

    val first = nodes.first()
    path.moveTo(first.x, first.y)

    for (i in 1 until nodes.size) {
      val prev = nodes[i - 1]
      val curr = nodes[i]
      path.cubicTo(
        x1 = prev.outHandleX,
        y1 = prev.outHandleY,
        x2 = curr.inHandleX,
        y2 = curr.inHandleY,
        x3 = curr.x,
        y3 = curr.y
      )
    }

    if (isClosed && nodes.size > 2) {
      val last = nodes.last()
      path.cubicTo(
        x1 = last.outHandleX,
        y1 = last.outHandleY,
        x2 = first.inHandleX,
        y2 = first.inHandleY,
        x3 = first.x,
        y3 = first.y
      )
      path.close()
    }

    return path
  }
}
