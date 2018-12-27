package com.creaginetech.myblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private FloatingActionButton btnAddPost;

    private String current_user_id;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        //for title on Toolbar
        getSupportActionBar().setTitle("My Blog");

        btnAddPost = findViewById(R.id.btn_add_post);
        btnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent newPostIntent = new Intent(MainActivity.this,NewPostActivity.class);
                startActivity(newPostIntent);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){

            sendToLogin();

        } else {
            //if user has not yet filled the account data
            current_user_id = mAuth.getCurrentUser().getUid();

            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()){

                        if (!task.getResult().exists()){

                            Intent setupIntent = new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }

                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error = "+errorMessage, Toast.LENGTH_LONG).show();

                    }

                }
            });

        }

    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_logout_btn:
            logOut();
            return true;

            case R.id.action_settings_btn:

                Intent settingIntent = new Intent(MainActivity.this,SetupActivity.class);
                startActivity(settingIntent);

                return true;

            default:

                return false;

        }

    }

    private void logOut() {

        mAuth.signOut();
        sendToLogin();
    }
}
