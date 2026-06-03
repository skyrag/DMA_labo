package ch.heigvd.iict.dma.exercice_sensors

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // --- Ball ---
    private var ballX = 0f
    private var ballY = 0f
    private var velX = 0f
    private var velY = 0f
    private val ballRadius get() = minOf(cellW, cellH) * 0.22f
    private var cellW = 0f
    private var cellH = 0f


    // --- Game state ---
    private var initialized = false
    private var gameState = State.PLAYING  // PLAYING, WON, FELL

    enum class State { PLAYING, WON, FELL }

    // --- Physics ---
    private var tiltX = 0f
    private var tiltY = 0f
    private var lastTimestamp = 0L
    private val gravity = 2400f
    private val friction = 0.993f
    private val bounce = 0.3f
    private val maxTilt = 1.5f

    // --- Paints ---
    private val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2C2C2A")
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    private val floorPaint = Paint().apply {
        color = Color.parseColor("#F5F0E8")
        style = Paint.Style.FILL
    }
    private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E85D24")
        style = Paint.Style.FILL
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#44000000")
        style = Paint.Style.FILL
    }
    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A1A")
        style = Paint.Style.FILL
    }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2ECC71")
        style = Paint.Style.FILL
    }
    private val endPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3498DB")
        style = Paint.Style.FILL
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#CC000000")
        style = Paint.Style.FILL
    }
    private val overlayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private lateinit var maze : Maze
    private lateinit var holes : Holes

    // --- Init ---
    private fun initialize() {
        val rows = 8
        val cols = 10
        maze = Maze(cols,rows)
        cellW = width.toFloat() / cols
        cellH = height.toFloat() / rows
        holes = Holes(maze, ballRadius,0.5f, )


        wallPaint.strokeWidth = minOf(cellW, cellH) * 0.08f
        labelPaint.textSize = minOf(cellW, cellH) * 0.4f
        overlayTextPaint.textSize = width * 0.1f

        maze.generateMaze()
        holes.placeHoles(cellW,cellH)
        gameState = State.PLAYING
        initialized = true
    }

    private fun resetBall() {
        // Start top-left cell center
        ballX = cellW * 0.5f
        ballY = cellH * 0.5f
        velX = 0f
        velY = 0f
        tiltX = 0f
        tiltY = 0f
    }

    // --- Gyroscope input ---
    fun onGyroscopeChanged(axisX: Float, axisY: Float, timestampNs: Long) {
        if (lastTimestamp == 0L) { lastTimestamp = timestampNs; return }
        val dt = (timestampNs - lastTimestamp) / 1_000_000_000f
        lastTimestamp = timestampNs
        tiltX = (tiltX + axisX * dt).coerceIn(-maxTilt, maxTilt)
        tiltY = (tiltY + axisY * dt).coerceIn(-maxTilt, maxTilt)
        invalidate()
    }

    // --- Draw ---
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!initialized) initialize()

        // Background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), floorPaint)

        // Start / End markers
        drawMarker(canvas, 0, 0, startPaint, "S")
        //rawMarker(canvas, cols - 1, rows - 1, endPaint, "E")

        // Holes
        for (hole in holes.getHoles()) {
            canvas.drawCircle(hole.x, hole.y, holes.holeRadius, holePaint)
        }

        // Maze walls
        val cells = maze.getCells()
        for (r in 0 until maze.getRows()) {
            for (c in 0 until maze.getCols()) {
                drawWalls(canvas, cells[r][c])
            }
        }

        if (gameState == State.PLAYING) {
            stepPhysics()
            checkHoles()
            checkWin()
        }

        // Shadow + ball
        canvas.drawCircle(ballX + ballRadius * 0.3f, ballY + ballRadius * 0.3f, ballRadius, shadowPaint)
        canvas.drawCircle(ballX, ballY, ballRadius, ballPaint)

        // Overlay on win/fell
        if (gameState != State.PLAYING) {
            drawOverlay(canvas)
        }


        postInvalidateOnAnimation()
    }

    private fun drawMarker(canvas: Canvas, col: Int, row: Int, paint: Paint, label: String) {
        val cx = col * cellW + cellW / 2f
        val cy = row * cellH + cellH / 2f
        val r = minOf(cellW, cellH) * 0.38f
        canvas.drawCircle(cx, cy, r, paint)
        val textY = cy - (labelPaint.descent() + labelPaint.ascent()) / 2f
        canvas.drawText(label, cx, textY, labelPaint)
    }

    private fun drawWalls(canvas: Canvas, cell: Cell) {
        val l = cell.col * cellW
        val t = cell.row * cellH
        val r = l + cellW
        val b = t + cellH
        if (cell.topWall)    canvas.drawLine(l, t, r, t, wallPaint)
        if (cell.bottomWall) canvas.drawLine(l, b, r, b, wallPaint)
        if (cell.leftWall)   canvas.drawLine(l, t, l, b, wallPaint)
        if (cell.rightWall)  canvas.drawLine(r, t, r, b, wallPaint)
    }

    private fun stepPhysics() {
        val dt = 1f / 60f
        val accelX = sin(tiltY) * gravity
        val accelY = sin(tiltX) * gravity

        velX = velX * friction + accelX * dt
        velY = velY * friction + accelY * dt

        // Move in small substeps for accuracy
        val steps = 4
        val stepDt = dt / steps
        repeat(steps) {
            ballX += velX * stepDt
            ballY += velY * stepDt
            //resolveWallCollisions()
        }
    }


    private fun resolveWallCollisions() {
        val col = (ballX / cellW).toInt().coerceIn(0, maze.getCols() - 1)
        val row = (ballY / cellH).toInt().coerceIn(0, maze.getRows() - 1)
        val cell = maze.getCells()

        val cellL = col * cellW
        val cellT = row * cellH
        val cellR = cellL + cellW
        val cellB = cellT + cellH

        if (cell.topWall    && ballY - ballRadius < cellT) { ballY = cellT + ballRadius; velY = abs(velY) * bounce }
        if (cell.bottomWall && ballY + ballRadius > cellB) { ballY = cellB - ballRadius; velY = -abs(velY) * bounce }
        if (cell.leftWall   && ballX - ballRadius < cellL) { ballX = cellL + ballRadius; velX = abs(velX) * bounce }
        if (cell.rightWall  && ballX + ballRadius > cellR) { ballX = cellR - ballRadius; velX = -abs(velX) * bounce }
    }



    private fun checkHoles() {
        for (hole in holes.getHoles()) {
            val dist = hypot(ballX - hole.x, ballY - hole.y)
            if (dist < holes.holeRadius * 0.7f) {
                gameState = State.FELL
                return
            }
        }
    }

    private fun checkWin() {
        val endCx = (maze.getCols() - 1) * cellW + cellW / 2f
        val endCy = (maze.getRows() - 1) * cellH + cellH / 2f
        if (hypot(ballX - endCx, ballY - endCy) < cellW * 0.35f) {
            gameState = State.WON
        }
    }



    private fun drawOverlay(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        val cx = width / 2f
        val cy = height / 2f
        val line1 = if (gameState == State.WON) "You won!" else "Fell in!"
        val line2 = "Tap to restart"
        overlayTextPaint.textSize = width * 0.12f
        canvas.drawText(line1, cx, cy - overlayTextPaint.textSize * 0.6f, overlayTextPaint)
        overlayTextPaint.textSize = width * 0.06f
        canvas.drawText(line2, cx, cy + overlayTextPaint.textSize * 1.2f, overlayTextPaint)
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        if (event.action == android.view.MotionEvent.ACTION_DOWN) {
            if (gameState != State.PLAYING) {
                initialize()
            }
        }
        return true
    }

    companion object {
        private val TAG = GameView::class.java.simpleName
    }
}