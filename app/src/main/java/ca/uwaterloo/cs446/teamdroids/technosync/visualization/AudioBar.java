package ca.uwaterloo.cs446.teamdroids.technosync.visualization;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;
import java.util.Random;

public class AudioBar extends View {


    private Visualizer visualizer;
    private byte[] audioBytes;
    protected Paint paint;

    private static final int BAR_MAX_POINTS = 120;
    private static final int BAR_MIN_POINTS = 3;

    private int mMaxBatchCount = 4;

    private int nPoints;

    private float[] mSrcY, mDestY;

    private float mBarWidth;
    private Rect mClipBounds;

    private int nBatchCount;

    private Random mRandom;


    //View Constructors
    public AudioBar(Context context) {
        super(context);
        init();
    }
    public AudioBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public AudioBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    protected void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);

        nPoints = (int) (BAR_MAX_POINTS * 2);
        if (nPoints < BAR_MIN_POINTS)
            nPoints = BAR_MIN_POINTS;

        mBarWidth = -1;
        nBatchCount = 0;



        mRandom = new Random();

        mClipBounds = new Rect();

        mSrcY = new float[nPoints];
        mDestY = new float[nPoints];
    }


    //Link view with visualization data
    public void setPlayer(int audioSessionId) {
        visualizer = new Visualizer(audioSessionId);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                              int samplingRate) {
                AudioBar.this.audioBytes = bytes;
                invalidate();
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                                         int samplingRate) {
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);

        visualizer.setEnabled(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBarWidth == -1) {

            canvas.getClipBounds(mClipBounds);

            mBarWidth = canvas.getWidth() / nPoints;

            //initialize points
            for (int i = 0; i < mSrcY.length; i++) {
                float posY;

                    posY = mClipBounds.bottom;

                mSrcY[i] = posY;
                mDestY[i] = posY;
            }
        }

        //create the path and draw
        if (audioBytes != null) {

            if (audioBytes.length == 0) {
                return;
            }

            //find the destination bezier point for a batch
            if (nBatchCount == 0) {
                float randPosY = mDestY[mRandom.nextInt(nPoints)];
                for (int i = 0; i < mSrcY.length; i++) {

                    int x = (int) Math.ceil((i + 1) * (audioBytes.length / nPoints));
                    int t = 0;
                    if (x < 1024)
                        t = canvas.getHeight() +
                                ((byte) (Math.abs(audioBytes[x]) + 128)) * canvas.getHeight() / 128;

                    float posY;
                        posY = mClipBounds.top + t;

                    //change the source and destination y
                    mSrcY[i] = mDestY[i];
                    mDestY[i] = posY;
                }

                mDestY[mSrcY.length - 1] = randPosY;
            }

            //increment batch count
            nBatchCount++;

            //calculate bar position and draw
            for (int i = 0; i < mSrcY.length; i++) {
                float barY = mSrcY[i] + (((float) (nBatchCount) / mMaxBatchCount) * (mDestY[i] - mSrcY[i]));
                float barX = (i * mBarWidth) + (mBarWidth / 2);
                canvas.drawLine(barX, canvas.getHeight(), barX, barY, paint);
            }

            //reset the batch count
            if (nBatchCount == mMaxBatchCount)
                nBatchCount = 0;

        }

        super.onDraw(canvas);
    }


}
