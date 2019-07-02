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

    private static final int NUMBER_OF_BARS = 32;
    private static final int BAR_COLOUR = 0x80100c05;

    private Visualizer visualizer;
    private byte[] audioBytes;
    protected Paint paint;

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
        paint.setColor(BAR_COLOUR);
    }

    //Link view with visualization data
    //Set player functionality modelled after MIT Licensed Visualiztion Framework
    //https://github.com/gauravk95/audio-visualizer-android/blob/master/audiovisualizer/src/main/java/com/gauravk/audiovisualizer/visualizer/BarVisualizer.java
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

    //On Draw just draws a simplified waveform
    @Override
    protected void onDraw(Canvas canvas) {

        //Normalize waveform
        float[] normalizedWaveForm = new float[audioBytes.length];
        for(int i = 0; i < audioBytes.length; i++){
            normalizedWaveForm[i] = ((float) audioBytes[i] + 128) / 256;
        }


        //Draw bars
        for(int i = 0; i < NUMBER_OF_BARS; i++) {

            //Get value based on current sample point
            int barWidthtoWaveForm = normalizedWaveForm.length / NUMBER_OF_BARS;
            int samplingIndex = (i * barWidthtoWaveForm) / 2;
            float value = normalizedWaveForm[samplingIndex];

            //Calculate bar width and height in relation to on-screen view
            float width = (float) canvas.getWidth() / NUMBER_OF_BARS;
            float height =  (float) canvas.getHeight() * value;

            //Calculate bar coordinates
            float lx = i * width;
            float ly = (float) canvas.getHeight() - height;
            float rx = (i + 1) * width;
            float ry = ((float)canvas.getHeight());


            //Draw rectangle
            RectF rectF = new RectF();
            rectF.set(lx, ly, rx, ry);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(rectF, paint);
        }


        super.onDraw(canvas);
    }


}
