package c.gingdev.mylocation

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*
import com.google.android.libraries.places.internal.i
import android.R.attr.data
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.AutocompleteActivity



class MainActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMapClickListener {
    private val AUTOCOMPLETE_REQUEST_CODE = 1

    private var map: GoogleMap? = null
    private var fromLatLng: LatLng? = null
    private var toLatLng: LatLng? = null

    private val placeIntente by lazy {
        Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
            Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)).build(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Places.isInitialized())
            Places.initialize(applicationContext, resources.getString(R.string.API_KEY), Locale.KOREA)

        val map
                = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        map?.getMapAsync(this)

        startActivityForResult(placeIntente, AUTOCOMPLETE_REQUEST_CODE)
    }

//    Map

    override fun onMapReady(map: GoogleMap?) {
        map?.setOnMapClickListener(this)
        this.map = map
    }

    override fun onMapClick(latlng: LatLng?) {
        if (fromLatLng == null)
            fromLatLng = latlng
        else if (toLatLng == null)
            toLatLng = latlng
    }

    private fun addMark(latlng: LatLng) {
        val markerOption = MarkerOptions()

        val mark = markerOption.let {
            it.position(latlng)
        }.run {
            map?.addMarker(this)
        }

        map?.run {
            moveCamera(CameraUpdateFactory.newLatLng(mark?.position ?: LatLng(0.0, 0.0)))
            animateCamera(CameraUpdateFactory.zoomTo(17f))
        }
    }

//    AutoComplete

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                Log.i("AutoComplete", "Place: ${place.name} , ${place.id} , ${place.latLng?.latitude} : ${place.latLng?.longitude}")

                addMark(place.latLng!!)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.i("AutoComplete", status.statusMessage)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
