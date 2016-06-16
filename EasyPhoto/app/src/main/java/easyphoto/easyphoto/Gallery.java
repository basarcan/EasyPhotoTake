package easyphoto.easyphoto;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;

import easyphoto.easyphoto.VoiceRecognition.SpeechRecognizerManager;


public class Gallery extends AppCompatActivity {


    /** The images. */
    private ArrayList<String> images;
    private String LOG_TAG = "LOG_TAG";

    public MediaPlayer mp;

    private SpeechRecognizerManager mSpeechManager;
    private String speechTextHolder;

    private boolean isInteger = false;
    private String longPath;
    private String[] imagePaths;
    private String[] imageNames;
    private String imageName;
    private String imageDate;
    private String  imageTime;
    private int imagePos;


    public boolean booleanDeneme;
    public String stringDeneme;
    public int intDeneme;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.helper);




        MediaPlayer mMedia = MediaPlayer.create(this, R.raw.gallery);
        mMedia.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 250);

        SetSpeechListener();




        GridView gallery = (GridView) findViewById(R.id.galleryGridView);

        gallery.setAdapter(new ImageAdapter(this));

        gallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                if (null != images && !images.isEmpty()) {


                    /*
                    String key = images.get(position);
                    String imageArray[] = key.split(" ");
                    key = "/storage/emulated/0/" + imageArray[2] + ".3gp";

                    mp = new MediaPlayer();
                    try {
                        mp.reset();
                        mp.setDataSource(getBaseContext(),Uri.parse(key));
                        mp.prepare();
                        mp.start();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "prepare() failed");
                    }*/

                    mSpeechManager.destroy();
                    Intent intent = new Intent();
                    intent.setClass(Gallery.this, ShowPicture.class);
                    intent.putExtra("path", images.get(position));
                    startActivity(intent);

                    /*Toast.makeText(
                            getApplicationContext(),
                            "position " + position + " " + images.get(position),
                            300).show();}*/

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
                    Log.d("Söylenen", speechTextHolder);
                    for(int i=0; i < part.length; i++)
                    {
                        if(part[i].equals("geri"))
                        {
                            mSpeechManager.destroy();///sonradan eklendi dikkat!
                            Intent gallery = new Intent();
                            gallery.setClass(Gallery.this, FaceTrackerActivity.class);
                            startActivity(gallery);
                            break;
                        }
                    }
                   isInteger = speechTextHolder.matches(("\\d+"));
                    if(isInteger)
                    {
                        imagePaths = images.toArray(new String[0]);
                        speechTextHolder = "Image" + results.get(0)+ ".jpg";
                        for(int i=0;i<= imagePaths.length;i++)  ///////////////////////fotonun length ini ve bulunduğu yeri gösterir.
                        {
                            imageNames = imagePaths[i].split(" ");
                            stringDeneme = imageNames[2];
                            if(stringDeneme.equals(speechTextHolder))
                            {
                                imageName = imageNames[2];
                                imageDate = imageNames[0];
                                imageTime = imageNames[1];
                                imagePos = i;
                                break;
                            }
                        }


                        mSpeechManager.destroy();
                        longPath = imageDate+ " " + imageTime + " " + imageName;

                        Intent intent = new Intent();
                        intent.setClass(Gallery.this, ShowPicture.class);
                        intent.putExtra("path", longPath);
                        startActivity(intent);
                    }


                }
                else
                    Log.d("Noresult","");

            }
        });
    }


    private class ImageAdapter extends BaseAdapter {


        private Activity context;

        public ImageAdapter(Activity localContext) {
            context = localContext;
            images = getAllShownImagesPath(context);


        }

        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ImageView picturesView;
            if (convertView == null) {
                picturesView = new ImageView(context);
                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picturesView
                        .setLayoutParams(new GridView.LayoutParams(270, 270));

            } else {
                picturesView = (ImageView) convertView;
            }

            Glide.with(context).load(images.get(position))
                    .placeholder(R.drawable.icon).centerCrop()
                    .into(picturesView);

            return picturesView;
        }


        public ArrayList<String> getAllShownImagesPath(Activity activity) {
            Uri uri;
            Cursor cursor;
            int column_index_data, column_index_folder_name;
            ArrayList<String> listOfAllImages = new ArrayList<String>();
            String absolutePathOfImage = null;
            uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = { MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

            cursor = activity.getContentResolver().query(uri, projection, null,
                    null, null);

            column_index_data = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
            column_index_folder_name = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(column_index_data);

                listOfAllImages.add(absolutePathOfImage);
            }
            return listOfAllImages;
        }
    }
}