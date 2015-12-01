package net.androidbootcamp.myfirstgame;

import android.graphics.Rect;

/**
 * Created by Paolo T. inocencion on 11/30/2015.
 */
public abstract class GameObject {
    protected int x;
    protected int y;
    protected int dy;
    protected int dx;
    protected int width;
    protected int height;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;

    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Rect getRectangle() {
        return new Rect(x, y, x + width, y + height);
    }
}
