package cat.xtec.ioc.eac3_2017s1;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MediaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int mIsVideo;
    private String mMediaPath;
    private float mLongitude;
    private float mLatitude;
    private ImageView mImageView;
    private VideoView mVideoView;
    private MediaController mMediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intentThatStartedThisActivity = getIntent();
        mIsVideo = intentThatStartedThisActivity.getIntExtra(getString(R.string.extra_isvideo),-1);
        mMediaPath = intentThatStartedThisActivity.getStringExtra(getString(R.string.extra_media_path));
        mLatitude = intentThatStartedThisActivity.getFloatExtra(getString(R.string.extra_latitude),0f);
        mLongitude = intentThatStartedThisActivity.getFloatExtra(getString(R.string.extra_longitude),0f);

        Log.d("LAT AND LONG ",  mLatitude + " : " + mLongitude);

        if (mIsVideo == 0) {
            showImage();
        } else if (mIsVideo == 1) {
            showVideo();
        } else {
            finish();
        }

    }

    private void showImage(){
        mImageView = findViewById(R.id.image_view);
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImageURI(Uri.parse(mMediaPath));
        mImageView.setScaleY(-1f);
    }

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



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng position = new LatLng(mLatitude, mLongitude);
        mMap.addMarker(new MarkerOptions().position(position).title("Marker Where Media Was Captured"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 8f));

    }
}
