package cat.xtec.ioc.eac3_2017s1;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cat.xtec.ioc.eac3_2017s1.Data.MediaDBHelper;
import cat.xtec.ioc.eac3_2017s1.Recycler.MediaAdapter;
import cat.xtec.ioc.eac3_2017s1.Data.MediaContract.MediaTable;

import static android.location.LocationManager.*;

public class MainActivity extends AppCompatActivity implements MediaAdapter.MediaAdapterOnClickHandler, LocationListener {

    private MediaAdapter mAdapter;
    private SQLiteDatabase mDb;
    private MediaDBHelper dbHelper;
    //Ruta del fitxer que es vol desar a la bd
    private String mCurrentPath;
    //Nom del fitxer que es vol desar a la bd
    private String mCurrentFileName;
    //Localització que es vol desar a la bd
    private Location mCurrentLocation;
    private static final int REQUEST_TAKE_VIDEO = 20;
    private static final int REQUEST_TAKE_PHOTO = 21;
    private static final int MY_PERMISSIONS_REQUEST = 22;
    private FloatingActionButton mPictureActionButton;
    private FloatingActionButton mVideoActionButton;
    LocationManager mLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView mediaRecyclerView = (RecyclerView) this.findViewById(R.id.media_list_view);
        mediaRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new MediaDBHelper(this);
        mDb = dbHelper.getWritableDatabase();
        mAdapter = new MediaAdapter(this, getAllMedia(), this);
        mediaRecyclerView.setAdapter(mAdapter);
        //Afegim al recyclerView un ItemTouchHelper per poder borrar fent swipe
        getItemTouchHelper().attachToRecyclerView(mediaRecyclerView);

        mPictureActionButton = (FloatingActionButton) this.findViewById(R.id.take_picture_floating_action_button);
        mPictureActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        mVideoActionButton = (FloatingActionButton) this.findViewById(R.id.rec_video_floating_action_button);
        mVideoActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakeVideoIntent();
            }
        });
        //D'entrada els botons per fer foto o video estaran deshabilitats
        dissableButtons();
        //Comprovam si tenim els permisos adients
        checkPermissions();
    }

    /**
     * Mètode sobreescrit de l'interfície MediaAdapterOnClickHandler que ens permet recuperar
     * les dades de la base de dades corresponents al item on hem fet clic i llençar l'activity
     * MediaActivity amb els extres corresponents.
     *
     * @param isVideo   0 foto / 1 video
     * @param mediaPath ruta de l'arxiu
     * @param latitude  latitud obtinguta en el moment de la captura
     * @param longitude longitud obtinguda en el moment de la captura
     */
    @Override
    public void onClick(int isVideo, String mediaPath, float latitude, float longitude) {
        Intent intent = new Intent(this, MediaActivity.class);
        intent.putExtra(getString(R.string.extra_isvideo), isVideo);
        intent.putExtra(getString(R.string.extra_media_path), mediaPath);
        intent.putExtra(getString(R.string.extra_latitude), latitude);
        intent.putExtra(getString(R.string.extra_longitude), longitude);
        startActivity(intent);
    }

    /**
     * Aquest mètode ens torna un nou objecte ItemTouchHelper configurat per tal de poder esborrar
     * arxius de la galeria fent swipe sobre el RecyclerView
     *
     * @return objecte ItemTouchHelper
     */
    private ItemTouchHelper getItemTouchHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            //Objecte que ens permèt dibuixar al canvas dins el mètode onChildDraw
            Paint p = new Paint();

            //No ens interesa
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            /**
             * Aquest mètode es llença quan fem swipe sobre un item del RecyclerView
             * @param viewHolder holder corresponent al item
             * @param direction direcció cap a la que fem swipe
             */
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = (long) viewHolder.itemView.getTag();
                //Si el mètode removeMedia torna true, actualitzem l'adapter amb un nou cursor de la bd
                if (removeMedia(id)) {
                    mAdapter.swapCursor(getAllMedia());
                }
            }

            /**
             * Aquest mètode ens permet dibuixar sobre la vista RecyclerView
             * @param c Canvas per dibuixar que ocupa tota la vista del RecyclerView
             * @param recyclerView RecyclerView al que esta fixat aquest ItemTouchHelper
             * @param viewHolder ViewHolder amb el que s'esta interactuant
             * @param dX Quantitat de moviment horitzontal causat per l'acció de l'usuari
             * @param dY Quantitat de moviment vertical causat per l'acció de l'usuari
             * @param actionState Tipus de interacció: ACTION_STATE_DRAG o ACTION_STATE_SWIPE.
             * @param isCurrentlyActive True si aquest view està sent controlat per l'usuari o fals
             *                          si està tornant al seu estat original.
             */
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Bitmap icon;
                //Quan el tipus d'interacció és swipe:
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    //Vista amb la que s'està interactuant
                    View itemView = viewHolder.itemView;
                    //Altura de la vista
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    //Quan el moviment horitzontal causat per l'usuari es major a zero:
                    if (dX > 0) {
                        //Dibuixem un rectangle de color gris degradat a blanc que aumentarà amb el moviment de l'usuari
                        Shader shader = new LinearGradient((float) itemView.getLeft(), (float) itemView.getBottom(), dX, (float) itemView.getTop(), Color.RED, Color.WHITE, Shader.TileMode.CLAMP);
                        p.setShader(shader);
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), p);
                        //Dibuixem una icona per informar a l'usuari que s'esborrarà l'arixu
                        //Posicions relatives a la posició de la vista:
                        float left = 100;
                        float bottom = itemView.getBottom() - height / 6;
                        float top = itemView.getTop() + height / 4;
                        float imageWidth = (bottom - top) / 1.2f;
                        float right = left + imageWidth;
                        //RectF emmagatzema les coordenades d'un rectangle per tal de dibuixar-hi l'icona a dintre
                        RectF rectF = new RectF(left, top, right, bottom);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.trash);
                        c.drawBitmap(icon, null, rectF, p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
    }

    /**
     * Comprova si s'han concedit els permisos per accedir al Gps, Càmera i emmagatzament extern:
     *
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
        } else {
            updateLocationChanges();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                        finish();
                    } else {
                        updateLocationChanges();
                    }
                }
        }
    }

    @SuppressLint("MissingPermission")
    private void updateLocationChanges() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(GPS_PROVIDER, 1000, 1, this);
        }
        mCurrentLocation = mLocationManager.getLastKnownLocation(GPS_PROVIDER);
        updateButtonsStatus();
    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateButtonsStatus();
    }

    @Override
    public void onStatusChanged(String s, int status, Bundle bundle) {
        updateButtonsStatus();
    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(getApplicationContext(), R.string.gps_enabled, Toast.LENGTH_LONG).show();
        updateLocationChanges();
        updateButtonsStatus();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(getApplicationContext(), R.string.gps_disabled, Toast.LENGTH_LONG).show();
        mLocationManager = null;
        dissableButtons();
    }

    public void updateButtonsStatus() {
        if (mCurrentLocation != null) {
            mVideoActionButton.setEnabled(true);
            mVideoActionButton.setAlpha(1f);
            mPictureActionButton.setEnabled(true);
            mPictureActionButton.setAlpha(1f);
        } else {
            dissableButtons();
        }
    }

    private void dissableButtons() {
        mVideoActionButton.setEnabled(false);
        mVideoActionButton.setAlpha(0.4f);
        mPictureActionButton.setEnabled(false);
        mPictureActionButton.setAlpha(0.4f);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createFile(getString(R.string.jpeg_prefix), getString(R.string.jpg_suffix));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, getString(R.string.fileprovider_authority), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File videoFile = null;
            try {
                videoFile = createFile(getString(R.string.mp4_prefix), getString(R.string.mp4_suffix));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (videoFile != null) {
                Uri videoURI = FileProvider.getUriForFile(this, getString(R.string.fileprovider_authority), videoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_VIDEO);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                mCurrentLocation = mLocationManager.getLastKnownLocation(GPS_PROVIDER);
                addNewMedia(mCurrentFileName, mCurrentPath, 0, (float) mCurrentLocation.getLatitude(), (float) mCurrentLocation.getLongitude());
                mAdapter.swapCursor(getAllMedia());
            }
        }
        if (requestCode == REQUEST_TAKE_VIDEO) {
            if (resultCode == RESULT_OK) {
                mCurrentLocation = mLocationManager.getLastKnownLocation(GPS_PROVIDER);
                addNewMedia(mCurrentFileName, mCurrentPath, 1, (float) mCurrentLocation.getLatitude(), (float) mCurrentLocation.getLongitude());
                mAdapter.swapCursor(getAllMedia());
            }
        }
    }


    private File createFile(String prefix, String suffix) throws IOException {
        File storageDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.external_directory_child));
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat(getString(R.string.simple_date_format_pattern)).format(new Date());
        String fileName = prefix + timeStamp + suffix;
        File file = File.createTempFile(fileName, suffix, storageDir);
        mCurrentPath = file.getAbsolutePath();
        mCurrentFileName = fileName;
        return file;
    }


    private Cursor getAllMedia() {
        return mDb.query(
                MediaTable.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private boolean removeMedia(long id) {
        if (deleteMediaFromStorage(getPathFromMedia(id))) {
            return mDb.delete(MediaTable.TABLE_NAME, MediaTable._ID + "=" + id, null) > 0;
        }
        return false;
    }

    private boolean deleteMediaFromStorage(Uri uri) {
        File fdelete = new File(uri.getPath());
        if (fdelete.exists()) {
            return fdelete.delete();
        }
        return false;
    }

    private Uri getPathFromMedia(long id) {
        String[] selectionArgs = new String[]{"" + id};
        Cursor cursor = mDb.query(
                MediaTable.TABLE_NAME,
                new String[]{MediaTable.COLUMN_PATH},
                MediaTable._ID + "=?",
                selectionArgs,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        return Uri.parse(cursor.getString(cursor.getColumnIndex(MediaTable.COLUMN_PATH)));
    }

    private long addNewMedia(String name, String path, int isVideo, float latitude, float longitude) {
        ContentValues cv = new ContentValues();
        cv.put(MediaTable.COLUMN_FILE_NAME, name);
        cv.put(MediaTable.COLUMN_PATH, path);
        cv.put(MediaTable.COLUMN_IS_VIDEO, isVideo);
        cv.put(MediaTable.COLUMN_LATITUDE, latitude);
        cv.put(MediaTable.COLUMN_LONGITUDE, longitude);
        return mDb.insert(MediaTable.TABLE_NAME, null, cv);
    }
}
