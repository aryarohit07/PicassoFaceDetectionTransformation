package com.rohitarya.picasso.facedetection.transformation.core;

import android.content.Context;

import com.google.android.gms.vision.face.FaceDetector;

/**
 * Created by Rohit Arya (http://rohitarya.com) on 19/7/16.
 */
public class PicassoFaceDetector {

    private static volatile FaceDetector faceDetector;
    private static Context mContext;

    public static Context getContext() {
        if(mContext==null) {
            throw new RuntimeException("Initialize PicassoFaceDetector by calling PicassoFaceDetector.initialize(context) in your application's/activity's onCreate() method.");
        }
        return mContext;
    }

    public static void initialize(Context context) {
        mContext = context;
        initDetector();
    }

    private static void initDetector() {
        if(null==faceDetector) {
            synchronized ((PicassoFaceDetector.class)) {
                if(null==faceDetector) {
                    faceDetector = new
                            FaceDetector.Builder(mContext)
                            .setTrackingEnabled(false)
                            .build();
                }
            }
        }
    }

    public static FaceDetector getFaceDetector() {
        if(mContext==null) {
            throw new RuntimeException("Initialize PicassoFaceDetector by calling PicassoFaceDetector.initialize(context) in your application's/activity's onCreate() method.");
        }
        initDetector();
        return faceDetector;
    }

    public static void releaseDetector() {
        if(faceDetector!=null) {
            faceDetector.release();
            faceDetector = null;
        }
    }
}
