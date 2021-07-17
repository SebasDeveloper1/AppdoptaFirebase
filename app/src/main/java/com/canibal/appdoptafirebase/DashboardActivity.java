package com.canibal.appdoptafirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.canibal.appdoptafirebase.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Actionbar and its title
        //Habilitar barra y titulo
        actionBar = getSupportActionBar();

        //init  firebaseAuth -- inicializar  firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        //botton navigation -- navegación inferior
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //Home fratment transaction (default) -- transacción en casa (por defecto al iniciar)
        actionBar.setTitle(R.string.txttiletoolbar_animalcommunity); // change actionbar title -- cambia el título de la barra de acción
        actionBar.setElevation(0);
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();

        checkUserStatus();

    }

    @Override
    protected void onResume() {
        checkUserStatus();
        //set online -- establecer en linea
        super.onResume();
    }


    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //handle item clicks -- manejar clics de elementos
                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            //Home fratment transaction -- transacción en casa
                            actionBar.setTitle(R.string.txttiletoolbar_animalcommunity); // change actionbar title -- cambia el título de la barra de acción
                            actionBar.setElevation(0);
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1, "");
                            ft1.commit();

                            return true;

                        case R.id.nav_Adoption:
                            //users fratment transaction -- transacción en users

                            actionBar.setTitle(R.string.txttiletoolbar_Adoption); // change actionbar title -- cambia el título de la barra de acción
                            actionBar.setElevation(0);
                            AdoptionFragment fragment2 = new AdoptionFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2, "");
                            ft2.commit();
                            return true;

                        case R.id.nav_Report:
                            //users fratment transaction -- transacción en users

                            actionBar.setTitle(R.string.txttiletoolbar_Reports); // change actionbar title -- cambia el título de la barra de acción
                            actionBar.setElevation(0);
                            ReportFragment fragment3 = new ReportFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3, "");
                            ft3.commit();
                            return true;

                        case R.id.nav_Services:
                            //users fratment transaction -- transacción en users

                            actionBar.setTitle(R.string.txttiletoolbar_Services); // change actionbar title -- cambia el título de la barra de acción
                            ServicesFragment fragment4 = new ServicesFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content, fragment4, "");
                            ft4.commit();
                            return true;

                        case R.id.nav_more:
                            //users fratment transaction -- transacción en users

                            actionBar.setTitle(R.string.txttiletoolbar_Menu); // change actionbar title -- cambia el título de la barra de acción
                            actionBar.setElevation(0);
                            MenuFragment fragment5 = new MenuFragment();
                            FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                            ft5.replace(R.id.content, fragment5, "");
                            ft5.commit();
                            return true;
                    }

                    return false;
                }
            };

    private void checkUserStatus() {
        //get current user -- obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here -- El usuario ha iniciado sesión aquí.
            // set email of logged in user -- configurar el correo electrónico del usuario conectado
            //mProfileTv.setText(user.getEmail());
            mUID = user.getUid();

            //save uid of currently signed in user in shared preferences
            // guardar el uid del usuario actualmente conectado en preferencias compartidas
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            //update token -- token de actualización
            updateToken(FirebaseInstanceId.getInstance().getToken());

        } else {
            //user not signed in, go to main activity -- usuario no ha iniciado sesión, vaya a la actividad principal
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //check on start app --comprobar en la aplicación de inicio
        checkUserStatus();
        super.onStart();
    }

}



