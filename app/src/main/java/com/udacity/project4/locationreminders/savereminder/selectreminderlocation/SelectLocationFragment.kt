package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.collections.ArrayList


class SelectLocationFragment : BaseFragment() {
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var map: GoogleMap
    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private var polygon: Polygon? = null
    //List of Markers and Lat Lng
    private var markerList =  ArrayList<Marker>()
    private var latLngList =  ArrayList<LatLng>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // TODO: add the map setup implementation
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        supportMapFragment?.getMapAsync(OnMapReadyCallback() { googleMap ->
            map = googleMap
            //These coordinates represent the lattitude and longitude of the Googleplex.
            val latitude = 30.91275530338768
            val longitude = 75.84048942122247
            val zoomLevel = 20f
            val homeLatLng = LatLng(latitude, longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))

            // TODO: add style to the map
            setMapStyle(map)

            // TODO: zoom to the user location after taking his permission
            enableMyLocation()

            // TODO: put a marker to location that the user selected
            setMapLongClick(googleMap)
            submitLocation(latLngList)
        })


        return binding.root
    }


    private fun onLocationSelected(latLng: LatLng) {
        val geoCoder = Geocoder(this.requireContext(),Locale.ENGLISH)
        val addressList = geoCoder.getFromLocation(latLng.latitude,latLng.longitude,1)
        _viewModel.reminderSelectedLocationStr.value = addressList?.get(0)?.featureName.toString()

        // TODO: When the user confirms on the selected location,
        //  send back the selected location details to the view model
        //  and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.latitude.value = latLng.latitude
        _viewModel.longitude.value = latLng.longitude
        _viewModel.navigationCommand.value = NavigationCommand.Back

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // Checks that users have given permission
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }


    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this.requireActivity(),
                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION)
                return
            }

            map.isMyLocationEnabled = true
            if (!latLngList.isNullOrEmpty())
                drawPolygon(map,latLngList)
            //map.uiSettings.isMyLocationButtonEnabled = true

        }
        else {
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    // Called when user makes a long press gesture on the map.
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            var marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
            //Append Marker and LatLng List
            latLngList.add(latLng)

            if (null != marker)
                markerList.add(marker)

            drawPolygon(map,latLngList)
            // TODO: call this function after the user confirms on the selected location
            //onLocationSelected(latLng)

        }

    }


    // Called when user makes a long press gesture on the map.
    private fun submitLocation(latLngList: ArrayList<LatLng>) {
        val buttonSubmit = binding.btnSubmit
        buttonSubmit.setOnClickListener {
            onLocationSelected(latLngList[0])
        }
    }

    // Allows map styling and theming to be customized.
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("Select Location", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("Select Location", "Can't find style. Error: ", e)
        }
    }

    fun drawPolygon(map: GoogleMap, latLngList: List<LatLng>?) {
        if (polygon != null) {
            polygon?.remove()
        }
        // Add a triangle in the Gulf of Guinea
        if (null != latLngList) {
            polygon = map.addPolygon(
                PolygonOptions()
                    .addAll(latLngList)
                    .strokeColor(Color.RED)
                    .fillColor(Color.BLUE)
            )
        }
    }


}