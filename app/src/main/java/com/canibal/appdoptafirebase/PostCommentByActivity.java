package com.canibal.appdoptafirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.canibal.appdoptafirebase.adapters.AdapterComments;
import com.canibal.appdoptafirebase.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostCommentByActivity extends AppCompatActivity {

    //to get detail of user and post
    String hisUid, myUid, myEmail, myName, myDp, hisDp;
    String postType, postId;


    boolean mProcessComment = false;

    //progress bar
    ProgressDialog pd;


    //views -- vistas
    RecyclerView recyclerView;
    LinearLayout profileLayout;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    //add comments views
    TextView pCommentsTv;
    EditText commentEt;
    ImageButton sendBtn, imagebackBtn;
    ImageView cAvatarIv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_comment_by);

        //get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        postType = intent.getStringExtra("postType");


        // init views
        pCommentsTv = findViewById(R.id.pCommentsTv);
        profileLayout = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);

        //init views of the comments
        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        imagebackBtn = findViewById(R.id.imagebackBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);


        loadPostInfo(postType);

        checkUserStatus();

        loadUserInfo();

        loadComments(postType);

        //back button click
        imagebackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });

        //send comment button click
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment(postType);
            }
        });

    }

    private void addToHisNotifications(String hisUid, String pId, String notification, String postType) {
        String timestamp = "" + System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("postType", postType);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });
    }

    private void loadComments(String postReference) {
        //Layout (Linear) for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //init comments list
        commentList = new ArrayList<>();

        //path of the post, to get it´s comments
        // ruta de la publicación, para obtener sus comentarios
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(postReference).child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    //pass myUid and postId as parameter of constructor  of Comment Adapter
                    // pasa myUid y postId como parámetro del constructor del Adaptador de comentarios

                    // setup adapter
                    adapterComments = new AdapterComments(getApplicationContext(), commentList, myUid, postId, postType);
                    //set adapter
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void postComment(final String postReference) {
        pd = new ProgressDialog(this);
        String progresbaradingcomment = getString(R.string.progresbaradingcomment);
        pd.setMessage(progresbaradingcomment);

        //get data from comment edit text
        String comment = commentEt.getText().toString().trim();
        //validate
        if (TextUtils.isEmpty(comment)) {
            //no value is entered
            Toast.makeText(this, R.string.txtcommentempty, Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());

        //each post will have a child "Comments" tha will contain comments of that post
        // cada publicación tendrá un "Comentario" secundario que contendrá comentarios de esa publicación
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(postReference).child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        //put info in hashmap
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        //put this data in db
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added
                        pd.dismiss();
                        Toast.makeText(PostCommentByActivity.this, R.string.txtaddcomment, Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount(postReference);

                        //enviar notificacion de comentario si el uid del usuario activo es diferente al uid del usuario que publico el post
                        if (!hisUid.equals(myUid)) {
                            if (postType.equals("Posts")) {
                                String txtnotificomment = getString(R.string.txtnotificomment);
                                addToHisNotifications("" + hisUid, "" + postId, txtnotificomment, postType);

                            } else if (postType.equals("Adoptions")) {
                                String txtnotificomment = getString(R.string.txtnotificommentadoption);
                                addToHisNotifications("" + hisUid, "" + postId, txtnotificomment, postType);

                            } else if (postType.equals("ReportsLost")) {
                                String txtnotificomment = getString(R.string.txtnotificommentReportsLost);
                                addToHisNotifications("" + hisUid, "" + postId, txtnotificomment, postType);

                            } else if (postType.equals("ReportsFound")) {
                                String txtnotificomment = getString(R.string.txtnotificommentReportsFound);
                                addToHisNotifications("" + hisUid, "" + postId, txtnotificomment, postType);

                            } else if (postType.equals("Services")) {
                                String txtnotificomment = getString(R.string.txtnotificommentService);
                                addToHisNotifications("" + hisUid, "" + postId, txtnotificomment, postType);
                            }

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed, not added
                        pd.dismiss();
                        Toast.makeText(PostCommentByActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void updateCommentCount(String postReference) {
        //whenever user adds comment increase  the comment count as we did for like count
        // cada vez que el usuario agrega un comentario, aumente el recuento de comentarios como lo hicimos para el recuento similar
        mProcessComment = true;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(postReference).child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessComment) {
                    String comments = "" + dataSnapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue("" + newCommentVal);
                    mProcessComment = false;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {
        //get current user info
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    myName = "" + ds.child("name").getValue();
                    myDp = "" + ds.child("image").getValue();

                    //set data
                    try {
                        //if image is received then set
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_default_img).into(cAvatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img).into(cAvatarIv);
                    }

                }
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
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String commentCount = "" + ds.child("pComments").getValue();


                    //convert timestamp to dd/mm/yyy hh:mm am/pm
                    //convierte la marca de tiempo a dd/mm/yyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    pCommentsTv.setText(commentCount);

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
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        //set online -- establecer en linea
        super.onResume();
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
