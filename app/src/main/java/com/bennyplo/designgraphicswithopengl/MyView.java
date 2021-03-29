package com.bennyplo.designgraphicswithopengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;

public class MyView extends GLSurfaceView {
    private final MyRenderer mRenderer;
    private Timer timer;
    private final float TOUCH_SCALE_FACTOR = 100.0f / 1080;
    private float mPreviousX;
    private float mPreviousY;

    public MyView(Context context) {
        super(context);
        setEGLContextClientVersion(2);// Create an OpenGL ES 2.0 context.
        mRenderer = new MyRenderer();// Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        timer = new Timer();
        TimerTask task = new TimerTask() {
            float angle = 0;
            float px = 0, py = 0, pz = 0;
            boolean dir = true;

            @Override
            public void run() {
//                mRenderer.setAngle(angle);
//                mRenderer.setXAngle(angle);
                mRenderer.setLightLocation(px, py, pz);
                requestRender();
//                angle+=1;
//
//                if (angle == 360) {
//                    angle = 0;
//                }

                if (dir) {
                    px += 0.1f;
                    py += 0.1f;
                    if (px >= 10) {
                        dir = false;
                    }
                } else {
                    px -= 0.1f;
                    py -= 0.1f;

                    if (px <= -10) {
                        dir = true;
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 10);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch(e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                if (y > getHeight() / 2) {
                    dx = dx * -1;
                }
                if (x < getWidth() / 2) {
                    dy = dy * -1;
                }
                mRenderer.setAngle(
                        mRenderer.getAngle() + (dx * TOUCH_SCALE_FACTOR)
                );
                mRenderer.setXAngle(
                        mRenderer.getXAngle() + (dy * TOUCH_SCALE_FACTOR)
                );

                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}
