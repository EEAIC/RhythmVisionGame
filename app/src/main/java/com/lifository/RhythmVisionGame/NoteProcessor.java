package com.lifository.RhythmVisionGame;

import android.graphics.PointF;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.ArrayList;
import java.util.List;

public class NoteProcessor {
    private Pose pose = null;
    private final List<Note> displayNotes;
    private GraphicOverlay overlay;
    NoteProcessor(GraphicOverlay overlay) {
        this.displayNotes = new ArrayList<>();
        this.overlay = overlay;
    }

    void setPose(Pose pose) {
        this.pose = pose;
    }

    void setOverlay(GraphicOverlay overlay) {
        this.overlay = overlay;
    }

    List<Note> getDisplayNotes() {
        synchronized (displayNotes) {
            return this.displayNotes;
        }
    }

    void move() {
        synchronized (displayNotes) {
            for (Note note : displayNotes) {
                note.move();
            }
        }
    }

    void destroyNote() {
        synchronized (displayNotes) {
            if (pose != null && overlay != null) {
                PoseLandmark leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX);
                PoseLandmark rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX);
                this.displayNotes.removeIf(note -> rightIndex != null && note.getDirection() == Note.Direction.RIGHT && isIndexInNote(rightIndex, note));
                this.displayNotes.removeIf(note -> leftIndex != null && note.getDirection() == Note.Direction.LEFT && isIndexInNote(leftIndex, note));
            }
        }
    }

    void makeRandomNote() {
        synchronized (displayNotes) {
            Note tmpNote = new Note(new PointF(- 20, ((float) Math.random() * (overlay.getBottom() - 20 * overlay.getScaleFactor()) + overlay.getPostScaleHeightOffset()) / overlay.getScaleFactor()), Note.Direction.RIGHT);
            Note tmpNote2 = new Note(new PointF(overlay.getImageWidth(), ((float) Math.random() * (overlay.getBottom() - 20 * overlay.getScaleFactor()) + overlay.getPostScaleHeightOffset()) / overlay.getScaleFactor()), Note.Direction.LEFT);
            this.displayNotes.add(tmpNote);
            this.displayNotes.add(tmpNote2);
        }
    }

    boolean isIndexInNote(PoseLandmark pose, Note note) {
        return note.getPosition().x <= pose.getPosition().x && pose.getPosition().x <= note.getPosition().x + note.getSize().getWidth() && note.getPosition().y <= pose.getPosition().y && pose.getPosition().y <= note.getPosition().y + note.getSize().getHeight();
    }
}
