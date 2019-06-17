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
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_location.*


class MainActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMapClickListener {
    private val AUTOCOMPLETE_REQUEST_CODE = 1

//    Map
    private var map: GoogleMap? = null
    private var fromLatLng: LatLng? = null
    private var toLatLng: LatLng? = null

    private var switch: Int = -1

//    BottomSheet
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

//    AutoComplete
    private val placeIntente by lazy {
        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,
            Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)).build(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Places.isInitialized())
            Places.initialize(applicationContext, resources.getString(R.string.API_KEY), Locale.KOREA)

        val map
                = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        map?.getMapAsync(this)
    }

//    Map

    override fun onMapReady(map: GoogleMap?) {
        map?.setOnMapClickListener(this)
        this.map = map
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheet))
        fromBtn.setOnClickListener {
            switch = 0
            startActivityForResult(placeIntente, AUTOCOMPLETE_REQUEST_CODE)
        }
        toBtn.setOnClickListener {
            switch = 1
            startActivityForResult(placeIntente, AUTOCOMPLETE_REQUEST_CODE)
        }
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
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                Log.i("AutoComplete", "Place: ${place.name} , ${place.id} , ${place.latLng?.latitude} : ${place.latLng?.longitude}")

                addMark(place.latLng!!)
                if (switch == 0) {
                    fromAddress.text = place.address
                    fromName.text = place.name
                } else {
                    toAddress.text = place.address
                    toName.text = place.name
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.i("AutoComplete", status.statusMessage)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}
