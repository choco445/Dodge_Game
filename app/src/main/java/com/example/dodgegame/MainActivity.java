package com.example.dodgegame;


import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;




public class MainActivity extends AppCompatActivity implements SensorEventListener{


    GameSurface gameSurface;
    MediaPlayer mediaPlayer;
    SoundPool negativeSound;
    public static int playBad;
    public static Canvas canvas;
    public static Display screenDisplay;
    public static Point sizeOfScreen;
    public static Paint paintProperty;
    public static Paint paintScore;
    public static Paint paintEnd;
    int file;
    public static int goodX=0;
    public static int badY=0;
    public static int goodEndX;
    public static int goodStartX;
    public static int goodBottomY;
    public static int badEndX;
    public static int badStartX;
    public static int badTopY;
    public static int badBottomY;
    public static int goodTopY;
    public static int badSpeed;
    public static int goodSpeed;
    public static int time=60;
    public static int score=20;
    public static int disappear = 0;
    boolean speed = false;
    public static int rand = (int)(Math.random()*830)+10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        file = R.raw.evermore;
        mediaPlayer = MediaPlayer.create(this, file);
        mediaPlayer.start();




        AudioAttributes
                audioAttributes
                = new AudioAttributes
                .Builder()
                .setUsage(
                        AudioAttributes
                                .USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(
                        AudioAttributes
                                .CONTENT_TYPE_SONIFICATION)
                .build();


        negativeSound = new SoundPool.Builder().setMaxStreams(20).setAudioAttributes(audioAttributes).build();


        playBad= negativeSound.load(this, R.raw.negativebeeps, 1);




        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometerSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manager.registerListener(this, accelerometerSensor, manager.SENSOR_DELAY_NORMAL);


        gameSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speed = !speed;
                if(speed)
                {
                    badSpeed = gameSurface.screenHeight / 50;
                }
                else
                {
                    badSpeed = gameSurface.screenHeight / 160;
                }
            }
        });


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if (time > 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        time--;
                    }
                }
            }
        });
        thread.start();


    }
    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }


    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }


    public class GameSurface extends SurfaceView implements Runnable{
        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap good;
        Bitmap bad;
        Bitmap sad;
        int screenWidth;
        int screenHeight;


        public GameSurface(Context context) {
            super(context);
            holder=getHolder();
            good= BitmapFactory.decodeResource(getResources(),R.drawable.kirby);
            bad= BitmapFactory.decodeResource(getResources(),R.drawable.trianglevil);
            bad = Bitmap.createScaledBitmap(bad,bad.getWidth()/4,bad.getHeight()/4,true);
            sad= BitmapFactory.decodeResource(getResources(),R.drawable.sadkirbynew);


            screenDisplay = getWindowManager().getDefaultDisplay();
            sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;


            paintProperty= new Paint();
            paintProperty.setColor(Color.BLACK);
            paintProperty.setTextSize(80);


            paintScore= new Paint();
            paintScore.setColor(Color.BLACK);
            paintScore.setTextSize(80);


            paintEnd= new Paint();
            paintEnd.setColor(Color.BLACK);
            paintEnd.setTextSize(80);


            badSpeed = screenHeight/120;
            goodSpeed = screenWidth/120;
        }




        @Override
        public void run() {
            while (running == true){
                if (holder.getSurface().isValid() == false)
                    continue;
                canvas= holder.lockCanvas();


                canvas.drawRGB(100,149,237);


                goodStartX = (screenWidth)/5 -220 +goodX;
                goodEndX= goodStartX +good.getWidth();
                goodTopY = (screenHeight) +disappear- 100- good.getHeight();
                goodBottomY = goodTopY+good.getHeight();


                badStartX = rand;
                badEndX = rand+bad.getWidth();
                badTopY = (screenHeight) - bad.getHeight()-1800+badY;
                badBottomY = badTopY+bad.getHeight();


                canvas.drawBitmap(good, goodStartX, goodTopY, null);
                canvas.drawBitmap(bad, badStartX, badTopY, null);
                canvas.drawText("Timer: " + time, 50, 250, paintProperty);
                canvas.drawText("Score: " + score, 700, 250, paintScore);


                if(time>0)
                {


                    if (badY < 2100) {
                        badY += badSpeed;
                    }
                    if (badY >= 2100) {
                        badY = 0;
                        rand = (int)(Math.random()*830)+10;
                    }


                    if ((badStartX >= goodStartX && badStartX <= goodEndX) && (badBottomY >= goodTopY && badBottomY <= goodBottomY))
                    {
                        rand = (int)(Math.random()*830)+10;
                        playSoundEffect();
                        switchImg();
                        badY = -20;
                        score--;
                        canvas.drawText("Score: " + score, 700, 250, paintScore);
                    }
                }
                else
                {
                    canvas.drawText("Game over", screenWidth/2-200, 550, paintEnd);
                }


                holder.unlockCanvasAndPost(canvas);
            }
        }


        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }


        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }


        public void playSoundEffect()
        {
            negativeSound.play(playBad,1,1,0,0,1);
        }


        public void switchImg()
        {
            new Handler(getMainLooper()).postDelayed(() -> {
                good= BitmapFactory.decodeResource(getResources(),R.drawable.sadkirbynew);


            }, 000);
            new Handler(getMainLooper()).postDelayed(() -> {
                good= BitmapFactory.decodeResource(getResources(),R.drawable.kirby);


            }, 1000);
        }


    }//GameSurface


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(time>0)
        {
            goodSpeed = (int)(event.values[0])*10;


            if ((event.values[0] > 0 && goodX < 640) ||(event.values[0] < 0 && goodX > 20))
            {
                goodX += goodSpeed;
            }
        }


    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }


}

