//package com.lifository.posedetection;
//
//import android.graphics.Bitmap;
//
//import androidx.annotation.RequiresApi;
//
//public abstract class VisionProcessorBase<T> implements VisionImageProcessor {
//
//    // Whether this processor is already shut down
//    private boolean isShutdown;
//
//    // -----------------Code for processing live preview frame from CameraX API-----------------------
//    @Override
//    @RequiresApi(VERSION_CODES.KITKAT)
//    @ExperimentalGetImage
//    public void processImageProxy(ImageProxy image, GraphicOverlay graphicOverlay) {
//        if (isShutdown) {
//            image.close();
//            return;
//        }
//
//        Bitmap bitmap = null;
//        if (!PreferenceUtils.isCameraLiveViewportEnabled(graphicOverlay.getContext())) {
//            bitmap = BitmapUtils.getBitmap(image);
//        }
//
//        requestDetectInImage(
//                InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees()),
//                graphicOverlay,
//                /* originalCameraImage= */ bitmap,
//                /* shouldShowFps= */ true)
//                // When the image is from CameraX analysis use case, must call image.close() on received
//                // images when finished using them. Otherwise, new images may not be received or the camera
//                // may stall.
//                .addOnCompleteListener(results -> image.close());
//    }
//
//}
