package com.example.cryoapp

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.Timer
import java.util.TimerTask

class PlayerCountdown(button: AppCompatButton,
                      initialTime: Int?, increaseTimeOverMaxValue: Boolean?,
                      color: Int, rootItem: ConstraintLayout,
                      mainVibrator: Vibrator,
                      clickSound: MediaPlayer,
                      disableFunction: () -> Unit)
{
    private var button_: AppCompatButton
    private var root_: ConstraintLayout
    private var currentTime_: Int
    private var maxTime_: Int
    private var increaseTimeOverMaxValue_: Boolean
    private var color_: Int
    private var working_: Boolean
    private var mainVibrator_: Vibrator
    private var clickSound_: MediaPlayer

    init {
        button_ = button
        root_ = rootItem
        currentTime_= initialTime!!
        maxTime_ = initialTime!!
        increaseTimeOverMaxValue_ = increaseTimeOverMaxValue!!
        color_ = color
        working_ = false
        mainVibrator_ = mainVibrator
        clickSound_ = clickSound
        updateText()

        button_.setOnClickListener {
            if (!isDead())
            {
                clickSound.start()
                mainVibrator_.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                disableFunction()
                setActive(true)
                root_.setBackgroundResource(getColor())
            }
        }
    }

    fun setVisibility(visible: Boolean)
    {
        if (visible)
        {
            button_.visibility = View.VISIBLE
        }
        else
        {
            button_.visibility = View.GONE
        }
    }

    fun isVisible(): Boolean
    {
        return (button_.visibility == View.VISIBLE)
    }

    fun setActive(active: Boolean)
    {
        working_ = active
    }

    fun isActive(): Boolean
    {
        return working_
    }

    fun isDead(): Boolean
    {
        return (currentTime_ == 0)
    }

    fun die()
    {
        currentTime_ = 0
    }

    fun increaseTime(increaseValue: Int?)
    {
        var tmpTime = currentTime_ + increaseValue!!

        if (tmpTime <= maxTime_)
        {
            currentTime_ = tmpTime
        }
        else
        {
            if (increaseTimeOverMaxValue_)
            {
                currentTime_ = tmpTime;
            }
            else
            {
                currentTime_ = maxTime_;
            }
        }

        updateText()
    }

    fun decreaseTime(decreaseValue: Int)
    {
        if (working_)
        {
            if (decreaseValue > currentTime_)
            {
                currentTime_ = 0
            }
            else
            {
                currentTime_ = currentTime_ - decreaseValue
            }
            updateText()
        }
    }

    fun getTime(): Int
    {
        return currentTime_
    }

    fun click(): Boolean
    {
        return button_.callOnClick()
    }

    fun getColor(): Int {
        return color_
    }

    private fun updateText()
    {

        val minutes = currentTime_ / 60
        val seconds = currentTime_ % 60

        var timeString: String

        if (minutes < 10)
        {
            timeString = "0$minutes:"
        }
        else
        {
            timeString = "$minutes:"
        }

        if (seconds < 10)
        {
            timeString = timeString + "0$seconds"
        }
        else
        {
            timeString = timeString + "$seconds"
        }

        button_.text = timeString
    }
}

class Alert(textView: TextView, rootItem: ConstraintLayout)
{
    private var textView_: TextView = textView
    private var root_ = rootItem
    private var gameOver_ = false

    fun showAlert()
    {
        if (!gameOver_)
        {
            textView_.setText("¡¡¡ ALERT !!!")
            textView_.visibility = View.VISIBLE
        }
    }

    fun hideAlert()
    {
        if (!gameOver_)
        {
            textView_.setText("")
            textView_.visibility = View.GONE
        }
    }

    fun gameOver()
    {
        gameOver_ = true
        textView_.setText("GAME OVER")
        textView_.visibility = View.VISIBLE
        root_.setBackgroundResource(R.color.white)
    }
}


class GameActivity : AppCompatActivity() {

    override fun onBackPressed() {
        super.onBackPressed()

        disableAllPlayers()
        gameRunning_ = false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Parameters
        val bundle = intent.extras
        timePerPlayer_ = bundle?.getInt("INITIAL_TIME_PER_PLAYER")?.or(0)
        extraTimeValue_ = bundle?.getInt("ADD_EXTRA_TIME_VALUE")?.or(0)
        timeBetweenAlert_ = bundle?.getInt("TIME_BETWEEN_ALERT")?.or(0)
        numberOfPlayers_ = bundle?.getInt("NUMBER_OF_PLAYERS")?.or(0)
        increaseTimeOverMaxValue_ = bundle?.getBoolean("INCREASE_TIME_OVER_MAX_VALUE")
        allowRevivePlayersValue_ = bundle?.getBoolean("ALLOW_REVIVE_PLAYERS")
        allowFreeze_ = bundle?.getBoolean("ALLOW_FREEZE")

        // Vibrator
        mainVibrator_ = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibrationService_ =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrationService_.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        // Sounds
        clickSound_ = MediaPlayer.create(this, R.raw.pop_up)
        collisionSound_ = MediaPlayer.create(this, R.raw.alert_collision)

        // Root element
        rootItem_ = findViewById<ConstraintLayout>(R.id.root)

        // Buttons for players
        redPlayer_ = PlayerCountdown(findViewById<AppCompatButton>(R.id.redButton),
                        timePerPlayer_, increaseTimeOverMaxValue_,
                        R.color.red, rootItem_, mainVibrator_, clickSound_, ::disableAllPlayers)
        bluePlayer_ = PlayerCountdown(findViewById<AppCompatButton>(R.id.blueButton),
                        timePerPlayer_, increaseTimeOverMaxValue_,
                        R.color.blue, rootItem_, mainVibrator_, clickSound_, ::disableAllPlayers)
        yellowPlayer_ = PlayerCountdown(findViewById<AppCompatButton>(R.id.yellowButton),
                        timePerPlayer_, increaseTimeOverMaxValue_,
                        R.color.yellow, rootItem_, mainVibrator_, clickSound_, ::disableAllPlayers)
        orangePlayer_ = PlayerCountdown(findViewById<AppCompatButton>(R.id.orangeButton),
                        timePerPlayer_, increaseTimeOverMaxValue_,
                        R.color.orange, rootItem_, mainVibrator_, clickSound_, ::disableAllPlayers)
        purplePlayer_ = PlayerCountdown(findViewById<AppCompatButton>(R.id.purpleButton),
                        timePerPlayer_, increaseTimeOverMaxValue_,
                        R.color.purple, rootItem_, mainVibrator_, clickSound_, ::disableAllPlayers)
        greyPlayer_ = PlayerCountdown(findViewById<AppCompatButton>(R.id.greyButton),
                        timePerPlayer_, increaseTimeOverMaxValue_,
                        R.color.grey, rootItem_, mainVibrator_, clickSound_, ::disableAllPlayers)


        playersMap_.put("Red", redPlayer_)
        playersMap_.put("Blue", bluePlayer_)
        playersMap_.put("Yellow", yellowPlayer_)
        playersMap_.put("Orange", orangePlayer_)
        playersMap_.put("Purple", purplePlayer_)
        playersMap_.put("Grey", greyPlayer_)

        assignNumberOfPlayers(numberOfPlayers_)

        // Alert text
        alert_ = Alert(findViewById<TextView>(R.id.textAlert), rootItem_)
        alert_.hideAlert()

        // Button for freeze
        freezeButton_ = findViewById<AppCompatButton>(R.id.freezeButton);
        if (allowFreeze_!!)
        {
            freezeButton_.setOnTouchListener { _, motionEvent  ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        //when user touch down
                        stopTicking_ = true
                        freezeButton_.setBackgroundResource(R.color.freeze_on)
                    }
                    MotionEvent.ACTION_UP -> {
                        //when user touch release
                        stopTicking_ = false
                        freezeButton_.setBackgroundResource(R.color.freeze_off)
                    }
                }
                true
            }
        }
        else
        {
            freezeButton_.isEnabled = false
        }


        // Button to add extra time
        addTimeButton_ = findViewById<AppCompatButton>(R.id.addTimeButton)
        addTimeButton_.setOnClickListener {


            var minorPlayerKey = ""
            var minorTimeValue = 0

            mainVibrator_.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))

            for (entry in playersMap_.entries.iterator()) {

                // Skip non-used players
                if (!entry.value.isVisible())
                {
                    continue
                }

                // Skip dead players if the setting is requested
                if (!allowRevivePlayersValue_!! && entry.value.isDead())
                {
                    continue
                }

                if (minorPlayerKey.isEmpty())
                {
                    minorPlayerKey = entry.key
                    minorTimeValue = entry.value.getTime()
                }
                else
                {
                    if (entry.value.getTime() < minorTimeValue)
                    {
                        minorPlayerKey = entry.key
                        minorTimeValue = entry.value.getTime()
                    }
                }
            }

            playersMap_.get(minorPlayerKey)!!.increaseTime(extraTimeValue_)

            var timeBonusSound: MediaPlayer = MediaPlayer.create(this, R.raw.shockwave);
            timeBonusSound.start()
        }

        // Set TIMER (Tick every second)
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

                if (stopTicking_) return

                if (isGameRunning())
                {
                    playersMap_.forEach { entry ->
                        entry.value.decreaseTime(1)
                    }
                    checkGameOver()
                    checkDeadAndJumpToAnotherPlayer()

                    secondsAppRunning_ = secondsAppRunning_ + 1
                    if (secondsAppRunning_ % timeBetweenAlert_!! == 0)
                    {
                        runOnUiThread {
                            alert_.showAlert()
                            collisionSound_.start()
                        }
                    }
                    else if (secondsAppRunning_ % (timeBetweenAlert_!! + 3) == 0)
                    {
                        runOnUiThread {
                            alert_.hideAlert()
                            collisionSound_.pause()
                            collisionSound_.seekTo(0)
                        }
                        secondsAppRunning_ = 0
                    }
                }
            }
        }, 0, 1000) //put here time 1000 milliseconds=1 seconds

    }


    fun disableAllPlayers()
    {
        playersMap_.forEach { entry -> entry.value.setActive(false) }
    }

    fun playCollisionSound()
    {
        collisionSound_.start()
    }

    private fun assignNumberOfPlayers(nPlayers: Int?)
    {

        val numPlayers = nPlayers!!

        if (numPlayers < 1) {
            redPlayer_.setVisibility(false)
            redPlayer_.die()
        }
        if (numPlayers < 2)
        {
            bluePlayer_.setVisibility(false)
            bluePlayer_.die()
        }
        if (numPlayers < 3)
        {
            yellowPlayer_.setVisibility(false)
            yellowPlayer_.die()
        }
        if (numPlayers < 4)
        {
            orangePlayer_.setVisibility(false)
            orangePlayer_.die()
        }
        if (numPlayers < 5)
        {
            purplePlayer_.setVisibility(false)
            purplePlayer_.die()
        }
        if (numPlayers < 6)
        {
            greyPlayer_.setVisibility(false)
            greyPlayer_.die()
        }
    }

    private fun isGameRunning(): Boolean
    {
        if (!gameRunning_)
        {
            for (entry in playersMap_.entries.iterator())
            {
                if (entry.value.isActive())
                {
                    gameRunning_ = true
                    return gameRunning_
                }
            }
            return false
        }
        else
        {
            return true
        }
    }

    private fun checkGameOver()
    {
        var gameOver = true

        for (entry in playersMap_.entries.iterator())
        {
            if (!entry.value.isDead())
            {
                gameOver = false;
                break;
            }
        }

        if (gameOver)
        {
            runOnUiThread {
                alert_.gameOver()
                addTimeButton_.visibility = View.GONE
                var gameOverSound: MediaPlayer = MediaPlayer.create(this, R.raw.game_over);
                gameOverSound.start()
                gameRunning_ = false
            }
        }


    }

    private fun checkDeadAndJumpToAnotherPlayer()
    {
        var activeIsDead = false

        for (entry in playersMap_.entries.iterator()) {
            if (entry.value.isActive())
            {
                activeIsDead = entry.value.isDead()
                if (activeIsDead)
                {
                    entry.value.setActive(false)
                }
                break
            }
        }

        if (activeIsDead)
        {
            for (entry in playersMap_.entries.iterator()) {
                if (!entry.value.isDead())
                {
                    entry.value.click()
                    break
                }
            }
        }
    }


    // PRIVATE ATTRIBUTES
    private var timePerPlayer_: Int? = 0
    private var extraTimeValue_: Int? = 0
    private var timeBetweenAlert_: Int? = 0
    private var numberOfPlayers_: Int? = 0
    private var increaseTimeOverMaxValue_: Boolean? = true
    private var allowRevivePlayersValue_: Boolean? = true
    private var allowFreeze_: Boolean? = true

    private lateinit var vibrationService_: VibratorManager
    private lateinit var mainVibrator_: Vibrator

    private lateinit var redPlayer_: PlayerCountdown
    private lateinit var bluePlayer_: PlayerCountdown
    private lateinit var yellowPlayer_: PlayerCountdown
    private lateinit var orangePlayer_: PlayerCountdown
    private lateinit var purplePlayer_: PlayerCountdown
    private lateinit var greyPlayer_: PlayerCountdown
    private var playersMap_: kotlin.collections.MutableMap<String, PlayerCountdown> = mutableMapOf()

    private lateinit var rootItem_: ConstraintLayout

    private lateinit var alert_: Alert
    private lateinit var addTimeButton_: AppCompatButton
    private lateinit var freezeButton_: AppCompatButton

    private lateinit var collisionSound_: MediaPlayer
    private lateinit var clickSound_: MediaPlayer

    private var gameRunning_: Boolean = false
    private var stopTicking_: Boolean = false
    private var secondsAppRunning_: Int = 0
}