package com.creaginetech.myblog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageUri = null;

    private String user_id;

    private boolean isChanged = false;

    private EditText edt_setup_name;
    private Button btn_setup;
    private ProgressBar setupProgress;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        user_id = firebaseAuth.getCurrentUser().getUid();

        setupImage = findViewById(R.id.setup_image);
        edt_setup_name = findViewById(R.id.edt_setup_name);
        btn_setup = findViewById(R.id.btn_setup);
        setupProgress = findViewById(R.id.setup_progressBar);

        setupProgress.setVisibility(View.VISIBLE);
        btn_setup.setEnabled(false);


        //check data and retrieve data exists or not from firestore where visit to setup activity
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    //check data from Users.user_id exist or not
                    if (task.getResult().exists()){

                        String nameProfile = task.getResult().getString("name");
                        String imageProfile = task.getResult().getString("image");

                        mainImageUri = Uri.parse(imageProfile);

                        //retrieve name from firestore
                        edt_setup_name.setText(nameProfile);

                        //retrieve image from firestore
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.account);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(imageProfile).into(setupImage);


                    }

                }else {

                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore Retrieve Error = "+errorMessage, Toast.LENGTH_LONG).show();

                }

                setupProgress.setVisibility(View.INVISIBLE);
                btn_setup.setEnabled(true);


            }
        });

        btn_setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String user_name = edt_setup_name.getText().toString();
                setupProgress.setVisibility(View.VISIBLE);

                //if image changed
                if (isChanged) {

                    if (!TextUtils.isEmpty(user_name) && mainImageUri != null) {

                        user_id = FirebaseAuth.getInstance().getUid();

                        //for uplad image to firebase storage
                        final StorageReference image_path = storageReference.child("profil_images").child(user_id + ".jpg");
                        UploadTask uploadTask = image_path.putFile(mainImageUri);


                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                // Continue with the task to get the download URL
                                return image_path.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {

                                    storeFirestore(task, user_name);


                                } else {

                                    // Handle failures
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Image Error = " + errorMessage, Toast.LENGTH_LONG).show();
                                    setupProgress.setVisibility(View.INVISIBLE);

                                }


                            }
                        });

                    }
                } else {

                    storeFirestore(null ,user_name);

                }

            }
        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //for check permission if sdk low from marsmalllow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(SetupActivity.this, "Permission denied !", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                    } else {

                        // start picker to get image for cropping and then use the image in cropping activity
                        bringImagePicture();

                    }

                } else {

                    bringImagePicture();

                }


            }
        });

    }

    private void storeFirestore(@NonNull Task<Uri> task, String user_name) {

        Uri downloadUri;

        if (task != null){

            downloadUri = task.getResult();

        } else {

            downloadUri = mainImageUri;

        }


        //create string name and image
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name",user_name);
        userMap.put("image",downloadUri.toString());

        //create collection "Users" at FireStore Database and save name,image url
        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    Toast.makeText(SetupActivity.this, "User settings updated !", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();

                } else {

                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore Error = "+errorMessage, Toast.LENGTH_LONG).show();


                }

                setupProgress.setVisibility(View.INVISIBLE);

            }
        });

    }

    private void bringImagePicture() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageUri = result.getUri();
                //set circleImage for change the picture profile
                setupImage.setImageURI(mainImageUri);

                // if image selected
                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }
}
