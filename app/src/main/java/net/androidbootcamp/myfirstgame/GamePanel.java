package net.androidbootcamp.myfirstgame;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;


public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;

    private long smokeStartTimer;
    private long missileStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<SmokePuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topBorder;
    private ArrayList<BottomBorder> bottomBorder;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    //increase to slow down difficulty, decrease to speed up difficulty
    private int progressDenom = 20;

    public GamePanel(Context context) {
        super(context);


        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smoke = new ArrayList<SmokePuff>();
        missiles = new ArrayList<Missile>();
        topBorder = new ArrayList<TopBorder>();
        bottomBorder = new ArrayList<BottomBorder>();
        smokeStartTimer = System.nanoTime();
        missileStartTime = System.nanoTime();


        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.getPlaying()) {
                player.setPlaying(true);
            }
            player.setUp(true);
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update() {
        if (player.getPlaying()) {
            bg.update();
            player.update();

            //calculate threshold of height the border can have base onw the score
            //max and min border are updated, and the border switched direction when max or
            //min is met.

            maxBorderHeight = 30+player.getScore()/progressDenom;
            //cap max border height so that borders can only take up to total of 1/2 the screen
            if(maxBorderHeight > HEIGHT/4)maxBorderHeight = HEIGHT/4;
            minBorderHeight = 5+player.getScore()/progressDenom;

            //update top border
            this.updateTopBorder();

            //update bottom border

            this.updateBottomBorder();


            //add missiles on timer
            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            if (missileElapsed > (2000 - player.getScore() / 4)) {

                //first missile goes down in the middle
                if (missiles.size() == 0) {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),
                            R.drawable.missile), WIDTH + 10, HEIGHT / 2, 45, 15, player.getScore(), 13));
                }
                else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),
                            R.drawable.missile),WIDTH+10, (int)(rand.nextDouble()*(HEIGHT)),45, 15, player.getScore(), 13));
                }

                //reset timer
                missileStartTime = System.nanoTime();
            }
            //loop through every missile and check for collision
            for (int i = 0; i < missiles.size(); i++) {

                //update missile
                missiles.get(i).update();

                if (collision(missiles.get(i), player)) {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //remove missile if it is way off screen
                if (missiles.get(i).getX() < -100) {
                    missiles.remove(i);
                    break;
                }
            }


            //add smoke puffs
            long elapsed = (System.nanoTime() - smokeStartTimer) / 1000000;
            if (elapsed > 120) {
                smoke.add(new SmokePuff(player.getX(), player.getY() + 10));
                smokeStartTimer = System.nanoTime();
            }

            for (int i = 0; i < smoke.size(); i++) {
                smoke.get(i).update();
                if (smoke.get(i).getX() < -10) {
                    smoke.remove(i);
                }
            }
        }
    }

    public boolean collision(GameObject a, GameObject b) {
        if (Rect.intersects(a.getRectangle(), b.getRectangle())) {
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1.f);

        if (canvas != null) {
            final int savedState = canvas.save();


            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            player.draw(canvas);

            //draw smokePuffs
            for (SmokePuff sp : smoke) {
                sp.draw(canvas);
            }

            //draw missile
            for (Missile m : missiles) {
                m.draw(canvas);
            }

            canvas.restoreToCount(savedState);
        }
    }

    public void updateBottomBorder(){
        //every 50 points insert random placed top blocks that break the pattern
        if(player.getScore()%50 == 0){
            topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),topBorder.get(topBorder.size()-1).getX()+20,0,(int)((rand.nextDouble()*(maxBorderHeight))+1)));
        }


    }

    public void updateTopBorder(){

    }


}