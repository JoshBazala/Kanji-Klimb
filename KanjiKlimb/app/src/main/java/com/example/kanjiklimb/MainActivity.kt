package com.example.kanjiklimb

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(RunnerPrototypeView(this))
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUi()
        }
    }

    private fun hideSystemUi() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}

private class RunnerPrototypeView(context: Context) : View(context) {
    private enum class GameState {
        Menu,
        Running,
        GameOver
    }

    private data class Obstacle(
        val lane: Int,
        var y: Float
    )

    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }
    private val obstaclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(210, 55, 55)
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 34f * resources.displayMetrics.scaledDensity
    }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        textSize = 18f * resources.displayMetrics.scaledDensity
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }
    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 20f * resources.displayMetrics.scaledDensity
    }

    private val laneCount = 4
    private val obstacles = mutableListOf<Obstacle>()
    private val playButtonBounds = RectF()
    private val playerRadius = 14f * resources.displayMetrics.density
    private val obstacleRadius = 18f * resources.displayMetrics.density
    private val minSwipeDistance = 48f * resources.displayMetrics.density
    private val obstacleSpeed = 280f * resources.displayMetrics.density
    private val spawnIntervalSeconds = 0.9f

    private var gameState = GameState.Menu
    private var currentLane = laneCount / 2
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var lastFrameTimeNanos = 0L
    private var spawnTimerSeconds = 0f
    private var scoreSeconds = 0f

    private val frameRunnable = object : Runnable {
        override fun run() {
            if (gameState != GameState.Running) return

            val now = System.nanoTime()
            val deltaSeconds = if (lastFrameTimeNanos == 0L) {
                0f
            } else {
                ((now - lastFrameTimeNanos) / 1_000_000_000f).coerceAtMost(0.05f)
            }
            lastFrameTimeNanos = now

            updateGame(deltaSeconds)
            invalidate()

            if (gameState == GameState.Running) {
                postOnAnimation(this)
            }
        }
    }

    init {
        setBackgroundColor(Color.WHITE)
        isFocusable = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)

        when (gameState) {
            GameState.Menu -> drawMenu(canvas)
            GameState.Running -> drawGame(canvas)
            GameState.GameOver -> {
                drawGame(canvas)
                drawGameOver(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (gameState == GameState.Running) {
                    handleSwipe(event.x - touchStartX, event.y - touchStartY)
                } else if (playButtonBounds.contains(event.x, event.y)) {
                    startGame()
                }
                performClick()
                return true
            }
        }

        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(frameRunnable)
        super.onDetachedFromWindow()
    }

    private fun startGame() {
        obstacles.clear()
        currentLane = laneCount / 2
        spawnTimerSeconds = spawnIntervalSeconds
        scoreSeconds = 0f
        lastFrameTimeNanos = System.nanoTime()
        gameState = GameState.Running
        removeCallbacks(frameRunnable)
        postOnAnimation(frameRunnable)
        invalidate()
    }

    private fun updateGame(deltaSeconds: Float) {
        scoreSeconds += deltaSeconds
        spawnTimerSeconds += deltaSeconds

        if (spawnTimerSeconds >= spawnIntervalSeconds) {
            spawnTimerSeconds = 0f
            obstacles.add(Obstacle(Random.nextInt(laneCount), -obstacleRadius))
        }

        obstacles.forEach { obstacle ->
            obstacle.y += obstacleSpeed * deltaSeconds
        }
        obstacles.removeAll { obstacle ->
            obstacle.y - obstacleRadius > height
        }

        val playerY = playerY()
        val hitPlayer = obstacles.any { obstacle ->
            obstacle.lane == currentLane && abs(obstacle.y - playerY) <= playerRadius + obstacleRadius
        }
        if (hitPlayer) {
            gameState = GameState.GameOver
        }
    }

    private fun drawGame(canvas: Canvas) {
        obstacles.forEach { obstacle ->
            canvas.drawCircle(laneCenterX(obstacle.lane), obstacle.y, obstacleRadius, obstaclePaint)
        }

        canvas.drawCircle(laneCenterX(currentLane), playerY(), playerRadius, playerPaint)
        canvas.drawText("Score: ${scoreSeconds.toInt()}", width / 2f, 48f * resources.displayMetrics.density, bodyPaint)
    }

    private fun drawMenu(canvas: Canvas) {
        canvas.drawText("Kanji Klimb", width / 2f, height * 0.34f, titlePaint)
        canvas.drawText("Swipe left and right to avoid falling objects.", width / 2f, height * 0.42f, bodyPaint)
        drawButton(canvas, "PLAY")
    }

    private fun drawGameOver(canvas: Canvas) {
        canvas.drawText("Game Over", width / 2f, height * 0.34f, titlePaint)
        canvas.drawText("Score: ${scoreSeconds.toInt()}", width / 2f, height * 0.42f, bodyPaint)
        drawButton(canvas, "PLAY AGAIN")
    }

    private fun drawButton(canvas: Canvas, label: String) {
        val buttonWidth = width * 0.48f
        val buttonHeight = 56f * resources.displayMetrics.density
        val top = height * 0.52f
        playButtonBounds.set(
            width / 2f - buttonWidth / 2f,
            top,
            width / 2f + buttonWidth / 2f,
            top + buttonHeight
        )

        canvas.drawRoundRect(playButtonBounds, 8f * resources.displayMetrics.density, 8f * resources.displayMetrics.density, buttonPaint)

        val textY = playButtonBounds.centerY() - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2f
        canvas.drawText(label, playButtonBounds.centerX(), textY, buttonTextPaint)
    }

    private fun handleSwipe(deltaX: Float, deltaY: Float) {
        if (abs(deltaX) < minSwipeDistance || abs(deltaX) <= abs(deltaY)) return

        currentLane = if (deltaX > 0f) {
            (currentLane + 1).coerceAtMost(laneCount - 1)
        } else {
            (currentLane - 1).coerceAtLeast(0)
        }
        invalidate()
    }

    private fun laneCenterX(lane: Int): Float {
        val laneWidth = width / laneCount.toFloat()
        return laneWidth * lane + laneWidth / 2f
    }

    private fun playerY(): Float = height * 0.82f
}
