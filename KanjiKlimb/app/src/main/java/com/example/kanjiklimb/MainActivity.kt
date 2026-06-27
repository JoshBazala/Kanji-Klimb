package com.example.kanjiklimb

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
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
        Paused,
        GameOver,
        Hiragana,
        Katakana,
        Kanji
    }

    private enum class SushiKind {
        Tuna,
        Salmon,
        Egg,
        Roll
    }

    private data class Obstacle(
        val lane: Int,
        var y: Float,
        val sushiKind: SushiKind
    )

    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
    }
    private val menuBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(232, 247, 255)
    }
    private val menuBubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 197, 202)
    }
    private val titleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 38f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
        style = Paint.Style.STROKE
        strokeWidth = 7f * resources.displayMetrics.density
        strokeJoin = Paint.Join.ROUND
    }
    private val menuTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(230, 91, 105)
        textAlign = Paint.Align.CENTER
        textSize = 38f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val menuBeltLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(230, 91, 105)
        textAlign = Paint.Align.LEFT
        textSize = 15f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val beltCharacterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(24, 32, 38)
        textAlign = Paint.Align.CENTER
        textSize = 24f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val beltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(58, 66, 72)
    }
    private val beltEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(28, 34, 39)
    }
    private val beltStripePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(96, 106, 112)
        strokeWidth = 2f * resources.displayMetrics.density
    }
    private val platePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(245, 247, 250)
    }
    private val plateRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(88, 173, 194)
        style = Paint.Style.STROKE
        strokeWidth = 2f * resources.displayMetrics.density
    }
    private val ricePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 252, 240)
    }
    private val seaweedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(24, 54, 43)
    }
    private val tunaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(220, 60, 78)
    }
    private val salmonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(247, 132, 84)
    }
    private val eggPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(246, 205, 78)
    }
    private val cucumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(104, 170, 87)
    }
    private val toppingShinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(120, 255, 245, 220)
        strokeWidth = 2f * resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
    }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 34f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        textSize = 18f * resources.displayMetrics.scaledDensity
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(230, 91, 105)
    }
    private val secondaryButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(114, 190, 224)
    }
    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 20f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val backTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 17f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val characterGridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(42, 61, 72)
        textAlign = Paint.Align.CENTER
        textSize = 22f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val characterReadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(230, 91, 105)
        textAlign = Paint.Align.CENTER
        textSize = 10f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val pauseIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(230, 91, 105)
    }
    private val pauseIconBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(175, 232, 247, 255)
    }

    private val laneCount = 4
    private val sushiKinds = SushiKind.values()
    private val obstacles = mutableListOf<Obstacle>()
    private val playButtonBounds = RectF()
    private val pauseButtonBounds = RectF()
    private val resumeButtonBounds = RectF()
    private val menuButtonBounds = RectF()
    private val backButtonBounds = RectF()
    private val hiraganaButtonBounds = RectF()
    private val katakanaButtonBounds = RectF()
    private val kanjiButtonBounds = RectF()
    private val drawingBounds = RectF()
    private val hiraganaCharacters = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわをんがぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽぁぃぅぇぉっゃゅょ".map { it.toString() }
    private val katakanaCharacters = "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲンガギグゲゴザジズゼゾダヂヅデドバビブベボパピプペポァィゥェォッャュョヴ".map { it.toString() }
    private val commonKanjiCharacters = "日一国会人年大十二本中長出三時行見月分後前生五間上東四今金九入学高円子外八六下来気小七山話女北午百書先名川千水半男西電校語土木聞食車何南万毎白天母火右読友左休父雨立手力多安知世度明不同道社心地理作用者".map { it.toString() }
    private val hiraganaReadings = listOf(
        "a", "i", "u", "e", "o", "ka", "ki", "ku", "ke", "ko",
        "sa", "shi", "su", "se", "so", "ta", "chi", "tsu", "te", "to",
        "na", "ni", "nu", "ne", "no", "ha", "hi", "fu", "he", "ho",
        "ma", "mi", "mu", "me", "mo", "ya", "yu", "yo", "ra", "ri",
        "ru", "re", "ro", "wa", "wo", "n", "ga", "gi", "gu", "ge",
        "go", "za", "ji", "zu", "ze", "zo", "da", "ji", "zu", "de",
        "do", "ba", "bi", "bu", "be", "bo", "pa", "pi", "pu", "pe",
        "po", "a", "i", "u", "e", "o", "tsu", "ya", "yu", "yo"
    )
    private val katakanaReadings = listOf(
        "a", "i", "u", "e", "o", "ka", "ki", "ku", "ke", "ko",
        "sa", "shi", "su", "se", "so", "ta", "chi", "tsu", "te", "to",
        "na", "ni", "nu", "ne", "no", "ha", "hi", "fu", "he", "ho",
        "ma", "mi", "mu", "me", "mo", "ya", "yu", "yo", "ra", "ri",
        "ru", "re", "ro", "wa", "wo", "n", "ga", "gi", "gu", "ge",
        "go", "za", "ji", "zu", "ze", "zo", "da", "ji", "zu", "de",
        "do", "ba", "bi", "bu", "be", "bo", "pa", "pi", "pu", "pe",
        "po", "a", "i", "u", "e", "o", "tsu", "ya", "yu", "yo", "vu"
    )
    private val commonKanjiReadings = listOf(
        "nichi", "ichi", "kuni", "kai", "hito", "nen", "dai", "juu", "ni", "hon",
        "naka", "nagai", "deru", "san", "toki", "iku", "miru", "tsuki", "fun", "ato",
        "mae", "sei", "go", "aida", "ue", "higashi", "yon", "ima", "kane", "kyuu",
        "hairu", "gaku", "takai", "en", "ko", "soto", "hachi", "roku", "shita", "kuru",
        "ki", "chiisai", "nana", "yama", "hanasu", "onna", "kita", "go", "hyaku", "kaku",
        "saki", "na", "kawa", "sen", "mizu", "han", "otoko", "nishi", "den", "kou",
        "go", "tsuchi", "ki", "kiku", "taberu", "kuruma", "nani", "minami", "man", "mai",
        "shiro", "ten", "haha", "hi", "migi", "yomu", "tomo", "hidari", "yasumu", "chichi",
        "ame", "tatsu", "te", "chikara", "ooi", "yasui", "shiru", "yo", "do", "akarui",
        "fu", "onaji", "michi", "sha", "kokoro", "chi", "ri", "suru", "you", "mono"
    )
    private val hiraganaMenuStream = buildMenuStream(hiraganaCharacters)
    private val katakanaMenuStream = buildMenuStream(katakanaCharacters)
    private val kanjiMenuStream = buildMenuStream(commonKanjiCharacters)
    private val playerRadius = 14f * resources.displayMetrics.density
    private val obstacleRadius = 18f * resources.displayMetrics.density
    private val minSwipeDistance = 48f * resources.displayMetrics.density
    private val obstacleSpeed = 280f * resources.displayMetrics.density
    private val spawnIntervalSeconds = 0.9f
    private val pauseButtonSize = 48f * resources.displayMetrics.density
    private val menuBeltSpeed = 22f * resources.displayMetrics.density

    private var gameState = GameState.Menu
    private var currentLane = laneCount / 2
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var lastFrameTimeNanos = 0L
    private var lastMenuFrameTimeNanos = 0L
    private var menuScrollDistance = 0f
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

    private val menuFrameRunnable = object : Runnable {
        override fun run() {
            if (gameState != GameState.Menu) return

            val now = System.nanoTime()
            val deltaSeconds = if (lastMenuFrameTimeNanos == 0L) {
                0f
            } else {
                ((now - lastMenuFrameTimeNanos) / 1_000_000_000f).coerceAtMost(0.05f)
            }
            lastMenuFrameTimeNanos = now
            menuScrollDistance += menuBeltSpeed * deltaSeconds
            invalidate()
            postOnAnimation(this)
        }
    }

    init {
        setBackgroundColor(Color.WHITE)
        isFocusable = true
        postOnAnimation(menuFrameRunnable)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)

        when (gameState) {
            GameState.Menu -> drawMenu(canvas)
            GameState.Running -> drawGame(canvas, showPauseButton = true)
            GameState.Paused -> {
                drawGame(canvas, showPauseButton = false)
                drawPauseOverlay(canvas)
            }
            GameState.GameOver -> {
                drawGame(canvas, showPauseButton = false)
                drawGameOver(canvas)
            }
            GameState.Hiragana -> drawCharacterScreen(canvas, "Hiragana", hiraganaCharacters, hiraganaReadings)
            GameState.Katakana -> drawCharacterScreen(canvas, "Katakana", katakanaCharacters, katakanaReadings)
            GameState.Kanji -> drawCharacterScreen(canvas, "Common Kanji", commonKanjiCharacters, commonKanjiReadings)
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
                when (gameState) {
                    GameState.Running -> {
                        if (pauseButtonBounds.contains(event.x, event.y)) {
                            pauseGame()
                        } else {
                            handleSwipe(event.x - touchStartX, event.y - touchStartY)
                        }
                    }
                    GameState.Paused -> {
                        when {
                            resumeButtonBounds.contains(event.x, event.y) -> resumeGame()
                            menuButtonBounds.contains(event.x, event.y) -> showMainMenu()
                        }
                    }
                    GameState.Menu,
                    GameState.GameOver -> {
                        if (gameState == GameState.Menu) {
                            when {
                                playButtonBounds.contains(event.x, event.y) -> startGame()
                                hiraganaButtonBounds.contains(event.x, event.y) -> showCharacterScreen(GameState.Hiragana)
                                katakanaButtonBounds.contains(event.x, event.y) -> showCharacterScreen(GameState.Katakana)
                                kanjiButtonBounds.contains(event.x, event.y) -> showCharacterScreen(GameState.Kanji)
                            }
                        } else if (playButtonBounds.contains(event.x, event.y)) {
                            startGame()
                        }
                    }
                    GameState.Hiragana,
                    GameState.Katakana,
                    GameState.Kanji -> {
                        if (backButtonBounds.contains(event.x, event.y)) {
                            showMainMenu()
                        }
                    }
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
        removeCallbacks(menuFrameRunnable)
        super.onDetachedFromWindow()
    }

    private fun startGame() {
        removeCallbacks(menuFrameRunnable)
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

    private fun pauseGame() {
        gameState = GameState.Paused
        removeCallbacks(frameRunnable)
        invalidate()
    }

    private fun resumeGame() {
        lastFrameTimeNanos = System.nanoTime()
        gameState = GameState.Running
        removeCallbacks(frameRunnable)
        postOnAnimation(frameRunnable)
        invalidate()
    }

    private fun showMainMenu() {
        obstacles.clear()
        gameState = GameState.Menu
        removeCallbacks(frameRunnable)
        lastMenuFrameTimeNanos = System.nanoTime()
        removeCallbacks(menuFrameRunnable)
        postOnAnimation(menuFrameRunnable)
        invalidate()
    }

    private fun showCharacterScreen(state: GameState) {
        gameState = state
        removeCallbacks(menuFrameRunnable)
        invalidate()
    }

    private fun updateGame(deltaSeconds: Float) {
        scoreSeconds += deltaSeconds
        spawnTimerSeconds += deltaSeconds

        if (spawnTimerSeconds >= spawnIntervalSeconds) {
            spawnTimerSeconds = 0f
            obstacles.add(
                Obstacle(
                    lane = Random.nextInt(laneCount),
                    y = -obstacleRadius,
                    sushiKind = sushiKinds[Random.nextInt(sushiKinds.size)]
                )
            )
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

    private fun drawGame(canvas: Canvas, showPauseButton: Boolean) {
        drawConveyorLanes(canvas)

        obstacles.forEach { obstacle ->
            drawSushiPlate(canvas, laneCenterX(obstacle.lane), obstacle.y, obstacle.sushiKind)
        }

        canvas.drawCircle(laneCenterX(currentLane), playerY(), playerRadius, playerPaint)
        canvas.drawText("Score: ${scoreSeconds.toInt()}", width / 2f, 48f * resources.displayMetrics.density, bodyPaint)
        if (showPauseButton) {
            drawPauseButton(canvas)
        }
    }

    private fun drawMenu(canvas: Canvas) {
        val density = resources.displayMetrics.density
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), menuBackgroundPaint)

        canvas.drawCircle(width * 0.16f, height * 0.2f, 46f * density, menuBubblePaint)
        canvas.drawCircle(width * 0.86f, height * 0.31f, 58f * density, menuBubblePaint)
        canvas.drawCircle(width * 0.24f, height * 0.62f, 34f * density, secondaryButtonPaint)

        val titleY = height * 0.31f
        canvas.drawText("Kanji", width / 2f, titleY, titleStrokePaint)
        canvas.drawText("Kanji", width / 2f, titleY, menuTitlePaint)
        canvas.drawText("Klimb", width / 2f, titleY + 48f * density, titleStrokePaint)
        canvas.drawText("Klimb", width / 2f, titleY + 48f * density, menuTitlePaint)

        canvas.drawText("Swipe left and right.", width / 2f, height * 0.48f, bodyPaint)
        canvas.drawText("Dodge the sushi plates.", width / 2f, height * 0.53f, bodyPaint)
        drawButton(canvas, playButtonBounds, "PLAY", buttonPaint, height * 0.56f)
        drawMenuCharacterBelt(canvas, hiraganaButtonBounds, "Hiragana", hiraganaMenuStream, height * 0.68f, true)
        drawMenuCharacterBelt(canvas, katakanaButtonBounds, "Katakana", katakanaMenuStream, height * 0.78f, false)
        drawMenuCharacterBelt(canvas, kanjiButtonBounds, "Kanji", kanjiMenuStream, height * 0.88f, true)
    }

    private fun drawGameOver(canvas: Canvas) {
        canvas.drawText("Game Over", width / 2f, height * 0.34f, titlePaint)
        canvas.drawText("Score: ${scoreSeconds.toInt()}", width / 2f, height * 0.42f, bodyPaint)
        drawButton(canvas, playButtonBounds, "PLAY AGAIN", buttonPaint, height * 0.52f)
    }

    private fun drawPauseButton(canvas: Canvas) {
        val density = resources.displayMetrics.density
        val margin = 14f * density
        pauseButtonBounds.set(
            width - margin - pauseButtonSize,
            margin,
            width - margin,
            margin + pauseButtonSize
        )

        canvas.drawCircle(pauseButtonBounds.centerX(), pauseButtonBounds.centerY(), pauseButtonSize / 2f, pauseIconPaint)
        val barWidth = 5f * density
        val barHeight = 20f * density
        val gap = 5f * density
        val centerY = pauseButtonBounds.centerY()
        canvas.drawRoundRect(
            pauseButtonBounds.centerX() - gap - barWidth,
            centerY - barHeight / 2f,
            pauseButtonBounds.centerX() - gap,
            centerY + barHeight / 2f,
            2f * density,
            2f * density,
            pauseIconBarPaint
        )
        canvas.drawRoundRect(
            pauseButtonBounds.centerX() + gap,
            centerY - barHeight / 2f,
            pauseButtonBounds.centerX() + gap + barWidth,
            centerY + barHeight / 2f,
            2f * density,
            2f * density,
            pauseIconBarPaint
        )
    }

    private fun drawPauseOverlay(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        canvas.drawText("Paused", width / 2f, height * 0.34f, titlePaint)
        drawButton(canvas, resumeButtonBounds, "RESUME", buttonPaint, height * 0.45f)
        drawButton(canvas, menuButtonBounds, "MAIN MENU", secondaryButtonPaint, height * 0.56f)
    }

    private fun drawButton(canvas: Canvas, bounds: RectF, label: String, paint: Paint, top: Float) {
        val buttonWidth = width * 0.48f
        val buttonHeight = 56f * resources.displayMetrics.density
        bounds.set(
            width / 2f - buttonWidth / 2f,
            top,
            width / 2f + buttonWidth / 2f,
            top + buttonHeight
        )

        canvas.drawRoundRect(bounds, 24f * resources.displayMetrics.density, 24f * resources.displayMetrics.density, paint)

        val textY = bounds.centerY() - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2f
        canvas.drawText(label, bounds.centerX(), textY, buttonTextPaint)
    }

    private fun drawBackButton(canvas: Canvas) {
        val density = resources.displayMetrics.density
        backButtonBounds.set(18f * density, 18f * density, 112f * density, 62f * density)
        canvas.drawRoundRect(backButtonBounds, 18f * density, 18f * density, secondaryButtonPaint)
        val textY = backButtonBounds.centerY() - (backTextPaint.descent() + backTextPaint.ascent()) / 2f
        canvas.drawText("BACK", backButtonBounds.centerX(), textY, backTextPaint)
    }

    private fun drawMenuCharacterBelt(
        canvas: Canvas,
        bounds: RectF,
        label: String,
        characters: List<String>,
        top: Float,
        leftToRight: Boolean
    ) {
        val density = resources.displayMetrics.density
        val beltHeight = 44f * density
        val left = 22f * density
        val right = width - 22f * density
        bounds.set(left, top, right, top + beltHeight)

        canvas.drawText(label, left, top - 5f * density, menuBeltLabelPaint)
        canvas.drawRoundRect(bounds, 18f * density, 18f * density, beltPaint)
        canvas.drawRect(left, top, left + 6f * density, top + beltHeight, beltEdgePaint)
        canvas.drawRect(right - 6f * density, top, right, top + beltHeight, beltEdgePaint)

        val slotWidth = 42f * density
        val beltWidth = right - left
        val directionOffset = menuScrollDistance % slotWidth
        val firstX = if (leftToRight) {
            left - slotWidth + directionOffset
        } else {
            left + directionOffset
        }

        var slot = 0
        var x = firstX
        while (x < right + slotWidth) {
            val characterIndex = if (leftToRight) {
                slot.floorMod(characters.size)
            } else {
                (characters.size - 1 - slot.floorMod(characters.size)).floorMod(characters.size)
            }
            val centerX = if (leftToRight) x else right - (x - left)
            if (centerX >= left - slotWidth && centerX <= right + slotWidth) {
                canvas.drawCircle(centerX, bounds.centerY(), 15f * density, platePaint)
                canvas.drawText(
                    characters[characterIndex],
                    centerX,
                    bounds.centerY() - (beltCharacterPaint.descent() + beltCharacterPaint.ascent()) / 2f,
                    beltCharacterPaint
                )
            }
            x += slotWidth
            slot += 1
            if (slotWidth * slot > beltWidth + slotWidth * 3f) break
        }
    }

    private fun drawCharacterScreen(
        canvas: Canvas,
        title: String,
        characters: List<String>,
        readings: List<String>
    ) {
        val density = resources.displayMetrics.density
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), menuBackgroundPaint)
        drawBackButton(canvas)
        canvas.drawText(title, width / 2f, 72f * density, titlePaint)

        val columns = if (characters.size > 80) 8 else 7
        val startY = 116f * density
        val horizontalPadding = 16f * density
        val cellWidth = (width - horizontalPadding * 2f) / columns
        val rows = ((characters.size + columns - 1) / columns).coerceAtLeast(1)
        val availableHeight = height - startY - 20f * density
        val cellHeight = (availableHeight / rows).coerceAtMost(48f * density)
        characterGridPaint.textSize = if (characters.size > 80) {
            18f * resources.displayMetrics.scaledDensity
        } else {
            21f * resources.displayMetrics.scaledDensity
        }
        characterReadingPaint.textSize = if (characters.size > 80) {
            8f * resources.displayMetrics.scaledDensity
        } else {
            9f * resources.displayMetrics.scaledDensity
        }

        characters.forEachIndexed { index, character ->
            val column = index % columns
            val row = index / columns
            val centerX = horizontalPadding + cellWidth * column + cellWidth / 2f
            val centerY = startY + cellHeight * row + cellHeight / 2f
            canvas.drawCircle(centerX, centerY - 4f * density, (cellHeight * 0.32f).coerceAtMost(15f * density), platePaint)
            canvas.drawText(
                character,
                centerX,
                centerY - 7f * density - (characterGridPaint.descent() + characterGridPaint.ascent()) / 2f,
                characterGridPaint
            )
            canvas.drawText(
                readings.getOrElse(index) { "" },
                centerX,
                centerY + 14f * density - (characterReadingPaint.descent() + characterReadingPaint.ascent()) / 2f,
                characterReadingPaint
            )
        }
    }

    private fun drawConveyorLanes(canvas: Canvas) {
        val density = resources.displayMetrics.density
        val laneWidth = width / laneCount.toFloat()
        val top = 78f * density
        val bottom = height.toFloat()
        val horizontalInset = 8f * density
        val edgeInset = 5f * density
        val rollerSpacing = 44f * density
        val rollerRadius = 3.5f * density
        val animationOffset = (scoreSeconds * obstacleSpeed * 0.25f) % rollerSpacing

        for (lane in 0 until laneCount) {
            val left = laneWidth * lane + horizontalInset
            val right = laneWidth * (lane + 1) - horizontalInset
            drawingBounds.set(left, top, right, bottom)
            canvas.drawRoundRect(drawingBounds, 12f * density, 12f * density, beltPaint)

            canvas.drawRect(left, top, left + edgeInset, bottom, beltEdgePaint)
            canvas.drawRect(right - edgeInset, top, right, bottom, beltEdgePaint)

            var rollerY = top + animationOffset
            while (rollerY < bottom) {
                canvas.drawLine(left + edgeInset * 2f, rollerY, right - edgeInset * 2f, rollerY, beltStripePaint)
                canvas.drawCircle(left + edgeInset * 1.5f, rollerY, rollerRadius, beltStripePaint)
                canvas.drawCircle(right - edgeInset * 1.5f, rollerY, rollerRadius, beltStripePaint)
                rollerY += rollerSpacing
            }
        }
    }

    private fun drawSushiPlate(canvas: Canvas, centerX: Float, centerY: Float, sushiKind: SushiKind) {
        val density = resources.displayMetrics.density
        val plateRadius = obstacleRadius * 1.12f
        canvas.drawCircle(centerX + 1.5f * density, centerY + 2f * density, plateRadius, platePaint)
        canvas.drawCircle(centerX, centerY, plateRadius, platePaint)
        canvas.drawCircle(centerX, centerY, plateRadius * 0.82f, plateRimPaint)

        when (sushiKind) {
            SushiKind.Tuna -> drawNigiri(canvas, centerX, centerY, tunaPaint)
            SushiKind.Salmon -> drawNigiri(canvas, centerX, centerY, salmonPaint)
            SushiKind.Egg -> drawEggSushi(canvas, centerX, centerY)
            SushiKind.Roll -> drawMakiRoll(canvas, centerX, centerY)
        }
    }

    private fun drawNigiri(canvas: Canvas, centerX: Float, centerY: Float, toppingPaint: Paint) {
        val density = resources.displayMetrics.density
        drawingBounds.set(
            centerX - 12f * density,
            centerY - 6f * density,
            centerX + 12f * density,
            centerY + 9f * density
        )
        canvas.drawOval(drawingBounds, ricePaint)

        drawingBounds.set(
            centerX - 15f * density,
            centerY - 11f * density,
            centerX + 15f * density,
            centerY + 2f * density
        )
        canvas.drawRoundRect(drawingBounds, 7f * density, 7f * density, toppingPaint)
        canvas.drawLine(
            centerX - 8f * density,
            centerY - 7f * density,
            centerX + 6f * density,
            centerY - 3f * density,
            toppingShinePaint
        )
    }

    private fun drawEggSushi(canvas: Canvas, centerX: Float, centerY: Float) {
        val density = resources.displayMetrics.density
        drawingBounds.set(
            centerX - 12f * density,
            centerY - 5f * density,
            centerX + 12f * density,
            centerY + 9f * density
        )
        canvas.drawOval(drawingBounds, ricePaint)

        drawingBounds.set(
            centerX - 15f * density,
            centerY - 12f * density,
            centerX + 15f * density,
            centerY + 2f * density
        )
        canvas.drawRoundRect(drawingBounds, 4f * density, 4f * density, eggPaint)
        canvas.drawRect(centerX - 4f * density, centerY - 11f * density, centerX + 4f * density, centerY + 8f * density, seaweedPaint)
    }

    private fun drawMakiRoll(canvas: Canvas, centerX: Float, centerY: Float) {
        val density = resources.displayMetrics.density
        canvas.drawCircle(centerX, centerY, 14f * density, seaweedPaint)
        canvas.drawCircle(centerX, centerY, 10f * density, ricePaint)
        canvas.drawCircle(centerX - 3f * density, centerY - 1f * density, 3f * density, tunaPaint)
        canvas.drawCircle(centerX + 4f * density, centerY + 2f * density, 3f * density, cucumberPaint)
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

    private fun buildMenuStream(source: List<String>): List<String> {
        return List(48) { index ->
            source[(index * 7 + index / 3).floorMod(source.size)]
        }
    }

    private fun Int.floorMod(divisor: Int): Int {
        val remainder = this % divisor
        return if (remainder < 0) remainder + divisor else remainder
    }
}
