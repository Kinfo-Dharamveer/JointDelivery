package com.jointdelivery.appviewmodule.mapactivities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat/*
import com.ahmadrosid.lib.drawroutemap.DrawMarker
import com.ahmadrosid.lib.drawroutemap.DrawRouteMaps*/
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

import com.jointdelivery.R
import com.jointdelivery.appviewmodule.ChatActivity
import com.jointdelivery.auth.ApiClient
import com.sendbird.android.GroupChannel
import kotlinx.android.synthetic.main.tool_bar_layout.*
import kotlinx.android.synthetic.main.user_detail_layout.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.HashMap

open class YourRideMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    internal lateinit var markerPoints: ArrayList<LatLng>
    private lateinit var mMap: GoogleMap
    internal var mFusedLocationClient: FusedLocationProviderClient? = null
    internal lateinit var mLocationRequest: LocationRequest
    internal var mLastLocation: Location? = null
    internal var mCurrLocationMarker: Marker? = null
    var isExtraDataShowing = false
    internal lateinit var mLocationManager: LocationManager
    val EXTRA_NEW_CHANNEL_URL = "EXTRA_NEW_CHANNEL_URL"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_ride_layout)

        toolbar_title.text = "Your Ride"

        back_button.visibility = View.VISIBLE

        back_button.setOnClickListener {
            onBackPressed()
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        markerPoints = ArrayList<LatLng>()



        show_details.setOnClickListener {
            if (isExtraDataShowing) {
                show_details.text = "Show Detail"
                detail_text_view.visibility = View.GONE
            } else {
                show_details.text = "Hide Detail"
                detail_text_view.visibility = View.VISIBLE
            }
            isExtraDataShowing = !isExtraDataShowing
        }


    }


    override fun onMapReady(googleMap: GoogleMap?) {


        mMap = googleMap!!
        mMap.getUiSettings().setMapToolbarEnabled(false);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.map_style
                )
            )

            if (!success) {
                Log.e("TAG", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("TAG", "Can't find style. Error: ", e)
        }


        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 120000 // two minute interval
        mLocationRequest.fastestInterval = 120000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Location Permission already granted
                mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                mMap.setMyLocationEnabled(true)
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
            mMap.setMyLocationEnabled(true)
        }

        /*  val myLocation = getLastKnownLocation()


          if (myLocation != null) {

              val yourposition = LatLng(myLocation!!.latitude, myLocation!!.longitude)
              mMap.addMarker(MarkerOptions().position(yourposition).title("your position"))
              mMap.moveCamera(CameraUpdateFactory.newLatLng(yourposition))


          }*/


        val originLat = 30.710920
        val originLong = 76.693320

        val destLat = 30.713989
        val destLong = 76.718231

        val orig = LatLng(originLat, originLong)
        val dest1 = LatLng(destLat, destLong)


        val origin = LatLng(originLat, originLong)
        val destination = LatLng(destLat, destLong)

        /* DrawRouteMaps.getInstance(this).draw(origin, destination, mMap)
         DrawMarker.getInstance(this).draw(mMap, origin, R.drawable.map_icon, "Origin Location")
         DrawMarker.getInstance(this).draw(mMap, destination, R.drawable.map_marker, "Destination Location")

         val bounds = LatLngBounds.Builder()
             .include(origin)
             .include(destination).build()
         val displaySize = Point()
         windowManager.defaultDisplay.getSize(displaySize)*/
        //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30))


        mMap.addMarker(
            MarkerOptions().position(orig).title("origin position").icon(BitmapDescriptorFactory.fromResource(R.drawable.map_location))
        )
        mMap.addMarker(
            MarkerOptions()
                .position(dest1).title("dest position")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_location))
        )


        mMap.moveCamera(CameraUpdateFactory.newLatLng(orig))

        // Getting URL to the Google Directions API
        val url = getDirectionsUrl(origin, dest1)
        Log.d("onRouteApiUrl", url.toString())

        ApiClient.createMapClient()
            .getMapData(origin = "${originLat},${originLong}", destination = "${destLat},${destLong}")
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@YourRideMapActivity, "Error ${t.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                    val string = response.body()?.string()
                    print(string)
                    val parserTask = ParserTask()

                    // Invokes the thread for parsing the JSON data
                    parserTask.execute(string)
//
                }

            })

        //  val downloadTask = DownloadTask()

        // Start downloading json data from Google Directions API
        //  downloadTask.execute(url)

/*

        val builder = LatLngBounds.Builder()
        for (marker in markers) {
            builder.include(marker.getPosition())
        }
        val bounds = builder.build()

*/


        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20f))

        /*  // val yourposition = LatLng(destLat, dest1)
          mMap.addMarker(MarkerOptions().position(orig).title("your position"))
          mMap.moveCamera(CameraUpdateFactory.newLatLng(orig))




          if (markerPoints.size > 1) {
              markerPoints.clear()
              mMap.clear()
          }


          markerPoints.add(orig!!)

          // Creating MarkerOptions
          val options = MarkerOptions()

          // Setting the position of the marker
          options.position(orig)

          if (markerPoints.size == 1) {
              options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
          } else if (markerPoints.size == 2) {
              options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
          }


          // Add new marker to the Google Map Android API V2
        //  mMap.addMarker(options)

          // Checks, whether start and end locations are captured
          // if (MarkerPoints.size >= 2) {
          if (markerPoints.size >= 2) {

              val originLat2 = 30.710920
              val originLong2 = 76.693320

              val destLat2 = 30.713989
              val destLong2 = 76.718231


              val orig2 = LatLng(originLat2, originLong2)
              val dest2 = LatLng(destLat2, destLong2)


              val origin = orig2
              val dest = dest2

              // Getting URL to the Google Directions API
              val url = getDirectionsUrl(origin, dest)
              Log.d("onRouteApiUrl", url.toString())
              val downloadTask = DownloadTask()

              // Start downloading json data from Google Directions API
              downloadTask.execute(url)
              //move map camera
              mMap.moveCamera(CameraUpdateFactory.newLatLng(origin))
               mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
          }
  */

    }

    public override fun onPause() {
        super.onPause()

        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }


    internal var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                //The last location in the list is the newest
                val location = locationList[locationList.size - 1]
                Log.i("MapsActivity", "Location: " + location.latitude + " " + location.longitude)
                mLastLocation = location
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker!!.remove()
                }

                //Place current location marker
                val latLng = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title("Current Position")
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                //    mCurrLocationMarker = mMap.addMarker(markerOptions)

                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11f))
            }
        }
    }


/*
    private fun getLastKnownLocation(): Location? {
        mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mLocationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val loc = mLocationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || loc.getAccuracy() < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = loc
            }
        }
        return bestLocation
    }
*/

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        mFusedLocationClient?.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.myLooper()
                        )
                        mMap.setMyLocationEnabled(true)
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    val MY_PERMISSIONS_REQUEST_LOCATION = 99

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK") { dialogInterface, i ->
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this@YourRideMapActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }


    fun contactCustomer(view: View) {

    }

    fun sendMessage(view: View) {
        var userIds = ArrayList<String>()
        userIds.add("dummy45")
        createGroupChannel(userIds, true)
    }

    fun createGroupChannel(userIds: List<String>, distinct: Boolean) {
        GroupChannel.createChannelWithUserIds(userIds, distinct,
            GroupChannel.GroupChannelCreateHandler { groupChannel, e ->
                if (e != null) {
                    // Error!
                    return@GroupChannelCreateHandler
                } else {
//                    val intent = Intent(applicationContext, ChatActivity::class.java)
//                    startActivity(intent)
                }


            })
    }

    fun share(view: View) {

        val intent = Intent(
            android.content.Intent.ACTION_VIEW,
            Uri.parse("http://maps.google.com/maps?saddr=30.7350626,76.6934885 &daddr=9.5448732,76.7719862")
        )
        startActivity(intent)
    }


    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {

        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude


        // Sensor enabled
        val sensor = "sensor=false"
        val mode = "mode=driving"
        val key = "key=AIzaSyAtCfoubDffBLNFHGPd0QnTAnVTcQ3fO9M"

        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$key"

        // Output format
        val output = "json"

        // Building the url to the web service
        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters"


        return url
    }

    // Fetches data from url passed
/*
    private inner class DownloadTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg url: String): String {

            var data = ""

            try {
                data = downloadUrl(url[0])
            } catch (e: Exception) {
                Log.d("Background Task", e.toString())
            }

            return data
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            val parserTask = ParserTask()

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result)

        }
    }
*/

    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {


        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)

            // Creating an http connection to communicate with url
            urlConnection = url.openConnection() as HttpURLConnection

            // Connecting to url
            urlConnection.connect()

            // Reading data from url
            iStream = urlConnection.inputStream

            val br = BufferedReader(InputStreamReader(iStream!!))

            val sb = StringBuffer()

            val line = br.readLine()

            while (line != null) {
                sb.append(line);
            }

            data = sb.toString()
            Log.d("downloadUrl", data.toString())
            br.close()

        } catch (e: Exception) {
            Log.d("Exception", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private inner class ParserTask : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {

        // Parsing the data in non-ui thread
        override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>>? {

            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null

            try {
                jObject = JSONObject(jsonData[0])
                val parser = DirectionsJSONParser()

                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return routes
        }

        override fun onPostExecute(result: List<List<HashMap<String, String>>>) {

            var points: ArrayList<LatLng>
            var lineOptions: PolylineOptions? = null

            for (i in result.indices) {
                points = ArrayList()
                lineOptions = PolylineOptions()

                val path = result[i]

                for (j in path.indices) {
                    val point = path[j]

                    val lat = java.lang.Double.parseDouble(point["lat"]!!)
                    val lng = java.lang.Double.parseDouble(point["lng"]!!)
                    val position = LatLng(lat, lng)

                    points.add(position)
                }

                lineOptions.addAll(points)
                lineOptions.width(10f)
                lineOptions.color(Color.RED)
                //   lineOptions.geodesic(true)

            }

            if (lineOptions != null) {
                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions)
            } else {
                Log.d("onPostExecute", "without Polylines drawn");

            }


        }
    }
}