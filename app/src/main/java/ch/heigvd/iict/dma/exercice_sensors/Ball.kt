package ch.heigvd.iict.dma.exercice_sensors

class Ball (private val cellW: Float, private val cellH: Float) {

    // --- Ball ---
    private var ballX = 0f
    private var ballY = 0f
    private var velX = 0f
    private var velY = 0f
    private val ballRadius get() = minOf(cellW, cellH) * 0.22f

    public fun setBallx( value : Float) {
        ballX = value;
    }

    public fun setBally(value: Float) {
        ballY = value;
    }

    public fun resetBall() {
        ballX = cellW * 0.5f;
        ballY = cellH * 0.5f;
        velY = 0f;
        velX = 0f;
    }

}