package com.lifository.posedetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class NoteGraphic extends GraphicOverlay.Graphic {
    private final GraphicOverlay overlay;
    final List<Note> Notes;
    private final Paint leftPaint;
    private final Paint rightPaint;

    NoteGraphic(GraphicOverlay overlay, List<Note> Notes) {
        super(overlay);
        this.overlay = overlay;
        this.Notes = Notes;
        leftPaint = new Paint();
        leftPaint.setColor(Color.CYAN);
        leftPaint.setShadowLayer(20, 0, 0, Color.DKGRAY);
        rightPaint = new Paint();
        rightPaint.setColor(Color.RED);
        rightPaint.setShadowLayer(20, 0, 0, Color.DKGRAY);
    }

    @Override
    public void draw(Canvas canvas) {
        synchronized (Notes) {
            if (Notes.isEmpty()) {
                return;
            }

            Notes.removeIf(note -> note.getDirection() == Note.Direction.RIGHT && note.getBoundingBox().right >= overlay.getImageWidth() / 2);
            Notes.removeIf(note -> note.getDirection() == Note.Direction.LEFT && note.getBoundingBox().left <= overlay.getImageWidth() / 2);

            // Draw all the notes
            for (Note note : Notes) {
//                if (note.getDirection() == Note.Direction.RIGHT && note.getBoundingBox().right >= overlay.getImageWidth() / 2) {
////                    note.setPos(new PointF(0 - note.getSize().getWidth(), note.getPosition().y));
//                    Notes.remove(note);
//                    continue;
//                }
//                if (note.getDirection() == Note.Direction.LEFT && note.getBoundingBox().left <= overlay.getImageWidth() / 2) {
////                    note.setPos(new PointF(overlay.getImageWidth(), note.getPosition().y));
//                    Notes.remove(note);
//                    continue;
//                }
                RectF rect = new RectF(note.getBoundingBox());

                // If the image is flipped, the left will be translated to right, and the right to left.
                float x0 = translateX(rect.left);
                float x1 = translateX(rect.right);
                rect.left = Math.min(x0, x1);
                rect.right = Math.max(x0, x1);
                rect.top = translateY(rect.top);
                rect.bottom = translateY(rect.bottom);
                if (note.getDirection() == Note.Direction.RIGHT) {
                    canvas.drawRoundRect(rect, 20, 20, rightPaint);
                } else
                    canvas.drawRoundRect(rect, 20, 20, leftPaint);

            }
        }
    }
}
