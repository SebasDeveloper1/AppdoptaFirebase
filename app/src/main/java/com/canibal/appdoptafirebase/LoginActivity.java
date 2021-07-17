package com.canibal.appdoptafirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;

    //views -- vistas
    EditText mEmailEt, mPasswordEt;
    TextView notHaveAnccountTv, mRecoverPassTv;
    Button mLoginBtn;
    SignInButton mGoogleLoginBtn;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    //progress dialog -- diálogo de progreso
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //before mAuth
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //In the onCreate() method, initialize the FirebaseAuth instance. --En el método onCreate (), inicialice la instancia de FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();

        //init --- Inicializar
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        notHaveAnccountTv = findViewById(R.id.nothave_accountTv);
        mRecoverPassTv = findViewById(R.id.recoverpasswordTv);
        mLoginBtn = findViewById(R.id.loginBtn);
        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);

        // login button click --haga clic en el botón de inicio de sesión
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data --datos de entrada
                String email = mEmailEt.getText().toString();
                String passw = mPasswordEt.getText().toString().trim();
                String txterror = getString(R.string.invalid_email);
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //invalid email paatern set error -- error de configuración de paatern de correo electrónico no válido
                    mEmailEt.setError(txterror);
                    mEmailEt.setFocusable(true);

                } else {
                    //valid email pattern -- patrón de correo electrónico válido
                    logiUser(email, passw);
                }

            }
        });
        //not have acoount textview click --  haga click en el textview de no tiene cuenta
        notHaveAnccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //Recover pass textview clik -- Recuperar paso textview clik
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        //handle google login btn click --  manejar el inicio de sesión de google btn clic
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //begin google login progress -- comenzar el progreso de inicio de sesión de google
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });

        //init progress dialog  -- inicializar el dialogo de progreso
        pd = new ProgressDialog(this);
    }

    private void showRecoverPasswordDialog() {
        //AlertDialog -- Dialogo de alerta
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_recoverypassword);

        //set layout linearlayout -- establecer linearlayout
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setBackgroundResource(R.color.editTextColorWhite);
        // views to set in dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint(R.string.textinput_email);
        emailEt.setBackgroundResource(R.drawable.rectangle_bag_gray);
        emailEt.setPadding(15, 15, 15, 15);
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        /*sets the min width of a EditView to fit a text of n 'M' letters regardless of the actual text extension and text size.*/
        /*establece el ancho mínimo de un EditView para que se ajuste a un texto de n letras 'M' independientemente de la extensión de texto real y el tamaño del texto.*/
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        //Buttons recovery
        builder.setPositiveButton(R.string.textBtn_recovery_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input email
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);

            }
        });

        //Buttons Cancel
        builder.setNegativeButton(R.string.textBtn_cancel_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog --cerrar diálogo
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    private void beginRecovery(String email) {
        mAuth.setLanguageCode("es");
        String sendEmail = getString(R.string.txtsending_email);
        // show progress dialog  -- mostrar el dialogo de progreso
        pd.setMessage(sendEmail);
        pd.show();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.txtrecovery_msm, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.txtrecovery_msmerror, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                //get and show proper error message --- obtener y mostrar el mensaje de error adecuado
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logiUser(String email, String passw) {
        // show progress dialog  -- mostrar el dialogo de progreso
        String textlogginin = getString(R.string.textloggin_in);
        pd.setMessage(textlogginin);
        pd.show();
        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // dismiss progress dialog -- descartar el diálogo de progreso
                            pd.dismiss();

                            // Sign in success, update UI with the signed-in user's information
                            //Inicie sesión correctamente, actualice la IU con la información del usuario que inició sesión
                            FirebaseUser user = mAuth.getCurrentUser();
                            //user is logged in, so start LoginActivity -- usuario ha iniciado sesión, así que inicie LoginActivity
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();

                        } else {
                            // dismiss progress dialog -- descartar el diálogo de progreso
                            pd.dismiss();

                            // If sign in fails, display a message to the user -- Si el inicio de sesión falla, muestre un mensaje al usuario
                            Toast.makeText(LoginActivity.this, R.string.txtauthentication_failed,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // dismiss progress dialog -- descartar el diálogo de progreso
                pd.dismiss();

                //error,  get and show error message -- error, obtener y mostrar mensaje de error
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//go previous activity  -- ir a la actividad anterior
        return super.onSupportNavigateUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        // Resultado devuelto al iniciar la intención desde GoogleSignInApi.getSignInIntent (...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                // El inicio de sesión de Google fue exitoso, autentíquese con Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // Inicio de sesión de Google falló, actualice la interfaz de usuario adecuadamente
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            // Inicie sesión correctamente, actualice la interfaz de usuario con la información del usuario que inició sesión

                            FirebaseUser user = mAuth.getCurrentUser();

                            //if user is signing if first time then get and show user info from google account
                            // si el usuario firma si es la primera vez, obtenga y muestre la información del usuario de la cuenta de Google
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {

                                //get user email and uid from auth -- obtener correo electrónico y usuario de autenticación
                                String email = user.getEmail();
                                String uid = user.getUid();
                                String name = user.getDisplayName();

                                //currenttime
                                String timeactual = String.valueOf(System.currentTimeMillis());

                                long time = Long.parseLong(timeactual);
                                long aux = 90000000;

                                long value = time - aux;
                                String currenttime = String.valueOf(value);

                                //when user is registered store user info in firebase realtime database too -- cuando el usuario está registrado, almacena la información del usuario en la base de datos en tiempo real de Firebase también
                                //using HashMap --usando hashmap
                                HashMap<Object, String> hashMap = new HashMap<>();
                                //Put info in hashmap -- poner información en hashmap
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", name); // will add later  (e.g. edit profile) --agregará más tarde
                                hashMap.put("registertype", "google"); // will add later  (e.g. edit profile) --agregará más tarde
                                hashMap.put("onlineStatus", "online"); // will add later  (e.g. edit profile) --agregará más tarde
                                hashMap.put("typingTo", "noOne"); // will add later  (e.g. edit profile) --agregará más tarde
                                hashMap.put("ccp", ""); // will add later  (e.g. edit profile) --agregará más tarde
                                hashMap.put("phone", "");// will add later  (e.g. edit profile) --agregará más tarde
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

                            }
                            //show user email in toast -- mostrar el correo electrónico del usuario en un Toast
                            Toast.makeText(LoginActivity.this, "" + user.getEmail(), Toast.LENGTH_SHORT).show();
                            //go to profile activity sfter logged in -- ir a la actividad de perfil después de iniciar sesión
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get and show error message --obtener y mostrar mensaje de error
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

