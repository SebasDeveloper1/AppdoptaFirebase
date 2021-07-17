package com.canibal.appdoptafirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;

import com.canibal.appdoptafirebase.adapters.AdapterUsers;
import com.canibal.appdoptafirebase.adapters.AdapterUsersLiked;
import com.canibal.appdoptafirebase.models.ModelUser;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostLikedByActivity extends AppCompatActivity {

    String postId, postType;
    String mUID;

    TextView pLikesTv;

    private RecyclerView recyclerView;

    private List<ModelUser> userList;
    private AdapterUsersLiked adapterUsersLiked;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked_by);

        //toolbar
        String txttitlelikebyactivity = getString(R.string.txttitlelikebyactivity);
        showToolbar(txttitlelikebyactivity, true);

        //init views
        pLikesTv = findViewById(R.id.pLikesTv);

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerView);

        //get the post id
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        postType = intent.getStringExtra("postType");

        FirebaseUser user = firebaseAuth.getCurrentUser();
        //user is signed in stay here -- El usuario ha iniciado sesión aquí.
        assert user != null;
        mUID = user.getUid();

        userList = new ArrayList<>();

        loadPostInfo(postType);

        //get the list of UID of users eho liked the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes");
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String hisUid = "" + ds.getRef().getKey();

                    //get user info from each id
                    getUsers(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUsers(String hisUid) {
        //get information of each user using uid
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelUser modelUser = ds.getValue(ModelUser.class);
                            userList.add(modelUser);
                        }

                        //setup adapter
                        adapterUsersLiked = new AdapterUsersLiked(PostLikedByActivity.this, userList);
                        //set adapter to recyclerview
                        recyclerView.setAdapter(adapterUsersLiked);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadPostInfo(String postReference) {
        //get post using the id of the  post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(postReference);
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep checking the posts until get the  required post
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    String hisDp = "" + ds.child("uDp").getValue();
                    String hisUid = "" + ds.child("uid").getValue();
                    String pLikes = "" + ds.child("pLikes").getValue();


                    //convert timestamp to dd/mm/yyy hh:mm am/pm
                    //convierte la marca de tiempo a dd/mm/yyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    pLikesTv.setText(pLikes);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        //check on start app --comprobar en la aplicación de inicio
        super.onStart();
    }

    @Override
    protected void onResume() {
        //set online -- establecer en linea
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go to previous activity
        return super.onSupportNavigateUp();
    }

    public void showToolbar(String tittle, boolean upButton) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(tittle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);

    }
}