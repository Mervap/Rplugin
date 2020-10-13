package org.jetbrains.r.run.graphics

import java.awt.Color

data class RAffinePoint(val xScale: Double, val xOffset: Double, val yScale: Double, val yOffset: Double)

enum class RFontStyle {
  PLAIN,
  BOLD,
  ITALIC,
  BOLD_ITALIC,
}

data class RFont(val name: String?, val size: Double, val style: RFontStyle)

enum class RLineCap {
  ROUND,
  BUTT,
  SQUARE,
}

enum class RLineJoin {
  ROUND,
  MITER,
  BEVEL,
}

class RStroke(val width: Double, val cap: RLineCap, val join: RLineJoin, val miterLimit: Double, val pattern: FloatArray?)

data class RLayer(val viewportIndex: Int, val clippingAreaIndex: Int, val figures: List<RFigure>, val isAxisText: Boolean)

data class RPlot(
  val number: Int,
  val fonts: List<RFont>,
  val colors: List<Color>,
  val strokes: List<RStroke>,
  val viewports: List<RViewport>,
  val layers: List<RLayer>
)
