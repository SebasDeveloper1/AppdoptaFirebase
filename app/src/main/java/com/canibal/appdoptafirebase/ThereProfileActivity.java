package com.canibal.appdoptafirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.canibal.appdoptafirebase.adapters.AdapterAdoptions;
import com.canibal.appdoptafirebase.adapters.AdapterPosts;
import com.canibal.appdoptafirebase.adapters.AdapterReportsFound;
import com.canibal.appdoptafirebase.adapters.AdapterReportsLost;
import com.canibal.appdoptafirebase.adapters.AdapterServices;
import com.canibal.appdoptafirebase.models.ModelAdoption;
import com.canibal.appdoptafirebase.models.ModelPost;
import com.canibal.appdoptafirebase.models.ModelReportsFound;
import com.canibal.appdoptafirebase.models.ModelReportsLost;
import com.canibal.appdoptafirebase.models.ModelServices;
import com.canibal.appdoptafirebase.notifications.Token;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    // views from xml -- vistas de xml
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, ccpTv, phoneTv, descriptionTv, tabComunityTv, tabAdoptionTv, tabReportsTv, TapReportsLost, TapReportsFound, tabServicesTv;
    FloatingActionButton fab;
    RecyclerView postsRecyclerView, adoptionsRecyclerView, recyclerView, recyclerView2, recyclerViewServices;
    LinearLayout LayoutReport;

    List<ModelPost> postList;
    List<ModelAdoption> adoptionList;
    List<ModelReportsLost> reportsLostList;
    List<ModelReportsFound> reportsFoundList;
    List<ModelServices> servicesList;
    AdapterPosts adapterPosts;
    AdapterAdoptions adapterAdoptions;
    AdapterReportsLost adapterReportsLost;
    AdapterReportsFound adapterReportsFound;
    AdapterServices adapterServices;
    String uid;
    String mUID;

    //progress dialog ---  dialogo de progreso
    ProgressDialog pd;

    private static final int REQUEST_PERMISSION_CALL = 255;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar = getSupportActionBar();
        String txttiletoolbar_profileuser = getString(R.string.txttiletoolbar_profileuser);
        actionBar.setTitle(txttiletoolbar_profileuser);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init views -- inicializar las vistas
        avatarIv = findViewById(R.id.avatarIv);
        coverIv = findViewById(R.id.coverIv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        ccpTv = findViewById(R.id.ccpTv);
        phoneTv = findViewById(R.id.phoneTv);
        descriptionTv = findViewById(R.id.descriptionTv);
        fab = findViewById(R.id.fab);
        tabComunityTv = findViewById(R.id.tabComunityTv);
        tabAdoptionTv = findViewById(R.id.tabAdoptionTv);
        tabReportsTv = findViewById(R.id.tabReportsTv);
        TapReportsLost = findViewById(R.id.TapReportsLost);
        TapReportsFound = findViewById(R.id.TapReportsFound);
        tabServicesTv = findViewById(R.id.tabServicesTv);
        postsRecyclerView = findViewById(R.id.recyclerview_posts);
        adoptionsRecyclerView = findViewById(R.id.recyclerview_adoptions);
        recyclerView = findViewById(R.id.recyclerview_reports);
        recyclerView2 = findViewById(R.id.recyclerview_reportsfound);
        recyclerViewServices = findViewById(R.id.servicesRecyclerview);
        LayoutReport = findViewById(R.id.LayoutReport);

        //init progress dialog  --  inicializar dialogo de progreso
        pd = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        //user is signed in stay here -- El usuario ha iniciado sesión aquí.
        assert user != null;
        mUID = user.getUid();

        //get  uid clicket user retrieve his posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //checkc until required data get -- checkc hasta que se obtengan los datos requeridos
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data -- obtener datos
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String ccp = "" + ds.child("ccp").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String description = "" + ds.child("description").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    //set data -- establecer datos
                    nameTv.setText(name);
                    emailTv.setText(email);
                    ccpTv.setText("+" + ccp);
                    phoneTv.setText(phone);
                    descriptionTv.setText(description);
                    try {
                        //if image is received then set -- si se recibe la imagen, establezca
                        Picasso.get().load(image).into(avatarIv);

                    } catch (Exception e) {
                        //if there is any exeption while getting image then set default -- si hay alguna excepción al obtener la imagen, configure el valor predeterminado

                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);

                    }

                    try {
                        //if image is received then set -- si se recibe la imagen, establezca
                        Picasso.get().load(cover).into(coverIv);

                    } catch (Exception e) {
                        //if there is any exeption while getting image then set default -- si hay alguna excepción al obtener la imagen, configure el valor predeterminado

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //fab button click -- clic en el botón fab
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditContactDialog();
            }
        });

        tabComunityTv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                postsRecyclerView.setVisibility(View.VISIBLE);
                adoptionsRecyclerView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                recyclerView2.setVisibility(View.GONE);
                LayoutReport.setVisibility(View.GONE);
                recyclerViewServices.setVisibility(View.GONE);

                tabComunityTv.setTextColor(getResources().getColor(R.color.editTextColorWhite));
                tabComunityTv.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_primary_dark));

                tabAdoptionTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabAdoptionTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));

                tabReportsTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabReportsTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));

                tabServicesTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabServicesTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));
            }
        });

        tabAdoptionTv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                postsRecyclerView.setVisibility(View.GONE);
                adoptionsRecyclerView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                recyclerView2.setVisibility(View.GONE);
                LayoutReport.setVisibility(View.GONE);
                recyclerViewServices.setVisibility(View.GONE);

                tabAdoptionTv.setTextColor(getResources().getColor(R.color.editTextColorWhite));
                tabAdoptionTv.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_primary_dark));

                tabComunityTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabComunityTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));

                tabReportsTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabReportsTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));

                tabServicesTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabServicesTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));
            }
        });

        tabReportsTv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                postsRecyclerView.setVisibility(View.GONE);
                adoptionsRecyclerView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView2.setVisibility(View.GONE);
                LayoutReport.setVisibility(View.VISIBLE);
                recyclerViewServices.setVisibility(View.GONE);

                tabReportsTv.setTextColor(getResources().getColor(R.color.editTextColorWhite));
                tabReportsTv.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_primary_dark));

                TapReportsLost.setTextColor(getResources().getColor(R.color.editTextColorBlack));
                TapReportsLost.setBackground(getResources().getDrawable(R.drawable.shape_rectangle02));

                TapReportsFound.setTextColor(getResources().getColor(R.color.editTextColorWhite));
                TapReportsFound.setBackground(null);

                tabComunityTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabComunityTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));

                tabAdoptionTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabAdoptionTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));

                tabServicesTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabServicesTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));
            }
        });

        TapReportsLost.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView2.setVisibility(View.GONE);

                TapReportsLost.setTextColor(getResources().getColor(R.color.editTextColorBlack));
                TapReportsLost.setBackground(getResources().getDrawable(R.drawable.shape_rectangle02));

                TapReportsFound.setTextColor(getResources().getColor(R.color.editTextColorWhite));
                TapReportsFound.setBackground(null);
            }
        });

        TapReportsFound.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                recyclerView2.setVisibility(View.VISIBLE);

                TapReportsFound.setTextColor(getResources().getColor(R.color.editTextColorBlack));
                TapReportsFound.setBackground(getResources().getDrawable(R.drawable.shape_rectangle02));


                TapReportsLost.setTextColor(getResources().getColor(R.color.editTextColorWhite));
                TapReportsLost.setBackground(null);
            }
        });

        tabServicesTv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                //pack de botones para tipo de reporte
                postsRecyclerView.setVisibility(View.GONE);
                adoptionsRecyclerView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                recyclerView2.setVisibility(View.GONE);
                recyclerViewServices.setVisibility(View.VISIBLE);

                //botones de funciones adic
                LayoutReport.setVisibility(View.GONE);

                tabAdoptionTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabAdoptionTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));

                tabComunityTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabComunityTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));

                tabReportsTv.setTextColor(getResources().getColor(R.color.TextcolorGray));
                tabReportsTv.setBackgroundColor(getResources().getColor(R.color.editTextColorWhite));

                tabServicesTv.setTextColor(getResources().getColor(R.color.editTextColorWhite));
                tabServicesTv.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_primary_dark));
            }
        });

        postList = new ArrayList<>();
        adoptionList = new ArrayList<>();
        reportsLostList = new ArrayList<>();
        reportsFoundList = new ArrayList<>();
        servicesList = new ArrayList<>();

        checkUserStatus();
        loadHistPosts();
        loadMyAdoptions();
        loadReportsLost();
        loadReportsFound();
        loadServices();
    }

    private void loadHistPosts() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post firs, for this load from last
        // muestra las últimas publicaciones, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview  -- establece este diseño para recyclerview
        postsRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load  posts
        /*whenever user publishes a post the uid of this  user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user */
        /* cada vez que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación
         * así que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //add  to list
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void loadMyAdoptions() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post firs, for this load from last
        // muestra las últimas publicaciones, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview  -- establece este diseño para recyclerview
        adoptionsRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Adoptions");
        //query to load  posts
        /*whenever user publishes a post the uid of this  user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user */
        /* cada vez que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación
         * así que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adoptionList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelAdoption myAdoptions = ds.getValue(ModelAdoption.class);

                    //add  to list
                    adoptionList.add(myAdoptions);

                    //adapter
                    adapterAdoptions = new AdapterAdoptions(ThereProfileActivity.this, adoptionList);
                    //set this adapter to recyclerview
                    adoptionsRecyclerView.setAdapter(adapterAdoptions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void loadReportsLost() {
        //recyclerview and its properties
        //vista del recyclerview y sus propiedades
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest pot first, for this load from last
        //muestra el bote más nuevo primero, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview -- establece el diseño en vista de reciclaje
        recyclerView.setLayoutManager(layoutManager);

        //path of all posts -- ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsLost");
        // get all data from this ref
        // obtener todos los datos de esta referencia
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reportsLostList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelReportsLost modelReportsLost = ds.getValue(ModelReportsLost.class);

                    reportsLostList.add(modelReportsLost);

                    //adapter
                    adapterReportsLost = new AdapterReportsLost(ThereProfileActivity.this, reportsLostList);
                    //set adapter  to recyclerview
                    recyclerView.setAdapter(adapterReportsLost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error se elimino es toast para correcto funcionamiento de cierre de secion
                // en caso de error
            }
        });
    }

    private void loadReportsFound() {
        //recyclerview and its properties
        //vista del recyclerview y sus propiedades
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest pot first, for this load from last
        //muestra el bote más nuevo primero, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview -- establece el diseño en vista de reciclaje
        recyclerView2.setLayoutManager(layoutManager);

        //path of all posts -- ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsFound");
        // get all data from this ref
        // obtener todos los datos de esta referencia
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reportsFoundList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelReportsFound modelReportsFound = ds.getValue(ModelReportsFound.class);

                    reportsFoundList.add(modelReportsFound);

                    //adapter
                    adapterReportsFound = new AdapterReportsFound(ThereProfileActivity.this, reportsFoundList);
                    //set adapter  to recyclerview
                    recyclerView2.setAdapter(adapterReportsFound);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error se elimino es toast para correcto funcionamiento de cierre de secion
                // en caso de error
            }
        });
    }

    private void loadServices() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post firs, for this load from last
        // muestra las últimas publicaciones, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview  -- establece este diseño para recyclerview
        recyclerViewServices.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Services");
        //query to load  posts
        /*whenever user publishes a post the uid of this  user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user */
        /* cada vez que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación
         * así que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                servicesList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelServices modelServices = ds.getValue(ModelServices.class);

                    servicesList.add(modelServices);

                    //adapter
                    adapterServices = new AdapterServices(ThereProfileActivity.this, servicesList);
                    //set adapter  to recyclerview
                    recyclerViewServices.setAdapter(adapterServices);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error se elimino es toast para correcto funcionamiento de cierre de secion
                // en caso de error
            }
        });
    }

    private void searchHistPosts(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);
        //show newest post firs, for this load from last
        // muestra las últimas publicaciones, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview  -- establece este diseño para recyclerview
        postsRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load  posts
        /*whenever user publishes a post the uid of this  user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user */
        /* cada vez que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación
         * así que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        //add  to list
                        postList.add(myPosts);
                    }

                    //adapter
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchAdoptions(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post firs, for this load from last
        // muestra las últimas publicaciones, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview  -- establece este diseño para recyclerview
        adoptionsRecyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Adoptions");
        //query to load  posts
        /*whenever user publishes a post the uid of this  user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user */
        /* cada vez que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación
         * así que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adoptionList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelAdoption modelAdoption = ds.getValue(ModelAdoption.class);

                    if (modelAdoption.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelAdoption.getpAge().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelAdoption.getpSpecies().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelAdoption.getpRace().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelAdoption.getpSex().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelAdoption.getpState().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelAdoption.getpLocation().toLowerCase().contains(searchQuery.toLowerCase())) {
                        adoptionList.add(modelAdoption);
                    }

                    //adapter -- adaptador
                    adapterAdoptions = new AdapterAdoptions(ThereProfileActivity.this, adoptionList);
                    //set adapter  to recyclerview
                    adoptionsRecyclerView.setAdapter(adapterAdoptions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error -- en caso de error
                Toast.makeText(ThereProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchReportsLost(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post firs, for this load from last
        // muestra las últimas publicaciones, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview  -- establece este diseño para recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsLost");
        //query to load  posts
        /*whenever user publishes a post the uid of this  user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user */
        /* cada vez que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación
         * así que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reportsLostList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelReportsLost modelReportsLost = ds.getValue(ModelReportsLost.class);

                    if (modelReportsLost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsLost.getpAge().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsLost.getpSpecies().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsLost.getpRace().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsLost.getpSex().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsLost.getpState().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsLost.getpLocation().toLowerCase().contains(searchQuery.toLowerCase())) {
                        reportsLostList.add(modelReportsLost);
                    }

                    //adapter -- adaptador
                    adapterReportsLost = new AdapterReportsLost(ThereProfileActivity.this, reportsLostList);
                    //set adapter  to recyclerview
                    recyclerView.setAdapter(adapterReportsLost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error -- en caso de error
                Toast.makeText(ThereProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchReportsFound(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post firs, for this load from last
        // muestra las últimas publicaciones, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview  -- establece este diseño para recyclerview
        recyclerView2.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsFound");
        //query to load  posts
        /*whenever user publishes a post the uid of this  user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user */
        /* cada vez que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación
         * así que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reportsFoundList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelReportsFound modelReportsFound = ds.getValue(ModelReportsFound.class);

                    if (modelReportsFound.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsFound.getpAge().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsFound.getpSpecies().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsFound.getpRace().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsFound.getpSex().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsFound.getpState().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelReportsFound.getpLocation().toLowerCase().contains(searchQuery.toLowerCase())) {
                        reportsFoundList.add(modelReportsFound);
                    }

                    //adapter -- adaptador
                    adapterReportsFound = new AdapterReportsFound(ThereProfileActivity.this, reportsFoundList);
                    //set adapter  to recyclerview
                    recyclerView2.setAdapter(adapterReportsFound);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error -- en caso de error
                Toast.makeText(ThereProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchServices(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post firs, for this load from last
        // muestra las últimas publicaciones, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview  -- establece este diseño para recyclerview
        recyclerViewServices.setLayoutManager(layoutManager);

        //init post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Services");
        //query to load  posts
        /*whenever user publishes a post the uid of this  user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user */
        /* cada vez que el usuario publica una publicación, el uid de este usuario también se guarda como información de la publicación
         * así que estamos recuperando publicaciones que tienen uid igual a uid del usuario actual */
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                servicesList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelServices modelServices = ds.getValue(ModelServices.class);

                    if (modelServices.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelServices.getpCategory().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelServices.getpLocation().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelServices.getpSchedule().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelServices.getpNumercontact().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelServices.getpServicewebsite().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelServices.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        servicesList.add(modelServices);
                    }

                    //adapter -- adaptador
                    adapterServices = new AdapterServices(ThereProfileActivity.this, servicesList);
                    //set adapter  to recyclerview
                    recyclerViewServices.setAdapter(adapterServices);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error -- en caso de error
                Toast.makeText(ThereProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showEditContactDialog() {
        /* Mostrar diálogo que contiene opciones
         * 1) Chat AppDopta
         * 2) Chat Whatsapp
         * 3) LLamada*/

        String txtcontact = getString(R.string.txtcontact);
        String txtchatappdopta = getString(R.string.txtchatappdopta);
        String txtchatwhatsapp = getString(R.string.txtchatwhatsapp);
        String txtllamada = getString(R.string.txtllamada);

        //options to show dialog -- opciones para mostrar el diálogo
        String options[] = {txtchatappdopta, txtchatwhatsapp, txtllamada};
        // alert dialog -- diálogo de alerta
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set title -- establecer título
        builder.setTitle(txtcontact);
        //set items to dialog -- establecer elementos para dialogar
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clics -- manejar clics de elementos de diálogo
                if (which == 0) {
                    /* iniciar actividad poniendo UID del receptor
                     * usaremos ese UID para identificar al usuario con el que vamos a chatear */
                    imBlockedORNot(uid);

                } else if (which == 1) {
                    Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            //checkc until required data get -- checkc hasta que se obtengan los datos requeridos
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                //get data -- obtener datos
                                String ccp = "" + ds.child("ccp").getValue();
                                String phone = "" + ds.child("phone").getValue();

                                //set data -- establecer datos
                                Uri uri = Uri.parse("https://wa.me/" + ccp + phone);
                                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(i);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else if (which == 2) {
                    Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            //checkc until required data get -- checkc hasta que se obtengan los datos requeridos
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                //get data -- obtener datos
                                String phone = "" + ds.child("phone").getValue();

                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {// Si el API es < a 23
                                    call(phone);
                                } else { //API > 23
                                    if (ContextCompat.checkSelfPermission(ThereProfileActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                        call(phone);
                                    } else {
                                        if (ActivityCompat.shouldShowRequestPermissionRationale(ThereProfileActivity.this, Manifest.permission.CALL_PHONE)) {//true
                                        } else {
                                        }
                                        ActivityCompat.requestPermissions(ThereProfileActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PERMISSION_CALL);
                                    }
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        });
        //create and show dialog -- crear y mostrar diálogo
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CALL) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //checkc until required data get -- checkc hasta que se obtengan los datos requeridos
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            //get data -- obtener datos
                            String phone = "" + ds.child("phone").getValue();

                            //set data -- establecer datos
                            call(phone);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(ThereProfileActivity.this, Manifest.permission.CALL_PHONE)) {//TRUE
                    String txtllamadapermission = getString(R.string.txtllamadapermission);
                    String psotivebtnllamadapermission = getString(R.string.psotivebtnllamadapermission);
                    String negativebtnllamadapermission = getString(R.string.negativebtnllamadapermission);
                    new AlertDialog.Builder(this).setMessage(txtllamadapermission)
                            .setPositiveButton(psotivebtnllamadapermission, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(ThereProfileActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PERMISSION_CALL);
                                }
                            })
                            .setNegativeButton(negativebtnllamadapermission, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                } else {//false
                    String txtllamadapermissionreqsettings = getString(R.string.txtllamadapermissionreqsettings);
                    Toast.makeText(this, txtllamadapermissionreqsettings, Toast.LENGTH_SHORT).show();
                }
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void call(String phone) {
        String start = "tel:" + phone;
        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse(start));
        startActivity(i);
    }

    private void imBlockedORNot(final String hisUID) {
        //first check if sender (current user) is blocked by receiver or not
        //Logic: if uid of the sender (current user) exists in "BlockedUsers" of receiver then sender (current user) is blocked , otherwise not
        //is blocked then just display a message e.g. You're blocked by that user, can't send message
        //is not blocked then simply start the chat activity

        // primero verifica si el remitente (usuario actual) está bloqueado por el receptor o no
        // Lógica: si el uid del remitente (usuario actual) existe en "BlockedUsers" del receptor, el remitente (usuario actual) está bloqueado, de lo contrario no
        // está bloqueado, luego solo muestra un mensaje, p. ej. Estás bloqueado por ese usuario, no puedes enviar mensajes
        // no está bloqueado, simplemente inicie la actividad de chat

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(mUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.exists()) {
                                String blocketuserpositive = getString(R.string.blocketuserpositive);
                                Toast.makeText(ThereProfileActivity.this, blocketuserpositive, Toast.LENGTH_SHORT).show();
                                //blocked dont proceed further
                                return;
                            }
                        }
                        //not blocked start activity chat
                        Intent intent = new Intent(ThereProfileActivity.this, ChatActivity.class);
                        intent.putExtra("hisUid", hisUID);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void checkUserStatus() {
        //get current user -- obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here -- El usuario ha iniciado sesión aquí.
            // set email of logged in user -- configurar el correo electrónico del usuario conectado
            //mProfileTv.setText(user.getEmail());
        } else {
            //user not signed in, go to main activity -- usuario no ha iniciado sesión, vaya a la actividad principal
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
        menu.findItem(R.id.action_add_post).setVisible(false); //hide add post from this activity--ocultar agregar publicación de esta actividad
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_chat).setVisible(false);

        //searchView
        MenuItem item = menu.findItem(R.id.action_search);
        //v7 searchview ot search user specific posts
        android.widget.SearchView searchView = (android.widget.SearchView) MenuItemCompat.getActionView(item);
        String txthitsearch = getString(R.string.txtsearch_homefragment);
        searchView.setQueryHint(txthitsearch);
        //search listener
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button from keyboard
                //if search query is not empty the search

                // se llama cuando el usuario presiona el botón de búsqueda desde el teclado
                // si la consulta de búsqueda no está vacía, la búsqueda
                if (!TextUtils.isEmpty(s.trim())) {
                    //search text contains text, search it -- buscar texto contiene texto, búscalo
                    searchHistPosts(s);
                    searchAdoptions(s);
                    searchReportsLost(s);
                    searchReportsFound(s);
                    searchServices(s);

                } else {
                    //search text empty, get all users -- Buscar texto vacío, obtener todos los usuarios
                    loadHistPosts();
                    loadMyAdoptions();
                    loadReportsLost();
                    loadReportsFound();
                    loadServices();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called whenever user press any single letter
                //if search query is not empty the search
                if (!TextUtils.isEmpty(s.trim())) {
                    //search text contains text, search it
                    searchHistPosts(s);
                    searchAdoptions(s);
                    searchReportsLost(s);
                    searchReportsFound(s);
                    searchServices(s);

                } else {
                    //search text empty, get all users
                    loadHistPosts();
                    loadMyAdoptions();
                    loadReportsLost();
                    loadReportsFound();
                    loadServices();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
