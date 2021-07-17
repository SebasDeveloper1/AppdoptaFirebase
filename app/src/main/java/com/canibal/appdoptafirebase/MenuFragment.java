package com.canibal.appdoptafirebase;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MenuFragment extends Fragment {
    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    // views from xml -- vistas de xml
    ImageView avatarIv;
    TextView nameTv;
    CardView notificationcontent, profilecontent, confcontent, helpcontent;

    String uid;

    public MenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment -- Inflar el diseño de este fragmento
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        //init views -- inicializar las vistas
        avatarIv = view.findViewById(R.id.avatarIv);
        nameTv = view.findViewById(R.id.nameTv);
        notificationcontent = view.findViewById(R.id.notificationcontent);
        profilecontent = view.findViewById(R.id.profilecontent);
        confcontent = view.findViewById(R.id.confcontent);
        helpcontent = view.findViewById(R.id.helpcontent);


        /*Al usar la consulta orderByChild mostraremos los detalles de un nodo
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
                    String image = "" + ds.child("image").getValue();


                    //set data -- establecer datos
                    nameTv.setText(name);
                    try {
                        //if image is received then set -- si se recibe la imagen, establezca
                        Picasso.get().load(image).into(avatarIv);

                    } catch (Exception e) {
                        //if there is any exeption while getting image then set default -- si hay alguna excepción al obtener la imagen, configure el valor predeterminado

                        Picasso.get().load(R.drawable.ic_default_img_white).into(avatarIv);

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profilecontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Dashboard2Activity.class));
            }
        });

        notificationcontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Dashboard4Activity.class));

            }
        });

        confcontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));

            }
        });

        helpcontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), HelpActivity.class));
            }
        });

        return view;
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
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
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
        } else if (id == R.id.action_chat) {
            startActivity(new Intent(getActivity(), Dashboard3Activity.class));
        }

        return super.onOptionsItemSelected(item);

    }
}