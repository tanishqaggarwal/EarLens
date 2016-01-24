package org.earlens.earlens;

import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.util.Log;


/**
 * Created by Tanishq on 1/23/16.
 */
public class MyFaceDetectionListener implements Camera.FaceDetectionListener {

    @Override
    public void onFaceDetection(Face[] faces, Camera camera) {
        if (faces.length > 0){
            Log.d("FaceDetection", "face detected: "+ faces.length +
                    " Face 1 Location X: " + faces[0].rect.centerX() +
                    "Y: " + faces[0].rect.centerY() );
        }
    }
}
