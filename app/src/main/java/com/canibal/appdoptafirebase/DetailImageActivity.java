package com.canibal.appdoptafirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class DetailImageActivity extends AppCompatActivity {
    //to get detail of user and post
    String hisUid, myUid, myEmail, postId, hisDp, hisName, pImage, pTitle;
    String hisUid2, hisDp2, hisName2, pImage2, pTitle2;

    ActionBar actionBar;

    //views -- vistas
    ImageView pImageIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_image);


        //get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        // init views
        pImageIv = findViewById(R.id.pImageIv);

        actionBar = getSupportActionBar();
        //enable back button in actionbar
        // habilitar el botón de retroceso en la barra de acción
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setElevation(0);


        loadPostInfo();

        loadAdoptionInfo();

        loadReportsLostInfo();

        loadReportsFoundInfo();

        loadServiceInfo();

        checkUserStatus();

    }

    private void loadPostInfo() {
        //get post using the id of the  post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep checking the posts until get the  required post
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    pTitle = "" + ds.child("pTitle").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    hisName = "" + ds.child("uName").getValue();


                    //convert timestamp to dd/mm/yyy hh:mm am/pm
                    //convierte la marca de tiempo a dd/mm/yyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //set data
                    actionBar.setTitle(hisName);
                    actionBar.setSubtitle(pTime);

                    // set image of the user who posted
                    //if there is no image i.e. pImage.equals("noImage") then hide ImageView
                    if (pImage.equals("noImage")) {
                        //hide Image Biew
                        pImageIv.setVisibility(View.GONE);
                    } else {
                        //show Image Biew
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(pImageIv);

                        } catch (Exception e) {

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadAdoptionInfo() {
        //get post using the id of the  post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Adoptions");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep checking the posts until get the  required post
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    pTitle2 = "" + ds.child("pTitle").getValue();
                    String pTimeStamp2 = "" + ds.child("pTime").getValue();
                    pImage2 = "" + ds.child("pImage").getValue();
                    hisDp2 = "" + ds.child("uDp").getValue();
                    hisUid2 = "" + ds.child("uid").getValue();
                    hisName2 = "" + ds.child("uName").getValue();

                    //convert timestamp to dd/mm/yyy hh:mm am/pm
                    //convierte la marca de tiempo a dd/mm/yyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp2));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //set data
                    actionBar.setTitle(hisName2);
                    actionBar.setSubtitle(pTime);

                    // set image of the user who posted
                    //if there is no image i.e. pImage.equals("noImage") then hide ImageView
                    if (pImage2.equals("noImage")) {
                        //hide Image Biew
                        pImageIv.setVisibility(View.GONE);
                    } else {
                        //show Image Biew
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage2).into(pImageIv);

                        } catch (Exception e) {

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadReportsLostInfo() {
        //get post using the id of the  post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsLost");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep checking the posts until get the  required post
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    pTitle2 = "" + ds.child("pTitle").getValue();
                    String pTimeStamp2 = "" + ds.child("pTime").getValue();
                    pImage2 = "" + ds.child("pImage").getValue();
                    hisDp2 = "" + ds.child("uDp").getValue();
                    hisUid2 = "" + ds.child("uid").getValue();
                    hisName2 = "" + ds.child("uName").getValue();

                    //convert timestamp to dd/mm/yyy hh:mm am/pm
                    //convierte la marca de tiempo a dd/mm/yyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp2));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //set data
                    actionBar.setTitle(hisName2);
                    actionBar.setSubtitle(pTime);

                    // set image of the user who posted
                    //if there is no image i.e. pImage.equals("noImage") then hide ImageView
                    if (pImage2.equals("noImage")) {
                        //hide Image Biew
                        pImageIv.setVisibility(View.GONE);
                    } else {
                        //show Image Biew
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage2).into(pImageIv);

                        } catch (Exception e) {

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadReportsFoundInfo() {
        //get post using the id of the  post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsFound");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep checking the posts until get the  required post
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    pTitle2 = "" + ds.child("pTitle").getValue();
                    String pTimeStamp2 = "" + ds.child("pTime").getValue();
                    pImage2 = "" + ds.child("pImage").getValue();
                    hisDp2 = "" + ds.child("uDp").getValue();
                    hisUid2 = "" + ds.child("uid").getValue();
                    hisName2 = "" + ds.child("uName").getValue();

                    //convert timestamp to dd/mm/yyy hh:mm am/pm
                    //convierte la marca de tiempo a dd/mm/yyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp2));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //set data
                    actionBar.setTitle(hisName2);
                    actionBar.setSubtitle(pTime);

                    // set image of the user who posted
                    //if there is no image i.e. pImage.equals("noImage") then hide ImageView
                    if (pImage2.equals("noImage")) {
                        //hide Image Biew
                        pImageIv.setVisibility(View.GONE);
                    } else {
                        //show Image Biew
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage2).into(pImageIv);

                        } catch (Exception e) {

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadServiceInfo() {
        //get post using the id of the  post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Services");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep checking the posts until get the  required post
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    pTitle = "" + ds.child("pTitle").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    hisName = "" + ds.child("uName").getValue();


                    //convert timestamp to dd/mm/yyy hh:mm am/pm
                    //convierte la marca de tiempo a dd/mm/yyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //set data
                    actionBar.setTitle(hisName);
                    actionBar.setSubtitle(pTime);

                    // set image of the user who posted
                    //if there is no image i.e. pImage.equals("noImage") then hide ImageView
                    if (pImage.equals("noImage")) {
                        //hide Image Biew
                        pImageIv.setVisibility(View.GONE);
                    } else {
                        //show Image Biew
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(pImageIv);

                        } catch (Exception e) {

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //user is signed in
            myEmail = user.getEmail();
            myUid = user.getUid();

        } else {
            //user not signed in, go to main activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        //check on start app --comprobar en la aplicación de inicio
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        //set online -- establecer en linea
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide some menu items
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_chat).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
