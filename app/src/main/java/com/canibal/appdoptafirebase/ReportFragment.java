package com.canibal.appdoptafirebase;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.core.widget.NestedScrollView;
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
import android.widget.TextView;
import android.widget.Toast;

import com.canibal.appdoptafirebase.adapters.AdapterReportsFound;
import com.canibal.appdoptafirebase.adapters.AdapterReportsLost;
import com.canibal.appdoptafirebase.models.ModelReportsFound;
import com.canibal.appdoptafirebase.models.ModelReportsLost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReportFragment extends Fragment {
    //firebase auth
    FirebaseAuth firebaseAuth;

    TextView TapReportsLost, TapReportsFound;
    Button reportlostBtn, reportfoundBtn;
    NestedScrollView Lost, Found;
    RecyclerView recyclerView, recyclerView2;
    List<ModelReportsLost> reportsLostList;
    List<ModelReportsFound> reportsFoundList;
    AdapterReportsLost adapterReportsLost;
    AdapterReportsFound adapterReportsFound;

    public ReportFragment() {
        // Required empty public constructor
        // Obligatorio constructor público vacío
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment -- Inflar el diseño de este fragmento
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // inicializar las vistas de recclage usadas para reportes de perdida y de encontrados

        TapReportsLost = view.findViewById(R.id.TapReportsLost);
        TapReportsFound = view.findViewById(R.id.TapReportsFound);
        recyclerView = view.findViewById(R.id.recyclerview_reports);
        Lost = view.findViewById(R.id.Lost);
        reportlostBtn = view.findViewById(R.id.reportlostBtn);
        recyclerView2 = view.findViewById(R.id.recyclerview_reportsfound);
        Found = view.findViewById(R.id.Found);
        reportfoundBtn = view.findViewById(R.id.reportfoundBtn);

        //init  firebaseAuth -- inicializar  firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        //init post list -- inicializar lista de publicaciones
        reportsLostList = new ArrayList<>();
        reportsFoundList = new ArrayList<>();

        Lost.setVisibility(View.VISIBLE);
        reportlostBtn.setVisibility(View.VISIBLE);
        reportlostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddReportsLostActivity.class));
            }
        });

        TapReportsLost.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                Lost.setVisibility(View.VISIBLE);
                reportlostBtn.setVisibility(View.VISIBLE);
                reportlostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), AddReportsLostActivity.class));
                    }
                });

                Found.setVisibility(View.GONE);
                reportfoundBtn.setVisibility(View.GONE);

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
                Lost.setVisibility(View.GONE);
                reportlostBtn.setVisibility(View.GONE);
                Found.setVisibility(View.VISIBLE);
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

        loadReportsLost();

        loadReportsFound();

        return view;
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
        ref.addValueEventListener(new ValueEventListener() {
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
        ref.addValueEventListener(new ValueEventListener() {
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

    private void searchReportsLost(final String searchQuery) {

        //path of all posts -- ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsLost");
        // get all data from this ref
        // obtener todos los datos de esta referencia
        ref.addValueEventListener(new ValueEventListener() {
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

        //path of all posts -- ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsFound");
        // get all data from this ref
        // obtener todos los datos de esta referencia
        ref.addValueEventListener(new ValueEventListener() {
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


    private void checkUserStatus() {
        //get current user -- obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here -- El usuario ha iniciado sesión aquí.
            // set email of logged in user -- configurar el correo electrónico del usuario conectado
            //mProfileTv.setText(user.getEmail());

        } else {
            //user not signed in, go to main activity -- usuario no ha iniciado sesión, vaya a la actividad principal
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//to show menu options in fragment -- para mostrar las opciones del menú en fragmentos
        super.onCreate(savedInstanceState);
    }

    /*inflate options menu -- menú de opciones de inflado*/
    @Override
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
                    searchReportsLost(s);
                    searchReportsFound(s);
                } else {
                    loadReportsLost();
                    loadReportsFound();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s.trim())) {
                    searchReportsLost(s);
                    searchReportsFound(s);
                } else {
                    loadReportsLost();
                    loadReportsFound();
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
