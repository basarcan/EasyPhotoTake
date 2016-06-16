package easyphoto.easyphoto;

import android.Manifest;

import android.content.ContentValues;
import android.content.Intent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;

import android.media.MediaPlayer;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

import android.os.Bundle;
import android.os.Environment;


import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import easyphoto.easyphoto.ui.camera.CameraSourcePreview;
import easyphoto.easyphoto.ui.camera.GraphicOverlay;

import easyphoto.easyphoto.VoiceRecognition.Permission;
import easyphoto.easyphoto.VoiceRecognition.SpeechRecognizerManager;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.sql.Timestamp;
import java.util.ArrayList;


/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public class FaceTrackerActivity extends AppCompatActivity implements FaceGraphic.IDataCallback {
    private static final String TAG = "EasyPhoto";

    private CameraSource mCameraSource = null;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;


    public int rotation;
    public File dir;


    private SpeechRecognizerManager mSpeechManager;
    private String speechTextHolder;


    private int mNumber;
    private int speechTrick = 1;

    public boolean deneme = true;

    private boolean backCamera=true;
    private boolean frontCamera=false;



    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);


        /** Check for the camera permission before accessing the camera.  If the
         permission is not granted yet, request permission.**/
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED || Permission.checkPermission(this,Permission.RECORD_AUDIO)) {
            verifyStoragePermissions(FaceTrackerActivity.this);
            createCameraSource();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            if(preferences.getBoolean("introduction",true))
            {
                MediaPlayer intro = MediaPlayer.create(this, R.raw.introduction);
                intro.start();
                editor.putBoolean("introduction", false);
                deneme=false;
                editor.commit();
            }


                if(mSpeechManager==null)
                {
                    SetSpeechListener();
                }else if (!mSpeechManager.ismIsListening()) {
                    mSpeechManager.destroy();
                    SetSpeechListener();
                }



        } else {
            requestCameraPermission();
        }





        /*findViewById(R.id.capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                {
                    private File imageFile;

                    @Override
                    public void onPictureTaken(byte[] bytes) {
                        try {
                            // convert byte array into bitmap
                            Bitmap loadedImage = null;
                            Bitmap rotatedBitmap = null;
                            loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                                    bytes.length);

                            Matrix rotateMatrix = new Matrix();
                            rotateMatrix.postRotate(rotation);
                            rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
                                    loadedImage.getWidth(), loadedImage.getHeight(),
                                    rotateMatrix, false);

                            dir = new File(
                                    Environment.getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_PICTURES), "CameraAppDemo");

                            boolean success = true;
                            if (!dir.exists()) {
                                success = dir.mkdirs();
                            }
                            if (success) {
                                java.util.Date date = new java.util.Date();
                                imageFile = new File(dir.getAbsolutePath()
                                        + File.separator
                                        + new Timestamp(date.getTime()).toString()
                                        + "Image.jpg");

                                imageFile.createNewFile();
                            } else {
                                Toast.makeText(getBaseContext(), "Image Not saved",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ByteArrayOutputStream ostream = new ByteArrayOutputStream();

                            // save image into gallery
                            rotatedBitmap.compress(CompressFormat.JPEG, 100, ostream);

                            FileOutputStream fout = new FileOutputStream(imageFile);
                            fout.write(ostream.toByteArray());
                            fout.close();
                            ContentValues values = new ContentValues();

                            values.put(Images.Media.DATE_TAKEN,
                                    System.currentTimeMillis());
                            values.put(Images.Media.MIME_TYPE, "image/jpeg");
                            values.put(MediaStore.MediaColumns.DATA,
                                    imageFile.getAbsolutePath());

                            FaceTrackerActivity.this.getContentResolver().insert(
                                    Images.Media.EXTERNAL_CONTENT_URI, values);

                            //saveToInternalStorage(loadedImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });*/

    }

    /** Dokunmatik foto çekme **/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex = event.getActionIndex();

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {

            case MotionEvent.ACTION_UP:
            {
                mSpeechManager.destroy();

                takePhoto();/** Foto çekme fonksiyonu **/
                break;
            }
        }
        return true;

    }



    private void takePhoto() {
        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {

            private File imageFile;


            @Override
            public void onPictureTaken(byte[] bytes) {
                try {
                    // convert byte array into bitmap
                    Bitmap loadedImage = null;
                    Bitmap rotatedBitmap = null;
                    loadedImage = BitmapFactory.decodeByteArray(bytes, 0,
                            bytes.length);



                    Matrix rotateMatrix = new Matrix();
                    rotateMatrix.postRotate(rotation);
                    rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0,
                            loadedImage.getWidth(), loadedImage.getHeight(),
                            rotateMatrix, false);

                   /* mBitmap = rotatedBitmap;

                    Intent intent = new Intent();
                    intent.setClass(FaceTrackerActivity.this,VoiceTag.class);
                    intent.putExtra("BitmapImage",mBitmap);
                    startActivity(intent);*/

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    if(preferences.getBoolean("numTrick",true))
                    {
                        mNumber = 1;
                        editor.putInt("imageNum", mNumber);
                        editor.putBoolean("numTrick", false);
                        editor.commit();
                    }
                    else
                    {
                        mNumber = preferences.getInt("imageNum", -1);
                        mNumber += 1;
                        editor.putInt("imageNum", mNumber);
                        editor.commit();
                    }


                    dir = new File(
                            Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES), "EasyPhoto");

                    boolean success = true;
                    if (!dir.exists()) {
                        success = dir.mkdirs();//If there exist return true or false
                    }
                    if (success) {
                        java.util.Date date = new java.util.Date();
                        imageFile = new File(dir.getAbsolutePath()
                                + File.separator
                                + new Timestamp(date.getTime()).toString()
                                + " Image"+ mNumber +".jpg");

                        imageFile.createNewFile();
                    } else {
                        Toast.makeText(getBaseContext(), "Image Not saved",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ByteArrayOutputStream ostream = new ByteArrayOutputStream();

                    // save image into gallery
                    rotatedBitmap.compress(CompressFormat.JPEG, 100, ostream);

                    FileOutputStream fout = new FileOutputStream(imageFile);
                    fout.write(ostream.toByteArray());
                    fout.close();
                    ContentValues values = new ContentValues();

                    values.put(Images.Media.DATE_TAKEN,
                            System.currentTimeMillis());
                    values.put(Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.MediaColumns.DATA,
                            imageFile.getAbsolutePath());

                    FaceTrackerActivity.this.getContentResolver().insert(
                            Images.Media.EXTERNAL_CONTENT_URI, values);

                    //saveToInternalStorage(loadedImage);

                    //When it saved find the path and call it secondActivity for voicetag
                    //mSpeechManager.destroy();
                    Intent intent = new Intent();
                    intent.setClass(FaceTrackerActivity.this, VoiceTag.class);
                    intent.putExtra("ImagePath", imageFile.getAbsolutePath());
                    intent.putExtra("num",mNumber);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



    public void SetSpeechListener()
    {
        mSpeechManager=new SpeechRecognizerManager(this, new SpeechRecognizerManager.onResultsReady() {
            @Override
            public void onResults(ArrayList<String> results) {

                if(results!=null && results.size()>0)
                {
                    speechTextHolder = results.get(0);
                    String part[] = speechTextHolder.split(" ");
                    Log.d("Söylenen",speechTextHolder);
                    for(int i=0; i < part.length; i++)
                    {
                        if(part[i].equals("çek") || part[i].equals("ç") || part[i].equals("foto"))
                        {
                            mSpeechManager.destroy();
                            takePhoto();
                            break;
                        }
                        else if(part[i].equals("galeri"))
                        {
                            mSpeechManager.destroy();
                            Intent gallery = new Intent();
                            gallery.setClass(FaceTrackerActivity.this, Gallery.class);
                            //gallery.putExtra("ImagePath", imageFile.getAbsolutePath());
                            startActivity(gallery);
                            break;
                        }
                        else if(part[i].equals("deneme"))
                        {
                            mPreview.stop();
                            startCameraSource();
                            createCameraSourceFront();
                            mPreview.stop();
                            startCameraSource();
                            createCameraSourceFront();

                        }
                    }
                    //Log.d("speecTextHolder", speechTextHolder);
                    //Log.d("speecTextSplitHolder", speechTextSplitHolder);


                    /*if(results.size()==1)
                    {
                        mSpeechManager.destroy();
                        mSpeechManager = null;

                    }
                    else {
                        StringBuilder sb = new StringBuilder();
                        if (results.size() > 5) {
                            results = (ArrayList<String>) results.subList(0, 5);
                        }
                        for (String result : results) {
                            sb.append(result).append("\n");
                        }
                        speechTextHolder = results.get(0);
                        Log.d("speechTextHolder", results.get(0));
                        if(speechTextHolder.equals("çek"))
                        {
                            takePhoto();
                            speechTextHolder = null;
                        }
                    }*/
                }
                else
                    Log.d("Noresult","");

            }
        });
    }


    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(1920, 1080)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build();

    }
    private void createCameraSourceFront() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(1920, 1080)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();

    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {

        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
            mSpeechManager.destroy();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();

        switch(requestCode)
        {
            case Permission.RECORD_AUDIO:
                if(grantResults.length>0) {
                    if(grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                        SetSpeechListener();
                    }
                }
                break;

        }
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(getBaseContext(),overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    public void sayLeft(){
        MediaPlayer sLeft = MediaPlayer.create(this,R.raw.left);
        sLeft.start();
    }

}
