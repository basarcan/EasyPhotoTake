package easyphoto.easyphoto;


import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;

import easyphoto.easyphoto.VoiceRecognition.SpeechRecognizerManager;

public class ShowPicture extends AppCompatActivity {

    public ImageView viewBitmap;
    public MediaPlayer mp;
    public String LOG_TAG = "LOG_TAG";

    private SpeechRecognizerManager mSpeechManager;
    private String speechTextHolder;


    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.show_picture);
        getTakenPhoto();

        SetSpeechListener();
    }

    public void getTakenPhoto()
    {
        Bundle bundle = getIntent().getExtras();
        String path = bundle.getString("path");
        viewBitmap = (ImageView) findViewById(R.id.showImage);
        viewBitmap.setImageBitmap(BitmapFactory.decodeFile(path));

        String key = path;
        String imageArray[] = key.split(" ");
        key = "/storage/emulated/0/" + imageArray[2] + ".3gp";

        mp = new MediaPlayer();
        try {
            mp.reset();
            mp.setDataSource(getBaseContext(), Uri.parse(key));
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
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
                        if(part[i].equals("geri"))
                        {
                            mSpeechManager.destroy();///sonradan eklendi dikkat!
                            Intent gallery = new Intent();
                            gallery.setClass(ShowPicture.this, Gallery.class);
                            startActivity(gallery);
                            break;
                        }

                    }
                }
                else
                    Log.d("Noresult","");

            }
        });
    }

}

