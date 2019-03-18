package pyroapp.cameratext;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    SurfaceView cameraView;
    TextView textView;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 100;
    Button b;
    EditText editText;
    ImageButton imageButton;
    String cliccato="stop";
    FloatingActionButton copia,condividi;
    private InterstitialAd mInterstitialAd;
    private ScheduledExecutorService scheduler;
    private boolean isVisible;



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        editText=findViewById(R.id.editText);
        editText.setFocusable(false);
        imageButton=findViewById(R.id.threadButton);
        copia=findViewById(R.id.cop);
        condividi=findViewById(R.id.cond);
        final Vibrator vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        MobileAds.initialize(this, "ca-app-pub-9751551150368721~2128701981");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-9751551150368721/6614741906");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(100);
                if(cliccato.equalsIgnoreCase("start")){
                    imageButton.setImageResource(R.mipmap.cazzo_fore);
                    cliccato="stop";
                }
                else {
                    imageButton.setImageResource(R.mipmap.a_f);

                    cliccato="start";
                }
            }
        });

        copia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(100);
                imageButton.setImageResource(R.mipmap.cazzo_fore);
                cliccato="stop";
                if(editText.getText().length()>0){
                    ClipboardManager clipboardManager= (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip=ClipData.newPlainText("Copiato",editText.getText().toString());
                    clipboardManager.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(),"Copied",Toast.LENGTH_SHORT).show();
                    //pubblicita
                }else  if(editText.getText().length()==0){
                    Toast.makeText(getApplicationContext(),"No Text",Toast.LENGTH_SHORT).show();
                }
            }
        });

        condividi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(100);
                imageButton.setImageResource(R.mipmap.cazzo_fore);
                cliccato="stop";
                if(editText.getText().length()>0){
                    Intent sendIntent=new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT,editText.getText().toString());
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }else  if(editText.getText().length()==0){
                    Toast.makeText(getApplicationContext(),"No Text",Toast.LENGTH_SHORT).show();
                }
            }
        });




        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available");
        } else {

            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {

                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });


            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {

                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size() != 0)
                    {
                        editText.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i =0;i<items.size();++i)
                                {
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                if(cliccato.equalsIgnoreCase("start")){
                                    editText.setText(stringBuilder.toString());

                                }
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        isVisible = true;
        if(scheduler == null){
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (mInterstitialAd.isLoaded() && isVisible) {
                                mInterstitialAd.show();
                            }
                            mInterstitialAd = new InterstitialAd(MainActivity.this);
                            mInterstitialAd.setAdUnitId("ca-app-pub-9751551150368721/3787685515");
                            mInterstitialAd.loadAd(new AdRequest.Builder().build());
                        }
                    });
                }
            }, 30, 45, TimeUnit.SECONDS);

        }

    }
    @Override
    protected void onStop() {
        super.onStop();
        scheduler.shutdownNow();
        scheduler = null;
        isVisible =false;
    }


}
