package easyphoto.easyphoto;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import easyphoto.easyphoto.ui.camera.GraphicOverlay;

import com.google.android.gms.vision.face.Face;

import easyphoto.easyphoto.ui.camera.GraphicOverlay;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private Context context;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;
    private Paint mLinePaint;




    public interface IDataCallback {
        void sayLeft();
    }

    private IDataCallback callerActivity;

    public FaceGraphic(Activity activity){
        super();
        callerActivity = (IDataCallback)activity;
    }




    private volatile Face mFace;
    private int mFaceId;
    public int speechTrick = 0;
    private boolean boolLeft = false;
    private boolean boolRight = false;
    private float mFaceHappiness;

    FaceGraphic(Context context,GraphicOverlay overlay) {
        super(overlay);
        this.context = context;

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.YELLOW);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;

        /** ------------------------------------------Sonradan eklenen bölüm -----------------------------------------------**/
        float boxWidth = right-left;


        int faceSum = this.mOverlay.mGraphics.size() + 2; /// ekranda beliren kişilerin kafa sayısını verir

        int screenBlocks = 720 / faceSum;


        if(left < screenBlocks && right < 720 - screenBlocks)
        {
            if(speechTrick == 0 || speechTrick == 70)
            {
                Log.d("Mesafeler", "hafif sol");
                MediaPlayer leftMedia = MediaPlayer.create(context,R.raw.left);
                leftMedia.start();

                speechTrick = 0;
                boolLeft = true;
            }
            speechTrick += 1;
        }
        else if(left > 720 - screenBlocks && right >720 - screenBlocks)
        {
            if(speechTrick == 0 || speechTrick == 70)
            {
                Log.d("Mesafeler", "hafif sağ");
                MediaPlayer rightMedia = MediaPlayer.create(context, R.raw.right);
                rightMedia.start();

                speechTrick = 0;
                boolRight = true;
            }
            speechTrick += 1;
        }
        else if(left > screenBlocks && right < 720 - screenBlocks)
        {
            //if(speechTrick == 0 || speechTrick == 70) {
            if(boolLeft || boolRight)
            {
                Log.d("Mesafeler", "hazırsın");
                MediaPlayer hazir = MediaPlayer.create(context, R.raw.ready);
                hazir.start();

                speechTrick = 0;
            }
            /* else if (!boolLeft && !boolRight) {
                    Log.d("Mesafeler", "hazırsın");
                    MediaPlayer ready = MediaPlayer.create(context, R.raw.ready);
                    ready.start();

                    speechTrick = 0;
                } */
            speechTrick += 1;
            //}
            boolLeft = false;
            boolRight = false;
        }

        /** --------------------------------------------------------------------------------------------------------------- **/
        canvas.drawRect(left, top, right, bottom, mBoxPaint);
    }
}
