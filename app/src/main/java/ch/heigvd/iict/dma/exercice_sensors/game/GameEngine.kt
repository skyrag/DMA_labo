package ch.heigvd.iict.dma.exercice_sensors.game

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin

class GameEngine(
    val cols: Int = 10,
    val rows: Int = 8,
    private val holeFrequency: Float = 0.5f
) {
    enum class State { PLAYING, WON, FELL }

    val maze = Maze(cols, rows)
    val holes = Holes(maze, holeFrequency)

    val ballPos = Vec2()
    private val vel = Vec2()
    private var tiltX = 0f
    private var tiltY = 0f

    var state = State.PLAYING
        private set

    // callback for the UI to react to state changes (observer)
    var onStateChanged: ((State) -> Unit)? = null

    private lateinit var geometry: MazeGeometry

    // tuning
    private val gravity = 2400f
    private val friction = 0.993f
    private val bounce = 0.3f
    private val maxTilt = 1.5f
    private var lastTimestamp = 0L

    val ballRadius: Float
        get() = minOf(geometry.cellW, geometry.cellH) * 0.22f
    val holeRadius: Float
        get() = ballRadius * 0.85f

    fun resize(widthPx: Float, heightPx: Float) {
        geometry = MazeGeometry(maze, widthPx, heightPx)
    }

    val geo: MazeGeometry get() = geometry

    fun newGame() {
        maze.generateMaze()
        holes.placeHoles()
        resetBall()
        setState(State.PLAYING)
    }

    private fun resetBall() {
        ballPos.x = geometry.cellW * 0.5f
        ballPos.y = geometry.cellH * 0.5f
        vel.x = 0f; vel.y = 0f
        tiltX = 0f; tiltY = 0f
        lastTimestamp = 0L
    }

    fun onGyroscope(axisX: Float, axisY: Float, timestampNs: Long) {
        if (lastTimestamp == 0L) { lastTimestamp = timestampNs; return }
        val dt = (timestampNs - lastTimestamp) / 1_000_000_000f
        lastTimestamp = timestampNs
        tiltX = (tiltX + axisX * dt).coerceIn(-maxTilt, maxTilt)
        tiltY = (tiltY + axisY * dt).coerceIn(-maxTilt, maxTilt)
    }

    /** Advance one frame. Returns true if a redraw is warranted. */
    fun update() {
        if (state != State.PLAYING) return
        stepPhysics()
        checkHoles()
        checkWin()
    }

    private fun stepPhysics() {
        val dt = 1f / 60f
        val accelX = sin(tiltY) * gravity
        val accelY = sin(tiltX) * gravity
        vel.x = vel.x * friction + accelX * dt
        vel.y = vel.y * friction + accelY * dt

        val steps = 4
        val stepDt = dt / steps
        repeat(steps) {
            ballPos.x += vel.x * stepDt
            ballPos.y += vel.y * stepDt
            resolveWallCollisions()
        }
    }

    private fun resolveWallCollisions() {
        val cw = geometry.cellW; val ch = geometry.cellH
        val col = (ballPos.x / cw).toInt().coerceIn(0, cols - 1)
        val row = (ballPos.y / ch).toInt().coerceIn(0, rows - 1)
        val cell = maze.cellAt(col, row)

        val l = col * cw; val t = row * ch
        val r = l + cw; val b = t + ch

        if (cell.topWall    && ballPos.y - ballRadius < t) { ballPos.y = t + ballRadius; vel.y = abs(vel.y) * bounce }
        if (cell.bottomWall && ballPos.y + ballRadius > b) { ballPos.y = b - ballRadius; vel.y = -abs(vel.y) * bounce }
        if (cell.leftWall   && ballPos.x - ballRadius < l) { ballPos.x = l + ballRadius; vel.x = abs(vel.x) * bounce }
        if (cell.rightWall  && ballPos.x + ballRadius > r) { ballPos.x = r - ballRadius; vel.x = -abs(vel.x) * bounce }
    }

    private fun checkHoles() {
        for (hole in holes.holes) {
            val hx = geometry.cellCenterX(hole.col)
            val hy = geometry.cellCenterY(hole.row)
            if (hypot(ballPos.x - hx, ballPos.y - hy) < holeRadius * 0.7f) {
                setState(State.FELL); return
            }
        }
    }

    private fun checkWin() {
        val ex = geometry.cellCenterX(cols - 1)
        val ey = geometry.cellCenterY(rows - 1)
        if (hypot(ballPos.x - ex, ballPos.y - ey) < geometry.cellW * 0.35f) {
            setState(State.WON)
        }
    }

    private fun setState(s: State) {
        if (s != state) { state = s; onStateChanged?.invoke(s) }
    }
}