package com.example.brick_braker_v_2

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    //non null properties
    private lateinit var scoreText: TextView
    private lateinit var paddle: View
    private lateinit var ball: View
    private lateinit var brickContainer: LinearLayout
    private lateinit var newGameButton: Button

    private var ballX = 0f
    private var ballY = 0f
    private var ballSpeedX = 0f
    private var ballSpeedY = 0f
    private var paddleX = 0f
    private var score = 0
    private var lives = 3
    private var isBallLaunched = false
    private var highScore = 0

    private val brickRows = 9
    private val brickColumns = 10
    private val brickWidth = 100
    private val brickHeight = 40
    private val brickMargin = 4

    private lateinit var sharedPreferences: SharedPreferences

    //call this function when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize views
        scoreText = findViewById(R.id.scoreText)
        paddle = findViewById(R.id.paddle)
        ball = findViewById(R.id.ball)
        brickContainer = findViewById(R.id.brickContainer)
        newGameButton = findViewById(R.id.newgame)

        //Retrieves the high score from shared preferences
        sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        highScore = sharedPreferences.getInt("highScore", 0)

        //restart game
        newGameButton.setOnClickListener {
            restartGame()
        }

        startGame()
    }

    private fun startGame() {
        //Sets up the game by initializing bricks
        initializeBricks()
        start()
        //hiding the new game button
        newGameButton.visibility = View.INVISIBLE
    }

    private fun restartGame() {
        resetGameState()
        startGame()
    }

    private fun resetGameState() {
        score = 0
        lives = 3
        scoreText.text = "Score: $score"
        brickContainer.removeAllViews()
    }

    private fun initializeBricks() {
        val brickWidthWithMargin = (brickWidth + brickMargin).toInt()

        //create rows of bricks based on brickRows
        for (row in 0 until brickRows) {
            //Creates a new LinearLayout instance to hold bricks in a row
            val rowLayout = LinearLayout(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.layoutParams = params

            //create individual bricks within each row based on brick columns
            for (col in 0 until brickColumns) {
                val brick = View(this)
                //create layout parameter on brick
                val brickParams = LinearLayout.LayoutParams(brickWidth, brickHeight)
                brickParams.setMargins(brickMargin, brickMargin, brickMargin, brickMargin)
                brick.layoutParams = brickParams
                //set background resource for brick
                brick.setBackgroundResource(R.drawable.ic_launcher_background)
                rowLayout.addView(brick)
            }

            brickContainer.addView(rowLayout)
        }
    }

    private fun moveBall() {
        //move ball base on speed
        ballX += ballSpeedX
        ballY += ballSpeedY

        ball.x = ballX
        ball.y = ballY
    }

    private fun movePaddle(x: Float) {
        paddleX = x - paddle.width / 2
        paddle.x = paddleX
    }

    //accessibility when handling touch events
    @SuppressLint("ClickableViewAccessibility")
    private fun checkCollision() {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        if (ballX <= 0 || ballX + ball.width >= screenWidth) {
            ballSpeedX *= -1
        }

        if (ballY <= 0) {
            ballSpeedY *= -1
        }

        //Handles ball-paddle collisions by reversing the ball's vertical direction if it hits the paddle, increments the score, and updates the score text.
        if (ballY + ball.height >= paddle.y && ballY + ball.height <= paddle.y + paddle.height
            && ballX + ball.width >= paddle.x && ballX <= paddle.x + paddle.width
        ) {
            ballSpeedY *= -1
            score++
            scoreText.text = "Score: $score"
        }

        if (score >= 100) {
            // Reset the high score
            highScore = 0
            val editor = sharedPreferences.edit()
            editor.putInt("highScore", highScore)
            editor.apply()
            // Reset the score
            score = 0
            scoreText.text = "Score: $score"
        }

        //Handles ball-floor collisions by decrementing the number of lives
        if (ballY + ball.height >= screenHeight) {
            lives--
            if (lives > 0 ) {
                Toast.makeText(this, "$lives balls left ", Toast.LENGTH_SHORT).show()
            }

            if (lives <= 0) {
                gameOver()
            } else {
                resetBallPosition()
                start()
            }
        }

        //check ball brick collisons
        for (row in 0 until brickRows) {
            val rowLayout = brickContainer.getChildAt(row) as LinearLayout

            val rowTop = rowLayout.y + brickContainer.y
            val rowBottom = rowTop + rowLayout.height

            for (col in 0 until brickColumns) {
                val brick = rowLayout.getChildAt(col) as View

                if (brick.visibility == View.VISIBLE) {
                    val brickLeft = brick.x + rowLayout.x
                    val brickRight = brickLeft + brick.width
                    val brickTop = brick.y + rowTop
                    val brickBottom = brickTop + brick.height

                    if (ballX + ball.width >= brickLeft && ballX <= brickRight
                        && ballY + ball.height >= brickTop && ballY <= brickBottom
                    ) {
                        brick.visibility = View.INVISIBLE
                        ballSpeedY *= -1
                        score++
                        scoreText.text = "Score: $score"
                        return
                    }
                }
            }
        }

        if (ballY + ball.height >= screenHeight - 100) {
            lives--
            if (lives > 0 ) {
                Toast.makeText(this, "$lives balls left ", Toast.LENGTH_SHORT).show()
            }

            if (lives <= 0) {
                gameOver()
            } else {
                resetBallPosition()
                start()
            }
        }
    }

    private fun resetBallPosition() {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        ballX = screenWidth / 2 - ball.width / 2
        ballY = screenHeight / 2 - ball.height / 2 + 525

        ball.x = ballX
        ball.y = ballY

        // Set the ball's speed to a fixed value in pixels per frame
        ballSpeedX = 5f
        ballSpeedY = -5f

        paddleX = screenWidth / 2 - paddle.width / 2
        paddle.x = paddleX
    }



    private fun gameOver() {
        // Display game over message
        val gameOverMessage = "Game Over\nScore: $score\nHigh Score: $highScore"
        scoreText.text = gameOverMessage
        newGameButton.visibility = View.VISIBLE

        // Update high score if current score is higher
        if (score > highScore) {
            highScore = score
            val editor = sharedPreferences.edit()
            editor.putInt("highScore", highScore)
            editor.apply()
        }
    }

    //touch event move padle
    @SuppressLint("ClickableViewAccessibility")
    private fun movepaddle() {
        paddle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    movePaddle(event.rawX)
                }
            }
            true
        }
    }

    private fun start() {
        movepaddle()
        val displayMetrics = resources.displayMetrics

        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        paddleX = screenWidth / 2 - paddle.width / 2
        paddle.x = paddleX

        ballX = screenWidth / 2 - ball.width / 2
        ballY = screenHeight / 2 - ball.height / 2

        val ballSpeed = 7f // Increased speed

        ballSpeedX = ballSpeed
        ballSpeedY = -ballSpeed // Negative value to move upwards

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = Long.MAX_VALUE
        animator.interpolator = LinearInterpolator()
        //animation loop continuously updates the ball's position and checks for collisions using the moveBall() and checkCollision() functions
        animator.addUpdateListener { animation ->
            moveBall()
            checkCollision()
        }
        animator.start()
    }


}
