package ch.heigvd.iict.dma.exercice_sensors.game

class Maze(val cols: Int, val rows: Int) {

    lateinit var cells: Array<Array<Cell>>
        private set

    fun generateMaze() {
        cells = Array(rows) { r -> Array(cols) { c -> Cell(c, r) } }
        val stack = ArrayDeque<Cell>()
        val start = cells[0][0]
        start.visited = true
        stack.addLast(start)

        while (stack.isNotEmpty()) {
            val current = stack.last()
            val neighbors = unvisitedNeighbors(current)
            if (neighbors.isEmpty()) stack.removeLast()
            else {
                val next = neighbors.random()
                removeWall(current, next)
                next.visited = true
                stack.addLast(next)
            }
        }
    }

    fun cellAt(col: Int, row: Int): Cell = cells[row][col]

    private fun unvisitedNeighbors(cell: Cell): List<Cell> {
        val result = mutableListOf<Cell>()
        val c = cell.col; val r = cell.row
        if (r > 0 && !cells[r-1][c].visited) result.add(cells[r-1][c])
        if (r < rows-1 && !cells[r+1][c].visited) result.add(cells[r+1][c])
        if (c > 0 && !cells[r][c-1].visited) result.add(cells[r][c-1])
        if (c < cols-1 && !cells[r][c+1].visited) result.add(cells[r][c+1])
        return result
    }

    private fun removeWall(a: Cell, b: Cell) {
        val dc = b.col - a.col
        val dr = b.row - a.row
        when {
            dc == 1  -> { a.rightWall = false; b.leftWall = false }
            dc == -1 -> { a.leftWall = false; b.rightWall = false }
            dr == 1  -> { a.bottomWall = false; b.topWall = false }
            dr == -1 -> { a.topWall = false; b.bottomWall = false }
        }
    }
}