package com.canibal.appdoptafirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.TimerTask;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class EditUserDataActivity extends AppCompatActivity {

    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //storage
    StorageReference storageReference;

    //path where images of user profileand cover will be stored -- ruta donde se almacenarán las imágenes del perfil del usuario y la portada
    String storagePath = "Users_Profile_Cover_Imgs/";

    ActionBar actionBar;
    ImageView avatarIv, coverIv;
    FloatingActionButton EditPhotoProfileTV, EditCoverProfileTV, EditNameUserTV, EditDescriptionUserTV, EditPhoneUserTV, EditPasswordUserTV;
    TextView nameTv, txtnameuser, descriptionTv, phoneTv;
    LinearLayout LayoutPasswordUser;

    //progress dialog ---  dialogo de progreso
    ProgressDialog pd;

    //permissions constants -- permisos constantes
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    //Arrays of permissions to be requested -- Matrices de permisos a solicitar
    String cameraPermissions[];
    String storagePermissions[];

    String uid;

    //uri if picked image -- uri si se selecciona la imagen
    Uri image_uri;

    //for checking profile or cover photo -- para verificar el perfil o la foto de portada
    String profileOrCoverPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_data);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //firebase storage reference -- referencia de almacenamiento

        //init arrays of the permissions --  inicializar matrices de permisos
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.txtuploadprofile);
        //enable back button in actionbar
        // habilitar el botón de retroceso en la barra de acción
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setElevation(0);

        //init views -- inicializar las vistas
        avatarIv = findViewById(R.id.avatarIv);
        coverIv = findViewById(R.id.coverIv);
        EditPhotoProfileTV = findViewById(R.id.EditPhotoProfileTV);
        EditCoverProfileTV = findViewById(R.id.EditCoverProfileTV);
        EditNameUserTV = findViewById(R.id.EditNameUserTV);
        EditDescriptionUserTV = findViewById(R.id.EditDescriptionUserTV);
        EditPhoneUserTV = findViewById(R.id.EditPhoneUserTV);
        EditPasswordUserTV = findViewById(R.id.EditPasswordUserTV);
        nameTv = findViewById(R.id.nameTv);
        txtnameuser = findViewById(R.id.txtnameuser);
        descriptionTv = findViewById(R.id.descriptionTv);
        phoneTv = findViewById(R.id.phoneTv);
        LayoutPasswordUser = findViewById(R.id.LayoutPasswordUser);

        //init progress dialog  --  inicializar dialogo de progreso
        pd = new ProgressDialog(this);

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
                    String ccp = "" + ds.child("ccp").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String description = "" + ds.child("description").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();
                    String nameupdatetime = "" + ds.child("nameupdatetime").getValue();
                    String photoprofileupdatetime = "" + ds.child("photoprofileupdatetime").getValue();
                    String registertype = "" + ds.child("registertype").getValue();

                    //set data -- establecer datos

                    nameTv.setText(name);
                    txtnameuser.setText(name);
                    descriptionTv.setText(description);
                    phoneTv.setText("+" + ccp + "\t" + phone);

                    if (registertype.equals("form")) {
                        LayoutPasswordUser.setVisibility(View.VISIBLE);
                    } else if (registertype.equals("google")) {
                        LayoutPasswordUser.setVisibility(View.GONE);
                    }

                    //limitacion de tiempo para la edicion de nombre y foto de perfil

                    //nombre de usuario
                    long time = Long.parseLong(nameupdatetime);
                    long aux = 86400000;
                    long timeactual = System.currentTimeMillis();

                    if (timeactual > (time + aux)) {
                        EditNameUserTV.setEnabled(true);
                    } else {
                        EditNameUserTV.setEnabled(false);
                    }

                    //imagen de perfil de usuario
                    long timephoto = Long.parseLong(photoprofileupdatetime);
                    long auxphoto = 86400000;

                    if (timeactual > (timephoto + auxphoto)) {
                        EditPhotoProfileTV.setEnabled(true);
                    } else {
                        EditPhotoProfileTV.setEnabled(false);
                    }

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

        EditPhotoProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //is clicked
                // muestra el cuadro de diálogo
                AlertDialog.Builder builder = new AlertDialog.Builder(EditUserDataActivity.this);
                builder.setTitle(R.string.msmtitleupdatephotoprofile);
                builder.setMessage(R.string.msmdescrupdatephotoprofile);
                builder.setPositiveButton(R.string.txtacept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // edit profile clicked
                        String Updatingprofilepicture = getString(R.string.Updatingprofilepicture);
                        pd.setMessage(Updatingprofilepicture);
                        profileOrCoverPhoto = "image"; // i.e. changing profile picture, make sure to assign same value -- es decir, cambiar la imagen de perfil, asegúrese de asignar el mismo valor
                        showImagePicDialog();

                    }
                });
                builder.setNegativeButton(R.string.textBtn_cancel_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });

                //show dialog
                builder.create().show();
            }
        });

        EditCoverProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit cover clicked
                String Updatingcoverphoto = getString(R.string.Updatingcoverphoto);
                pd.setMessage(Updatingcoverphoto);
                profileOrCoverPhoto = "cover"; // i.e. changing cover photo, make sure to assign same value -- es decir, cambiar la foto de portada, asegúrese de asignar el mismo valor
                showImagePicDialog();
            }
        });

        EditNameUserTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //delete is clicked
                // muestra el cuadro de diálogo eliminar
                AlertDialog.Builder builder = new AlertDialog.Builder(EditUserDataActivity.this);
                builder.setTitle(R.string.msmtitleupdatenameuser);
                builder.setMessage(R.string.msmdescrupdatenameuser);
                builder.setPositiveButton(R.string.txtacept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // edit profile clicked
                        showChangeNameUserDialog();
                    }
                });
                builder.setNegativeButton(R.string.textBtn_cancel_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });

                //show dialog
                builder.create().show();

            }
        });

        EditDescriptionUserTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeDescriptionUserDialog();
            }
        });

        EditPhoneUserTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePhoneUserDialog();
            }
        });

        EditPasswordUserTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

        checkUserStatus();
    }

    private boolean checkStoragePermission() {
        //check if storage permissions is enabled or not -- verifica si los permisos de almacenamiento están habilitados o no
        //return true if enable -- devuelve verdadero si habilita
        //return false if not enabled -- devuelve falso si no está habilitado

        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission -- solicitar permiso de almacenamiento en tiempo de ejecución
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //check if storage permissions is enabled or not -- verifica si los permisos de almacenamiento están habilitados o no
        //return true if enable -- devuelve verdadero si habilita
        //return false if not enabled -- devuelve falso si no está habilitado
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        //request runtime storage permission -- solicitar permiso de almacenamiento en tiempo de ejecución
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showImagePicDialog() {
        //show dialog containig options camera and gallery to pick the image -- Mostrar cuadro de diálogo que contiene opciones de cámara y galería para elegir la imagen

        String pickimagen = getString(R.string.pickimagen);
        String camerapermission = getString(R.string.camerapermission);
        String gallerypermission = getString(R.string.gallerypermission);
        String options[] = {camerapermission, gallerypermission};
        // alert dialog -- diálogo de alerta
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set title -- establecer título
        builder.setTitle(pickimagen);
        //set items to dialog -- establecer elementos para dialogar
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clics -- manejar clics de elementos de diálogo
                if (which == 0) {
                    // camera clicked

                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                } else if (which == 1) {
                    //gallery clicked

                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }

                }
            }
        });
        //create and show dialog -- crear y mostrar diálogo
        builder.create().show();

        /* for picking image from:
         * camera [ camera and storage permission required]
         * gallery [storage permission required]*/

        /* para elegir una imagen de:
         * cámara [se requiere permiso de cámara y almacenamiento]
         * galería [se requiere permiso de almacenamiento] */
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*this metod called when user press allow or Deny from permission request dialog
         * here we will handle permission cases (allowed & denied) */

        /*Este método se llama cuando el usuario presiona permitir o denegar desde el diálogo de solicitud de permiso
         * aquí manejaremos casos de permisos (permitidos y denegados)*/

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {

                //picking from camera, frist check if camera and storage permissions allowed or not  -- seleccionando desde la cámara, primero verifique si los permisos de cámara y almacenamiento están permitidos o no
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //permissions enables -- permisos habilitados
                        pickFromCamera();
                    } else {
                        //permissions denied --  permisos denegados
                        String textpermissionsimagen = getString(R.string.textpermissionsimagen);
                        Toast.makeText(this, textpermissionsimagen, Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE: {

                //picking from gallery, frist check if storage permissions allowed or not  -- seleccionando desde la galeria, primero verifique si los permisos están permitidos o no
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //permissions enables -- permisos habilitados
                        pickFromGallery();
                    } else {
                        //permissions denied --  permisos denegados
                        String textpermissionstorage = getString(R.string.textpermissionstorage);
                        Toast.makeText(this, textpermissionstorage, Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        /*This method will be called after picking image from Camera or Gallery*/
        /* Se llamará a este método después de seleccionar la imagen de la Cámara o Galería */
        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get ui of image
                //la imagen se selecciona de la galería, obtenga la interfaz de usuario de la imagen
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image is picked from camera, get ui of image
                //la imagen se selecciona de la camara, obtenga la interfaz de usuario de la imagen

                uploadProfileCoverPhoto(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        //show progress  -- mostrar progreso
        pd.show();

        /*Instead of creating separate function for profile picture and cover photo
         * i'm doing work for both in same function
         *
         * to add check ill add a string variable and assing it value "image" when user clicks
         * "Edit profile pic", and assing it value "cover" when user clicks "Edit cover photo"
         * Here: image is the key in each user containig url of user's profile picture
         *       cover is athe key in each user containig url or user's cover photo */

        /* En lugar de crear una función separada para la foto de perfil y la foto de portada
         * Estoy trabajando para ambos en la misma función
         *
         * para agregar cheque, agregaré una variable de cadena y le asignaremos el valor "imagen" cuando el usuario haga clic
         * "Editar foto de perfil" y asignarle el valor "portada" cuando el usuario hace clic en "Editar foto de portada"
         * Aquí: la imagen es la clave en cada usuario que contiene la URL de la imagen de perfil del usuario
         * la portada es la clave en cada usuario que contiene una url o foto de portada del usuario */

        /*the parameter "image_uri" contains the uri of image picked either from camera or gallery
         * we will use UID of the currently signed in user as name of the images so there will be only one image
         * profile and one image for cover for each user*/

        /* usaremos el UID del usuario actualmente registrado como nombre de las imágenes, por lo que solo habrá una imagen
         * perfil y una imagen de portada para cada usuario */

        //path and name of image to be stored in firebase storage -- ruta y nombre de la imagen que se almacenará en el almacenamiento de Firebase
        //e.g. Users_profile_cover_imgs/image_e12f3456f789.jpg
        //e.g. Users_profile_cover_imgs/cover_c123n4567g89.jpg
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage, new get it's url and store in user's database
                        // la imagen se carga en el almacenamiento, se obtiene su URL y se almacena en la base de datos del usuario
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        final Uri downloadUri = uriTask.getResult();

                        //check if image is uploaded or not and url is received -- //compruebe si la imagen está cargada o no y se recibe la URL
                        if (uriTask.isSuccessful()) {
                            //image uploaded -- imagen cargada
                            //add/update url in user's database -- agregar / actualizar url en la base de datos del usuario
                            HashMap<String, Object> results = new HashMap<>();
                            /*First parameter is  profileOrCoverPhoto that has value "image" or "cover"
                                which are keys in user's database where urlof image will be saved in one
                                of them
                                Second parameter contains the url of the image stored in firebase storade, this
                                url will be saved as value against key "image" or "cover*/
                            results.put(profileOrCoverPhoto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //url in database of user is added successfully -- url en la base de datos del usuario se agrega correctamente
                                            //dismiss progress bar --descartar la barra de progreso
                                            pd.dismiss();

                                            String imageupdate = getString(R.string.imageupdate);
                                            Toast.makeText(EditUserDataActivity.this, imageupdate, Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error adding url in database or user -- error al agregar la URL en la base de datos de usuario
                                            //dismiss progress bar --descartar la barra de progreso
                                            pd.dismiss();
                                            String imageupdateerror = getString(R.string.imageupdateerror);
                                            Toast.makeText(EditUserDataActivity.this, imageupdateerror, Toast.LENGTH_SHORT).show();

                                        }
                                    });

                            if (profileOrCoverPhoto.equals("image")) {
                                String photoprofileupdatetime = String.valueOf(System.currentTimeMillis());
                                HashMap<String, Object> results2 = new HashMap<>();
                                results2.put("photoprofileupdatetime", photoprofileupdatetime);

                                databaseReference.child(user.getUid()).updateChildren(results2)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //updated, dismiss progress -- actualizada despedir progreso
                                                EditPhotoProfileTV.setEnabled(false);

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed, dismiss progress, get and show error message -- falló, descartar progreso, obtener y mostrar mensaje de error
                                            }
                                        });
                            }

                            //if user edit his name, also change it from hist posts
                            // si el usuario edita su nombre, también cámbielo de las publicaciones históricas
                            if (profileOrCoverPhoto.equals("image")) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //update user image in curent users comments on posts
                                // actualiza la imagen del usuario en los comentarios de los usuarios actuales sobre publicaciones
                                //update name in current users comments on posts
                                // actualizar nombre en comentarios de usuarios actuales en publicaciones
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            if (dataSnapshot.child(child).hasChild("Comments")) {
                                                String child1 = "" + dataSnapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            if (profileOrCoverPhoto.equals("image")) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Adoptions");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //update user image in curent users comments on posts
                                // actualiza la imagen del usuario en los comentarios de los usuarios actuales sobre publicaciones
                                //update name in current users comments on posts
                                // actualizar nombre en comentarios de usuarios actuales en publicaciones
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            if (dataSnapshot.child(child).hasChild("Comments")) {
                                                String child1 = "" + dataSnapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Adoptions").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            if (profileOrCoverPhoto.equals("image")) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsFound");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //update user image in curent users comments on posts
                                // actualiza la imagen del usuario en los comentarios de los usuarios actuales sobre publicaciones
                                //update name in current users comments on posts
                                // actualizar nombre en comentarios de usuarios actuales en publicaciones
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            if (dataSnapshot.child(child).hasChild("Comments")) {
                                                String child1 = "" + dataSnapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("ReportsFound").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            if (profileOrCoverPhoto.equals("image")) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsLost");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //update user image in curent users comments on posts
                                // actualiza la imagen del usuario en los comentarios de los usuarios actuales sobre publicaciones
                                //update name in current users comments on posts
                                // actualizar nombre en comentarios de usuarios actuales en publicaciones
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            if (dataSnapshot.child(child).hasChild("Comments")) {
                                                String child1 = "" + dataSnapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("ReportsLost").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            if (profileOrCoverPhoto.equals("image")) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Services");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //update user image in curent users comments on posts
                                // actualiza la imagen del usuario en los comentarios de los usuarios actuales sobre publicaciones
                                //update name in current users comments on posts
                                // actualizar nombre en comentarios de usuarios actuales en publicaciones
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            if (dataSnapshot.child(child).hasChild("Comments")) {
                                                String child1 = "" + dataSnapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Services").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                        } else {
                            // error
                            pd.dismiss();
                            String imagesomeerror = getString(R.string.imagesomeerror);
                            Toast.makeText(EditUserDataActivity.this, imagesomeerror, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were some error(s),get and show error message, dimiss progress dialog
                        //hubo algunos errores, obtener y mostrar mensaje de error, diálogo de progreso de dimiss
                        pd.dismiss();
                        Toast.makeText(EditUserDataActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void pickFromCamera() {
        // intent of picking image from device camera -- intención de elegir la imagen de la cámara del dispositivo
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri -- poner imagen uri
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // intent to start -- intención de iniciar la cámara
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);

    }

    private void pickFromGallery() {
        //pick from gallery -- elegir de la galeria
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);

    }

    private void showChangeNameUserDialog() {
        //password change dialog with custom layout having  currentPassword, newPassword and update button
        //inflate layout for dialog
        View view = LayoutInflater.from(EditUserDataActivity.this).inflate(R.layout.dialog_update_username, null);
        final EditText updateNameEt = view.findViewById(R.id.updateNameEt);
        final Button updateNameBtn = view.findViewById(R.id.updateNameBtn);
        FloatingActionButton dialogcloseBtn = view.findViewById(R.id.dialogcloseBtn);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view); //set view to dialog

        final AlertDialog dialog = builder.create();
        dialog.show();

        updateNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate data
                String NewName = updateNameEt.getText().toString().trim();
                if (TextUtils.isEmpty(NewName)) {
                    String updatemsmnameuser = getString(R.string.updatemsmnameuser);
                    updateNameEt.setError(updatemsmnameuser);
                    updateNameEt.setFocusable(true);
                    return;
                }

                dialog.dismiss();
                //edit name clicked
                String UpdatingName = getString(R.string.UpdatingName);
                pd.setMessage(UpdatingName);
                //calling method and pass key "name" as parameter to update it's value in database
                //método de llamada y clave de paso "nombre" como parámetro para actualizar su valor en la base de datos
                showNamePhoneUpdateDialog("name", NewName, "", "");
            }
        });

        dialogcloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showChangeDescriptionUserDialog() {
        //password change dialog with custom layout having  currentPassword, newPassword and update button
        //inflate layout for dialog
        View view = LayoutInflater.from(EditUserDataActivity.this).inflate(R.layout.dialog_update_userdescription, null);
        final EditText updateDescriptionEt = view.findViewById(R.id.updateDescriptionEt);
        final Button updateDescriptionBtn = view.findViewById(R.id.updateDescriptionBtn);
        FloatingActionButton dialogcloseBtn = view.findViewById(R.id.dialogcloseBtn);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view); //set view to dialog

        final AlertDialog dialog = builder.create();
        dialog.show();

        updateDescriptionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate data
                String NewDescription = updateDescriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(NewDescription)) {
                    String updatemsmdescriptionuser = getString(R.string.updatemsmdescriptionuser);
                    updateDescriptionEt.setError(updatemsmdescriptionuser);
                    updateDescriptionEt.setFocusable(true);
                    return;
                }

                dialog.dismiss();
                //edit name clicked
                String UpdatingDescription = getString(R.string.UpdatingDescription);
                pd.setMessage(UpdatingDescription);
                //calling method and pass key "name" as parameter to update it's value in database
                //método de llamada y clave de paso "nombre" como parámetro para actualizar su valor en la base de datos
                showNamePhoneUpdateDialog("description", NewDescription, "", "");
            }
        });

        dialogcloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showChangePhoneUserDialog() {
        //password change dialog with custom layout having  currentPassword, newPassword and update button
        //inflate layout for dialog
        View view = LayoutInflater.from(EditUserDataActivity.this).inflate(R.layout.dialog_update_userphone, null);
        final CountryCodePicker updateccpUser = view.findViewById(R.id.updateccpUser);
        final EditText updatePhoneEt = view.findViewById(R.id.updatePhoneEt);
        final Button updatePhoneBtn = view.findViewById(R.id.updatePhoneBtn);
        FloatingActionButton dialogcloseBtn = view.findViewById(R.id.dialogcloseBtn);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view); //set view to dialog

        final AlertDialog dialog = builder.create();
        dialog.show();

        updatePhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate data
                String NewPhone = updatePhoneEt.getText().toString().trim();
                String NewCcp = updateccpUser.getSelectedCountryCode();
                if (TextUtils.isEmpty(NewPhone) || NewPhone.length() < 10) {
                    String updatemsmphoneuser = getString(R.string.updatemsmphoneuser);
                    updatePhoneEt.setError(updatemsmphoneuser);
                    updatePhoneEt.setFocusable(true);
                    return;
                }

                dialog.dismiss();
                //edit name clicked
                String UpdatingPhone = getString(R.string.UpdatingPhone);
                pd.setMessage(UpdatingPhone);
                //calling method and pass key "name" as parameter to update it's value in database
                //método de llamada y clave de paso "nombre" como parámetro para actualizar su valor en la base de datos
                showNamePhoneUpdateDialog("phone", NewPhone, "ccp", NewCcp);
            }
        });

        dialogcloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showChangePasswordDialog() {
        //password change dialog with custom layout having  currentPassword, newPassword and update button

        //inflate layout for dialog
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_password, null);
        final EditText passwordEt = view.findViewById(R.id.passwordEt);
        final EditText newPasswordEt = view.findViewById(R.id.newPasswordEt);
        final EditText confnewPasswordEt = view.findViewById(R.id.confnewPasswordEt);
        final Button updatePasswordBtn = view.findViewById(R.id.updatePasswordBtn);
        FloatingActionButton dialogcloseBtn = view.findViewById(R.id.dialogcloseBtn);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view); //set view to dialog

        final AlertDialog dialog = builder.create();
        dialog.show();

        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate data
                String oldPassword = passwordEt.getText().toString().trim();
                String newPassword = newPasswordEt.getText().toString().trim();
                String confnewPassword = confnewPasswordEt.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword)) {
                    String txtentercurrentpass = getString(R.string.txtentercurrentpass);
                    passwordEt.setError(txtentercurrentpass);
                    passwordEt.setFocusable(true);
                    return;
                }
                if (newPassword.length() < 8) {
                    String length_password = getString(R.string.length_password);
                    newPasswordEt.setError(length_password);
                    newPasswordEt.setFocusable(true);
                    return;
                }
                if (!confnewPassword.equals(newPassword)) {
                    //set error and focuss to conf password edittext -- establecer error y enfoques a confirmar contraseña edittext
                    String txterrordifferentPass = getString(R.string.different_passwords);
                    confnewPasswordEt.setError(txterrordifferentPass);
                    confnewPasswordEt.setFocusable(true);
                    return;
                }

                dialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });

        dialogcloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void updatePassword(String oldPassword, final String newPassword) {
        pd.show();

        //get current user
        final FirebaseUser user = firebaseAuth.getCurrentUser();

        //before changing password re-authenticate th user
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //successfully authenticated, begin update

                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //password update
                                        pd.dismiss();
                                        String txtPasswordUpdated = getString(R.string.txtPasswordUpdated);
                                        Toast.makeText(EditUserDataActivity.this, txtPasswordUpdated, Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed updating password, show reason
                                        pd.dismiss();
                                        Toast.makeText(EditUserDataActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //authentication failed, show reason
                        pd.dismiss();
                        Toast.makeText(EditUserDataActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showNamePhoneUpdateDialog(final String key, final String value, final String ccpkey, final String ccpvalue) {
        /*parameter "key" will contain value:
            either "name" which is key in user's database which is used to update user's name
            or "phone" which is key in user's database which is used to update user's phone*/

        /* parámetro "clave" contendrá el valor:
            ya sea "nombre", que es clave en la base de datos del usuario que se utiliza para actualizar el nombre del usuario
            o "teléfono" que es clave en la base de datos del usuario que se utiliza para actualizar el teléfono del usuario */
        pd.show();
        HashMap<String, Object> result = new HashMap<>();
        result.put(key, value);

        databaseReference.child(user.getUid()).updateChildren(result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //updated, dismiss progress -- actualizada despedir progreso
                        pd.dismiss();
                        Toast.makeText(EditUserDataActivity.this, R.string.Updatingtxt, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed, dismiss progress, get and show error message -- falló, descartar progreso, obtener y mostrar mensaje de error
                        pd.dismiss();
                        Toast.makeText(EditUserDataActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        if (ccpkey.equals("ccp")) {
            pd.show();
            HashMap<String, Object> result2 = new HashMap<>();
            result2.put(ccpkey, ccpvalue);

            databaseReference.child(user.getUid()).updateChildren(result2)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //updated, dismiss progress -- actualizada despedir progreso
                            pd.dismiss();
                            Toast.makeText(EditUserDataActivity.this, R.string.Updatingtxt, Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed, dismiss progress, get and show error message -- falló, descartar progreso, obtener y mostrar mensaje de error
                            pd.dismiss();
                            Toast.makeText(EditUserDataActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        if (key.equals("name")) {
            String nameupdatetime = String.valueOf(System.currentTimeMillis());
            HashMap<String, Object> result3 = new HashMap<>();
            result3.put("nameupdatetime", nameupdatetime);

            databaseReference.child(user.getUid()).updateChildren(result3)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //updated, dismiss progress -- actualizada despedir progreso
                            EditNameUserTV.setEnabled(false);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed, dismiss progress, get and show error message -- falló, descartar progreso, obtener y mostrar mensaje de error
                        }
                    });
        }

        //if user edit his name, also change it from hist posts
        // si el usuario edita su nombre, también cámbielo de las publicaciones históricas
        if (key.equals("name")) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            Query query = ref.orderByChild("uid").equalTo(uid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String child = ds.getKey();
                        dataSnapshot.getRef().child(child).child("uName").setValue(value);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //update name in current users comments on posts
            // actualizar nombre en comentarios de usuarios actuales en publicaciones
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String child = ds.getKey();
                        if (dataSnapshot.child(child).hasChild("Comments")) {
                            String child1 = "" + dataSnapshot.child(child).getKey();
                            Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                            child2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String child = ds.getKey();
                                        dataSnapshot.getRef().child(child).child("uName").setValue(value);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        if (key.equals("name")) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Adoptions");
            Query query = ref.orderByChild("uid").equalTo(uid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String child = ds.getKey();
                        dataSnapshot.getRef().child(child).child("uName").setValue(value);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //update name in current users comments on posts
            // actualizar nombre en comentarios de usuarios actuales en publicaciones
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String child = ds.getKey();
                        if (dataSnapshot.child(child).hasChild("Comments")) {
                            String child1 = "" + dataSnapshot.child(child).getKey();
                            Query child2 = FirebaseDatabase.getInstance().getReference("Adoptions").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                            child2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String child = ds.getKey();
                                        dataSnapshot.getRef().child(child).child("uName").setValue(value);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        if (key.equals("name")) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsFound");
            Query query = ref.orderByChild("uid").equalTo(uid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String child = ds.getKey();
                        dataSnapshot.getRef().child(child).child("uName").setValue(value);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //update name in current users comments on posts
            // actualizar nombre en comentarios de usuarios actuales en publicaciones
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String child = ds.getKey();
                        if (dataSnapshot.child(child).hasChild("Comments")) {
                            String child1 = "" + dataSnapshot.child(child).getKey();
                            Query child2 = FirebaseDatabase.getInstance().getReference("ReportsFound").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                            child2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String child = ds.getKey();
                                        dataSnapshot.getRef().child(child).child("uName").setValue(value);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        if (key.equals("name")) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReportsLost");
            Query query = ref.orderByChild("uid").equalTo(uid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String child = ds.getKey();
                        dataSnapshot.getRef().child(child).child("uName").setValue(value);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //update name in current users comments on posts
            // actualizar nombre en comentarios de usuarios actuales en publicaciones
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String child = ds.getKey();
                        if (dataSnapshot.child(child).hasChild("Comments")) {
                            String child1 = "" + dataSnapshot.child(child).getKey();
                            Query child2 = FirebaseDatabase.getInstance().getReference("ReportsLost").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                            child2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String child = ds.getKey();
                                        dataSnapshot.getRef().child(child).child("uName").setValue(value);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        if (key.equals("name")) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Services");
            Query query = ref.orderByChild("uid").equalTo(uid);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String child = ds.getKey();
                        dataSnapshot.getRef().child(child).child("uName").setValue(value);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //update name in current users comments on posts
            // actualizar nombre en comentarios de usuarios actuales en publicaciones
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String child = ds.getKey();
                        if (dataSnapshot.child(child).hasChild("Comments")) {
                            String child1 = "" + dataSnapshot.child(child).getKey();
                            Query child2 = FirebaseDatabase.getInstance().getReference("Services").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                            child2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String child = ds.getKey();
                                        dataSnapshot.getRef().child(child).child("uName").setValue(value);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void checkUserStatus() {
        //get current user -- obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here -- El usuario ha iniciado sesión aquí.
            // set email of logged in user -- configurar el correo electrónico del usuario conectado
            //mProfileTv.setText(user.getEmail());
            uid = user.getUid();

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previus activity-- ir a la actividad anterior
        return super.onSupportNavigateUp();
    }
}