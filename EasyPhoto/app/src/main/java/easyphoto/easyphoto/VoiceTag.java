package easyphoto.easyphoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.media.MediaPlayer.OnErrorListener;

import android.widget.Toast;



import java.io.File;
import java.io.IOException;



public class VoiceTag extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private String imagePath;

    private static String mFileName = null;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    //private PlayButton   mPlayButton = null;
    private MediaPlayer mPlayer = null;

    public String mStringNumber;
    public boolean trick;



    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.voice_tag);
        getTakenPhoto();


        Bundle bundle = getIntent().getExtras();
        int mNumber = bundle.getInt("num");
        mStringNumber = ""+mNumber;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                speachText(mStringNumber);
            }
        }, 3800);
        //speachText(mStringNumber);

 MediaPlayer mMedia = MediaPlayer.create(this, R.raw.tag);
        mMedia.start();



        LinearLayout ll = new LinearLayout(this);
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0));
       /* mPlayButton = new PlayButton(this);
        ll.addView(mPlayButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));*/
        setContentView(ll);

    }

    public void getTakenPhoto(){
        Bundle bundle = getIntent().getExtras();
        String path = bundle.getString("ImagePath");
        imagePath = path;
        setContentView(R.layout.voice_tag);
        ImageView viewBitmap = (ImageView) findViewById(R.id.showImage);
        viewBitmap.setImageBitmap(BitmapFactory.decodeFile(path));


        String imageArray[] = imagePath.split(" ");
        imagePath = imageArray[2];
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName +="/" + imagePath + ".3gp";
    }



    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        Intent intent = new Intent();
        intent.setClass(VoiceTag.this, FaceTrackerActivity.class);
        startActivity(intent);


    }

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                    Toast.makeText(getBaseContext(),mFileName,Toast.LENGTH_SHORT).show();
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

   /* class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }*/

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }


    public void speachText(final String textToSpeach){

        final Context context = getBaseContext();

        new Thread(null, new Runnable() {
            public void run() {

                try {
                    String baseURLFormat = "https://translate.google.com/translate_tts?ie=UTF-8&q=";
                    String speachTextAPIURL = baseURLFormat + "Bu%20fotoğrafın%20adı%20" + textToSpeach + "&tl=tr-TW&client=tw-ob";
                    Uri speachTextAPIURI = Uri.parse(speachTextAPIURL);

                    final MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(context, speachTextAPIURI);
                    trick = false;

                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer arg0) {
                            mediaPlayer.start();
                            trick = true;
                        }
                    });

                    if(!trick)
                    {
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer arg0) {
                                mediaPlayer.start();
                            }
                        });
                    }


                    mediaPlayer.prepareAsync();
                    // mediaPlayer.start();

                    // Ses bitince yonlendirme baslatilir
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            // Tanitim bitti yonlendirme yapilabilir

                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                }
                            }, 500);

                            mediaPlayer.release();
                        }
                    });

                    mediaPlayer.setOnErrorListener(new OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            mediaPlayer.release();
                            return false;
                        }
                    });

                } catch (Exception e) {

                    e.printStackTrace();
                }

            }
        }).start();
    }


}
