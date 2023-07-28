package com.theseuntaylor.mda

//package com.theseuntaylor.mda
//
//import android.content.Context
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.util.Log
//
//class Gyroscope(context: Context) {
//
//    private lateinit var listener: Listener
//    private var sensorManager: SensorManager
//    private var sensor: Sensor
//    private var sensorEventListener: SensorEventListener
//
//    interface Listener {
//        fun onRotation(rx: Float, ry: Float, rz: Float)
//    }
//
//    fun setListener(l: Listener) {
//        listener = l
//    }
//
//    init {
//        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
//        sensorEventListener = object : SensorEventListener {
//            override fun onSensorChanged(event: SensorEvent?) {
//                if (event != null) {
//                    listener.onRotation(
//                        event.values[0],
//                        event.values[1],
//                        event.values[2]
//                    )
//                }
//            }
//
//            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//                Log.e("GYROSCOPE", "OnAccuracy called!")
//            }
//        }
//    }
//
//    fun register() {
//        sensorManager.registerListener(
//            sensorEventListener,
//            sensor,
//            SensorManager.SENSOR_DELAY_NORMAL
//        )
//    }
//
//    fun unregister() {
//        sensorManager.unregisterListener(sensorEventListener)
//    }
//}