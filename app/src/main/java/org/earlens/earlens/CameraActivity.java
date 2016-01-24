package org.earlens.earlens;

import android.os.Bundle;
import android.hardware.Camera;
import android.text.Layout;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.view.LayoutInflater;

import android.view.Window;
import android.view.WindowManager;

import android.view.ViewGroup.LayoutParams;

import java.io.IOException;

public class CameraActivity extends BaseSensorManager implements SurfaceHolder.Callback{

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    LayoutInflater textInflator = null;
    boolean previewing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        surfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        textInflator = LayoutInflater.from(getBaseContext());
        View textView = textInflator.inflate(R.layout.speech_bubble, null);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        this.addContentView(textView, layoutParams);
    }



    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if (faces.length > 0) {
            float x = faces[0].rect.centerX();
            float y = faces[0].rect.centerY();
            Log.d("CameraActivity", "FOUND FACE 1 AT X = " + x + " AND Y = " + y);
            findViewById(R.id.message_text).setX(x - 100);
            findViewById(R.id.message_text).setY(y - 150);
            findViewById(R.id.message_text).setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (previewing) {
            camera.stopPreview();
            previewing = false;
        }
        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }
}
