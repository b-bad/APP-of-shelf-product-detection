package com.example.firstapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PostProcessor;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    private ImageView picture;
    private Uri imageUri;
    private File outputImage = null;
    private Bitmap bitmap = null;
    private TextView textView = null;
    public static final int CHOOSE_PHOTO = 2;
    public static final int UPDATE_UI = 100;
    public boolean imageExistence_FromCamera = false;
    public boolean imageExistence_FromAlbum = false;
    public boolean send_flag = false;
    public String imgPath;
    public String[] nameClass = new String[200];
    public int[] rectValue = new int[800];
    public int[] resultArray = new int[8];
    private String[] name = {
            "hfs", "wt", "hryg", "bbz", "kkkl", "hzdg", "hsnrm", "ksf32"
    };

    ////端口测试代码
    private static final String HOST = "192.168.1.106";//ECUST:172.21.167.132 ECUST.1x(xh):172.24.59.102 (fx):172.24.16.75//当前网络ip
    private static final int PORT = 5000;
    private Socket socket = null;
    ////



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button takePhoto = (Button) findViewById(R.id.take_photo);
        Button chooseAlbum = (Button) findViewById(R.id.choose_from_album);
        //Button upload = (Button) findViewById(R.id.upload);
        Button sendImage = (Button) findViewById(R.id.send_image);
        Button getResult = (Button) findViewById(R.id.get_result);
        picture = (ImageView) findViewById(R.id.picture);
        textView = (TextView) findViewById(R.id.chat_massage);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(MainActivity.this,
                            "com.example.firstapp",
                            outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });
        chooseAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
            }
        });
        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (imageExistence_FromAlbum == true && imageExistence_FromCamera == false) {
                            try {
                                socket = new Socket(HOST, PORT);
                                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                                sendImageMessage(dataOutputStream, MainActivity.this.imgPath);
                                MainActivity.this.send_flag = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else if (imageExistence_FromCamera == true && imageExistence_FromAlbum == false){
                            try {
                                socket = new Socket(HOST, PORT);
                                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                                sendImageMessage(MainActivity.this.bitmap, dataOutputStream);
                                MainActivity.this.send_flag = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else if (imageExistence_FromAlbum == false && imageExistence_FromCamera == false){
                            //DO NOTHING
                        }
                    }
                }).start();
            }
        });
        getResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Socket socket;
                        try {
                            socket = new Socket(HOST, PORT);
                            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                            int len = dataInputStream.readInt();
                            byte[] bytes = new byte[len];
                            dataInputStream.read(bytes);
                            String result = new String(bytes, "utf-8");
                            System.out.println(result);
                            int txtLen = result.length();
                            char[] result_char = new char[result.length()];
                            result.getChars(0, txtLen, result_char, 0);
                            String name_temp = "";
                            String int_temp = "";
                            int name_cnt = 0;
                            int int_cnt = 0;
                            boolean flag_ = true;
                            for (int i = 0; i < txtLen; i++){
                                if(result_char[i] != ';' && result_char[i] != ',' && result_char[i] != ':'){
                                    if(flag_)
                                        name_temp += result_char[i];
                                    else
                                        int_temp += result_char[i];
                                }
                                else if (result_char[i] == ':'){
                                    switch (name_temp){
                                        case "hfs":resultArray[0]++;break;
                                        case "wt":resultArray[1]++;break;
                                        case "hryg":resultArray[2]++;break;
                                        case "bbz":resultArray[3]++;break;
                                        case "kkkl":resultArray[4]++;break;
                                        case "hzdg":resultArray[5]++;break;
                                        case "hsnrm":resultArray[6]++;break;
                                        case "ksf32":resultArray[7]++;break;
                                    }
                                    nameClass[name_cnt] = name_temp;
                                    name_temp = "";
                                    name_cnt++;
                                    flag_ = false;
                                }
                                else if (result_char[i] == ','){
                                    rectValue[int_cnt] = Integer.valueOf(int_temp).intValue();
                                    int_temp = "";
                                    int_cnt++;
                                }
                                else if (result_char[i] == ';'){
                                    rectValue[int_cnt] = Integer.valueOf(int_temp).intValue();
                                    int_temp = "";
                                    int_cnt++;
                                    flag_ = true;
                                }
                                else {
                                    //DO NOTHING
                                }
                            }
                            Bitmap resultBitmap = null;
                            if (imageExistence_FromCamera == true && imageExistence_FromAlbum == false){
                                resultBitmap = MainActivity.this.bitmap;
                            }
                            else if (imageExistence_FromAlbum == true && imageExistence_FromCamera == false){
                                resultBitmap = BitmapFactory.decodeFile(MainActivity.this.imgPath);
                            }
                            Bitmap copy1 = resultBitmap.copy(Bitmap.Config.ARGB_8888,  true);
                            float width = copy1.getWidth();
                            float height = copy1.getHeight();
                            float scaleWidth, scaleHeight;
                            if (width >= height && width > 4000){
                                scaleWidth = 2000/width;
                                scaleHeight = scaleWidth;
                            }
                            else if (height > width && height > 4000){
                                scaleHeight = 2000/height;
                                scaleWidth = scaleHeight;
                            }
                            else {
                                scaleHeight = 1;
                                scaleWidth = 1;
                            }
                            Matrix matrix = new Matrix();
                            matrix.postScale(scaleWidth, scaleHeight);
                            System.out.println(width);
                            System.out.println(height);
                            System.out.println(scaleWidth);
                            System.out.println(scaleHeight);
                            Bitmap copy = Bitmap.createBitmap(copy1, 0, 0, (int)width, (int)height, matrix, true);
                            Canvas canvas = new Canvas(copy);
                            Paint paint = new Paint();
                            int value_length = rectValue.length;
                            for (int i = 0; i < name_cnt; i++){
                                int j = i * 4;
                                paint.setColor(Color.RED);
                                paint.setStyle(Paint.Style.STROKE);
                                paint.setStrokeWidth(((int)(copy.getWidth()/1000))+4);
                                canvas.drawRect(rectValue[j]*scaleWidth, rectValue[j + 1]*scaleHeight, rectValue[j + 2]*scaleWidth, rectValue[j + 3]*scaleHeight, paint);
                                paint.setTextSize((int)((rectValue[j + 2]*scaleWidth - rectValue[j]*scaleWidth)));
                                //System.out.println(nameClass[i]);
                                canvas.drawText(nameClass[i], (float)rectValue[j]*scaleWidth, (float)rectValue[j + 1]*scaleWidth, paint);
                            }
                            System.out.println(copy.getWidth());
                            System.out.println(copy.getHeight());
                            Message message = handler.obtainMessage();
                            message.what = UPDATE_UI;
                            message.obj = copy;
                            handler.sendMessage(message);

                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            //textView.setText(String.valueOf(msg.obj));
            String resultText = "RESULT:";
            for(int i = 0; i < 8; i++){
                if (resultArray[i] != 0){
                    resultText += (name[i] + Integer.toString(resultArray[i]) + "; ");
                }
            }
            textView.setText(resultText);
            picture.setImageBitmap((Bitmap) msg.obj);
        }
    };
    public void sendImageMessage(DataOutputStream dataOutputStream, String imgPath)throws IOException{//发送图片（图片由相册获取）
        //Bitmap outputBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap outputBitmap = BitmapFactory.decodeFile(imgPath);;
        outputBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        long len = byteArrayOutputStream.size();
        dataOutputStream.writeLong(len);
        dataOutputStream.write(byteArrayOutputStream.toByteArray());
    }
    public void sendImageMessage(Bitmap InputBitmap, DataOutputStream dataOutputStream)throws IOException{//发送图片（图片由相机获取）
        //Bitmap outputBitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //Bitmap outputBitmap = BitmapFactory.decodeFile(imgPath);;
        InputBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        long len = byteArrayOutputStream.size();
        dataOutputStream.writeLong(len);
        dataOutputStream.write(byteArrayOutputStream.toByteArray());
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
        //Toast.makeText(this, "openAlbum", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "无权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        this.bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(this.bitmap);
                        imageExistence_FromCamera = true;
                        imageExistence_FromAlbum = false;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitkat(data);
                    } else {
                        handleImageBeforeKitkat(data);
                    }
                }
            default:
                break;
        }
    }


    @TargetApi(19)
    private void handleImageOnKitkat(Intent data) {
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                this.imgPath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                this.imgPath = getImagePath(contentUri, null);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())) {
                this.imgPath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                this.imgPath = uri.getPath();
        }
        displayImage(this.imgPath);
        imageExistence_FromAlbum = true;
        imageExistence_FromCamera = false;

    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过uri 和 selection 获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images
                        .Media.DATA));

            }
            cursor.close();
        }
        return path;
    }

    private void handleImageBeforeKitkat(Intent data) {
        Uri uri = data.getData();
        this.imgPath = getImagePath(uri, null);
        displayImage(this.imgPath);
        imageExistence_FromAlbum = true;
        imageExistence_FromCamera = false;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap_ = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap_);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }
}
