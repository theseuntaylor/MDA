package com.theseuntaylor.mda

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.util.Calendar
import kotlin.math.sqrt

class Accelerometer(context: Context) {

    private var listener: Listener? = null
    private var sensorManager: SensorManager
    private var sensor: Sensor?
    private var sensorEventListener: SensorEventListener
    private var endTime: Long? = null

    private val CSV_FILE_PATH = "./result.csv"

    interface Listener {
        fun onTranslation(tx: Float, ty: Float, tz: Float)
    }

    fun setListener(l: Listener) {
        listener = l
    }

    init {
        endTime = System.currentTimeMillis() * 3601 * 1000

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorEventListener = object :  SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val tx = event?.values?.get(0) ?: -10f
                val ty = event?.values?.get(1) ?: -10f
                val tz = event?.values?.get(2) ?: -10f

                if (event != null) {
                    listener?.onTranslation(
                        tx,
                        ty,
                        tz
                    )

                    val tx_squared = tx * tx
                    val ty_squared = ty * ty
                    val tz_squared = tz * tz
                    val acceleration = sqrt((tx_squared + ty_squared + tz_squared).toDouble())

                    // want to write to csv file while the time the app has been started is less than 1 hour.
                    // writeToCsv(tx, ty, tz, acceleration)

                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.e(javaClass.simpleName, sensor?.name.toString())
            }
        }
    }

    fun register() {
        sensorManager.registerListener(
            sensorEventListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun unregister() {
        sensorManager.unregisterListener(sensorEventListener)
    }

    private fun writeToCsv(tx: Float, ty: Float, tz: Float, acceleration: Double) {
        val file = File(CSV_FILE_PATH)
        try {
            // create FileWriter object with file as parameter
            val outputfile = FileWriter(file)

            // create CSVWriter object filewriter object as parameter
            val writer = CSVWriter(
                outputfile, CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END
            )

            // adding header to csv
            val header = arrayOf("Time", "tx", "ty", "tz", "acceleration")
            writer.writeNext(header)
            while (System.currentTimeMillis() < endTime!!) {
                val data = arrayOf(
                    Calendar.getInstance().time.toString(),
                    tx.toString(),
                    ty.toString(),
                    tz.toString(),
                    acceleration.toString()
                )
                writer.writeNext(data)
            }

            // closing writer connection
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}