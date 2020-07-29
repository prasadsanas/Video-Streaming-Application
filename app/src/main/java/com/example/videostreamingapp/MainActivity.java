package com.example.videostreamingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.content.Intent;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class MainActivity extends AppCompatActivity {

    private static final int PICK_VIDEO = 1;
    VideoView videoView;
    Button button;
    ProgressBar progressBar;



    EditText editText;
    private Uri videoUri;
    MediaController mediaController;

    StorageReference storageReference;
    DatabaseReference databaseReference;
    Member member;
    UploadTask uploadTask;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        member = new Member ();
        storageReference = FirebaseStorage.getInstance ().getReference ("Video");
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        databaseReference = FirebaseDatabase.getInstance ().getReference ("Video");


        videoView = findViewById (R.id.videoview_main);
        button = findViewById (R.id.button_upload_main);
        progressBar = findViewById (R.id.progressBar_main);
        editText = findViewById (R.id.et_video_name);
        mediaController = new MediaController (this);
        videoView.setMediaController (mediaController);
        videoView.start ();



        button.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                UploadVideo();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode , int resultCode , @Nullable Intent data) {
        super.onActivityResult (requestCode , resultCode , data);

        if(requestCode == PICK_VIDEO || resultCode == RESULT_OK ||
        data != null || data.getData () != null){
            videoUri = data.getData ();
            videoView.setVideoURI (videoUri);
        }
    }

    public void ChooseVideo(View view) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction (Intent.ACTION_GET_CONTENT);
        startActivityForResult (intent,PICK_VIDEO);
    }

    private String getExt(Uri uri){
        ContentResolver contentResolver = getContentResolver ();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton ();
        return mimeTypeMap.getExtensionFromMimeType (contentResolver.getType (uri));
    }

    public void ShowVideo(View view) {

        Intent intent = new Intent (MainActivity.this,ShowVideo.class);
        startActivity (intent);
    }

    private void UploadVideo(){
        final String videoName = editText.getText ().toString ();
        final String search = editText.getText ().toString ().toLowerCase ();
        if(videoUri != null || !TextUtils.isEmpty (videoName)){
            progressBar.setVisibility (View.VISIBLE);
            final ProgressDialog progressDialog = new ProgressDialog (this);
            progressDialog.setTitle ("Uploading... ");
            progressDialog.show ();
            final StorageReference reference = storageReference.child (System.currentTimeMillis () + "." + getExt (videoUri));

             reference.putFile (videoUri).addOnProgressListener (new OnProgressListener<UploadTask.TaskSnapshot> () {
                 @Override
             public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
             double progress =(100.0*taskSnapshot.getBytesTransferred ()/taskSnapshot.getTotalByteCount ());
              progressDialog.setMessage ("Uploaded " + (int)progress + "%");
                   }
               });

            uploadTask = reference.putFile (videoUri);
            Task<Uri> urltask = uploadTask.continueWithTask (new Continuation<UploadTask.TaskSnapshot, Task<Uri>> () {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful ()){
                        throw task.getException ();
                    }
                    return reference.getDownloadUrl ();
                }
            })
                    .addOnCompleteListener (new OnCompleteListener<Uri> () {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful ()){
                                Uri downloadUrl = task.getResult ();

                                progressBar.setVisibility (View.INVISIBLE);
                                Toast.makeText (MainActivity.this,"Video Saved", Toast.LENGTH_LONG).show ();
                                progressDialog.dismiss ();
                                member.setName (videoName);
                                member.setVideourl (downloadUrl.toString ());
                                member.setSearch (search);

                                String i = databaseReference.push().getKey();
                                databaseReference.child(i).setValue (member);

                            }else{
                                Toast.makeText (MainActivity.this,"Failed", Toast.LENGTH_LONG).show ();
                            }

                        }
                    });
        }
        else {
            Toast.makeText (MainActivity.this,"All fields are required", Toast.LENGTH_LONG).show ();
        }
    }

}