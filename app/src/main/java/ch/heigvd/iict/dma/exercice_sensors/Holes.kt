package ch.heigvd.iict.dma.exercice_sensors

import android.graphics.*

class Holes (private val maze: Maze, private val ballRadius: Float,private val holeFrequency : Float) {
    // --- Holes ---
    private val holes = mutableListOf<PointF>()
    val holeRadius get() = ballRadius * 0.85f

    fun getHoles(): MutableList<PointF> = holes
    // Place holes in random interior cells (not start or end)
    public fun placeHoles(cellW : Float, cellH : Float) {
        holes.clear()
        val rows = maze.getRows();
        val cols = maze.getCols()
        val cells = maze.getCells()
        val holeCount = (cols * rows * holeFrequency).toInt().coerceAtLeast(3)
        val candidates = mutableListOf<Cell>()
        for (r in 0 until rows) for (c in 0 until cols) {
            if ((r == 0 && c == 0) || (r == rows-1 && c == cols-1)) continue
            candidates.add(cells[r][c])
        }
        candidates.shuffle()
        for (i in 0 until holeCount) {
            val cell = candidates[i]
            holes.add(PointF(
                cell.col * cellW + cellW / 2f,
                cell.row * cellH + cellH / 2f
            ))
        }
    }
}