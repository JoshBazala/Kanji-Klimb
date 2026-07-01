package com.example.kanjiklimb

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
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
        Reference
    }

    private enum class SushiKind {
        Tuna,
        Salmon,
        Egg,
        Roll
    }

    private enum class QuestionKind {
        Learning,
        Review
    }

    private enum class LearningMode(
        val title: String,
        val storageKey: String
    ) {
        Hiragana("Hiragana", "hiragana"),
        Katakana("Katakana", "katakana"),
        Kanji("Kanji", "kanji")
    }

    private data class LearningItem(
        val symbol: String,
        val reading: String,
        val meaning: String
    )

    private data class LearningProgress(
        var correct: Int = 0,
        var wrong: Int = 0,
        var streak: Int = 0,
        var level: Int = 0,
        var dueRound: Int = 0
    )

    private data class FallingSushi(
        val lane: Int,
        var y: Float,
        val sushiKind: SushiKind
    )

    private data class QuizPlate(
        val lane: Int,
        var y: Float,
        val item: LearningItem,
        val sushiKind: SushiKind,
        val isCorrect: Boolean
    )

    private val preferences: SharedPreferences =
        context.getSharedPreferences("kanji_klimb_learning", Context.MODE_PRIVATE)

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
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(24, 32, 38)
        textAlign = Paint.Align.CENTER
        textSize = 34f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        textSize = 18f * resources.displayMetrics.scaledDensity
    }
    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(42, 61, 72)
        textAlign = Paint.Align.CENTER
        textSize = 13f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(230, 91, 105)
    }
    private val selectedButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(230, 91, 105)
    }
    private val secondaryButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(114, 190, 224)
    }
    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 19f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val modeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 17f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val modeSubTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(230, 255, 255, 255)
        textAlign = Paint.Align.CENTER
        textSize = 10f * resources.displayMetrics.scaledDensity
    }
    private val labelBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 18, 24, 28)
    }
    private val learningBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(35, 121, 214)
    }
    private val reviewBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(216, 70, 82)
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
    private val choiceTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(24, 32, 38)
        textAlign = Paint.Align.CENTER
        textSize = 23f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val choiceLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(235, 255, 255, 255)
    }
    private val promptPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 20f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val promptLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 197, 202)
        textAlign = Paint.Align.CENTER
        textSize = 13f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val scoreTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 15f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val pauseIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(230, 91, 105)
    }
    private val pauseIconBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(185, 232, 247, 255)
    }
    private val referenceCharacterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(42, 61, 72)
        textAlign = Paint.Align.CENTER
        textSize = 22f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }
    private val referenceReadingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(230, 91, 105)
        textAlign = Paint.Align.CENTER
        textSize = 10f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
    }

    private val laneCount = 4
    private val sushiKinds = SushiKind.values()
    private val normalObstacles = mutableListOf<FallingSushi>()
    private val quizPlates = mutableListOf<QuizPlate>()
    private val progress = mutableMapOf<String, LearningProgress>()
    private val playButtonBounds = RectF()
    private val pauseButtonBounds = RectF()
    private val resumeButtonBounds = RectF()
    private val menuButtonBounds = RectF()
    private val backButtonBounds = RectF()
    private val hiraganaButtonBounds = RectF()
    private val katakanaButtonBounds = RectF()
    private val kanjiButtonBounds = RectF()
    private val referenceButtonBounds = RectF()
    private val drawingBounds = RectF()

    private val hiraganaItems = buildKanaItems(
        symbols = listOf(
            "\u3042", "\u3044", "\u3046", "\u3048", "\u304a",
            "\u304b", "\u304d", "\u304f", "\u3051", "\u3053",
            "\u3055", "\u3057", "\u3059", "\u305b", "\u305d",
            "\u305f", "\u3061", "\u3064", "\u3066", "\u3068",
            "\u306a", "\u306b", "\u306c", "\u306d", "\u306e",
            "\u306f", "\u3072", "\u3075", "\u3078", "\u307b",
            "\u307e", "\u307f", "\u3080", "\u3081", "\u3082",
            "\u3084", "\u3086", "\u3088",
            "\u3089", "\u308a", "\u308b", "\u308c", "\u308d",
            "\u308f", "\u3092", "\u3093"
        ),
        readings = listOf(
            "a", "i", "u", "e", "o",
            "ka", "ki", "ku", "ke", "ko",
            "sa", "shi", "su", "se", "so",
            "ta", "chi", "tsu", "te", "to",
            "na", "ni", "nu", "ne", "no",
            "ha", "hi", "fu", "he", "ho",
            "ma", "mi", "mu", "me", "mo",
            "ya", "yu", "yo",
            "ra", "ri", "ru", "re", "ro",
            "wa", "wo", "n"
        )
    )
    private val katakanaItems = buildKanaItems(
        symbols = listOf(
            "\u30a2", "\u30a4", "\u30a6", "\u30a8", "\u30aa",
            "\u30ab", "\u30ad", "\u30af", "\u30b1", "\u30b3",
            "\u30b5", "\u30b7", "\u30b9", "\u30bb", "\u30bd",
            "\u30bf", "\u30c1", "\u30c4", "\u30c6", "\u30c8",
            "\u30ca", "\u30cb", "\u30cc", "\u30cd", "\u30ce",
            "\u30cf", "\u30d2", "\u30d5", "\u30d8", "\u30db",
            "\u30de", "\u30df", "\u30e0", "\u30e1", "\u30e2",
            "\u30e4", "\u30e6", "\u30e8",
            "\u30e9", "\u30ea", "\u30eb", "\u30ec", "\u30ed",
            "\u30ef", "\u30f2", "\u30f3"
        ),
        readings = listOf(
            "a", "i", "u", "e", "o",
            "ka", "ki", "ku", "ke", "ko",
            "sa", "shi", "su", "se", "so",
            "ta", "chi", "tsu", "te", "to",
            "na", "ni", "nu", "ne", "no",
            "ha", "hi", "fu", "he", "ho",
            "ma", "mi", "mu", "me", "mo",
            "ya", "yu", "yo",
            "ra", "ri", "ru", "re", "ro",
            "wa", "wo", "n"
        )
    )
    private val kanjiItems = listOf(
        LearningItem("\u65e5", "nichi", "sun / day"),
        LearningItem("\u4e00", "ichi", "one"),
        LearningItem("\u4eba", "hito", "person"),
        LearningItem("\u5e74", "nen", "year"),
        LearningItem("\u5927", "dai", "big"),
        LearningItem("\u4e8c", "ni", "two"),
        LearningItem("\u672c", "hon", "book / origin"),
        LearningItem("\u4e2d", "naka", "inside"),
        LearningItem("\u51fa", "deru", "exit"),
        LearningItem("\u4e09", "san", "three"),
        LearningItem("\u6642", "toki", "time"),
        LearningItem("\u884c", "iku", "go"),
        LearningItem("\u898b", "miru", "see"),
        LearningItem("\u6708", "tsuki", "moon / month"),
        LearningItem("\u524d", "mae", "before"),
        LearningItem("\u751f", "sei", "life"),
        LearningItem("\u4e0a", "ue", "up"),
        LearningItem("\u6771", "higashi", "east"),
        LearningItem("\u4eca", "ima", "now"),
        LearningItem("\u91d1", "kane", "gold / money"),
        LearningItem("\u5165", "hairu", "enter"),
        LearningItem("\u5b66", "gaku", "study"),
        LearningItem("\u9ad8", "takai", "high / expensive"),
        LearningItem("\u5b50", "ko", "child"),
        LearningItem("\u5916", "soto", "outside"),
        LearningItem("\u4e0b", "shita", "down"),
        LearningItem("\u5c71", "yama", "mountain"),
        LearningItem("\u5973", "onna", "woman"),
        LearningItem("\u5317", "kita", "north"),
        LearningItem("\u6c34", "mizu", "water"),
        LearningItem("\u7537", "otoko", "man"),
        LearningItem("\u897f", "nishi", "west"),
        LearningItem("\u8eca", "kuruma", "car"),
        LearningItem("\u5357", "minami", "south"),
        LearningItem("\u767d", "shiro", "white"),
        LearningItem("\u706b", "hi", "fire"),
        LearningItem("\u53f3", "migi", "right"),
        LearningItem("\u5de6", "hidari", "left"),
        LearningItem("\u96e8", "ame", "rain"),
        LearningItem("\u624b", "te", "hand")
    )
    private val menuStreams = mapOf(
        LearningMode.Hiragana to buildMenuStream(hiraganaItems.map { it.symbol }),
        LearningMode.Katakana to buildMenuStream(katakanaItems.map { it.symbol }),
        LearningMode.Kanji to buildMenuStream(kanjiItems.map { it.symbol })
    )

    private val playerRadius = 14f * resources.displayMetrics.density
    private val plateRadius = 22f * resources.displayMetrics.density
    private val minSwipeDistance = 48f * resources.displayMetrics.density
    private val obstacleSpeed = 275f * resources.displayMetrics.density
    private val questionPlateSpeed = 145f * resources.displayMetrics.density
    private val normalSpawnIntervalSeconds = 1.05f
    private val questionIntervalSeconds = 12f
    private val pauseButtonSize = 48f * resources.displayMetrics.density
    private val menuBeltSpeed = 22f * resources.displayMetrics.density

    private var gameState = GameState.Menu
    private var selectedMode = LearningMode.Hiragana
    private var referenceMode = LearningMode.Hiragana
    private var currentQuestionKind = QuestionKind.Learning
    private var currentPrompt = ""
    private var correctItem: LearningItem? = null
    private var currentLane = laneCount / 2
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var lastFrameTimeNanos = 0L
    private var lastMenuFrameTimeNanos = 0L
    private var menuScrollDistance = 0f
    private var normalSpawnTimerSeconds = 0f
    private var questionTimerSeconds = 0f
    private var feedbackTimerSeconds = 0f
    private var scoreSeconds = 0f
    private var correctThisRun = 0
    private var questionRound = 0
    private var feedbackText = ""

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
        loadAllProgress()
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
            GameState.Reference -> drawReferenceScreen(canvas)
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
                    GameState.Menu -> {
                        when {
                            playButtonBounds.contains(event.x, event.y) -> startGame()
                            hiraganaButtonBounds.contains(event.x, event.y) -> selectMode(LearningMode.Hiragana)
                            katakanaButtonBounds.contains(event.x, event.y) -> selectMode(LearningMode.Katakana)
                            kanjiButtonBounds.contains(event.x, event.y) -> selectMode(LearningMode.Kanji)
                            referenceButtonBounds.contains(event.x, event.y) -> showReferenceScreen()
                        }
                    }
                    GameState.GameOver -> {
                        when {
                            playButtonBounds.contains(event.x, event.y) -> startGame()
                            menuButtonBounds.contains(event.x, event.y) -> showMainMenu()
                        }
                    }
                    GameState.Reference -> {
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

    private fun selectMode(mode: LearningMode) {
        selectedMode = mode
        referenceMode = mode
        invalidate()
    }

    private fun startGame() {
        removeCallbacks(menuFrameRunnable)
        normalObstacles.clear()
        quizPlates.clear()
        currentLane = laneCount / 2
        currentPrompt = "Dodge sushi until the next question"
        correctItem = null
        normalSpawnTimerSeconds = normalSpawnIntervalSeconds
        questionTimerSeconds = 4f
        feedbackTimerSeconds = 0f
        scoreSeconds = 0f
        correctThisRun = 0
        questionRound = preferences.getInt("${selectedMode.storageKey}_round", 0)
        feedbackText = ""
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
        normalObstacles.clear()
        quizPlates.clear()
        gameState = GameState.Menu
        removeCallbacks(frameRunnable)
        lastMenuFrameTimeNanos = System.nanoTime()
        removeCallbacks(menuFrameRunnable)
        postOnAnimation(menuFrameRunnable)
        invalidate()
    }

    private fun showReferenceScreen() {
        referenceMode = selectedMode
        gameState = GameState.Reference
        removeCallbacks(menuFrameRunnable)
        invalidate()
    }

    private fun updateGame(deltaSeconds: Float) {
        scoreSeconds += deltaSeconds

        if (quizPlates.isEmpty()) {
            if (feedbackTimerSeconds > 0f) {
                feedbackTimerSeconds -= deltaSeconds
                currentPrompt = feedbackText
            } else {
                currentPrompt = "Dodge sushi: next question in ${questionTimerSeconds.toInt().coerceAtLeast(0)}"
            }
            updateNormalObstacles(deltaSeconds)
            questionTimerSeconds -= deltaSeconds
            if (questionTimerSeconds <= 0f) {
                spawnQuestion()
            }
        } else {
            quizPlates.forEach { plate ->
                plate.y += questionPlateSpeed * deltaSeconds
            }

            val reachedPlayer = quizPlates.first().y >= playerY()
            if (reachedPlayer) {
                resolveCurrentQuestion()
            }
        }
    }

    private fun updateNormalObstacles(deltaSeconds: Float) {
        normalSpawnTimerSeconds += deltaSeconds
        if (normalSpawnTimerSeconds >= normalSpawnIntervalSeconds) {
            normalSpawnTimerSeconds = 0f
            normalObstacles.add(
                FallingSushi(
                    lane = Random.nextInt(laneCount),
                    y = -plateRadius,
                    sushiKind = sushiKinds[Random.nextInt(sushiKinds.size)]
                )
            )
        }

        normalObstacles.forEach { obstacle ->
            obstacle.y += obstacleSpeed * deltaSeconds
        }
        normalObstacles.removeAll { obstacle ->
            obstacle.y - plateRadius > height
        }

        val hitObstacle = normalObstacles.any { obstacle ->
            obstacle.lane == currentLane && abs(obstacle.y - playerY()) <= playerRadius + plateRadius
        }
        if (hitObstacle) {
            feedbackText = "A sushi plate hit you. Keep dodging between questions."
            gameState = GameState.GameOver
        }
    }

    private fun spawnQuestion() {
        questionRound += 1
        preferences.edit().putInt("${selectedMode.storageKey}_round", questionRound).apply()

        val items = itemsForMode(selectedMode)
        val question = chooseQuestionItem(items)
        val correct = question.first
        currentQuestionKind = question.second
        val choices = buildChoices(items, correct)
        val correctLane = choices.indexOf(correct)

        correctItem = correct
        currentPrompt = promptFor(correct)
        feedbackText = ""
        normalObstacles.clear()
        quizPlates.clear()
        choices.forEachIndexed { lane, item ->
            quizPlates.add(
                QuizPlate(
                    lane = lane,
                    y = -plateRadius * 1.8f,
                    item = item,
                    sushiKind = sushiKinds[(questionRound + lane).floorMod(sushiKinds.size)],
                    isCorrect = lane == correctLane
                )
            )
        }
    }

    private fun resolveCurrentQuestion() {
        val chosenPlate = quizPlates.firstOrNull { it.lane == currentLane }
        val correct = chosenPlate?.isCorrect == true
        val item = correctItem ?: return

        quizPlates.clear()
        if (correct) {
            updateProgress(item, answeredCorrectly = true)
            correctThisRun += 1
            feedbackText = "Correct! ${item.symbol} = ${answerFor(item)}"
            currentPrompt = feedbackText
            feedbackTimerSeconds = 2.2f
            questionTimerSeconds = questionIntervalSeconds
            normalSpawnTimerSeconds = normalSpawnIntervalSeconds * 0.5f
        } else {
            updateProgress(item, answeredCorrectly = false)
            val chosenText = chosenPlate?.item?.symbol ?: "nothing"
            feedbackText = "Wrong lane: $chosenText. Answer: ${item.symbol} (${answerFor(item)})"
            currentPrompt = feedbackText
            feedbackTimerSeconds = 2.6f
            if (currentQuestionKind == QuestionKind.Review) {
                gameState = GameState.GameOver
            } else {
                questionTimerSeconds = 3.5f
                normalSpawnTimerSeconds = normalSpawnIntervalSeconds * 0.5f
            }
        }
    }

    private fun drawGame(canvas: Canvas, showPauseButton: Boolean) {
        drawConveyorLanes(canvas)
        drawPrompt(canvas)

        normalObstacles.forEach { obstacle ->
            drawSushiPlate(canvas, laneCenterX(obstacle.lane), obstacle.y, obstacle.sushiKind)
        }

        quizPlates.forEach { plate ->
            drawSushiPlate(canvas, laneCenterX(plate.lane), plate.y, plate.sushiKind)
            drawChoiceText(canvas, plate)
        }

        canvas.drawCircle(laneCenterX(currentLane), playerY(), playerRadius, playerPaint)
        if (showPauseButton) {
            drawPauseButton(canvas)
        }
    }

    private fun drawPrompt(canvas: Canvas) {
        val density = resources.displayMetrics.density
        val isQuestionActive = quizPlates.isNotEmpty() || feedbackTimerSeconds > 0f
        val promptBoxPaint = when {
            !isQuestionActive -> labelBoxPaint
            currentQuestionKind == QuestionKind.Learning -> learningBoxPaint
            else -> reviewBoxPaint
        }
        val promptLabel = when {
            !isQuestionActive -> "${selectedMode.title} run"
            currentQuestionKind == QuestionKind.Learning -> "BLUE: new item - mistakes are safe"
            else -> "RED: review - mistakes end the run"
        }
        drawingBounds.set(16f * density, 16f * density, width - 74f * density, 84f * density)
        canvas.drawRoundRect(drawingBounds, 18f * density, 18f * density, promptBoxPaint)
        canvas.drawText(promptLabel, drawingBounds.centerX(), 39f * density, promptLabelPaint)
        canvas.drawText(currentPrompt, drawingBounds.centerX(), 65f * density, promptPaint)

        val scoreWidth = 150f * density
        drawingBounds.set(
            width / 2f - scoreWidth / 2f,
            92f * density,
            width / 2f + scoreWidth / 2f,
            122f * density
        )
        canvas.drawRoundRect(drawingBounds, 15f * density, 15f * density, labelBoxPaint)
        val scoreY = drawingBounds.centerY() - (scoreTextPaint.descent() + scoreTextPaint.ascent()) / 2f
        canvas.drawText("Correct: $correctThisRun", drawingBounds.centerX(), scoreY, scoreTextPaint)
    }

    private fun drawChoiceText(canvas: Canvas, plate: QuizPlate) {
        val density = resources.displayMetrics.density
        val centerX = laneCenterX(plate.lane)
        val labelWidth = 46f * density
        val labelHeight = 30f * density
        drawingBounds.set(
            centerX - labelWidth / 2f,
            plate.y - labelHeight / 2f,
            centerX + labelWidth / 2f,
            plate.y + labelHeight / 2f
        )
        canvas.drawRoundRect(drawingBounds, 10f * density, 10f * density, choiceLabelPaint)
        val textY = drawingBounds.centerY() - (choiceTextPaint.descent() + choiceTextPaint.ascent()) / 2f
        canvas.drawText(plate.item.symbol, centerX, textY, choiceTextPaint)
    }

    private fun drawMenu(canvas: Canvas) {
        val density = resources.displayMetrics.density
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), menuBackgroundPaint)

        canvas.drawCircle(width * 0.16f, height * 0.2f, 46f * density, menuBubblePaint)
        canvas.drawCircle(width * 0.86f, height * 0.31f, 58f * density, menuBubblePaint)
        canvas.drawCircle(width * 0.24f, height * 0.62f, 34f * density, secondaryButtonPaint)

        val titleY = height * 0.23f
        canvas.drawText("Kanji", width / 2f, titleY, titleStrokePaint)
        canvas.drawText("Kanji", width / 2f, titleY, menuTitlePaint)
        canvas.drawText("Klimb", width / 2f, titleY + 48f * density, titleStrokePaint)
        canvas.drawText("Klimb", width / 2f, titleY + 48f * density, menuTitlePaint)

        canvas.drawText("Pick what you want to practice.", width / 2f, height * 0.37f, bodyPaint)
        drawModeButton(canvas, hiraganaButtonBounds, LearningMode.Hiragana, height * 0.42f)
        drawModeButton(canvas, katakanaButtonBounds, LearningMode.Katakana, height * 0.52f)
        drawModeButton(canvas, kanjiButtonBounds, LearningMode.Kanji, height * 0.62f)

        canvas.drawText("Blue teaches new items. Red reviews learned ones.", width / 2f, height * 0.73f, bodyPaint)
        drawButton(canvas, referenceButtonBounds, "STUDY CHART", secondaryButtonPaint, height * 0.76f, width * 0.58f)
        drawButton(canvas, playButtonBounds, "START RUN", buttonPaint, height * 0.86f, width * 0.58f)
    }

    private fun drawModeButton(canvas: Canvas, bounds: RectF, mode: LearningMode, top: Float) {
        val density = resources.displayMetrics.density
        val left = 24f * density
        val right = width - 24f * density
        val height = 54f * density
        val paint = if (selectedMode == mode) selectedButtonPaint else secondaryButtonPaint
        bounds.set(left, top, right, top + height)

        canvas.drawRoundRect(bounds, 22f * density, 22f * density, paint)
        val stream = menuStreams.getValue(mode)
        drawModeStream(canvas, bounds, stream)

        val labelWidth = 136f * density
        val labelHeight = 28f * density
        drawingBounds.set(
            bounds.centerX() - labelWidth / 2f,
            bounds.centerY() - labelHeight / 2f - 6f * density,
            bounds.centerX() + labelWidth / 2f,
            bounds.centerY() + labelHeight / 2f - 6f * density
        )
        canvas.drawRoundRect(drawingBounds, 13f * density, 13f * density, labelBoxPaint)
        val textY = bounds.centerY() - 5f * density - (modeTextPaint.descent() + modeTextPaint.ascent()) / 2f
        canvas.drawText(mode.title, bounds.centerX(), textY, modeTextPaint)

        val progressWidth = 178f * density
        val progressHeight = 17f * density
        drawingBounds.set(
            bounds.centerX() - progressWidth / 2f,
            bounds.centerY() + 8f * density,
            bounds.centerX() + progressWidth / 2f,
            bounds.centerY() + 8f * density + progressHeight
        )
        canvas.drawRoundRect(drawingBounds, 8f * density, 8f * density, labelBoxPaint)
        canvas.drawText(progressSummary(mode), bounds.centerX(), bounds.centerY() + 16f * density, modeSubTextPaint)
    }

    private fun drawModeStream(canvas: Canvas, bounds: RectF, characters: List<String>) {
        val density = resources.displayMetrics.density
        val slotWidth = 42f * density
        val directionOffset = menuScrollDistance % slotWidth
        var slot = 0
        var x = bounds.left - slotWidth + directionOffset
        canvas.save()
        canvas.clipRect(bounds)
        while (x < bounds.right + slotWidth) {
            if (x > bounds.left - slotWidth / 2f && x < bounds.right + slotWidth / 2f) {
                val symbol = characters[slot.floorMod(characters.size)]
                canvas.drawCircle(x, bounds.centerY(), 14f * density, platePaint)
                canvas.drawText(
                    symbol,
                    x,
                    bounds.centerY() - (smallTextPaint.descent() + smallTextPaint.ascent()) / 2f,
                    smallTextPaint
                )
            }
            x += slotWidth
            slot += 1
        }
        canvas.restore()
    }

    private fun drawGameOver(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        canvas.drawText("Game Over", width / 2f, height * 0.28f, titlePaint)
        canvas.drawText("Correct this run: $correctThisRun", width / 2f, height * 0.36f, bodyPaint)
        drawWrappedCenteredText(canvas, feedbackText, height * 0.43f, bodyPaint, width * 0.82f)
        drawButton(canvas, playButtonBounds, "TRY AGAIN", buttonPaint, height * 0.58f, width * 0.52f)
        drawButton(canvas, menuButtonBounds, "MAIN MENU", secondaryButtonPaint, height * 0.68f, width * 0.52f)
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
        drawButton(canvas, resumeButtonBounds, "RESUME", buttonPaint, height * 0.45f, width * 0.52f)
        drawButton(canvas, menuButtonBounds, "MAIN MENU", secondaryButtonPaint, height * 0.56f, width * 0.52f)
    }

    private fun drawButton(canvas: Canvas, bounds: RectF, label: String, paint: Paint, top: Float, buttonWidth: Float) {
        val buttonHeight = 52f * resources.displayMetrics.density
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
        val textY = backButtonBounds.centerY() - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2f
        canvas.drawText("BACK", backButtonBounds.centerX(), textY, buttonTextPaint)
    }

    private fun drawReferenceScreen(canvas: Canvas) {
        val items = itemsForMode(referenceMode)
        val density = resources.displayMetrics.density
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), menuBackgroundPaint)
        drawBackButton(canvas)
        canvas.drawText("${referenceMode.title} Chart", width / 2f, 72f * density, titlePaint)

        val columns = if (items.size > 40) 7 else 5
        val startY = 116f * density
        val horizontalPadding = 16f * density
        val cellWidth = (width - horizontalPadding * 2f) / columns
        val rows = ((items.size + columns - 1) / columns).coerceAtLeast(1)
        val availableHeight = height - startY - 18f * density
        val cellHeight = (availableHeight / rows).coerceAtMost(52f * density)

        items.forEachIndexed { index, item ->
            val column = index % columns
            val row = index / columns
            val centerX = horizontalPadding + cellWidth * column + cellWidth / 2f
            val centerY = startY + cellHeight * row + cellHeight / 2f
            canvas.drawCircle(centerX, centerY - 4f * density, (cellHeight * 0.32f).coerceAtMost(15f * density), platePaint)
            canvas.drawText(
                item.symbol,
                centerX,
                centerY - 7f * density - (referenceCharacterPaint.descent() + referenceCharacterPaint.ascent()) / 2f,
                referenceCharacterPaint
            )
            canvas.drawText(
                answerFor(item),
                centerX,
                centerY + 14f * density - (referenceReadingPaint.descent() + referenceReadingPaint.ascent()) / 2f,
                referenceReadingPaint
            )
        }
    }

    private fun drawConveyorLanes(canvas: Canvas) {
        val density = resources.displayMetrics.density
        val laneWidth = width / laneCount.toFloat()
        val top = 132f * density
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

    private fun chooseQuestionItem(items: List<LearningItem>): Pair<LearningItem, QuestionKind> {
        val unlearnedItems = items.filter { !isLearned(it) }
        val dueReviewItems = items.filter { item ->
            isLearned(item) && progressFor(item).dueRound <= questionRound
        }
        val shouldReview = dueReviewItems.isNotEmpty() && (unlearnedItems.isEmpty() || questionRound % 3 != 1)

        return if (shouldReview) {
            chooseReviewItem(dueReviewItems) to QuestionKind.Review
        } else if (unlearnedItems.isNotEmpty()) {
            chooseLearningItem(unlearnedItems) to QuestionKind.Learning
        } else {
            chooseReviewItem(items.filter { isLearned(it) }.ifEmpty { items }) to QuestionKind.Review
        }
    }

    private fun chooseLearningItem(items: List<LearningItem>): LearningItem {
        val ordered = items.sortedWith(
            compareBy<LearningItem> { progressFor(it).dueRound > questionRound }
                .thenByDescending { progressFor(it).wrong }
                .thenBy { progressFor(it).dueRound }
        )
        val candidatePool = ordered.take(6.coerceAtMost(ordered.size))
        return candidatePool[Random.nextInt(candidatePool.size)]
    }

    private fun chooseReviewItem(items: List<LearningItem>): LearningItem {
        val ordered = items.sortedWith(
            compareBy<LearningItem> { progressFor(it).dueRound }
                .thenBy { progressFor(it).level }
                .thenBy { progressFor(it).streak }
        )
        val candidatePool = ordered.take(8.coerceAtMost(ordered.size))
        return candidatePool[Random.nextInt(candidatePool.size)]
    }

    private fun isLearned(item: LearningItem): Boolean {
        return progressFor(item).correct > 0
    }

    private fun buildChoices(items: List<LearningItem>, correct: LearningItem): List<LearningItem> {
        val distractors = items
            .filter { it != correct }
            .shuffled()
            .take(laneCount - 1)
            .toMutableList()
        distractors.add(correct)
        return distractors.shuffled()
    }

    private fun updateProgress(item: LearningItem, answeredCorrectly: Boolean) {
        val itemProgress = progressFor(item)
        if (answeredCorrectly) {
            itemProgress.correct += 1
            itemProgress.streak += 1
            itemProgress.level = (itemProgress.level + 1).coerceAtMost(4)
            itemProgress.dueRound = questionRound + srsIntervalFor(itemProgress.level)
        } else {
            itemProgress.wrong += 1
            itemProgress.streak = 0
            itemProgress.level = (itemProgress.level - 1).coerceAtLeast(0)
            itemProgress.dueRound = questionRound + 1
        }
        saveProgress(item, itemProgress)
    }

    private fun progressSummary(mode: LearningMode): String {
        val items = itemsForMode(mode)
        val learned = items.count { progressFor(mode, it).correct > 0 }
        val mastered = items.count { progressFor(mode, it).level >= 4 }
        return "$learned learned  |  $mastered mastered"
    }

    private fun srsIntervalFor(level: Int): Int {
        return when (level) {
            0 -> 1
            1 -> 2
            2 -> 4
            3 -> 8
            else -> 16
        }
    }

    private fun progressFor(item: LearningItem): LearningProgress {
        return progressFor(selectedMode, item)
    }

    private fun progressFor(mode: LearningMode, item: LearningItem): LearningProgress {
        return progress.getOrPut(progressKey(mode, item)) {
            loadProgress(mode, item)
        }
    }

    private fun loadAllProgress() {
        LearningMode.values().forEach { mode ->
            itemsForMode(mode).forEach { item ->
                progress[progressKey(mode, item)] = loadProgress(mode, item)
            }
        }
    }

    private fun loadProgress(mode: LearningMode, item: LearningItem): LearningProgress {
        val key = progressKey(mode, item)
        return LearningProgress(
            correct = preferences.getInt("${key}_correct", 0),
            wrong = preferences.getInt("${key}_wrong", 0),
            streak = preferences.getInt("${key}_streak", 0),
            level = preferences.getInt("${key}_level", 0),
            dueRound = preferences.getInt("${key}_due", 0)
        )
    }

    private fun saveProgress(item: LearningItem, itemProgress: LearningProgress) {
        val key = progressKey(selectedMode, item)
        preferences.edit()
            .putInt("${key}_correct", itemProgress.correct)
            .putInt("${key}_wrong", itemProgress.wrong)
            .putInt("${key}_streak", itemProgress.streak)
            .putInt("${key}_level", itemProgress.level)
            .putInt("${key}_due", itemProgress.dueRound)
            .apply()
    }

    private fun progressKey(mode: LearningMode, item: LearningItem): String {
        return "${mode.storageKey}_${item.symbol.hashCode()}"
    }

    private fun promptFor(item: LearningItem): String {
        return when (selectedMode) {
            LearningMode.Hiragana,
            LearningMode.Katakana -> "Find: ${item.reading}"
            LearningMode.Kanji -> "Find: ${item.meaning}"
        }
    }

    private fun answerFor(item: LearningItem): String {
        return when (referenceMode) {
            LearningMode.Hiragana,
            LearningMode.Katakana -> item.reading
            LearningMode.Kanji -> item.meaning
        }
    }

    private fun itemsForMode(mode: LearningMode): List<LearningItem> {
        return when (mode) {
            LearningMode.Hiragana -> hiraganaItems
            LearningMode.Katakana -> katakanaItems
            LearningMode.Kanji -> kanjiItems
        }
    }

    private fun laneCenterX(lane: Int): Float {
        val laneWidth = width / laneCount.toFloat()
        return laneWidth * lane + laneWidth / 2f
    }

    private fun playerY(): Float = height * 0.82f

    private fun buildKanaItems(symbols: List<String>, readings: List<String>): List<LearningItem> {
        return symbols.mapIndexed { index, symbol ->
            val reading = readings.getOrElse(index) { "" }
            LearningItem(symbol, reading, reading)
        }
    }

    private fun buildMenuStream(source: List<String>): List<String> {
        return List(48) { index ->
            source[(index * 7 + index / 3).floorMod(source.size)]
        }
    }

    private fun drawWrappedCenteredText(canvas: Canvas, text: String, startY: Float, paint: Paint, maxWidth: Float) {
        if (text.isBlank()) return
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var line = ""
        words.forEach { word ->
            val candidate = if (line.isBlank()) word else "$line $word"
            if (paint.measureText(candidate) <= maxWidth) {
                line = candidate
            } else {
                if (line.isNotBlank()) lines.add(line)
                line = word
            }
        }
        if (line.isNotBlank()) lines.add(line)

        val lineHeight = 22f * resources.displayMetrics.density
        lines.forEachIndexed { index, wrappedLine ->
            canvas.drawText(wrappedLine, width / 2f, startY + index * lineHeight, paint)
        }
    }

    private fun Int.floorMod(divisor: Int): Int {
        val remainder = this % divisor
        return if (remainder < 0) remainder + divisor else remainder
    }
}
