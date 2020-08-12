package com.example.donappt5.QRStuff;


import com.example.donappt5.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class QRGenerateActivity extends Activity implements OnClickListener{

    private String LOG_TAG = "GenerateQRCode";

    TextView txt_result;
    SurfaceView surfaceView;
    QREader qreader;
    Context ctx;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generateqr);
        ctx = this;
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(this);
        Dexter.withActivity(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                setupCamera();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Toast.makeText(ctx, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();

    }

    private void setupCamera() {
        txt_result = findViewById(R.id.code_info);
        surfaceView = findViewById(R.id.camera_view);
        setupQREader();
        if (qreader != null) {
            qreader.initAndStart(surfaceView);
        }
    }

    private void setupQREader() {
        qreader = new QREader.Builder(ctx, surfaceView, new QRDataListener() {
            @Override
            public void onDetected(String data) {
                txt_result.setText(data);
            }
        }).facing(QREader.BACK_CAM).enableAutofocus(true).height(surfaceView.getHeight()).width(surfaceView.getWidth()).build();
        qreader.start();
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button1:
                EditText qrInput = (EditText) findViewById(R.id.qrInput);
                String qrInputText = qrInput.getText().toString();
                Log.v(LOG_TAG, qrInputText);

                //Find screen size
                WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                Display display = manager.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int width = point.x;
                int height = point.y;
                int smallerDimension = width < height ? width : height;
                smallerDimension = smallerDimension * 3/4;

                //Encode with a QR Code image
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText,
                        null,
                        Contents.Type.TEXT,
                        BarcodeFormat.QR_CODE.toString(),
                        smallerDimension);
                try {
                    Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                    ImageView myImage = (ImageView) findViewById(R.id.imageView1);
                    myImage.setImageBitmap(bitmap);

                } catch (WriterException e) {
                    e.printStackTrace();
                }


                break;

            // More buttons go here (if any) ...

        }
    }

}