package ca.uwaterloo.cs446.teamdroids.technosync.visualization;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.audiofx.Visualizer;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Rectangle;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;
import java.util.Random;

public class AudioBar extends View {


    private Visualizer visualizer;
    private byte[] audioBytes;
    protected Paint paint;

    private Path mSpikePath;
    private int mRadius;
    private int nPoints = 1000;

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

        mRadius = -1;

        mSpikePath = new Path();
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

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect((float) 0.0 , (float) 0.0, (float)canvas.getWidth(), (float)canvas.getHeight(), paint);


        paint.setColor(Color.RED);


        for(int i = 0; i < audioBytes.length; i++) {

            float x = audioBytes[i];
            float value = (float) audioBytes[i] / 128;
            float width = (float) canvas.getWidth() / audioBytes.length;
            float height =  (float) (canvas.getHeight() / 2) * value;

            float lx = i * width;
            float ly = (float) canvas.getHeight() / 2;
            float rx = (i + 1) * width;
            float ry = ((float)canvas.getHeight() / 2) + height;

            RectF rectF = new RectF();
            if(ry < ly){
                rectF.set(lx, ry, rx, ly);
            }
            else{
                rectF.set(lx, ly, rx, ry);
            }



            paint.setColor(Color.RED);
            canvas.drawRect(rectF, paint);


            paint.setColor(Color.WHITE);
            canvas.drawText(String.valueOf(audioBytes[i]), 0, 0, paint);
        }


        super.onDraw(canvas);
    }


}
