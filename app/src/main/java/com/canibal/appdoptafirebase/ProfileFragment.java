package com.canibal.appdoptafirebase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //storage
    StorageReference storageReference;

    // views from xml -- vistas de xml
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, ccpTv, phoneTv, descriptionTv, tabComunityTv, tabAdoptionTv, tabReportsTv, TapReportsLost, TapReportsFound, tabServicesTv;
    FloatingActionButton fab;
    RecyclerView postsRecyclerView, adoptionsRecyclerView, recyclerView, recyclerView2, recyclerViewServices;
    LinearLayout LayoutReport;
    Button ServicesBtn, reportlostBtn, reportfoundBtn, ComunityBtn, AdoptionBtn;

    //progress dialog ---  dialogo de progreso
    ProgressDialog pd;

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

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //firebase storage reference -- referencia de almacenamiento


        //init views -- inicializar las vistas
        avatarIv = view.findViewById(R.id.avatarIv);
        coverIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        ccpTv = view.findViewById(R.id.ccpTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        descriptionTv = view.findViewById(R.id.descriptionTv);
        fab = view.findViewById(R.id.fab);
        tabComunityTv = view.findViewById(R.id.tabComunityTv);
        tabAdoptionTv = view.findViewById(R.id.tabAdoptionTv);
        tabReportsTv = view.findViewById(R.id.tabReportsTv);
        TapReportsLost = view.findViewById(R.id.TapReportsLost);
        TapReportsFound = view.findViewById(R.id.TapReportsFound);
        tabServicesTv = view.findViewById(R.id.tabServicesTv);
        postsRecyclerView = view.findViewById(R.id.recyclerview_posts);
        adoptionsRecyclerView = view.findViewById(R.id.recyclerview_adoptions);
        recyclerView = view.findViewById(R.id.recyclerview_reports);
        recyclerView2 = view.findViewById(R.id.recyclerview_reportsfound);
        recyclerViewServices = view.findViewById(R.id.servicesRecyclerview);
        LayoutReport = view.findViewById(R.id.LayoutReport);
        ServicesBtn = view.findViewById(R.id.ServicesBtn);
        reportlostBtn = view.findViewById(R.id.reportlostBtn);
        reportfoundBtn = view.findViewById(R.id.reportfoundBtn);
        ComunityBtn = view.findViewById(R.id.ComunityBtn);
        AdoptionBtn = view.findViewById(R.id.AdoptionBtn);

        //init progress dialog  --  inicializar dialogo de progreso
        pd = new ProgressDialog(getActivity());

        /*we have to get info of currently signed in user. we can get it using user's email or uid
        I'm gonna retrieve user detail using email -- tenemos que obtener información del usuario
        actualmente conectado. podemos obtenerlo usando el correo electrónico o el uid del usuario
        Voy a recuperar los detalles del usuario usando el correo electrónico*/

        /*By using orderByChild query we will show the detail from a node
        whose key named email has value equal to currently signed in email.
        It will search all nodes, where the key matches it will get its detail

        Al usar la consulta orderByChild mostraremos los detalles de un nodo
        cuya clave llamada correo electrónico tiene un valor igual al correo
        electrónico actualmente registrado.
        Buscará todos los nodos, donde la clave coincida obtendrá su detalle*/

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //checkc until required data get -- checkc hasta que se obtengan los datos requeridos
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data -- obtener datos
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String ccp = "" + ds.child("ccp").getValue();
                    String description = "" + ds.child("description").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    //set data -- establecer datos
                    nameTv.setText(name);
                    emailTv.setText(email);
                    ccpTv.setText("+" + ccp + "/");
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
                startActivity(new Intent(getActivity(), EditUserDataActivity.class));
            }
        });

        //Caracteristicas del boton
        ComunityBtn.setVisibility(View.VISIBLE);
        ComunityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddPostActivity.class));
            }
        });

        tabComunityTv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                //vistas de reciclage
                postsRecyclerView.setVisibility(View.VISIBLE);
                adoptionsRecyclerView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                recyclerView2.setVisibility(View.GONE);
                recyclerViewServices.setVisibility(View.GONE);

                //pack de botones para tipo de reporte
                LayoutReport.setVisibility(View.GONE);
                reportlostBtn.setVisibility(View.GONE);
                reportfoundBtn.setVisibility(View.GONE);

                //botones de funciones adic
                AdoptionBtn.setVisibility(View.GONE);
                ServicesBtn.setVisibility(View.GONE);

                //Caracteristicas del boton
                ComunityBtn.setVisibility(View.VISIBLE);
                ComunityBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), AddPostActivity.class));
                    }
                });

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
                //vistas de reciclage
                postsRecyclerView.setVisibility(View.GONE);
                adoptionsRecyclerView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                recyclerView2.setVisibility(View.GONE);
                recyclerViewServices.setVisibility(View.GONE);

                //pack de botones para tipo de reporte
                LayoutReport.setVisibility(View.GONE);
                reportlostBtn.setVisibility(View.GONE);
                reportfoundBtn.setVisibility(View.GONE);

                //botones de funciones adic
                ComunityBtn.setVisibility(View.GONE);
                ServicesBtn.setVisibility(View.GONE);

                //Caracteristicas del boton
                AdoptionBtn.setVisibility(View.VISIBLE);
                AdoptionBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), AddAdoptionActivity.class));
                    }
                });

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
                //vistas de reciclage
                postsRecyclerView.setVisibility(View.GONE);
                adoptionsRecyclerView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView2.setVisibility(View.GONE);
                recyclerViewServices.setVisibility(View.GONE);

                //pack de botones para tipo de reporte
                LayoutReport.setVisibility(View.VISIBLE);
                reportfoundBtn.setVisibility(View.GONE);

                //botones de funciones adic
                ComunityBtn.setVisibility(View.GONE);
                AdoptionBtn.setVisibility(View.GONE);
                ServicesBtn.setVisibility(View.GONE);

                //Attributos del boton
                reportlostBtn.setVisibility(View.VISIBLE);
                reportlostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), AddReportsLostActivity.class));
                    }
                });

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
                //vistas de reciclage
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView2.setVisibility(View.GONE);
                recyclerViewServices.setVisibility(View.GONE);
                reportfoundBtn.setVisibility(View.GONE);
                ComunityBtn.setVisibility(View.GONE);
                AdoptionBtn.setVisibility(View.GONE);

                //Attributos del boton
                reportlostBtn.setVisibility(View.VISIBLE);
                reportlostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), AddReportsLostActivity.class));
                    }
                });

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
                reportlostBtn.setVisibility(View.GONE);
                ComunityBtn.setVisibility(View.GONE);
                AdoptionBtn.setVisibility(View.GONE);

                //Attributos del boton
                reportfoundBtn.setVisibility(View.VISIBLE);
                reportfoundBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), AddReportsFoundActivity.class));
                    }
                });

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
                reportlostBtn.setVisibility(View.GONE);
                reportfoundBtn.setVisibility(View.GONE);

                //Caracteristicas del boton
                ComunityBtn.setVisibility(View.GONE);
                AdoptionBtn.setVisibility(View.GONE);
                ServicesBtn.setVisibility(View.VISIBLE);
                ServicesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), AddServiceActivity.class));
                    }
                });

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
        loadMyPosts();
        loadMyAdoptions();
        loadReportsLost();
        loadReportsFound();
        loadServices();

        return view;
    }

    private void loadMyPosts() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error se elimino es toast para correcto funcionamiento de cierre de secion
            }
        });
    }

    private void loadMyAdoptions() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterAdoptions = new AdapterAdoptions(getActivity(), adoptionList);
                    //set this adapter to recyclerview
                    adoptionsRecyclerView.setAdapter(adapterAdoptions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error se elimino es toast para correcto funcionamiento de cierre de secion
            }
        });
    }

    private void loadReportsLost() {
        //recyclerview and its properties
        //vista del recyclerview y sus propiedades
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterReportsLost = new AdapterReportsLost(getActivity(), reportsLostList);
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterReportsFound = new AdapterReportsFound(getActivity(), reportsFoundList);
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterServices = new AdapterServices(getActivity(), servicesList);
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

    private void searchMyPosts(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchAdoptions(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterAdoptions = new AdapterAdoptions(getActivity(), adoptionList);
                    //set adapter  to recyclerview
                    adoptionsRecyclerView.setAdapter(adapterAdoptions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error -- en caso de error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchReportsLost(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterReportsLost = new AdapterReportsLost(getActivity(), reportsLostList);
                    //set adapter  to recyclerview
                    recyclerView.setAdapter(adapterReportsLost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error -- en caso de error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchReportsFound(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterReportsFound = new AdapterReportsFound(getActivity(), reportsFoundList);
                    //set adapter  to recyclerview
                    recyclerView2.setAdapter(adapterReportsFound);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error -- en caso de error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchServices(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    adapterServices = new AdapterServices(getActivity(), servicesList);
                    //set adapter  to recyclerview
                    recyclerViewServices.setAdapter(adapterServices);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error -- en caso de error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            uid = user.getUid();

        } else {
            //user not signed in, go to main activity -- usuario no ha iniciado sesión, vaya a la actividad principal
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//to show menu options in fragment
        super.onCreate(savedInstanceState);
    }

    /*inflate options menu -- menú de opciones de inflado*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflating menu -- menú inflado
        inflater.inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_chat).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

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
                    searchMyPosts(s);
                    searchAdoptions(s);
                    searchReportsLost(s);
                    searchReportsFound(s);
                    searchServices(s);

                } else {
                    //search text empty, get all users -- Buscar texto vacío, obtener todos los usuarios
                    loadMyPosts();
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
                    searchMyPosts(s);
                    searchAdoptions(s);
                    searchReportsLost(s);
                    searchReportsFound(s);
                    searchServices(s);

                } else {
                    //search text empty, get all users
                    loadMyPosts();
                    loadMyAdoptions();
                    loadReportsLost();
                    loadReportsFound();
                    loadServices();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    /*handle menu item clicks -- manejar clics de elementos de menú*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        } else if (id == R.id.action_settings) {
            //go to SettingsActivity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);

    }

}
