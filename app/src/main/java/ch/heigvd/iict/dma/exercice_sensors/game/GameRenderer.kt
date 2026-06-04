package ch.heigvd.iict.dma.exercice_sensors.game

import android.graphics.*

class GameRenderer {
    private val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2C2C2A"); strokeCap = Paint.Cap.ROUND; style = Paint.Style.STROKE
    }
    private val floorPaint = Paint().apply { color = Color.parseColor("#F5F0E8") }
    private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E85D24") }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#44000000") }
    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1A1A1A") }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2ECC71") }
    private val endPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#3498DB") }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }
    private val overlayPaint = Paint().apply { color = Color.parseColor("#CC000000") }
    private val overlayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER; typeface = Typeface.DEFAULT_BOLD
    }

    fun onResize(engine: GameEngine) {
        val geo = engine.geo
        wallPaint.strokeWidth = minOf(geo.cellW, geo.cellH) * 0.08f
        labelPaint.textSize = minOf(geo.cellW, geo.cellH) * 0.4f
    }

    fun draw(canvas: Canvas, engine: GameEngine, width: Int, height: Int) {
        val geo = engine.geo
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), floorPaint)

        drawMarker(canvas, geo, 0, 0, startPaint, "S")
        drawMarker(canvas, geo, engine.cols - 1, engine.rows - 1, endPaint, "E")

        for (hole in engine.holes.holes) {
            canvas.drawCircle(geo.cellCenterX(hole.col), geo.cellCenterY(hole.row),
                engine.holeRadius, holePaint)
        }

        for (r in 0 until engine.rows)
            for (c in 0 until engine.cols)
                drawWalls(canvas, geo, engine.maze.cellAt(c, r))

        val ball = engine.ballPos
        val rad = engine.ballRadius
        canvas.drawCircle(ball.x + rad * 0.3f, ball.y + rad * 0.3f, rad, shadowPaint)
        canvas.drawCircle(ball.x, ball.y, rad, ballPaint)

        if (engine.state != GameEngine.State.PLAYING)
            drawOverlay(canvas, engine, width, height)
    }

    private fun drawMarker(canvas: Canvas, geo: MazeGeometry, col: Int, row: Int, paint: Paint, label: String) {
        val cx = geo.cellCenterX(col); val cy = geo.cellCenterY(row)
        canvas.drawCircle(cx, cy, minOf(geo.cellW, geo.cellH) * 0.38f, paint)
        val textY = cy - (labelPaint.descent() + labelPaint.ascent()) / 2f
        canvas.drawText(label, cx, textY, labelPaint)
    }

    private fun drawWalls(canvas: Canvas, geo: MazeGeometry, cell: Cell) {
        val l = cell.col * geo.cellW; val t = cell.row * geo.cellH
        val r = l + geo.cellW; val b = t + geo.cellH
        if (cell.topWall)    canvas.drawLine(l, t, r, t, wallPaint)
        if (cell.bottomWall) canvas.drawLine(l, b, r, b, wallPaint)
        if (cell.leftWall)   canvas.drawLine(l, t, l, b, wallPaint)
        if (cell.rightWall)  canvas.drawLine(r, t, r, b, wallPaint)
    }

    private fun drawOverlay(canvas: Canvas, engine: GameEngine, width: Int, height: Int) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        val cx = width / 2f; val cy = height / 2f
        val line1 = if (engine.state == GameEngine.State.WON) "You won!" else "Fell in!"
        overlayTextPaint.textSize = width * 0.12f
        canvas.drawText(line1, cx, cy - overlayTextPaint.textSize * 0.6f, overlayTextPaint)
        overlayTextPaint.textSize = width * 0.06f
        canvas.drawText("Tap to restart", cx, cy + overlayTextPaint.textSize * 1.2f, overlayTextPaint)
    }
}