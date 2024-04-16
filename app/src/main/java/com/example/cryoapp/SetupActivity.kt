package com.example.cryoapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ToggleButton
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.appcompat.widget.SwitchCompat

class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        val startButton = findViewById<AppCompatButton>(R.id.startButton)

        val timePerPlayerValue = findViewById<AppCompatEditText>(R.id.timePerPlayerValue)
        val addExtraTimeValue = findViewById<AppCompatEditText>(R.id.addExtraTimeValue)
        val frequencyAlertsValue = findViewById<AppCompatEditText>(R.id.frequencyAlertsValue)
        val numberPlayersValue = findViewById<AppCompatEditText>(R.id.numberPlayersValue)

        val increaseTimeOverMaxValue = findViewById<SwitchCompat>(R.id.increaseTimeOverMaxValue)
        val allowRevivePlayersValue = findViewById<SwitchCompat>(R.id.allowRevivePlayersValue)
        val allowFreezeValue = findViewById<SwitchCompat>(R.id.allowFreezeValue)

        startButton.setOnClickListener {

            //Create intent
            val intent = Intent(this, GameActivity::class.java)

            //Read all values and send it to GameActivity
            var value:String = timePerPlayerValue.getText().toString();
            intent.putExtra("INITIAL_TIME_PER_PLAYER", Integer.parseInt(value))

            value = addExtraTimeValue.getText().toString();
            intent.putExtra("ADD_EXTRA_TIME_VALUE", Integer.parseInt(value))

            value = frequencyAlertsValue.getText().toString();
            intent.putExtra("TIME_BETWEEN_ALERT", Integer.parseInt(value))

            value = numberPlayersValue.getText().toString();
            intent.putExtra("NUMBER_OF_PLAYERS", Integer.parseInt(value))

            var booleanValue:Boolean = increaseTimeOverMaxValue.isChecked();
            intent.putExtra("INCREASE_TIME_OVER_MAX_VALUE", booleanValue)

            booleanValue = allowRevivePlayersValue.isChecked();
            intent.putExtra("ALLOW_REVIVE_PLAYERS", booleanValue)

            booleanValue = allowFreezeValue.isChecked();
            intent.putExtra("ALLOW_FREEZE", booleanValue)

            //Start Game activy
            startActivity(intent)
        }
    }
}