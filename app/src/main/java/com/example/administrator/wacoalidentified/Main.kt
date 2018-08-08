package com.example.administrator.wacoalidentified

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v4.util.DebugUtils
import android.support.v7.app.AlertDialog
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationRequest
import java.security.Permission
import java.util.*


class Main : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    var googleApiClient : GoogleApiClient? = null

    lateinit var locationRequest : LocationRequest

    lateinit var locationSettingRequest : LocationSettingsRequest

    var currentLocation : Location? = null

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var locationManager: LocationManager? = null

    override fun onConnected(p0: Bundle?) {

//        this.createLocationRequest()

//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                return
//            }
//        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // ...
        startLocationUpdates()

        var fusedLocationProviderClient :
                FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        Log.i("RESULT_II", location.latitude.toString())
                    }
                })
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(p0: Location?) {
        if (p0 != null) {
//            Log.i("LOCATION_UPDATED", p0.latitude.toString())

            val metre = meterDistanceBetweenPoints(p0.latitude.toFloat(), p0.longitude.toFloat(), 13.72F, 100.52F)

            message.setText("Lat" + p0.latitude.toString() + " Long" + p0.longitude.toString() + " Difference:" + metre)
        }
    }

    private fun initGoogleAPIClient(): Boolean {
//        this.googleApiClient = GoogleApiClient.Builder(applicationContext)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build()

//        this.fusedLocationClient = getFusedLocationProviderClient(this)
        return true

//        if (ContextCompat.checkSelfPermission(this,
//            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            return true
//        } else {
//            val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
//            ActivityCompat.requestPermissions(this, permissions, 0)
//            return false
//        }

//        this.fusedLocationClient!!.getLastLocation()
//                .addOnSuccessListener(OnSuccessListener<Location> { location ->
//                    // GPS location can be null if GPS is switched off
//                    if (location != null) {
//                        onLocationChanged(location)
//                    } else {
//                        Log.i("LOCATIONS", "LOCATION NURUPO")
//                    }
//                })
//                .addOnFailureListener(OnFailureListener { e ->
//                    Log.i("MapDemoActivity", "Error trying to get last GPS location")
//                    e.printStackTrace()
//                })

//                GoogleApiClient.Builder(applicationContext)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build()
    }

    private fun createLocationRequest() {
//        locationRequest = LocationRequest()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(10000)

        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun createLocationSettings() {
        val builder : LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        if (this.locationRequest != null) {
            builder.addLocationRequest(locationRequest)
            this.locationSettingRequest = builder.build()
        }
    }

    private fun updateUI() {
        // Location Update UI
        if (currentLocation != null) {
            Log.i("LOCATION_UPDATED ", String.format("Latitude:%f Longitude:%f", currentLocation?.latitude, currentLocation?.longitude))
            message.setText(String.format("Latitude:%f Longitude:%f", currentLocation?.latitude, currentLocation?.longitude))
        } else {
            Log.i("LOCATION_UPDATED ", "NURUPO")
        }
    }

    private fun isLocationEnabled(context : Context) : Boolean {
        var locationMode: Int = 0
        var locationProviders: String?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
                return locationMode != Settings.Secure.LOCATION_MODE_OFF
            } catch(e: Settings.SettingNotFoundException) {
                e.printStackTrace()
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF
        } else {
            locationProviders = Settings.Secure.getString(context.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
            return locationProviders != null && locationProviders.isNotEmpty()
        }
    }

    private lateinit var locationCallback: LocationCallback

    protected fun startLocationUpdates() {

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000)
                .setFastestInterval(2000)

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                locationRequest, this)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    // # Life Cycle
    //@Throws(SecurityException::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        val button : Button = findViewById(R.id.geoGetButton) as Button
        button.setOnClickListener {
            try {
                startLocationUpdates()
            }
            catch (e : SecurityException) {
            }
        }

        checkLocation()

//        locationRequest = LocationRequest()
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//        locationRequest.setInterval(5000)
//        locationRequest.setFastestInterval(5000)
//
//        initGoogleAPIClient()

//        createLocationRequest()
//        createLocationSettings()

//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult?) {
//                locationResult ?: return
//                for (location in locationResult.locations){
//                    // Update UI with location data
//                    // ...
//                    Log.i("LOCATION_RESULT", location.latitude.toString()+" "+location.longitude.toShort())
//                }
//            }
//        }
    }

    private fun checkLocation(): Boolean {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled()
    }

    private fun isLocationEnabled(): Boolean {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager != null)
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        else return false
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt -> })
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        this.googleApiClient?.connect()
    }

    override fun onResume() {
        super.onResume()

//        if (requestingLocationUpdates)
//            startLocationUpdates()


//        if (isLocationEnabled(this)) {
//            if (googleApiClient != null && googleApiClient!!.isConnected()) {
//                startLocationUpdates()
//            }
//        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        if (fusedLocationClient != null) {
//            fusedLocationClient!!.removeLocationUpdates(locationCallback)
        }
    }

    private fun meterDistanceBetweenPoints(lat_a: Float, lng_a: Float, lat_b: Float, lng_b: Float): Double {
        val pk = (180f / Math.PI).toFloat()

        val a1 = lat_a / pk
        val a2 = lng_a / pk
        val b1 = lat_b / pk
        val b2 = lng_b / pk

        val t1 = Math.cos(a1.toDouble()) * Math.cos(a2.toDouble()) * Math.cos(b1.toDouble()) * Math.cos(b2.toDouble())
        val t2 = Math.cos(a1.toDouble()) * Math.sin(a2.toDouble()) * Math.cos(b1.toDouble()) * Math.sin(b2.toDouble())
        val t3 = Math.sin(a1.toDouble()) * Math.sin(b1.toDouble())
        val tt = Math.acos(t1 + t2 + t3)

        return 6366000 * tt
    }
}
