package ch.heigvd.iict.dma.exercice_sensors.game

class MazeGeometry(val maze: Maze, var widthPx: Float, var heightPx: Float) {
    val cellW: Float get() = widthPx / maze.cols
    val cellH: Float get() = heightPx / maze.rows

    fun cellCenterX(col: Int) = col * cellW + cellW / 2f
    fun cellCenterY(row: Int) = row * cellH + cellH / 2f
}