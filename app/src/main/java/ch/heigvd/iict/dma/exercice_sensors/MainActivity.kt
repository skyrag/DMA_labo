package ch.heigvd.iict.dma.exercice_sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ch.heigvd.iict.dma.exercice_sensors.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var mSensorManager: SensorManager
    private var mGyroscope: Sensor? = null

    private var mAccel: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // depuis android 15 (sdk 35), le mode edge2edge doit être activé
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // comme edge2edge est activé, l'application doit garder un espace suffisant pour la barre système
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // la barre d'action doit être définie dans le layout, on la lie à l'activité
        setSupportActionBar(binding.toolbar)

        // we manage sensors
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if(mGyroscope == null) {
            Toast.makeText(this, "Not all required sensors are available", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    private var mRotationMatrix = FloatArray(16)
    override fun onSensorChanged(event: SensorEvent) {
        // TODO listen to sensor and get new coords
        val axisX = event.values[0]
        val axisY = event.values[1]
        binding.gameView.onGyroscopeChanged(axisX, axisY, event.timestamp)
        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE ->
                binding.gameView.onGyroscopeChanged(event.values[0], event.values[1], event.timestamp)
            Sensor.TYPE_ACCELEROMETER ->
                binding.gameView.onAccelChanged(event.values[0], event.values[1])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "onAccuracyChanged $sensor $accuracy")
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

}