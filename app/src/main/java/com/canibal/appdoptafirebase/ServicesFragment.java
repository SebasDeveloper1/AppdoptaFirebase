package com.canibal.appdoptafirebase;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.canibal.appdoptafirebase.adapters.AdapterPosts;
import com.canibal.appdoptafirebase.adapters.AdapterServices;
import com.canibal.appdoptafirebase.models.ModelPost;
import com.canibal.appdoptafirebase.models.ModelServices;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ServicesFragment extends Fragment {
    //Firebase
    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    List<ModelServices> servicesList;
    AdapterServices adapterServices;

    // views from xml -- vistas de xml
    FloatingActionButton fapvete, fapstore, fapguarde, fapfundations, fapAesthetic;
    ImageView BannerIv, DogIv;
    TextView txtTitleService;
    Button AddServiceBt;


    String uid;

    public ServicesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment -- Inflar el diseño de este fragmento
        View view = inflater.inflate(R.layout.fragment_services, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //init views -- inicializar las vistas
        fapvete = view.findViewById(R.id.fapvete);
        fapstore = view.findViewById(R.id.fapstore);
        fapguarde = view.findViewById(R.id.fapguarde);
        fapfundations = view.findViewById(R.id.fapfundations);
        fapAesthetic = view.findViewById(R.id.fapAesthetic);
        BannerIv = view.findViewById(R.id.BannerIv);
        DogIv = view.findViewById(R.id.DogIv);
        txtTitleService = view.findViewById(R.id.txtTitleService);
        AddServiceBt = view.findViewById(R.id.AddServiceBt);

        //recyclerview and its properties
        //vista del recyclerview y sus propiedades
        recyclerView = view.findViewById(R.id.servicesRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest pot first, for this load from last
        //muestra el bote más nuevo primero, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview -- establece el diseño en vista de reciclaje
        recyclerView.setLayoutManager(layoutManager);

        //init post list -- inicializar lista de publicaciones
        servicesList = new ArrayList<>();

        loadServices();

        AddServiceBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddServiceActivity.class));
            }
        });

        Buttons();

        return view;
    }

    private void loadServices() {
        //path of all posts -- ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Services");
        // get all data from this ref
        // obtener todos los datos de esta referencia
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                servicesList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelServices modelServices = ds.getValue(ModelServices.class);

                    servicesList.add(modelServices);

                    //adapter
                    adapterServices = new AdapterServices(getActivity(), servicesList);
                    //set adapter  to recyclerview
                    recyclerView.setAdapter(adapterServices);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error se elimino es toast para correcto funcionamiento de cierre de secion
                // en caso de error
            }
        });
    }

    private void searchPosts(final String searchQuery) {

        //path of all posts -- ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Services");
        // get all data from this ref
        // obtener todos los datos de esta referencia
        ref.addValueEventListener(new ValueEventListener() {
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
                    recyclerView.setAdapter(adapterServices);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error -- en caso de error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchCategory(final String searchCategory) {

        //path of all posts -- ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Services");
        // get all data from this ref
        // obtener todos los datos de esta referencia
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                servicesList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelServices modelServices = ds.getValue(ModelServices.class);

                    if (modelServices.getpCategory().toLowerCase().contains(searchCategory.toLowerCase())) {
                        servicesList.add(modelServices);
                    }

                    //adapter -- adaptador
                    adapterServices = new AdapterServices(getActivity(), servicesList);
                    //set adapter  to recyclerview
                    recyclerView.setAdapter(adapterServices);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error -- en caso de error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void Buttons() {

        fapvete.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                BannerIv.setBackground(getResources().getDrawable(R.drawable.coverveterinarias));
                DogIv.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                fapvete.setColorNormal(getResources().getColor(R.color.colorPrimaryDark));
                fapvete.setIconDrawable(getResources().getDrawable(R.drawable.veterinarianwhite));

                fapstore.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapstore.setIconDrawable(getResources().getDrawable(R.drawable.ic_store_purple));

                fapguarde.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapguarde.setIconDrawable(getResources().getDrawable(R.drawable.animal_home_red));

                fapfundations.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapfundations.setIconDrawable(getResources().getDrawable(R.drawable.dog_heart_green));

                fapAesthetic.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapAesthetic.setIconDrawable(getResources().getDrawable(R.drawable.ic_aesthetic_orange));

                txtTitleService.setText(R.string.txtveterinary);

                String txtVeterinarias = getString(R.string.txtVeterinarias);
                searchCategory(txtVeterinarias);

            }
        });

        fapstore.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                BannerIv.setBackground(getResources().getDrawable(R.drawable.covertiendas));
                DogIv.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                fapvete.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapvete.setIconDrawable(getResources().getDrawable(R.drawable.veterinarianblue));

                fapstore.setColorNormal(getResources().getColor(R.color.ColorPruplePrimay));
                fapstore.setIconDrawable(getResources().getDrawable(R.drawable.ic_store_white));

                fapguarde.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapguarde.setIconDrawable(getResources().getDrawable(R.drawable.animal_home_red));

                fapfundations.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapfundations.setIconDrawable(getResources().getDrawable(R.drawable.dog_heart_green));

                fapAesthetic.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapAesthetic.setIconDrawable(getResources().getDrawable(R.drawable.ic_aesthetic_orange));

                txtTitleService.setText(R.string.txtstorepets);

                String txtTiendas = getString(R.string.txtTiendas);
                searchCategory(txtTiendas);
            }
        });

        fapguarde.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                BannerIv.setBackground(getResources().getDrawable(R.drawable.coverguarder_as));
                DogIv.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                fapvete.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapvete.setIconDrawable(getResources().getDrawable(R.drawable.veterinarianblue));

                fapstore.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapstore.setIconDrawable(getResources().getDrawable(R.drawable.ic_store_purple));

                fapguarde.setColorNormal(getResources().getColor(R.color.ColorRedPrimay));
                fapguarde.setIconDrawable(getResources().getDrawable(R.drawable.animal_home_white));

                fapfundations.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapfundations.setIconDrawable(getResources().getDrawable(R.drawable.dog_heart_green));

                fapAesthetic.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapAesthetic.setIconDrawable(getResources().getDrawable(R.drawable.ic_aesthetic_orange));

                txtTitleService.setText(R.string.txtdaycarecenters);

                String txtGuarderias = getString(R.string.txtGuarderías);
                searchCategory(txtGuarderias);
            }
        });

        fapfundations.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                BannerIv.setBackground(getResources().getDrawable(R.drawable.coverfundaciones));
                DogIv.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                fapvete.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapvete.setIconDrawable(getResources().getDrawable(R.drawable.veterinarianblue));

                fapstore.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapstore.setIconDrawable(getResources().getDrawable(R.drawable.ic_store_purple));

                fapguarde.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapguarde.setIconDrawable(getResources().getDrawable(R.drawable.animal_home_red));

                fapfundations.setColorNormal(getResources().getColor(R.color.ColorGreenicon));
                fapfundations.setIconDrawable(getResources().getDrawable(R.drawable.dog_heart_white));

                fapAesthetic.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapAesthetic.setIconDrawable(getResources().getDrawable(R.drawable.ic_aesthetic_orange));

                txtTitleService.setText(R.string.txtfundtions);

                String txtFundaciones = getString(R.string.txtFundaciones);
                searchCategory(txtFundaciones);
            }
        });

        fapAesthetic.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                BannerIv.setBackground(getResources().getDrawable(R.drawable.coveresteticas));
                DogIv.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                fapvete.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapvete.setIconDrawable(getResources().getDrawable(R.drawable.veterinarianblue));

                fapstore.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapstore.setIconDrawable(getResources().getDrawable(R.drawable.ic_store_purple));

                fapguarde.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapguarde.setIconDrawable(getResources().getDrawable(R.drawable.animal_home_red));

                fapfundations.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
                fapfundations.setIconDrawable(getResources().getDrawable(R.drawable.dog_heart_green));

                fapAesthetic.setColorNormal(getResources().getColor(R.color.ColororangePrimary));
                fapAesthetic.setIconDrawable(getResources().getDrawable(R.drawable.ic_aesthetic_white));

                txtTitleService.setText(R.string.txtaesthetic);

                String txtEsteticas = getString(R.string.txtEstéticas);
                searchCategory(txtEsteticas);
            }
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void retun() {
        BannerIv.setBackground(getResources().getDrawable(R.drawable.coverservicios));
        DogIv.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        fapvete.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
        fapvete.setIconDrawable(getResources().getDrawable(R.drawable.veterinarianblue));

        fapstore.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
        fapstore.setIconDrawable(getResources().getDrawable(R.drawable.ic_store_purple));

        fapguarde.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
        fapguarde.setIconDrawable(getResources().getDrawable(R.drawable.animal_home_red));

        fapfundations.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
        fapfundations.setIconDrawable(getResources().getDrawable(R.drawable.dog_heart_green));

        fapAesthetic.setColorNormal(getResources().getColor(R.color.editTextColorWhite));
        fapAesthetic.setIconDrawable(getResources().getDrawable(R.drawable.ic_aesthetic_orange));

        txtTitleService.setText(null);
        servicesList.clear();
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflating menu -- menú inflado
        inflater.inflate(R.menu.menu_main, menu);
        //ocultar opciones innecesarias para el fragmento
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

        //searchview to search posts by post title / description -- searchview para buscar publicaciones por título / descripción de la publicación
        MenuItem item = menu.findItem(R.id.action_search);
        final android.widget.SearchView searchView = (android.widget.SearchView) MenuItemCompat.getActionView(item);
        String txthitsearch = getString(R.string.txtsearch_homefragment);
        searchView.setQueryHint(txthitsearch);
        //search listener
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s.trim())) {
                    txtTitleService.setText(R.string.txtresults);
                    DogIv.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    searchPosts(s);
                } else {
                    retun();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s.trim())) {
                    txtTitleService.setText(R.string.txtresults);
                    searchPosts(s);
                    DogIv.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    retun();
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
        if (id == R.id.action_chat) {
            startActivity(new Intent(getActivity(), Dashboard3Activity.class));
        }

        return super.onOptionsItemSelected(item);

    }
}