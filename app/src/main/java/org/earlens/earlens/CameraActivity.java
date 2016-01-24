package org.earlens.earlens;

import android.os.Bundle;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.view.LayoutInflater;

import android.view.Window;
import android.view.WindowManager;

public class CameraActivity extends BaseSensorManager implements Camera.FaceDetectionListener {

    private String TAG = "EarLens";

    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        ViewGroup mFrameLayout;
        mFrameLayout = (ViewGroup) findViewById(R.id.camera_preview);
        assert mFrameLayout != null;
        LinearLayout bubble = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.speech_bubble, mFrameLayout, false);
        assert bubble != null;

        TextView message_bubble = (TextView) bubble.findViewById(R.id.message_text);
        assert message_bubble != null;
        message_bubble.setText("This fucker was found!");
        ((ViewGroup)message_bubble.getParent()).removeView(message_bubble);
        message_bubble.setX(0);
        message_bubble.setY(0);
        mFrameLayout.addView(message_bubble);
        //message_bubble.setVisibility(View.INVISIBLE);
        //Set to zero

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        mFrameLayout.addView(mPreview);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if (faces.length > 0) {
            float x = faces[0].rect.centerX();
            float y = faces[0].rect.centerY();
            Log.d(TAG, "FOUND FACE 1 AT X = " + x + " AND Y = " + y);
            findViewById(R.id.message_text).setX(x - 100);
            findViewById(R.id.message_text).setY(y - 150);
            findViewById(R.id.message_text).setVisibility(View.VISIBLE);
        }
    }


}
