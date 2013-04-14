/* -----------------------------------------------------------------------
 * Programmer:   padde
 * Date:         April 8, 2009
 * Platform:     Android
 * Description:  MJPEG Viewer
 * Dependencies: 
 * -----------------------------------------------------------------------
 * Code was taken from:
 *     http://www.anddev.org/mjpeg_on_android_anyone-t1871.html
 *     
 *     
 *     
 * Developed by padde.
 * -----------------------------------------------------------------------
 */

package me.codyhazelwood.research.robot_control;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressLint("ViewConstructor")
public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "MjpegView";

    public final static int POSITION_UPPER_LEFT  = 9;
    public final static int POSITION_UPPER_RIGHT = 3;
    public final static int POSITION_LOWER_LEFT  = 12;
    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_STANDARD   = 1; 
    public final static int SIZE_BEST_FIT   = 4;
    public final static int SIZE_FULLSCREEN = 8;

    private MjpegViewThread thread;
    private MjpegInputStream mIn = null;
    private boolean mRun = false;
    private boolean surfaceDone = false;    
    private Paint overlayPaint;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;

    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;

        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) {
            mSurfaceHolder = surfaceHolder;
        }

        private Rect destRect(int bmw, int bmh) {
            int tempx;
            int tempy;
            if (displayMode == MjpegView.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_BEST_FIT) {
                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_FULLSCREEN){
                return new Rect(0, 0, dispWidth, dispHeight);
            }
            return null;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized(mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }

        public void run() {
            Bitmap bm;
            Rect destRect;
            Canvas c = null;
            Paint p = new Paint();
            while (mRun) {
                if(surfaceDone) {
                    try {
                        c = mSurfaceHolder.lockCanvas();
                        synchronized (mSurfaceHolder) {
                            try {
                                bm = mIn.readMjpegFrame();
                                destRect = destRect(bm.getWidth(),bm.getHeight());
                                c.drawColor(Color.BLACK);
                                c.drawBitmap(bm, null, destRect, p);
                            } catch (IOException e) {
                                e.getStackTrace();
                                Log.d(TAG, "catch IOException hit in run", e);
                            }
                        }
                    } finally { 
                        if (c != null) {
                            mSurfaceHolder.unlockCanvasAndPost(c); 
                        }
                    }
                }
            }
        }
    }

    private void init(Context context, SurfaceView view) {
        SurfaceHolder holder = view.getHolder();
        holder.addCallback(this);
        thread = new MjpegViewThread(holder, context);
        setFocusable(true);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
        displayMode = MjpegView.SIZE_STANDARD;
        dispWidth = getWidth();
        dispHeight = getHeight();
    }

    public void startPlayback() { 
        if(mIn != null) {
            mRun = true;
            thread.start();         
        }
    }

    public void stopPlayback() { 
        mRun = false;
        boolean retry = true;
        while(retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.getStackTrace();
                Log.d(TAG, "catch IOException hit in stopPlayback", e);
            }
        }
    }

    public MjpegView(Context context, AttributeSet attrs, SurfaceView view) { 
        super(context, attrs); init(context, view); 
    }

    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) { 
        thread.setSurfaceSize(w, h); 
    }

    public void surfaceDestroyed(SurfaceHolder holder) { 
        surfaceDone = false; 
        stopPlayback(); 
    }

    public MjpegView(Context context, SurfaceView view) { 
        super(context);
        init(context, view); 
    }

    public void surfaceCreated(SurfaceHolder holder) { 
        surfaceDone = true; 
    }

    public void setSource(MjpegInputStream source) { 
        mIn = source;
        startPlayback();
    }

    public void setOverlayPaint(Paint p) { 
        overlayPaint = p; 
    }

    public void setDisplayMode(int s) { 
        displayMode = s; 
    }
}