package mx.brandonvargas.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 2;

    private ImageViewTouch mImageView;
    private TextView tv;
    private DrawView drawView;
    private Button takePic,save;
    private int count = 0;
    private float l,t,r,b;
    private Bitmap bitmap;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView)findViewById(R.id.tv);
        mImageView = (ImageViewTouch)findViewById(R.id.iv);
        takePic = (Button)findViewById(R.id.tp);
        save = (Button)findViewById(R.id.save);
        save.setEnabled(false);
        drawView = (DrawView)findViewById(R.id.draw);
        mImageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        filePath = sharedPref.getString(getString(R.string.IMAGE), null);
        if(filePath != null){
            count = 2;
            tv.setVisibility(View.GONE);
            l = sharedPref.getFloat(getString(R.string.LEFT),0);
            t = sharedPref.getFloat(getString(R.string.TOP),0);
            r = sharedPref.getFloat(getString(R.string.RIGHT),0);
            b = sharedPref.getFloat(getString(R.string.BOTTOM),0);
            bitmap = BitmapFactory.decodeFile(filePath);
            if(bitmap!=null) {
                mImageView.setImageBitmap(bitmap, null, -1, -1);
                if (r < l && b < t) {
                    drawView.newRectangle(r, b, l, t);
                } else if (r < l && b > t) {
                    drawView.newRectangle(r, t, l, b);
                } else if (r > l && b < t) {
                    drawView.newRectangle(l, b, r, t);
                } else {
                    drawView.newRectangle(l, t, r, b);
                }
            }else{
                tv.setVisibility(View.VISIBLE);
                tv.setText("Oops, the last photo was removed from the device :(\n" +"Take another;)");
            }
        }
        mImageView.setSingleTapListener(
                new ImageViewTouch.OnImageViewTouchSingleTapListener() {

                    @Override
                    public void onSingleTapConfirmed(float rawX, float rawY) {
                        if (bitmap != null) {
                            count++;
                            switch (count) {
                                case 1:
                                    Log.e("1", "pressed");
                                    l = rawX;
                                    t = rawY;
                                    break;
                                case 2:
                                    Log.e("2", "pressed");
                                    r = rawX;
                                    b = rawY;
                                    if (r < l && b < t) {
                                        drawView.newRectangle(r, b, l, t);
                                    } else if (r < l && b > t) {
                                        drawView.newRectangle(r, t, l, b);
                                    } else if (r > l && b < t) {
                                        drawView.newRectangle(l, b, r, t);
                                    } else {
                                        drawView.newRectangle(l, t, r, b);
                                    }
                                    save.setEnabled(true);
                                    break;
                                case 3:
                                    Log.e("3", "pressed");
                                    save.setEnabled(false);
                                    l = t = r = b = 0f;
                                    count = 0;
                                    drawView.newRectangle(l, t, r, b);
                                    break;
                            }
                            Log.d("ImageView", "onSingleTapConfirmed " + rawX + "/" + rawY);
                        }
                    }
                }
        );
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }else{
                    EasyImage.openCamera(MainActivity.this, 0);
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putFloat(getString(R.string.LEFT), l);
                editor.putFloat(getString(R.string.RIGHT), r);
                editor.putFloat(getString(R.string.TOP), t);
                editor.putFloat(getString(R.string.BOTTOM), b);
                editor.putString(getString(R.string.IMAGE),filePath);
                editor.commit();
                save.setEnabled(false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                tv.setVisibility(View.VISIBLE);
                tv.setText("Something went wrong D:");
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the image
                tv.setVisibility(View.GONE);
                onPhotoReturned(imageFile);
            }
            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(MainActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EasyImage.openCamera(MainActivity.this, 0);
                } else {
                    Toast.makeText(this,"Access to camera denied, bye",Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    private void onPhotoReturned(File photoFile) {
        tv.setVisibility(View.GONE);
        filePath = photoFile.getPath();
        bitmap = BitmapFactory.decodeFile(filePath);
        mImageView.setImageBitmap(bitmap,null,-1,-1);
    }
}
