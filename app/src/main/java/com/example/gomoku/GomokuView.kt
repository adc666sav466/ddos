package com.example.gomoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

class GomokuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Player(val labelRes: Int) {
        BLACK(R.string.player_black),
        WHITE(R.string.player_white);

        fun next(): Player = if (this == BLACK) WHITE else BLACK
    }

    private val boardSize = 15
    private val board: Array<IntArray> = Array(boardSize) { IntArray(boardSize) }
    private var currentPlayer = Player.BLACK
    private var winner: Player? = null
    private var moveCount = 0
    private var cellSize = 0f
    private var lastMove: Pair<Int, Int>? = null

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.grid_line)
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 1.5f
    }

    private val blackPiecePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.black_piece)
        style = Paint.Style.FILL
    }

    private val whitePiecePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white_piece)
        style = Paint.Style.FILL
        setShadowLayer(resources.displayMetrics.density * 2f, 0f, 0f, 0x55000000)
    }

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.teal_200)
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 2f
    }

    var onStatusChanged: ((String) -> Unit)? = null

    init {
        isClickable = true
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.getSize(widthMeasureSpec).coerceAtMost(MeasureSpec.getSize(heightMeasureSpec))
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = w.toFloat() / (boardSize - 1)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBoard(canvas)
        drawPieces(canvas)
    }

    private fun drawBoard(canvas: Canvas) {
        val padding = cellSize / 2
        for (i in 0 until boardSize) {
            val coord = padding + i * cellSize
            canvas.drawLine(padding, coord, width - padding, coord, gridPaint)
            canvas.drawLine(coord, padding, coord, height - padding, gridPaint)
        }
    }

    private fun drawPieces(canvas: Canvas) {
        val radius = cellSize * 0.4f
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                when (board[row][col]) {
                    1 -> canvas.drawCircle(colToPosition(col), rowToPosition(row), radius, blackPiecePaint)
                    2 -> canvas.drawCircle(colToPosition(col), rowToPosition(row), radius, whitePiecePaint)
                }
            }
        }

        lastMove?.let { (row, col) ->
            val cx = colToPosition(col)
            val cy = rowToPosition(row)
            val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
            canvas.drawOval(rect, highlightPaint)
        }
    }

    private fun colToPosition(col: Int): Float = cellSize / 2 + col * cellSize
    private fun rowToPosition(row: Int): Float = cellSize / 2 + row * cellSize

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        if (event.action != MotionEvent.ACTION_UP) {
            return true
        }

        if (winner != null) {
            return true
        }

        val col = ((event.x - cellSize / 2) / cellSize).toInt()
        val row = ((event.y - cellSize / 2) / cellSize).toInt()

        if (row !in 0 until boardSize || col !in 0 until boardSize) {
            return true
        }

        if (board[row][col] != 0) {
            return true
        }

        placePiece(row, col)
        return true
    }

    private fun placePiece(row: Int, col: Int) {
        board[row][col] = if (currentPlayer == Player.BLACK) 1 else 2
        moveCount++
        lastMove = row to col

        if (checkWin(row, col)) {
            winner = currentPlayer
            updateStatus(Status.Win(currentPlayer))
            isEnabled = false
        } else if (moveCount == boardSize * boardSize) {
            updateStatus(Status.Draw)
            isEnabled = false
        } else {
            currentPlayer = currentPlayer.next()
            updateStatus(Status.Turn(currentPlayer))
        }

        invalidate()
    }

    fun resetGame() {
        for (row in 0 until boardSize) {
            board[row].fill(0)
        }
        moveCount = 0
        currentPlayer = Player.BLACK
        winner = null
        lastMove = null
        isEnabled = true
        updateStatus(Status.Turn(currentPlayer))
        invalidate()
    }

    fun refreshStatus() {
        if (winner != null) {
            updateStatus(Status.Win(winner!!))
        } else if (moveCount == boardSize * boardSize && moveCount > 0) {
            updateStatus(Status.Draw)
        } else {
            updateStatus(Status.Turn(currentPlayer))
        }
    }

    private fun checkWin(row: Int, col: Int): Boolean {
        val target = board[row][col]
        if (target == 0) return false

        val directions = arrayOf(
            1 to 0,
            0 to 1,
            1 to 1,
            1 to -1
        )

        for ((dx, dy) in directions) {
            var count = 1
            count += countDirection(row, col, dx, dy, target)
            count += countDirection(row, col, -dx, -dy, target)
            if (count >= 5) {
                return true
            }
        }
        return false
    }

    private fun countDirection(row: Int, col: Int, dx: Int, dy: Int, target: Int): Int {
        var r = row + dy
        var c = col + dx
        var count = 0
        while (r in 0 until boardSize && c in 0 until boardSize && board[r][c] == target) {
            count++
            r += dy
            c += dx
        }
        return count
    }

    private fun updateStatus(status: Status) {
        val text = when (status) {
            is Status.Turn -> context.getString(R.string.status_player_turn, context.getString(status.player.labelRes))
            is Status.Win -> context.getString(R.string.status_winner, context.getString(status.player.labelRes))
            Status.Draw -> context.getString(R.string.status_draw)
        }
        onStatusChanged?.invoke(text)
    }

    private sealed interface Status {
        data class Turn(val player: Player) : Status
        data class Win(val player: Player) : Status
        data object Draw : Status
    }
}
