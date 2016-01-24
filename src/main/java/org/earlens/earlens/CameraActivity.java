package org.earlens.earlens;

import android.content.Context;
import android.hardware.Camera;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.hound.android.sdk.VoiceSearch;
import com.hound.android.sdk.VoiceSearchInfo;
import com.hound.android.sdk.VoiceSearchListener;
import com.hound.android.sdk.audio.SimpleAudioByteStreamSource;
import com.hound.android.sdk.util.HoundRequestInfoFactory;
import com.hound.core.model.sdk.HoundRequestInfo;
import com.hound.core.model.sdk.HoundResponse;
import com.hound.core.model.sdk.PartialTranscript;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import static android.hardware.Camera.Face;
import static android.hardware.Camera.FaceDetectionListener;

public class CameraActivity extends BaseSensorManager implements SurfaceHolder.Callback{

    Camera camera = null;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    LayoutInflater textInflator = null;
    boolean previewing = false;

    private TextView textView;

    private VoiceSearch voiceSearch;

    private LocationManager locationManager;

    private JsonNode lastConversationState;

    FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
        private int counter = 0;
        @Override
        public void onFaceDetection(Face[] faces, Camera camera) {
            if (faces.length > 0) {
                int left = faces[0].rect.left;
                int right = faces[0].rect.right;
                int top = faces[0].rect.top;
                int bottom = faces[0].rect.bottom;
                Log.d("CameraActivity", "FOUND FACE 1 AT LEFT = " + left + " RIGHT = " + right + " TOP = " + top + " BOTTOM = " + bottom);
                FrameLayout bubble = (FrameLayout) findViewById(R.id.speech_bubble);
                LayoutParams layoutParams = new LayoutParams(bubble.getWidth(), bubble.getHeight());
                layoutParams.setMargins(-top + 100, left + 500, 0, 0);
                bubble.setLayoutParams(layoutParams);
            }
        }
    };

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
        View bubble = textInflator.inflate(R.layout.speech_bubble, null);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        this.addContentView(bubble, layoutParams);

        textView = (TextView)findViewById(R.id.message_text);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
            camera.setFaceDetectionListener(faceDetectionListener);
            camera.startFaceDetection();
            camera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            startSearch();
            return;
        }
        startSearch();
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
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera.setFaceDetectionListener(null);
            camera = null;
            previewing = false;
        }
        if (voiceSearch != null) {
            voiceSearch.abort();
        }
    }

    public void onStop() {
        super.onStop();
        if (voiceSearch != null) {
            Log.d("BING", "onStop ABORT!!!");
            voiceSearch.abort();
        }
    }

    public void onPause() {
        super.onPause();
        if (voiceSearch != null) {
            Log.d("BING", "onPause ABORT!!!");
            voiceSearch.abort();
        }
    }

    public void onResume() {
        super.onResume();
        startSearch();
    }

    private HoundRequestInfo getHoundRequestInfo() {
        final HoundRequestInfo requestInfo = HoundRequestInfoFactory.getDefault(this);

        requestInfo.setUserId("User ID");
        requestInfo.setRequestId(UUID.randomUUID().toString());
        RequestInfoHelper.setLocation(requestInfo, locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));

        // for the first search lastConversationState will be null, this is okay.  However any future
        // searches may return us a conversation state to use.  Add it to the request info when we have one.
        requestInfo.setConversationState(lastConversationState);

        return requestInfo;
    }

    private void startSearch() {
        if (voiceSearch != null) {
            return;
        }

        setProgressBarIndeterminateVisibility(true);

        voiceSearch = new VoiceSearch.Builder()
                .setRequestInfo(getHoundRequestInfo())
                .setAudioSource(new SimpleAudioByteStreamSource())
                .setClientId(Constants.CLIENT_ID)
                .setClientKey(Constants.CLIENT_KEY)
                .setListener(voiceListener)
                .build();


        voiceSearch.start();
    }

    private final VoiceSearchListener voiceListener = new VoiceSearchListener() {
        @Override
        public void onTranscriptionUpdate(final PartialTranscript transcript) {
            if (transcript.getPartialTranscript() != "")
                textView.setText(transcript.getPartialTranscript());
        }

        @Override
        public void onResponse(final HoundResponse response, final VoiceSearchInfo info) {
            voiceSearch = null;
//
//            if (!response.getResults().isEmpty()) {
//                // Save off the conversation state.  This information will be returned to the server
//                // in the next search. Note that at some point in the future the results CommandResult list
//                // may contain more than one item. For now it does not, so just grab the first result's
//                // conversation state and use it.
//                lastConversationState = response.getResults().get(0).getConversationState();
//            }
//
//            // We put pretty printing JSON on a separate thread as the server JSON can be quite large and will stutter the UI
//
//            // Not meant to be configuration change proof, this is just a demo
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    final String finalMessage = response.getResults().get(0).getWrittenResponse();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            textView.setText(finalMessage);
//                        }
//                    });
//                }
//            }).start();

            startSearch();
        }

        @Override
        public void onError(final Exception ex, final VoiceSearchInfo info) {
            voiceSearch = null;
            textView.setText(exceptionToString(ex));
        }

        @Override
        public void onRecordingStopped() {

        }

        @Override
        public void onAbort(final VoiceSearchInfo info) {
            voiceSearch = null;
        }
    };

    private static String exceptionToString(final Exception ex) {
        try {
            final StringWriter sw = new StringWriter(1024);
            final PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            pw.close();
            return sw.toString();
        }
        catch (final Exception e) {
            return "";
        }
    }
}
