package com.lifository.RhythmVisionGame;

import android.graphics.PointF;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.ArrayList;
import java.util.List;

public class NoteProcessor {
    private Pose pose = null;
    private final List<Note> displayNotes;
    private final GraphicOverlay overlay;
    NoteProcessor(GraphicOverlay overlay) {
        this.displayNotes = new ArrayList<Note>();
        this.overlay = overlay;
    }

    void setPose(Pose pose) {
        this.pose = pose;
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
            Note tmpNote = new Note(new PointF(-20, (float) Math.random() * (124) + 10), Note.Direction.RIGHT);
            Note tmpNote2 = new Note(new PointF((176 + 20), (float) Math.random() * (124)), Note.Direction.LEFT);

            this.displayNotes.add(tmpNote);
            this.displayNotes.add(tmpNote2);
        }
    }

    boolean isIndexInNote(PoseLandmark pose, Note note) {
//        Log.e("WRISTX", Float.toString(176 - pose.getPosition().x));
//        Log.e("WRISTY", Float.toString(176 - pose.getPosition().y));

        if (note.getPosition().x <= pose.getPosition().x && pose.getPosition().x <= note.getPosition().x + note.getSize().getWidth() && note.getPosition().y <= pose.getPosition().y && pose.getPosition().y <= note.getPosition().y + note.getSize().getHeight()){
            return true;
        } else {
            return  false;
        }
    }
}
