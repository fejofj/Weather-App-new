package com.example.weatherapp.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.weatherapp.MainViewmodel
import com.example.weatherapp.MainviewmodalFactory
import com.example.weatherapp.R
import com.example.weatherapp.repository.Repository
import com.google.android.gms.location.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import java.text.SimpleDateFormat
import java.util.*

class LoadFragment : Fragment(),EasyPermissions.PermissionCallbacks {
private   var lat:Double=0.0
private  var lon:Double=0.0
    private lateinit var repository:Repository
    private lateinit var viewmodel: MainViewmodel
private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest=LocationRequest()

    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


       val  view =  inflater.inflate(R.layout.fragment_load, container, false)
       val dialog=Dialog(requireContext())
        dialog.setContentView(R.layout.reloading)
dialog.show()
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback=object : LocationCallback(){
            @SuppressLint("SimpleDateFormat")
            override fun onLocationResult(locationResult: LocationResult) {
                for(location: Location in locationResult.locations){
                    lat=location.latitude
                    lon=location.longitude
                    repository = Repository(lat,lon,"432815a1727a48cafc241d7128cb2743")
                    val place= view.findViewById<TextView>(R.id.place)
                    val date = view.findViewById<TextView>(R.id.date)
                    val icon = view.findViewById<ImageView>(R.id.imageView)
                    val temp = view.findViewById<TextView>(R.id.temp)
                    val desc = view.findViewById<TextView>(R.id.desc)

                    val viewmodelFactory=MainviewmodalFactory(repository)
                    viewmodel=ViewModelProvider(this@LoadFragment,viewmodelFactory).get(MainViewmodel::class.java)
                    viewmodel.getWeather()
                    viewmodel.weatherRes.observe(viewLifecycleOwner, Observer {
                            observe ->
                        place.text=observe.name
                        temp.text= (observe.main.temp - 273.15).toInt().toString()
                        desc.text= observe.weather.first().main
                        when(observe.weather.first().main){
                            "Clouds" -> {
                                icon.setImageResource(R.drawable.cloudy)
                            }
                            "Rain","Drizzle" ->{
                                icon.setImageResource(R.drawable.rain)
                            }
                            "Clear" ->{
                                icon.setImageResource(R.drawable.sun)
                            }
                            "Thunderstorm","Strom" ->{
                                icon.setImageResource(R.drawable.storm)
                            }
                        }
                        //   Log.d("Name",observe.weather[1].toString())
                        Log.d("Name",observe.toString())


                    })
                    val sdf = SimpleDateFormat("E hh:mm a")
                    val currentDate = sdf.format(Date())
                    date.text=currentDate
                    Log.d("Lat",lat.toString())
                    Log.d("Lat",lon.toString())

                }
                dialog.hide()
            }

        }

        if(hasPermission()){
    Toast.makeText(requireContext(), "Granted", Toast.LENGTH_SHORT).show()
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())


            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->

              if(location !=null){
                  Log.d("location Name",location.latitude.toString())
                  Log.d("location name",location.longitude.toString())
                  lat = location.latitude
                  lon=location.longitude
              }


            }

}else {
    requestPermission()
}



        return view

    }
private fun hasPermission() =
    EasyPermissions.hasPermissions(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    )


    private fun requestPermission(){
        EasyPermissions.requestPermissions(this,
            "Weather App need to access your location",
        1,Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            SettingsDialog.Builder(requireActivity()).build().show()
        }else{
            requestPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(requireContext(), "Location Prermission Granted", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



}