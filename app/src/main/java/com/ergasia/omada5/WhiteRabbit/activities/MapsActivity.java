package com.ergasia.omada5.WhiteRabbit.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ergasia.omada5.WhiteRabbit.R;
import com.ergasia.omada5.WhiteRabbit.Services.GeoService;
import com.ergasia.omada5.WhiteRabbit.entities.Poi;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.lang.String.format;

//public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("poi");

    Map<String, Marker> markers;

    private String TAG = "banana maps activity";

    private GoogleMap mMap;

    SupportMapFragment mapFragment;

    private ChildEventListener poiChildEventListener;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            uid = mAuth.getCurrentUser().getUid();
        } else {
            uid = "anonymous";
        }


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        markers = new HashMap();

//        setContentView(R.layout.activity_maps);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            Toast.makeText(this, "ΕΠΙΛΟΓΗ ΓΙΑ ΑΝΑΖΗΤΗΣΗ ΜΕ ΠΑΡΑΜΕΤΡΟΥΣ .... ", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_profile) {
            Toast.makeText(this, "ΕΠΙΛΟΓΗ ΓΙΑ ΠΡΟΦΙΛ ΧΡΗΣΤΗ .... ", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ProfileActivity.class);
            //intent.putExtra("poi", poi);
            //intent.putExtra("key", "0");
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_settings) {
            Toast.makeText(this, "ΕΠΙΛΟΓΗ ΓΙΑ ΡΥΘΜΙΣΕΙΣ .... ", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.actionFaq) {
            Toast.makeText(this, "ΕΠΙΛΟΓΗ ΓΙΑ ΣΥΧΝΕΣ ΕΡΩΤΗΣΕΙΣ (FAQ) .... ", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.actionPrivacy) {
            Toast.makeText(this, "ΕΠΙΛΟΓΗ ΓΙΑ ΕΝΗΜΕΡΩΣΗ PRIVACY POLICY .... ", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.actionDisclaimer) {
            Toast.makeText(this, "ΕΠΙΛΟΓΗ ΓΙΑ ΕΝΗΜΕΡΩΣΗ DISCLAIMER.... ", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.actionAbout) {
            Toast.makeText(this, "ΕΠΙΛΟΓΗ ΓΙΑ ΕΝΗΜΕΡΩΣΗ ABOUT .... ", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_exit) {
            Toast.makeText(this, "ΕΠΙΛΟΓΗ ΓΙΑ ΕΞΟΔΟ ΑΠΟ ΤΗΝ ΕΦΑΡΜΟΓΗ .... ", Toast.LENGTH_SHORT).show();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v(TAG, "map ready");

        mMap = googleMap;
        //θα εχω location εδω ?

        GoogleMap.InfoWindowAdapter myInfoAdapter = new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

//                ImageView image = new ImageView(getApplicationContext());
//                image.setImageResource(R.mipmap.playground);
//                return image;

                // return createView(R.layout.poi_0_info_short, getApplicationContext(), 2232);
                View v = getLayoutInflater().inflate(R.layout.poi_0_info_short, null);

                TextView address = (TextView) v.findViewById(R.id.poiAddr);
                TextView category = (TextView) v.findViewById(R.id.poiCategoryTxt);
                ImageView image = (ImageView) v.findViewById(R.id.poiImage);
                TextView score = (TextView) v.findViewById(R.id.poiAvgScoreShort);


//               RatingBar rating = (RatingBar)  v.findViewById(R.id.poiRating);
                Poi poi = (Poi) marker.getTag();
                address.setText(GeoService.getAddress(getApplicationContext(), poi.lat, poi.lon));
                category.setText(poi.category);
                Random rScore = new Random();
                DecimalFormat df = new DecimalFormat("#.#");
                Float fObj = new Float(rScore.nextFloat() * 10);
                String str = fObj.toString();
                Log.v(TAG, "Average Score :" + rScore.toString() + " --> " + str + "  " + format("%.1f", fObj));
                score.setText(format("%.1f", fObj));

//                image.setImageResource(R.mipmap.playground);  -- ebala sto layout thn eikona


//                Random r = new Random();
//                rating.setNumStars(10);
//                rating.setRating(r.nextFloat()*10);
//                rating.setStepSize(1f);
                return v;
            }
        };

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                dtlPoi(marker);
            }
        });

        mMap.setInfoWindowAdapter(myInfoAdapter);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addPoi(latLng);
            }
        });

        // πιανουμε το click στο info window
//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//              //  viewPoi(marker);
//                // Return false to indicate that we have not consumed the event and that we wish
//                // for the default behavior to occur (which is for the camera to move such that the
//                // marker is centered and for the marker's info window to open, if it has one).
//                return false;
//            }
//        });


        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poi[1].location,15));


    }


    private void addPoi(LatLng latLng) {
        Log.v(TAG, "creating new poi at " + latLng);
        Poi poi = new Poi();

        poi.lat = latLng.latitude;
        poi.lon = latLng.longitude;
        Intent intent = new Intent(this, PoiActivity.class);
        intent.putExtra("poi", poi);
        //intent.putExtra("key", "0");
        startActivity(intent);


    }

    private void viewPoi(Marker marker) {
        Poi poi = (Poi) marker.getTag();
        String key = marker.getSnippet();
        Intent intent = new Intent(this, PoiActivity.class);
        intent.putExtra("poi", poi);
        intent.putExtra("key", key);
        startActivity(intent);
    }


    private void dtlPoi(Marker marker) {
        Poi poi = (Poi) marker.getTag();
        String key = marker.getSnippet();
        Intent intent = new Intent(this, PoiActivity.class);
        intent.putExtra("poi", poi);
        intent.putExtra("key", key);
        startActivity(intent);


        Log.v(TAG, "poi details");

//        View v = getLayoutInflater().inflate(R.layout.poi_dtl_0_master, null);
//        TextView category = (TextView) v.findViewById(R.id.poiCategoryTxt);
//        ImageView image1  = (ImageView) v.findViewById(R.id.poiImage1);
//        ImageView image2  = (ImageView) v.findViewById(R.id.poiImage2);
//        ImageView image3  = (ImageView) v.findViewById(R.id.poiImage3);
//
//        RatingBar rating = (RatingBar)  v.findViewById(R.id.poiRating);
//        Poi poi = (Poi) marker.getTag();
//        category.setText(poi.category);
//
//        image1.setImageResource(R.mipmap.playground);
//        image2.setImageResource(R.mipmap.playground);
//        image3.setImageResource(R.mipmap.playground);
//
//        Random r = new Random();
////                rating.setNumStars(10);
////                rating.setRating(r.nextFloat()*10);
////                rating.setStepSize(1f);
//        return v;

    }

    @Override
    protected void onStart() {
        // γιατι το ενα το βαζω πριν το super και το αλλο μετα ?

        mGoogleApiClient.connect();

        super.onStart();
        // εδω ακουω για αλλαγες στα children του poi
        // το child added σκαει για ΟΟΟΟΛΑ ΤΑ POI οταν ανοιγω την εφαρμογή.

        mAuth.addAuthStateListener(mAuthListener);

        poiChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.v(TAG,"previous : " + s);
                String key = dataSnapshot.getKey();
//                Log.v(TAG,"current  : " + key);

                Poi poi = dataSnapshot.getValue(Poi.class);
//                Log.v(TAG, "POI ADDED :" + poi.toString());
                LatLng location = new LatLng(poi.lat, poi.lon);

                Marker m = mMap.addMarker(new MarkerOptions().position(location).title(poi.category).snippet(key));
                m.setTag(poi);
                //BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(R.color.wr_background_color);
                //m.setIcon(bitmapDescriptor);
                markers.put(key, m);


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousKey) {

                String key = dataSnapshot.getKey();
                Poi poi = dataSnapshot.getValue(Poi.class);

//                Log.v(TAG, "POI CHANGED :" + poi.toString());

                Marker marker = markers.get(key);
                marker.setTitle(poi.category);
                marker.setTag(poi);

//                Log.v(TAG,((Poi) marker.getTag()).catDescr);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                //  Poi poi = dataSnapshot.getValue(Poi.class);
                markers.get(key).remove();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        myRef.addChildEventListener(poiChildEventListener);

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "location ready");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            //     mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            //    mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            LatLng loc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 17));
            // εμφανιζει την μπλε κουκιδα.
            mMap.setMyLocationEnabled(true);
            try {
                Address address = new Geocoder(getApplicationContext()).getFromLocation(loc.latitude, loc.longitude, 1).iterator().next();
                Log.v(TAG, "address is :" + address.getAddressLine(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    protected void onDestroy() {
        Log.v(TAG, "ON DESTROY sign out");
        mAuth.signOut();
        super.onDestroy();
    }


}

