package com.canibal.appdoptafirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //views
    EditText nameEt, mEmailEt, phoneEt, mPasswordEt, confirm_passwordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv, txttermsandconditionsTv;
    CheckBox aceptCB;
    CountryCodePicker ccpEt;

    //progressbar to display while registering user
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth -- Declarar una instancia de FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Actionbar and its title
        //Habilitar barra y titulo
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.texttitle_toolbarregister);
        // Eenable back button
        //Boton de retroceso habilitar
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init -- inicializar
        nameEt = findViewById(R.id.nameEt);
        mEmailEt = findViewById(R.id.emailEt);
        ccpEt = findViewById(R.id.ccp);
        phoneEt = findViewById(R.id.phoneEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        confirm_passwordEt = findViewById(R.id.confirm_passwordEt);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);
        txttermsandconditionsTv = findViewById(R.id.txttermsandconditionsTv);
        aceptCB = findViewById(R.id.aceptCB);

        //In the onCreate() method, initialize the FirebaseAuth instance. -- En el método onCreate (), inicialice la instancia de FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        String TextReg_User = getString(R.string.textregistering_user);
        progressDialog.setMessage(TextReg_User);

        txttermsandconditionsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //password change dialog with custom layout having  currentPassword, newPassword and update button
                //inflate layout for dialog
                View view = LayoutInflater.from(RegisterActivity.this).inflate(R.layout.dialog_terms_conditions, null);
                FloatingActionButton dialogcloseBtn = view.findViewById(R.id.dialogcloseBtn);

                final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setView(view); //set view to dialog

                final AlertDialog dialog = builder.create();
                dialog.show();

                dialogcloseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

            }
        });

        //handle register button click -- haga clic en el botón de registro

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //input Email, Password -- entrada de correo electrónico, contraseña
                String name = nameEt.getText().toString().trim();
                String email = mEmailEt.getText().toString().trim();
                String ccp = ccpEt.getSelectedCountryCode();
                String phone = phoneEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();
                String confirm_password = confirm_passwordEt.getText().toString().trim();
                String txterrorName = getString(R.string.validate_name);
                String txterrorEmail = getString(R.string.invalid_email);
                String txterrorPhone = getString(R.string.validate_phone);
                String txterrorLengthPass = getString(R.string.length_password);
                String txterrordifferentPass = getString(R.string.different_passwords);
                String msmaceptCB = getString(R.string.msmaceptCB);

                //validate -- validar
                if (TextUtils.isEmpty(name)) {
                    //validate name -- validar nombre
                    nameEt.setError(txterrorName);
                    nameEt.setFocusable(true);
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focuss to email edittext -- establecer error y enfoques al correo electrónico edittext
                    mEmailEt.setError(txterrorEmail);
                    mEmailEt.setFocusable(true);
                } else if (TextUtils.isEmpty(phone) || phone.length() < 10) {
                    //validate name -- validar nombre
                    phoneEt.setError(txterrorPhone);
                    phoneEt.setFocusable(true);
                } else if (password.length() < 8) {
                    //set error and focuss to password edittext -- establecer error y enfoques a la contraseña edittext
                    mPasswordEt.setError(txterrorLengthPass);
                    mPasswordEt.setFocusable(true);
                } else if (!confirm_password.equals(password)) {
                    //set error and focuss to conf password edittext -- establecer error y enfoques a confirmar contraseña edittext
                    confirm_passwordEt.setError(txterrordifferentPass);
                    confirm_passwordEt.setFocusable(true);
                } else if (!aceptCB.isChecked()) {
                    Toast.makeText(RegisterActivity.this, msmaceptCB, Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(email, password); //register the user -- registra el usuario
                }

            }
        });
        //handle login textview click listener --manejar login textview click oyente
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password) {
        //email and password pattern is valid, show progress dialog and start registering user
        //el patrón de correo electrónico y contraseña es válido, muestra el diálogo de progreso y comienza a registrar usuarios
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start register activity --cerrar el diálogo y comenzar a registrar actividad
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            //get user email and uid from auth -- obtener correo electrónico y usuario de autenticación
                            String email = user.getEmail();
                            String uid = user.getUid();
                            String name = nameEt.getText().toString().trim();
                            String ccp = ccpEt.getSelectedCountryCode();
                            String phone = phoneEt.getText().toString().trim();

                            //currenttime
                            String timeactual = String.valueOf(System.currentTimeMillis());

                            long time = Long.parseLong(timeactual);
                            long aux = 90000000;

                            long value = time - aux;
                            String currenttime = String.valueOf(value);

                            //when user is registered store user info in firebase realtime database too --cuando el usuario está registrado, almacena la información del usuario en la base de datos en tiempo real de Firebase también
                            //using HashMap --usando hashmap
                            HashMap<Object, String> hashMap = new HashMap<>();
                            //Put info in hashmap -- poner información en hashmap
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", name); // will add later  (e.g. edit profile) --agregará más tarde
                            hashMap.put("registertype", "form"); // will add later  (e.g. edit profile) --agregará más tarde
                            hashMap.put("onlineStatus", "online"); // will add later  (e.g. edit profile) --agregará más tarde
                            hashMap.put("typingTo", "noOne"); // will add later  (e.g. edit profile) --agregará más tarde
                            hashMap.put("ccp", ccp); // will add later  (e.g. edit profile) --agregará más tarde
                            hashMap.put("phone", phone);// will add later  (e.g. edit profile) --agregará más tarde
                            hashMap.put("image", "");// will add later  (e.g. edit profile) --agregará más tarde
                            hashMap.put("cover", "");// will add later  (e.g. edit profile) --agregará más tarde
                            hashMap.put("description", "");// will add later  (e.g. edit profile) --agregará más tarde
                            hashMap.put("photoprofileupdatetime", currenttime);// will add later  (e.g. edit profile) --agregará más tarde
                            hashMap.put("nameupdatetime", currenttime);// will add later  (e.g. edit profile) --agregará más tarde

                            // firebase database instance -- instancia de base de datos firebase
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //path to store user data named -- "Users"ruta para almacenar datos de usuario llamados "Users"
                            DatabaseReference reference = database.getReference("Users");
                            //put data within hashmap in database -- poner datos dentro de hashmap en la base de datos
                            reference.child(uid).setValue(hashMap);

                            String hell = getString(R.string.txtregistered);
                            Toast.makeText(RegisterActivity.this, hell + "\n" + user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user. -- Mostrar un mensaje al usuario
                            progressDialog.dismiss();
                            String failedAuth = getString(R.string.txtauthentication_failed);
                            Toast.makeText(RegisterActivity.this, failedAuth, Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error, dismiss progress dialog and get and show the error message -- error, descartar el diálogo de progreso y obtener y mostrar el mensaje de error
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//go previous activity  -- ir a la actividad anterior
        return super.onSupportNavigateUp();
    }
}

