package com.rohitarya.picasso.facedetection.transformation;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.rohitarya.picasso.facedetection.transformation.core.PicassoFaceDetector;
import com.squareup.picasso.Transformation;

/**
 * Created by Rohit Arya (http://rohitarya.com) on 19/7/16.
 */
public class CenterFaceCrop implements Transformation {

    protected int width, height;

    public static final int PIXEL = 0;
    public static final int DP = 1;

    public CenterFaceCrop(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public CenterFaceCrop(int width, int height, int unit) {
        if(unit == PIXEL) {
            this.width = width;
            this.height = height;
        } else if(unit == DP) {
            Resources resources = PicassoFaceDetector.getContext().getResources();
            this.width = resources.getDimensionPixelSize(width);
            this.height = resources.getDimensionPixelSize(height);
        }else {
            throw new IllegalArgumentException("unit should either be CenterFaceCrop.PIXEL, CenterFaceCrop.DP");
        }
    }

    @Override
    public Bitmap transform(Bitmap original) {
        if(width==0 || height==0) {
            throw new IllegalArgumentException("width or height should not be zero!");
        }
        float scaleX = (float) width / original.getWidth();
        float scaleY = (float) height / original.getHeight();

        if(scaleX!=scaleY) {

            Bitmap.Config config =
                    original.getConfig() != null ? original.getConfig() : Bitmap.Config.ARGB_8888;
            Bitmap result = Bitmap.createBitmap(width, height, config);

            float scale = Math.max(scaleX, scaleY);

            float left = 0f;
            float top = 0f;

            float scaledWidth = width, scaledHeight = height;

            int[] faceRect = new int[4];
            boolean faceDetected = detectFace(original, faceRect);

            if(scaleX < scaleY) {

                scaledWidth = scale * original.getWidth();

                if(faceDetected) {
                    float scaledFaceLeft = scale * faceRect[0];
                    float scaledFaceWidth = scale * faceRect[2];
                    float faceCenterX = scaledFaceLeft + (scaledFaceWidth/2);
                    left = getLeftPoint(width, scaledWidth, faceCenterX);
                }else {
                    left = (width - scaledWidth) / 2; // center crop
                }

            }else {

                scaledHeight = scale * original.getHeight();

                if(faceDetected) {
                    float scaledFaceTop = scale * faceRect[1];
                    float scaledFaceHeight = scale * faceRect[3];
                    float faceCenterY = scaledFaceTop + (scaledFaceHeight/2);
                    top = getTopPoint(height, scaledHeight, faceCenterY);
                }else {
                    top = (height - scaledHeight) / 2; // center crop
                }
            }

            RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);
            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(original, null, targetRect, null);

            original.recycle();

            return result;
        } else {
            return original;
        }
    }

    @Override
    public String key() {
        return CenterFaceCrop.class.getCanonicalName()+"-width-"+width+"height-"+height;
    }

    private boolean detectFace(Bitmap bitmap, int[] faceRect) {
        FaceDetector faceDetector = PicassoFaceDetector.getFaceDetector();
        if(!faceDetector.isOperational()){
            return false;
        }
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);
        if(faces!=null && faces.size()>0) {
            Face face1 = faces.valueAt(0);
            faceRect[0] = (int)face1.getPosition().x;
            faceRect[1] = (int)face1.getPosition().y;
            faceRect[2] = (int)face1.getWidth();
            faceRect[3] = (int)face1.getHeight();
            return true;
        }
        return false;
    }

    private float getTopPoint(int height, float scaledHeight, float faceCenterY) {
        if(faceCenterY <= height/2) { // Face is near the top edge
            return 0f;
        } else if((scaledHeight - faceCenterY) <= height/2) { // face is near bottom edge
            return height - scaledHeight;
        } else {
            return (height/2) - faceCenterY;
        }
    }

    private float getLeftPoint(int width, float scaledWidth, float faceCenterX) {
        if(faceCenterX <= width/2) { // face is near the left edge.
            return 0f;
        } else if((scaledWidth-faceCenterX) <= width/2) {  // face is near right edge
            return (width - scaledWidth);
        } else {
            return (width/2) - faceCenterX;
        }
    }

}
