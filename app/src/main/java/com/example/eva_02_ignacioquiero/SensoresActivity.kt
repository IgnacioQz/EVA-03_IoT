package com.example.eva_02_ignacioquiero

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt

class SensoresActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    // Views
    private lateinit var backButton: ImageView

    // GPS
    private lateinit var gpsStatusCard: MaterialCardView
    private lateinit var gpsStatusText: TextView
    private lateinit var gpsIconStatus: TextView
    private var lastLocation: Location? = null

    // WiFi
    private lateinit var wifiStatusCard: MaterialCardView
    private lateinit var wifiStatusText: TextView
    private lateinit var wifiIconStatus: TextView
    private lateinit var wifiNameText: TextView

    // Bluetooth
    private lateinit var bluetoothStatusCard: MaterialCardView
    private lateinit var bluetoothStatusText: TextView
    private lateinit var bluetoothIconStatus: TextView

    // Giroscopio
    private lateinit var gyroStatusCard: MaterialCardView
    private lateinit var gyroXText: TextView
    private lateinit var gyroYText: TextView
    private lateinit var gyroZText: TextView
    private var gyroValues = FloatArray(3)

    // Acelerómetro
    private lateinit var accelStatusCard: MaterialCardView
    private lateinit var accelXText: TextView
    private lateinit var accelYText: TextView
    private lateinit var accelZText: TextView
    private var accelValues = FloatArray(3)

    // Luz
    private lateinit var lightStatusCard: MaterialCardView
    private lateinit var lightValueText: TextView
    private var lightValue = 0f

    // Managers
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private var gyroscopeSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var lightSensor: Sensor? = null

    companion object {
        private const val LOCATION_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensores)

        initializeViews()
        setupListeners()
        initializeSensors()
        checkPermissions()
        updateAllSensors()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)

        // GPS
        gpsStatusCard = findViewById(R.id.gpsStatusCard)
        gpsStatusText = findViewById(R.id.gpsStatusText)
        gpsIconStatus = findViewById(R.id.gpsIconStatus)

        // WiFi
        wifiStatusCard = findViewById(R.id.wifiStatusCard)
        wifiStatusText = findViewById(R.id.wifiStatusText)
        wifiIconStatus = findViewById(R.id.wifiIconStatus)
        wifiNameText = findViewById(R.id.wifiNameText)

        // Bluetooth
        bluetoothStatusCard = findViewById(R.id.bluetoothStatusCard)
        bluetoothStatusText = findViewById(R.id.bluetoothStatusText)
        bluetoothIconStatus = findViewById(R.id.bluetoothIconStatus)

        // Giroscopio
        gyroStatusCard = findViewById(R.id.gyroStatusCard)
        gyroXText = findViewById(R.id.gyroXText)
        gyroYText = findViewById(R.id.gyroYText)
        gyroZText = findViewById(R.id.gyroZText)

        // Acelerómetro
        accelStatusCard = findViewById(R.id.accelStatusCard)
        accelXText = findViewById(R.id.accelXText)
        accelYText = findViewById(R.id.accelYText)
        accelZText = findViewById(R.id.accelZText)

        // Luz
        lightStatusCard = findViewById(R.id.lightStatusCard)
        lightValueText = findViewById(R.id.lightValueText)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        gpsStatusCard.setOnClickListener {
            showGPSDetails()
        }

        wifiStatusCard.setOnClickListener {
            showWiFiDetails()
        }

        bluetoothStatusCard.setOnClickListener {
            showBluetoothDetails()
        }

        gyroStatusCard.setOnClickListener {
            showGyroscopeDetails()
        }

        accelStatusCard.setOnClickListener {
            showAccelerometerDetails()
        }

        lightStatusCard.setOnClickListener {
            showLightDetails()
        }
    }

    private fun initializeSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (gyroscopeSensor == null) {
            gyroXText.text = "No disponible"
            gyroYText.text = "No disponible"
            gyroZText.text = "No disponible"
        }

        if (accelerometerSensor == null) {
            accelXText.text = "No disponible"
            accelYText.text = "No disponible"
            accelZText.text = "No disponible"
        }

        if (lightSensor == null) {
            lightValueText.text = "Sensor no disponible"
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_CODE
            )
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10f,
                    this
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateAllSensors() {
        checkGPSStatus()
        checkWiFiStatus()
        checkBluetoothStatus()
    }

    private fun checkGPSStatus() {
        try {
            val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (isGPSEnabled) {
                gpsStatusText.text = "GPS Activo"
                gpsIconStatus.text = "✅"
                gpsStatusCard.setCardBackgroundColor(getColor(R.color.success_light))
            } else {
                gpsStatusText.text = "GPS Desactivado"
                gpsIconStatus.text = "❌"
                gpsStatusCard.setCardBackgroundColor(getColor(R.color.error_light))
            }
        } catch (e: Exception) {
            gpsStatusText.text = "Error al verificar GPS"
            gpsIconStatus.text = "⚠️"
        }
    }

    private fun checkWiFiStatus() {
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo

                wifiStatusText.text = "WiFi Conectado"
                wifiIconStatus.text = "✅"
                wifiNameText.text = "Red: ${wifiInfo.ssid.replace("\"", "")}"
                wifiStatusCard.setCardBackgroundColor(getColor(R.color.success_light))
            } else {
                wifiStatusText.text = "WiFi Desconectado"
                wifiIconStatus.text = "❌"
                wifiNameText.text = "No conectado a ninguna red"
                wifiStatusCard.setCardBackgroundColor(getColor(R.color.error_light))
            }
        } catch (e: Exception) {
            wifiStatusText.text = "Error al verificar WiFi"
            wifiIconStatus.text = "⚠️"
            wifiNameText.text = "Error: ${e.message}"
        }
    }

    private fun checkBluetoothStatus() {
        try {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter

            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                bluetoothStatusText.text = "Bluetooth Activo"
                bluetoothIconStatus.text = "✅"
                bluetoothStatusCard.setCardBackgroundColor(getColor(R.color.success_light))
            } else {
                bluetoothStatusText.text = "Bluetooth Desactivado"
                bluetoothIconStatus.text = "❌"
                bluetoothStatusCard.setCardBackgroundColor(getColor(R.color.error_light))
            }
        } catch (e: Exception) {
            bluetoothStatusText.text = "Error al verificar Bluetooth"
            bluetoothIconStatus.text = "⚠️"
        }
    }

    // Diálogos de detalles
    private fun showGPSDetails() {
        val isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val message = buildString {
            append("📍 Estado del GPS\n\n")
            append("Estado: ${if (isEnabled) "✅ Activo" else "❌ Desactivado"}\n\n")

            if (lastLocation != null) {
                append("📌 Última ubicación conocida:\n")
                append("Latitud: ${String.format("%.6f", lastLocation!!.latitude)}°\n")
                append("Longitud: ${String.format("%.6f", lastLocation!!.longitude)}°\n")
                append("Precisión: ${lastLocation!!.accuracy.roundToInt()}m\n")
                append("Altitud: ${lastLocation!!.altitude.roundToInt()}m\n")
            } else {
                append("Sin ubicación disponible\n")
            }

            if (!isEnabled) {
                append("\n💡 Tip: Activa el GPS en ajustes del sistema")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Detalles GPS")
            .setMessage(message)
            .setPositiveButton("Actualizar") { dialog, _ ->
                checkGPSStatus()
                dialog.dismiss()
            }
            .setNeutralButton("Configurar") { dialog, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun showWiFiDetails() {
        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            val message = buildString {
                append("📶 Detalles de WiFi\n\n")

                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    append("Estado: ✅ Conectado\n\n")
                    append("🌐 Red: ${wifiInfo.ssid.replace("\"", "")}\n")
                    append("🔢 IP: ${intToIp(wifiInfo.ipAddress)}\n")
                    append("⚡ Velocidad: ${wifiInfo.linkSpeed} Mbps\n")
                    append("🔒 Seguridad: WPA/WPA2\n")
                } else {
                    append("Estado: ❌ Desconectado\n\n")
                    append("No hay conexión WiFi activa\n")
                    append("\n💡 Tip: Conecta a una red WiFi en ajustes")
                }
            }

            AlertDialog.Builder(this)
                .setTitle("Detalles WiFi")
                .setMessage(message)
                .setPositiveButton("Actualizar") { dialog, _ ->
                    checkWiFiStatus()
                    dialog.dismiss()
                }
                .setNeutralButton("Configurar") { dialog, _ ->
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                    dialog.dismiss()
                }
                .setNegativeButton("Cerrar", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al obtener detalles: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBluetoothDetails() {
        try {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter

            val message = buildString {
                append("🔵 Detalles de Bluetooth\n\n")

                if (bluetoothAdapter != null) {
                    append("Estado: ${if (bluetoothAdapter.isEnabled) "✅ Activo" else "❌ Desactivado"}\n\n")

                    if (bluetoothAdapter.isEnabled) {
                        append("📱 Nombre del dispositivo: ${bluetoothAdapter.name ?: "Sin nombre"}\n")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(
                                    this@SensoresActivity,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                val bondedDevices = bluetoothAdapter.bondedDevices
                                append("\n📲 Dispositivos emparejados: ${bondedDevices.size}\n")
                                bondedDevices.take(5).forEach { device ->
                                    append("  • ${device.name ?: "Desconocido"}\n")
                                }
                            }
                        }
                    } else {
                        append("\n💡 Tip: Activa Bluetooth en ajustes del sistema")
                    }
                } else {
                    append("⚠️ Bluetooth no disponible en este dispositivo")
                }
            }

            AlertDialog.Builder(this)
                .setTitle("Detalles Bluetooth")
                .setMessage(message)
                .setPositiveButton("Actualizar") { dialog, _ ->
                    checkBluetoothStatus()
                    dialog.dismiss()
                }
                .setNeutralButton("Configurar") { dialog, _ ->
                    startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    dialog.dismiss()
                }
                .setNegativeButton("Cerrar", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al obtener detalles: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showGyroscopeDetails() {
        val message = buildString {
            append("🔄 Detalles del Giroscopio\n\n")

            if (gyroscopeSensor != null) {
                append("Estado: ✅ Disponible\n\n")
                append("📊 Lecturas actuales:\n")
                append("Eje X: ${String.format("%.3f", gyroValues[0])} rad/s\n")
                append("Eje Y: ${String.format("%.3f", gyroValues[1])} rad/s\n")
                append("Eje Z: ${String.format("%.3f", gyroValues[2])} rad/s\n\n")

                append("💡 El giroscopio mide la velocidad de rotación del dispositivo")
            } else {
                append("⚠️ Giroscopio no disponible en este dispositivo")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Detalles Giroscopio")
            .setMessage(message)
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun showAccelerometerDetails() {
        val magnitude = kotlin.math.sqrt(
            accelValues[0] * accelValues[0] +
                    accelValues[1] * accelValues[1] +
                    accelValues[2] * accelValues[2]
        )

        val message = buildString {
            append("⚡ Detalles del Acelerómetro\n\n")

            if (accelerometerSensor != null) {
                append("Estado: ✅ Disponible\n\n")
                append("📊 Lecturas actuales:\n")
                append("Eje X: ${String.format("%.2f", accelValues[0])} m/s²\n")
                append("Eje Y: ${String.format("%.2f", accelValues[1])} m/s²\n")
                append("Eje Z: ${String.format("%.2f", accelValues[2])} m/s²\n")
                append("Magnitud: ${String.format("%.2f", magnitude)} m/s²\n\n")

                append("💡 El acelerómetro mide la aceleración incluyendo la gravedad (9.81 m/s²)")
            } else {
                append("⚠️ Acelerómetro no disponible en este dispositivo")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Detalles Acelerómetro")
            .setMessage(message)
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun showLightDetails() {
        val message = buildString {
            append("💡 Detalles del Sensor de Luz\n\n")

            if (lightSensor != null) {
                append("Estado: ✅ Disponible\n\n")
                append("📊 Lectura actual:\n")
                append("Luminosidad: ${lightValue.roundToInt()} lux\n\n")

                append("📏 Interpretación:\n")
                when {
                    lightValue < 1 -> append("🌑 Oscuridad total\n")
                    lightValue < 10 -> append("🌃 Muy oscuro\n")
                    lightValue < 50 -> append("🏠 Luz interior tenue\n")
                    lightValue < 400 -> append("💡 Luz interior normal\n")
                    lightValue < 1000 -> append("🏢 Oficina bien iluminada\n")
                    lightValue < 10000 -> append("☁️ Día nublado\n")
                    else -> append("☀️ Luz solar directa\n")
                }

                append("💡 El sensor de luz mide la intensidad luminosa ambiental")
            } else {
                append("⚠️ Sensor de luz no disponible en este dispositivo")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Detalles Sensor de Luz")
            .setMessage(message)
            .setPositiveButton("Entendido", null)
            .show()
    }

    // Utilidad para convertir IP
    private fun intToIp(ip: Int): String {
        return String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_GYROSCOPE -> {
                    gyroValues = it.values.clone()
                    gyroXText.text = "X: ${it.values[0].roundToInt()}°/s"
                    gyroYText.text = "Y: ${it.values[1].roundToInt()}°/s"
                    gyroZText.text = "Z: ${it.values[2].roundToInt()}°/s"
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    accelValues = it.values.clone()
                    accelXText.text = "X: ${String.format("%.1f", it.values[0])} m/s²"
                    accelYText.text = "Y: ${String.format("%.1f", it.values[1])} m/s²"
                    accelZText.text = "Z: ${String.format("%.1f", it.values[2])} m/s²"
                }
                Sensor.TYPE_LIGHT -> {
                    lightValue = it.values[0]
                    lightValueText.text = "${it.values[0].roundToInt()} lux"
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onLocationChanged(location: Location) {
        lastLocation = location
        checkGPSStatus()
    }

    override fun onResume() {
        super.onResume()
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        updateAllSensors()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
                checkGPSStatus()
                Toast.makeText(this, "✅ Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "❌ Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}