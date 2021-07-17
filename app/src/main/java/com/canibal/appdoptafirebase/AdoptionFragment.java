package com.canibal.appdoptafirebase;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.canibal.appdoptafirebase.adapters.AdapterAdoptions;
import com.canibal.appdoptafirebase.adapters.AdapterPosts;
import com.canibal.appdoptafirebase.models.ModelAdoption;
import com.canibal.appdoptafirebase.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdoptionFragment extends Fragment {
    //firebase auth
    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    List<ModelAdoption> adoptionList;
    AdapterAdoptions adapterAdoptions;
    Button AdoptionBtn;

    public AdoptionFragment() {
        // Required empty public constructor
        // Obligatorio constructor público vacío
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment -- Inflar el diseño de este fragmento
        View view = inflater.inflate(R.layout.fragment_adoption, container, false);

        //init  firebaseAuth -- inicializar  firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        //recyclerview and its properties
        //vista del recyclerview y sus propiedades
        recyclerView = view.findViewById(R.id.recyclerview_adoptions);
        AdoptionBtn = view.findViewById(R.id.AdoptionBtn);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest pot first, for this load from last
        //muestra el bote más nuevo primero, para esta carga desde el último
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recyclerview -- establece el diseño en vista de reciclaje
        recyclerView.setLayoutManager(layoutManager);

        AdoptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddAdoptionActivity.class));

            }
        });

        //init post list -- inicializar lista de publicaciones
        adoptionList = new ArrayList<>();

        loadPosts();

        return view;
    }

    private void loadPosts() {
        //path of all posts -- ruta de todas las publicaciones
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Adoptions");
        // get all data from this ref
        // obtener todos los datos de esta referencia
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adoptionList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelAdoption modelAdoption = ds.getValue(ModelAdoption.class);

                    adoptionList.add(modelAdoption);

                    //adapter
                    adapterAdoptions = new AdapterAdoptions(getActivity(), adoptionList);
                    //set adapter  to recyclerview
                    recyclerView.setAdapter(adapterAdoptions);
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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Adoptions");
        // get all data from this ref
        // obtener todos los datos de esta referencia
        ref.addValueEventListener(new ValueEventListener() {
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
                    recyclerView.setAdapter(adapterAdoptions);
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
                    searchPosts(s);
                } else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s.trim())) {
                    searchPosts(s);
                } else {
                    loadPosts();
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
