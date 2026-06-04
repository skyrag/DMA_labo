package ch.heigvd.iict.dma.exercice_sensors.game

class Holes(private val maze: Maze, private val holeFrequency: Float) {

    // stored as (col, row) cell coordinates — pixel conversion happens in the renderer
    private val _holes = mutableListOf<Cell>()
    val holes: List<Cell> get() = _holes

    fun placeHoles() {
        _holes.clear()
        val holeCount = (maze.cols * maze.rows * holeFrequency)
            .toInt().coerceAtLeast(3)
        val candidates = mutableListOf<Cell>()
        for (r in 0 until maze.rows) for (c in 0 until maze.cols) {
            if ((r == 0 && c == 0) || (r == maze.rows-1 && c == maze.cols-1)) continue
            candidates.add(maze.cellAt(c, r))
        }
        candidates.shuffle()
        for (i in 0 until minOf(holeCount, candidates.size)) {
            _holes.add(candidates[i])
        }
    }
}