package com.lifository.RhythmVisionGame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.io.SyncFailedException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@ExperimentalGetImage
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final Executor executor = Executors.newSingleThreadExecutor();

    // Permissions
    private final String[] REQUIRED_PERMISSIONS = new String[] {"android.permission.CAMERA", "android.permission.RECORD_AUDIO"};
    private final int REQUEST_CODE_PERMISSIONS = 1001;

    // Views
    private PreviewView mPreviewView;
    private VisualizerView mVisualizerView;
    private SwitchCompat mMusicSwitch;

    private GraphicOverlay graphicOverlay;
    PoseDetectorOptions options = new PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE).build();
    PoseDetector poseDetector = PoseDetection.getClient(options);
    NoteProcessor noteProcessor;
    private MediaPlayer mPlayer;
    private Visualizer mVisualizer;

    Thread musicThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreviewView = findViewById(R.id.previewView);
        mVisualizerView = findViewById(R.id.visualizer_view);
        mMusicSwitch = findViewById(R.id.switchMusicStart);
        ImageView mSettingsButton = findViewById(R.id.settings_button);
        TextView tvTime = findViewById(R.id.tvTime);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        // SettingsActivity
        mSettingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        });

        // Permission Check
        if (allPermissionsGranted()) {



        // 화면이 꺼지는 것을 방지
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_IMMERSIVE
//                        // Set the content to appear under the system bars so that the
//                        // content doesn't resize when the system bars hide and show.
//                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        // Hide the nav bar and status bar
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        mPlayer = MediaPlayer.create(this, R.raw.f_777_1up);
        mPlayer.setLooping(true);


        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMusicSwitch.setOnClickListener(view -> {
            if (mMusicSwitch.isChecked()) {
                mPlayer.start();

                musicThread = new Thread() {
                    final SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss", Locale.US);
                    @Override
                    public void run() {
                        while(mPlayer.isPlaying()) {
                            runOnUiThread(() -> tvTime.setText(timeFormat.format(mPlayer.getCurrentPosition())));
                            SystemClock.sleep(200);
                        }
                    }
                };
                musicThread.start();
            }
            else {
                mPlayer.pause();
            }
        });


        mPreviewView.setVisibility(View.INVISIBLE);

        setupVisualizerFxAndUI();

        noteProcessor = new NoteProcessor(graphicOverlay);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableVisualizer = true;
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

            startCamera();
            mVisualizer.setEnabled(enableVisualizer);
            TimerTask noteMove = new TimerTask()
            {
                public void run()
                {
                    noteProcessor.move();
                }
            };

            TimerTask makeNote = new TimerTask()
            {
                public void run()
                {
                    if (noteProcessor.getDisplayNotes().size() < 100)
                        noteProcessor.makeRandomNote();
                }
            };

            Timer noteTimer = new Timer();
            noteTimer.schedule(noteMove, 0, 25);
            noteTimer.schedule(makeNote, 0, 1000);
        } else {

            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.permission_request_msg, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            finishAffinity();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(176, 144)).build();
        imageAnalysis.setAnalyzer(executor, image -> {
            Image myImage = image.getImage();
            if (myImage != null) {
                InputImage inputImage = InputImage.fromMediaImage(myImage, image.getImageInfo().getRotationDegrees());

                graphicOverlay.setImageSourceInfo(image.getWidth(), image.getHeight(), true);
                noteProcessor.setOverlay(graphicOverlay);
                poseDetector.process(inputImage).addOnSuccessListener(executor, results -> {
                    noteProcessor.setPose(results);
                    noteProcessor.destroyNote();
                    graphicOverlay.clear();
                    graphicOverlay.add(new PoseGraphic(graphicOverlay, results, false));
                    graphicOverlay.add(new NoteGraphic(graphicOverlay, noteProcessor.getDisplayNotes()));
                    graphicOverlay.postInvalidate();
                }).addOnFailureListener(e -> {
                    graphicOverlay.clear();
                    graphicOverlay.postInvalidate();
                }).addOnCompleteListener(results -> image.close());
            }
        });

        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (switch1.isChecked()) {
//            mPlayer.start();
//        }
//        if (musicThread != null && musicThread.isInterrupted()) {
//            musicThread.start();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    private void setupVisualizerFxAndUI() {
        mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);


        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            final int EXPECTED_PEAK_MB = -4015;
            final int EXPECTED_RMS_MB =  -4300;
            final int MAX_MEASUREMENT_ERROR_MB = 2000;
            int status = mVisualizer.setMeasurementMode(Visualizer.MEASUREMENT_MODE_PEAK_RMS);
            Visualizer.MeasurementPeakRms measurement = new Visualizer.MeasurementPeakRms();
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                mVisualizerView.updateVisualizer(bytes);

                status = mVisualizer.getMeasurementPeakRms(measurement);
//                Log.i(TAG, "peak="+measurement.mPeak+"  rms="+measurement.mRms);
//                int deltaPeak = Math.abs(measurement.mPeak - EXPECTED_PEAK_MB);
//                int deltaRms =  Math.abs(measurement.mRms - EXPECTED_RMS_MB);
//                if (deltaPeak < MAX_MEASUREMENT_ERROR_MB)
//                    Log.i(TAG, "peak deviation in mB=" + deltaPeak);
//                if (deltaRms < MAX_MEASUREMENT_ERROR_MB)
//                    Log.i(TAG,"RMS deviation in mB=" + deltaRms);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                mVisualizerView.updateVisualizer(bytes);
            }
        }, Visualizer.getMaxCaptureRate(), true, false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "key_enable_preview":
                if (sharedPreferences.getBoolean(s, true)) {
                    mPreviewView.setVisibility(View.VISIBLE);
                } else {
                    mPreviewView.setVisibility(View.INVISIBLE);
                }
                break;
            case "key_enable_visualizer":
                if (sharedPreferences.getBoolean(s, true)) {
                    mVisualizerView.setVisibility(View.VISIBLE);
                } else {
                    mVisualizerView.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }
}