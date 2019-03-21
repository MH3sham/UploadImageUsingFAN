package app.com.example.vip.koranewsadminapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

//http://www.hendiware.com/%D8%B1%D9%81%D8%B9-%D8%A7%D9%84%D9%85%D9%84%D9%81%D8%A7%D8%AA-%D8%A8%D8%A7%D8%B3%D8%AA%D8%AE%D8%AF%D8%A7%D9%85-http-url-connection/
//https://stackoverflow.com/questions/33208911/get-realpath-return-null-on-android-marshmallow (to get filepath as string from uri)
//https://stackoverflow.com/questions/35839130/send-file-and-parameters-to-server-with-httpurlconnection-in-android-api-23
//https://learnwithmehere.blogspot.com.eg/2015/10/is-multipart-request-complicated-think.html
// Uploading Image to Server Using multipart/form-data not Base64
//https://www.w3schools.com/php/php_file_upload.asp

public class MainActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public static final String TAG = "YOOOOODA >>";
    String ServerURL ="https://devyooda.000webhostapp.com/koranews_add_new_post.php" ;
    Button SelectImageGallery, btnUploadImage;
    Bitmap bitmap;
    ImageView imageView;
    EditText titleEt ,contentEt;
    String mTitle, mContent;
    String mStringFilePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SelectImageGallery = (Button)findViewById(R.id.buttonSelect);
        btnUploadImage = (Button)findViewById(R.id.buttonUploadImage);
        imageView = (ImageView)findViewById(R.id.imageView);
        titleEt = (EditText) findViewById(R.id.titleET);
        contentEt = (EditText) findViewById(R.id.contentET);

        //initialize AndroidNetworking
        AndroidNetworking.initialize(getApplicationContext());



        //btn to select image from Gallery and post it as a bitmap in imageView
        SelectImageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissionREAD_EXTERNAL_STORAGE(MainActivity.this)) {
                    // do your stuff..
                    Intent intent = new Intent();
                    //type of file is image to preview images from phone
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    //startActivityForResult method to get the result image
                    startActivityForResult(Intent.createChooser(intent, "Select Image From Gallery"), 1);
                }

            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTitle = titleEt.getText().toString();
                mContent = contentEt.getText().toString();

                //make sure that u got the file path to upload it and then call the method
                if (mStringFilePath != null){
                    //hendiwareFileUpload(mStringFilePath, mTitle, mContent);
                    mAndroidFastNetworkFileUpload(ServerURL ,mStringFilePath, mTitle, mContent);
                }
                else {
                    Toast.makeText(MainActivity.this, "Please choose a valid file!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void mAndroidFastNetworkFileUpload(String url, String mStringFilePath, String mTitle, String mContent) {
        AndroidNetworking.upload(url)
                .addMultipartFile("fileToUpload", new File(mStringFilePath))
                .addMultipartParameter("title",mTitle)
                .addMultipartParameter("content",mContent)
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        //responseTextView.setText(finalResponse);
                        Toast.makeText(MainActivity.this, response.toString() , Toast.LENGTH_LONG).show();  //<<<<<<<<<<<<<< msh btzhar leeeh???
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });
    }

    //startActivityForResult method to get the result image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == 1 && resultCode == RESULT_OK && intent != null && intent.getData() != null) {

            Uri uri = intent.getData();

            //class made it after fucking search for a week :D to get the File Path from Uri as String.
            GetRealPathFromURIforAllAPIs mFilePath = new GetRealPathFromURIforAllAPIs();
            String filepath = mFilePath.getFilePath(MainActivity.this, uri);

            try {
                //preview the selected image in a bitmap from uri (not from filepath)
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imageView.setImageBitmap(bitmap); //add image to bitmap
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("File Path >>>> ", " " + filepath);
            mStringFilePath = filepath; //3shan bs ast5dmo fil button btnUploadImage
            //hendiwareFileUpload(filepath);
            //Log.e(TAG, "File Uploaded!");
        }
    }




    // Permission Dialog ///////////////////////
    //https://stackoverflow.com/questions/37672338/java-lang-securityexception-permission-denial-reading-com-android-providers-me
    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context, Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }
    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(MainActivity.this, "GET_ACCOUNTS Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    // Permission Dialog ///////////////////////






    //method to take the filepath and upload it to server using multipart/form-data
    //then read the response from echo in InputStream
    //OLD WAY
    private void hendiwareFileUpload(final String filePath , final String titleString, final String contentString) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection uploadConnection = null;
                DataOutputStream outputStream;
                String boundary = "********";
                String CRLF = "\r\n";
                String Hyphens = "--";
                int bytesRead, bytesAvailable, bufferSize;
                int maxBufferSize = 1024 * 1024;
                byte[] buffer;
                File ourFile = new File(filePath);

                try {
                    //must encode the string to UTF-8 and in php file must be decoded
                    //$title= urldecode($title);  >> decode the unicode %D51%D10 to normal arabic chars
                    String mEncodedTitle = URLEncoder.encode(titleString, "UTF-8"); //>>>>>>>>>>>>>>>>>>> %D8%B1%D8%B1
                    String mEncodedContent = URLEncoder.encode(contentString, "UTF-8");
                    Log.e(TAG, mEncodedTitle);  //>>>>>>>>>>>>>>>>>>> %D8%B1%D8%B1
                    Log.e(TAG, mEncodedContent);

                    FileInputStream fileInputStream = new FileInputStream(ourFile);
                    URL url = new URL(ServerURL);
                    uploadConnection = (HttpURLConnection) url.openConnection();
                    uploadConnection.setDoInput(true);
                    uploadConnection.setDoOutput(true);
                    uploadConnection.setReadTimeout(20000);
                    uploadConnection.setConnectTimeout(20000);
                    uploadConnection.setRequestMethod("POST");

                    uploadConnection.setRequestProperty("Connection", "Keep-Alive");
                    uploadConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    uploadConnection.setRequestProperty("fileToUpload", filePath);

                    outputStream = new DataOutputStream(uploadConnection.getOutputStream());

                    //start uploading my file
                    outputStream.writeBytes(Hyphens + boundary + CRLF);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\";filename=\"" + filePath + "\"" + CRLF);
                    outputStream.writeBytes(CRLF);

                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        outputStream.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    outputStream.writeBytes(CRLF);
                    outputStream.writeBytes(Hyphens + boundary + CRLF); //only one Hyphens coz it's not the end we still gonna add title and content
                    //End uploading my file

                    //start uploading my TITLE string in the body
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"title\"" + CRLF);
                    //outputStream.writeBytes("Content-Type: text/plain;charset=UTF-8;Content-Transfer-Encoding: 8bit" + CRLF);
                    outputStream.writeBytes(CRLF);
                    outputStream.writeBytes(mEncodedTitle); //write my encoded title
                    Log.e(TAG, mEncodedTitle);
                    outputStream.writeBytes(CRLF);
                    outputStream.writeBytes(Hyphens + boundary + CRLF); //only one Hyphens coz it's not the end we still gonna add content
                    //End uploading my TITLE string in the body

                    //Start uploading my CONTENT string in the body
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"content\"" + CRLF);
                    //outputStream.writeBytes("Content-Type: text/plain" + CRLF);
                    outputStream.writeBytes(CRLF);
                    outputStream.writeBytes(mEncodedContent); //write my encoded content
                    Log.e(TAG, mEncodedContent);
                    outputStream.writeBytes(CRLF);
                    outputStream.writeBytes(Hyphens + boundary + Hyphens + CRLF); // Two Hyphens as the second one indicates the end of the stream.
                    //End uploading my CONTENT string in the body

                    //to get the respose from echo in php
                    InputStreamReader resultReader = new InputStreamReader(uploadConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(resultReader);
                    String line = "";
                    String response = "";
                    while ((line = reader.readLine()) != null) {
                        response += line;
                    }

                    final String finalResponse = response;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //responseTextView.setText(finalResponse);
                            Toast.makeText(MainActivity.this, finalResponse, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "File Uploaded! >> " + mStringFilePath);
                        }
                    });

                    fileInputStream.close();
                    outputStream.flush();
                    outputStream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }
        }).start();


    }


}
