package ch.heigvd.iict.dma.exercice_sensors

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import ch.heigvd.iict.dma.exercice_sensors.game.GameEngine
import ch.heigvd.iict.dma.exercice_sensors.game.GameRenderer

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val engine = GameEngine(cols = 10, rows = 8, holeFrequency = 0.5f)
    private val renderer = GameRenderer()
    private var started = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w == 0 || h == 0) return
        engine.resize(w.toFloat(), h.toFloat())
        renderer.onResize(engine)
        if (!started) { engine.newGame(); started = true }
    }

    fun onGyroscopeChanged(axisX: Float, axisY: Float, timestampNs: Long) {
        engine.onGyroscope(axisX, axisY, timestampNs)
    }

    fun onAccelChanged(axisX: Float, axisY: Float) {
        engine.onAccel(axisX, axisY)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!started) return
        engine.update()
        renderer.draw(canvas, engine, width, height)
        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN &&
            engine.state != GameEngine.State.PLAYING) {
            engine.newGame()
        }
        return true
    }
}