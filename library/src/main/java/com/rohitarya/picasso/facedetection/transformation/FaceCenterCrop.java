package com.rohitarya.picasso.facedetection.transformation;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
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
public class FaceCenterCrop implements Transformation {

    public static final int PIXEL = 0;
    public static final int DP = 1;
    protected int width, height;

    public FaceCenterCrop(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public FaceCenterCrop(int width, int height, int unit) {
        if (unit == PIXEL) {
            this.width = width;
            this.height = height;
        } else if (unit == DP) {
            Resources resources = PicassoFaceDetector.getContext().getResources();
            this.width = resources.getDimensionPixelSize(width);
            this.height = resources.getDimensionPixelSize(height);
        } else {
            throw new IllegalArgumentException("unit should either be FaceCenterCrop.PIXEL, FaceCenterCrop.DP");
        }
    }

    @Override
    public Bitmap transform(Bitmap original) {
        if (width == 0 || height == 0) {
            throw new IllegalArgumentException("width or height should not be zero!");
        }
        float scaleX = (float) width / original.getWidth();
        float scaleY = (float) height / original.getHeight();

        if (scaleX != scaleY) {

            Bitmap.Config config =
                    original.getConfig() != null ? original.getConfig() : Bitmap.Config.ARGB_8888;
            Bitmap result = Bitmap.createBitmap(width, height, config);

            float scale = Math.max(scaleX, scaleY);

            float left = 0f;
            float top = 0f;

            float scaledWidth = width, scaledHeight = height;

            PointF focusPoint = new PointF();
            detectFace(original, focusPoint);

            if (scaleX < scaleY) {

                scaledWidth = scale * original.getWidth();

                float faceCenterX = scale * focusPoint.x;
                left = getLeftPoint(width, scaledWidth, faceCenterX);

            } else {

                scaledHeight = scale * original.getHeight();

                float faceCenterY = scale * focusPoint.y;
                top = getTopPoint(height, scaledHeight, faceCenterY);
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
        return FaceCenterCrop.class.getCanonicalName() + "-width-" + width + "height-" + height;
    }

    /**
     * Calculates center point in bitmap, around which cropping needs to be performed.
     * Right now, it takes on the average of all faces (their centers).
     *
     * @param bitmap           Bitmap in which faces are to be detected.
     * @param centerOfAllFaces To store the center point.
     */
    private void detectFace(Bitmap bitmap, PointF centerOfAllFaces) {
        FaceDetector faceDetector = PicassoFaceDetector.getFaceDetector();
        if (!faceDetector.isOperational()) {
            centerOfAllFaces.set(bitmap.getWidth() / 2, bitmap.getHeight() / 2); // center crop
            return;
        }
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);
        final int totalFaces = faces.size();
        if (totalFaces > 0) {
            float sumX = 0f;
            float sumY = 0f;
            for (int i = 0; i < totalFaces; i++) {
                PointF faceCenter = new PointF();
                getFaceCenter(faces.get(faces.keyAt(i)), faceCenter);
                sumX = sumX + faceCenter.x;
                sumY = sumY + faceCenter.y;
            }
            centerOfAllFaces.set(sumX / totalFaces, sumY / totalFaces);
            return;
        }
        centerOfAllFaces.set(bitmap.getWidth() / 2, bitmap.getHeight() / 2); // center crop
    }

    /**
     * Calculates center of a given face
     *
     * @param face   Face
     * @param center Center of the face
     */
    private void getFaceCenter(Face face, PointF center) {
        float x = face.getPosition().x;
        float y = face.getPosition().y;
        float width = face.getWidth();
        float height = face.getHeight();
        center.set(x + (width / 2), y + (height / 2)); // face center in original bitmap
    }

    private float getTopPoint(int height, float scaledHeight, float faceCenterY) {
        if (faceCenterY <= height / 2) { // Face is near the top edge
            return 0f;
        } else if ((scaledHeight - faceCenterY) <= height / 2) { // face is near bottom edge
            return height - scaledHeight;
        } else {
            return (height / 2) - faceCenterY;
        }
    }

    private float getLeftPoint(int width, float scaledWidth, float faceCenterX) {
        if (faceCenterX <= width / 2) { // face is near the left edge.
            return 0f;
        } else if ((scaledWidth - faceCenterX) <= width / 2) {  // face is near right edge
            return (width - scaledWidth);
        } else {
            return (width / 2) - faceCenterX;
        }
    }

}
