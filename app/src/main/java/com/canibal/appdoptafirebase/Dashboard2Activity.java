package com.canibal.appdoptafirebase;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.canibal.appdoptafirebase.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class Dashboard2Activity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard2);

        //Actionbar and its title
        //Habilitar barra y titulo
        actionBar = getSupportActionBar();
        // habilitar el botón de retroceso en la barra de acción
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init  firebaseAuth -- inicializar  firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        //Profile fratment transaction (default) -- transacción en perfil (por defecto al iniciar)
        //profile fratment transaction -- transacción en perfil
        actionBar.setTitle(R.string.txttiletoolbar_profileuser); // change actionbar title -- cambia el título de la barra de acción
        ProfileFragment fragment1 = new ProfileFragment();
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
            startActivity(new Intent(Dashboard2Activity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        //check on start app --comprobar en la aplicación de inicio
        //set online -- establecer en linea
        checkUserStatus();
        super.onStart();
    }

}



