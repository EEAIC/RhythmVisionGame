package com.lifository.RhythmVisionGame;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Size;

public class Note {
    private final int DEFAULT_WIDTH = 20;
    private final int DEFAULT_HEIGHT = 20;

    enum Direction {
        RIGHT, LEFT
    }

    private Size size;
    private PointF pos;
    private final Direction direction;

    Note(PointF pos) {
        this.pos = pos;
        this.size = new Size(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.direction = Direction.RIGHT;
    }

    Note(PointF pos, Direction direction) {
        this.pos = pos;
        this.size = new Size(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.direction = direction;
    }

    Note(PointF pos, Direction direction, Size size) {
        this.pos = pos;
        this.size = size;
        this.direction = direction;
    }

    PointF getPosition() {
        return this.pos;
    }

    Size getSize() {
        return this.size;
    }

    Direction getDirection() {
        return  this.direction;
    }

    RectF getBoundingBox() {
        RectF rect = new RectF();
        rect.left = pos.x;
        rect.right = pos.x + size.getWidth();
        rect.top = pos.y;
        rect.bottom = pos.y + size.getHeight();
        return rect;
    }

    void move() {
        if (direction == Direction.RIGHT)
            this.pos.x += 1;
        else
            this.pos.x -= 1;
    }
}
