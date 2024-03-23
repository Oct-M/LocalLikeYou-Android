package com.example.lly_004

//Default Imports
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lly_004.ui.theme.LLY_004Theme

/*Added MapBox Imports*/
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.CameraOptions

/*Imports used for permission handling*/
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    //Variables used for permission handling
    private var fusedLocationProvider: FusedLocationProviderClient? = null //fusedLocationProvider used to communicate with App Store Api to get user location
    private var locationRequest: LocationRequest = LocationRequest.create().apply { //locationRequest
        interval = 20
        fastestInterval = 10
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime = 60
    }
    private var locationCallback: LocationCallback = object : LocationCallback() { //Store result from location request
        override fun onLocationResult(locationResult: LocationResult){
            val locationList = locationResult.locations
            if(locationList.isNotEmpty()){
                val location = locationList.last()
                Toast.makeText(
                    this@MainActivity,
                    "Got Location: " + location.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    //onCreate initializes the main activity for the Local Like You app
    @OptIn(MapboxExperimental::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LLY_004Theme {
            }

            fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
            checkLocationPermission()

            MapboxMap(
                Modifier.fillMaxSize(),
                mapInitOptionsFactory = { context ->
                    MapInitOptions(
                        context = context,
                        styleUri = "mapbox://styles/locallikeyou/clo61gomh003y01r70gk2784w",
                        cameraOptions = CameraOptions.Builder()
                            .center(Point.fromLngLat(47.6061, 122.3328))
                            .zoom(12.0)
                            .build()
                    )
                }
            ){
                //AddPointer(Point.fromLngLat(24.9384, 60.1699))
            }



        }
    }

    //Resume the main activity after an interrupt. Checks if permission is granted before requesting user location updates
    override fun onResume(){
        super.onResume()
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProvider?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    //called when the activity is not in the foreground of the users device. Method will stop location updates while in the background
    override fun onPause(){
        super.onPause()
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            fusedLocationProvider?.removeLocationUpdates(locationCallback)
        }
    }

    //After getting results from permission request, method checks if permission was granted. If so, method performs tasks that require permission.
    //If not, checks if user has clicked do not ask again before doing one more request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                //If request is cancelled, the result arrays are empty
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission was granted, perform location related task that needed to be done
                    if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        fusedLocationProvider?.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                        checkBackgroundLocation()
                    }
                } else {
                    //permission is denied, disable the functionality that depends on location permissions
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()

                    //Check if we are in a state where the user has denied the permission and selected 'Don't ask again'
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)){
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.packageName, null)
                            ),
                        )
                    }
                }
                return
            }
        }
    }

    //objects used to request location permissions
    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    }

    //This function checks permissions and requests them if needed
    private fun checkLocationPermission(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response!
                // After the user sees the explanation, try again to request permission.
                AlertDialog.Builder(this)
                    .setTitle("")
                    .setMessage("")
                    .setPositiveButton(
                        "Ok"
                    ){_, _ ->
                        //Prompt the user once explanation has been shown
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {
                //No explanation needed, we can request the permission
                requestLocationPermission()
            }
        } else {
            checkBackgroundLocation()
        }
    }

    //checks background location permission status
    private fun checkBackgroundLocation(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestBackgroundLocationPermission()
        }
    }

    //This function checks coarse location permission status
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    //This function requests background location permissions
    private fun requestBackgroundLocationPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    /*
    @OptIn(MapboxExperimental::class)
    @Composable
    fun AddPointer(point: Point) {
        val drawable = ResourcesCompat.getDrawable(
            resources,
            R.drawable.marker,
            null
        )
        val bitmap = drawable!!.toBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        PointAnnotation(
            iconImageBitmap = bitmap,
            iconSize = 0.5,
            point = point,
            onClick = {
                Toast.makeText(
                    this,
                    "Clicked on Circle Annotation: $it",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
        )
    }
    */
}