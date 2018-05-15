package edu.uw.ischool.awty

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import edu.uw.ischool.awty.MainActivity.Companion.PHONE
import edu.uw.ischool.awty.MainActivity.Companion.TEXT

class MainActivity : AppCompatActivity() {

    companion object {
        val PHONE = "phone"
        val TEXT = "text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn  = findViewById<Button>(R.id.button) as Button
        val phone = findViewById(R.id.phone) as EditText
        val text = findViewById(R.id.text) as EditText
        val times = findViewById(R.id.times) as EditText

        var error = ""

        fun isTextEmpty(input : EditText) : Boolean {
            if (input.getText().length < 1) {
              return true
            }
            return false
        }
        
        fun handlePhone(phone: String) :String {
            if(phone.length >= 10) {
               return  "(" + phone.substring(0, 3) + ") " + phone.substring(3, 6) + "-" + phone.substring(6, 10)
            } else {
                return PhoneNumberUtils.formatNumber(phone)
            }
        }

        fun isValid() : Boolean {
            if (isTextEmpty(phone) || isTextEmpty(text) || isTextEmpty(times)) {
                error = "Can't have empty fields"
                return false
                // uncomment both 'else if' if you want to
                // text with an Emulator Port Number
            } else if (phone.text.length < 7 || phone.text.length > 10) {
                error = "Phone number is invalid"
                return false
            } else if (times.text.toString().toInt() < 1) {
                error = "Frequency can not be less than 1 minute"
                return false
            }else{
                return true
            }
        }

            btn.setOnClickListener({
                if(isValid()) {
                    var  phoneNumber = phone.text.toString()
                    phoneNumber = handlePhone(phoneNumber)
                    var message = text.text.toString()
                    var repetition = times.text.toString().toInt()
                    val intent = Intent("edu.uw.ischool.awty.Alarm")
                    intent.putExtra(PHONE, phoneNumber)
                    intent.putExtra(TEXT,message)
                    val intentFilter = IntentFilter("edu.uw.ischool.awty.Alarm")
                    registerReceiver(AlarmReceiver(), intentFilter)
                    val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (btn.text == "Start") {
                        btn.text = "Stop"
                        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime() + (repetition * 60000).toLong(), ((repetition * 60000).toLong()), pendingIntent)
                    } else {
                        btn.text = "Start"
                        alarmManager.cancel(pendingIntent)
                        pendingIntent.cancel()
                    }
                }else {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(AlarmReceiver())
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
       val phone = p1!!.getStringExtra(PHONE)
        val message = p1!!.getStringExtra(TEXT)
        Log.i("Message",phone + ": " + message )
            var sms = SmsManager.getDefault()
            sms.sendTextMessage(phone, null, message, null, null)
      //  }
    }
}
