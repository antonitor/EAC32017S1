package cat.xtec.ioc.eac3_2017s1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity que conté un FrameLayout que ocupa 6/10 de la pantalla i conté un VideoView i un
 * ImageView superposats; tan sols es mostrarà un dels dos en funció de si es tracta d'un video o una foto.
 *
 * Aquesta activty conté també un fragment que ocupa el 4/10 inferior de la pantalla i ens mostra
 * un mapa de GoogleMaps.
 */
public class MediaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int mIsVideo;
    private String mMediaPath;
    private float mLongitude;
    private float mLatitude;
    private ImageView mImageView;
    private VideoView mVideoView;
    private MediaController mMediaController;
    private final float ZOOM = 14f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Recupera els extres que s'han pasat a l'intent que ha llençat aquesta Activty
        Intent intentThatStartedThisActivity = getIntent();
        mIsVideo = intentThatStartedThisActivity.getIntExtra(getString(R.string.extra_isvideo),-1);
        mMediaPath = intentThatStartedThisActivity.getStringExtra(getString(R.string.extra_media_path));
        mLatitude = intentThatStartedThisActivity.getFloatExtra(getString(R.string.extra_latitude),0f);
        mLongitude = intentThatStartedThisActivity.getFloatExtra(getString(R.string.extra_longitude),0f);

        //Si es tracta d'un video es mostrarà el VIdeoView, si es tracta d'una foto el ImageVIew
        if (mIsVideo == 0) {
            showImage();
        } else if (mIsVideo == 1) {
            showVideo();
        } else {
            finish();
        }

    }

    /**
     * Mostra l'ImageView i l'infla amb l'imatge a la que apunta l'Uri
     */
    private void showImage(){
        mImageView = findViewById(R.id.image_view);
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImageURI(Uri.parse(mMediaPath));
    }

    /**
     * Mostra el VideoView, l'infla amb el video al que apunta l'Uri i comença la reproducció.
     * Afegeix a aquest VideoView un MediaController per tal de poder controlar la reproducció.
     */
    private void showVideo(){
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setVisibility(View.VISIBLE);
        if (mMediaController == null){
            mMediaController = new MediaController(this);
            mMediaController.setAnchorView(mVideoView);
            mVideoView.setMediaController(mMediaController);
            mVideoView.setVideoURI(Uri.parse(mMediaPath));
            mVideoView.start();
        }

    }


    /**
     * Quan el GoogleMap està llest es llença aquest mètode que agafa la latitud i longitud
     * que hem pasat a aquesta Activity com a extra i crea un marcador en aquesta localització.
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng position = new LatLng(mLatitude, mLongitude);
        mMap.addMarker(new MarkerOptions().position(position).title(getString(R.string.marker_media)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM));

    }
}
