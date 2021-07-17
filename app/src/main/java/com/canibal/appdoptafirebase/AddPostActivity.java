package com.canibal.appdoptafirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;


    ActionBar actionBar;

    //permissions constants
    // permisos constantes
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //permisions array
    //conjunto de permisos
    String[] cameraPermissions;
    String[] storagePermissions;


    //views
    EditText titleEt, descriptionEt;
    ImageView imageIv, avatarIv;
    TextView nameTv;
    Button uploadBtn, imageBtn;
    ImageButton closeimageBtn;

    //user info
    String name, email, uid, dp;

    //info of post to be edited
    // información de la publicación a editar
    String editTitle, editDescription, editImage;

    //image pick will be samed in this uri
    // la selección de imagen se mostrará en esta uri
    Uri image_rui = null;

    //progress bar
    //barra de progreso
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.txttiletoolbar_addnewpost);
        //enable back button in actionbar
        // habilitar el botón de retroceso en la barra de acción
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setElevation(0);

        //init permissions array
        //inicializar matriz de permisos
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init progress bar
        //inicializar barra de progreso
        pd = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        //init views
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageIv = findViewById(R.id.pImageIv);
        uploadBtn = findViewById(R.id.pUploadBtn);
        imageBtn = findViewById(R.id.imageBtn);
        closeimageBtn = findViewById(R.id.closeimageBtn);

        avatarIv = findViewById(R.id.avatarIv);
        nameTv = findViewById(R.id.nameTv);

        //get data through intent from previus activitie's adapter
        // obtener datos a través del intento del adaptador de la actividad anterior
        Intent intent = getIntent();

        //get data and its type from intent -- obtener datos y su tipo a partir de la intención
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {

            if ("text/plain".equals(type)) {
                //text type data
                handleSendText(intent);
            } else if (type.startsWith("image")) {
                //image type data
                handleSendImage(intent);
            }

        }

        final String isUpdateKey = "" + intent.getStringExtra("key");
        final String editPostId = "" + intent.getStringExtra("editPostId");
        //validate if we came  here  to update post i.e. came from AdapterPost
        // validar si vinimos aquí para actualizar la publicación, es decir, vino de AdapterPost
        if (isUpdateKey.equals("editPost")) {
            //update-actualice
            actionBar.setTitle(R.string.txttiletoolbar_updatepost);
            uploadBtn.setText(R.string.updatepostbtn);
            loadPostData(editPostId);
        } else {
            //add-- agregar
            actionBar.setTitle(R.string.txttiletoolbar_addnewpost);
            uploadBtn.setText(R.string.uploadpostbtn);

        }

        //get some info of current user to include in post
        // obtener información del usuario actual para incluir en la publicación
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("image").getValue();

                    //set data -- establecer dato de nombre en TV e imagen en avatar
                    nameTv.setText(name);
                    try {
                        //if image is received then set -- si se recibe la imagen, establezca
                        Picasso.get().load(dp).into(avatarIv);

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


        //get image from camera/gallery on cick
        //obtener la imagen de la camara/galeria al hacer click
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                //muestra el cuadro de dialogo de seleccion de imagen
                showImagePickDialog();
            }
        });

        //Upload button click listener
        //al hacer click en el boton de publicar
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data (title, description) from EditText
                // obtener datos (título, descripción) de EditText
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    String txterrortiltepost = getString(R.string.txterrortiltepost);
                    titleEt.setError(txterrortiltepost);
                    titleEt.setFocusable(true);
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    String txterrordescpost = getString(R.string.txterrordescpost);
                    descriptionEt.setError(txterrordescpost);
                    descriptionEt.setFocusable(true);
                    return;
                }

                if (isUpdateKey.equals("editPost")) {
                    beginUpdate(title, description, editPostId);
                } else {
                    uploadData(title, description);
                }

            }
        });
    }

    private void handleSendImage(Intent intent) {
        //handle the received image(uri) -- maneja la imagen recibida (uri)
        Uri imageURI = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageURI != null) {
            image_rui = imageURI;
            //set to image imageView -- establecer en imagen imageView
            imageIv.setImageURI(image_rui);
        }
    }

    private void handleSendText(Intent intent) {
        //handle the received text -- maneja el texto recibido
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            //set to description edit text --establece la descripcion en el editText
            descriptionEt.setText(sharedText);
        }
    }

    private void beginUpdate(String title, String description, String editPostId) {
        closeimageBtn.setVisibility(View.GONE);
        String txtprogresbar_updatingpost = getString(R.string.txtprogresbar_updatingpost);
        pd.setMessage(txtprogresbar_updatingpost);
        pd.show();

        if (!editImage.equals("noImage")) {
            //with image--con imagen
            updateWasWithImage(title, description, editPostId);
        } else if (imageIv.getDrawable() != null) {
            //with image--con imagen
            updateWithNowImage(title, description, editPostId);
        } else {
            //without image--sin imagen
            updateWithoutImage(title, description, editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {
        final String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        //put post info--poner información de la publicación
        hashMap.put("uid", uid);
        hashMap.put("uName", name);
        hashMap.put("uEmail", email);
        hashMap.put("uDp", dp);
        hashMap.put("pTitle", title);
        hashMap.put("pDescr", description);
        hashMap.put("pImage", "noImage");
        hashMap.put("pTime", timeStamp);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        imageIv.setVisibility(View.GONE);
                        closeimageBtn.setVisibility(View.GONE);
                        Toast.makeText(AddPostActivity.this, R.string.Updatingtxt, Toast.LENGTH_SHORT).show();
                        onBackPressed();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateWithNowImage(final String title, final String description, final String editPostId) {

        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;

        //get image from image view
        Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        final byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded get its url-- la imagen cargada obtiene su url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;

                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            //url is recieved, upload to Firebase database
                            // se recibe la url, subir a la base de datos de Firebase

                            HashMap<String, Object> hashMap = new HashMap<>();
                            //put post info--poner información de la publicación
                            hashMap.put("uid", uid);
                            hashMap.put("uName", name);
                            hashMap.put("uEmail", email);
                            hashMap.put("uDp", dp);
                            hashMap.put("pTitle", title);
                            hashMap.put("pDescr", description);
                            hashMap.put("pImage", downloadUri);
                            hashMap.put("pTime", timeStamp);


                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                            ref.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            imageIv.setVisibility(View.GONE);
                                            closeimageBtn.setVisibility(View.GONE);
                                            Toast.makeText(AddPostActivity.this, R.string.Updatingtxt, Toast.LENGTH_SHORT).show();
                                            onBackPressed();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void updateWasWithImage(final String title, final String description, final String editPostId) {
        //post is with image, delete previous image first
        // la publicación es con imagen, elimine la imagen anterior primero
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        if (imageIv.getDrawable() != null) {
                            //image deleted, upload new image
                            //from post-image name, post-id, publish-time
                            // imagen eliminada, subir nueva imagen
                            // de post-nombre de imagen, post-id, tiempo de publicación
                            final String timeStamp = String.valueOf(System.currentTimeMillis());
                            String filePathAndName = "Posts/" + "post_" + timeStamp;

                            //get image from image view --obtener la imagen del ImageView
                            Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            //image compress
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            final byte[] data = baos.toByteArray();

                            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                            ref.putBytes(data)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            //image uploaded get its url-- la imagen cargada obtiene su url
                                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                            while (!uriTask.isSuccessful()) ;

                                            String downloadUri = uriTask.getResult().toString();
                                            if (uriTask.isSuccessful()) {
                                                //url is recieved, upload to Firebase database
                                                // se recibe la url, subir a la base de datos de Firebase

                                                HashMap<String, Object> hashMap = new HashMap<>();
                                                //put post info--poner información de la publicación
                                                hashMap.put("uid", uid);
                                                hashMap.put("uName", name);
                                                hashMap.put("uEmail", email);
                                                hashMap.put("uDp", dp);
                                                hashMap.put("pTitle", title);
                                                hashMap.put("pDescr", description);
                                                hashMap.put("pImage", downloadUri);
                                                hashMap.put("pTime", timeStamp);


                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                                ref.child(editPostId)
                                                        .updateChildren(hashMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                pd.dismiss();
                                                                imageIv.setVisibility(View.GONE);
                                                                closeimageBtn.setVisibility(View.GONE);
                                                                Toast.makeText(AddPostActivity.this, R.string.Updatingtxt, Toast.LENGTH_SHORT).show();
                                                                onBackPressed();

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                pd.dismiss();
                                                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        } else {
                            updateWithoutImage(title, description, editPostId);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //get detail of post using id of post
        //optener los detalles de la publicacion usando su id
        Query fquery = reference.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    editTitle = "" + ds.child("pTitle").getValue();
                    editDescription = "" + ds.child("pDescr").getValue();
                    editImage = "" + ds.child("pImage").getValue();

                    //set data to views
                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);

                    //set image
                    if (!editImage.equals("noImage")) {
                        try {
                            Picasso.get().load(editImage).into(imageIv);
                            deleteIV();
                        } catch (Exception e) {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadData(final String title, final String description) {
        String txtprogresbar_Publishingpost = getString(R.string.txtprogresbar_Publishingpost);
        pd.setMessage(txtprogresbar_Publishingpost);
        pd.show();

        //for post-image name, post-id, post-publishing-time
        final String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post_" + timeStamp;

        if (imageIv.getDrawable() != null) {
            //get image from image view
            Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //image compress
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();


            //post with image
            //publicar con imagen
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image is uploaded to firebase storage, now get it's url
                            // la imagen se carga en el almacenamiento de Firebase, ahora obtén su URL
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;

                            String downloadUri = uriTask.getResult().toString();

                            if (uriTask.isSuccessful()) {

                                //url is received upload post to firebase database
                                // se recibe la url carga la publicación en la base de datos de Firebase

                                HashMap<Object, String> hashMap = new HashMap<>();
                                //put post into
                                //poner informacion de la publicacion
                                hashMap.put("uid", uid);
                                hashMap.put("uName", name);
                                hashMap.put("uEmail", email);
                                hashMap.put("uDp", dp);
                                hashMap.put("pId", timeStamp);
                                hashMap.put("pTitle", title);
                                hashMap.put("pDescr", description);
                                hashMap.put("pImage", downloadUri);
                                hashMap.put("pTime", timeStamp);
                                hashMap.put("pLikes", "0");
                                hashMap.put("pComments", "0");

                                //path to store post data
                                // ruta para almacenar datos de publicación
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                //put data in this ref
                                //poner datos en esta referencia
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //added in database
                                                //agregado en la base de datos
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, R.string.txtpostpublished, Toast.LENGTH_SHORT).show();
                                                //reset views
                                                //restablecer vistas
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIv.setVisibility(View.GONE);
                                                closeimageBtn.setVisibility(View.GONE);
                                                imageIv.setImageURI(null);
                                                image_rui = null;

                                                //send notification
                                                String msmnotificationpos = getString(R.string.msmnotificationpos);
                                                prepareNotification(
                                                        "" + timeStamp, //since we are using timeStamp for post id
                                                        "" + name + "\n" + msmnotificationpos,//message of the notification
                                                        "" + title + "\n" + description,
                                                        "PostNotification",
                                                        "POST"
                                                );
                                                onBackPressed();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed adding post in database
                                                // error al agregar publicacion en la base de datos
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            //imagen de carga fallida
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            //post without image
            // publicar sin imagen

            HashMap<Object, String> hashMap = new HashMap<>();
            //put post into
            //poner informacion de la publicacion
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescr", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");

            //path to store post data
            // ruta para almacenar datos de publicación
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //put data in this ref
            //poner datos en esta referencia
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //added in database
                            //agregado en la base de datos
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, R.string.txtpostpublished, Toast.LENGTH_SHORT).show();
                            //reset views
                            //restablecer vistas
                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageIv.setVisibility(View.GONE);
                            closeimageBtn.setVisibility(View.GONE);
                            imageIv.setImageURI(null);
                            image_rui = null;

                            //send notification
                            String msmnotificationpos = getString(R.string.msmnotificationpos);
                            prepareNotification(
                                    "" + timeStamp, //since we are using timeStamp for post id
                                    "" + name + "\n" + msmnotificationpos,//message of the notification
                                    "" + title + "\n" + description,
                                    "PostNotification",
                                    "POST"
                            );
                            onBackPressed();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed adding post in database
                            // error al agregar publicacion en la base de datos
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }

    private void prepareNotification(String pId, String title, String description, String notificationType, String notificationTopic) {
        //prepare data from notification

        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic; //topic must match with what the receiver subscribed to
        String NOTIFICATION_TITLE = title;
        String NOTIFICATION_MESSAGE = description; //content of post
        String NOTIFICATION_TYPE = notificationType; // now there are two notification types chat & post,  so to differentiate in FirebaseMessagin.java class

        //prepare json what to send, and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try {
            //what to send
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("sender", uid);//uid of current use/sender
            notificationBodyJo.put("pId", pId);//post id
            notificationBodyJo.put("pTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescription", NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to", NOTIFICATION_TOPIC);

            notificationJo.put("data", notificationBodyJo); // combine data to be sent
        } catch (JSONException e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        sendPostNotification(notificationJo);

    }

    private void sendPostNotification(JSONObject notificationJo) {
        //send volley object request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE", "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //error occured
                        Toast.makeText(AddPostActivity.this, "" + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required handles
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AAAAJW58G0Q:APA91bH13JDQFclw7If5Q832cn5c0RRgOHzH1Hv4os6IFbBC-V1mn7xzMYRS2SR5CC_XtsCgFsIoqcPkub2JlcxBzqaAifsSWuGwAR6Hl_FQbimMFXvGc5oW2cbBPaf-UZAKVUfzq8qx");
                return headers;
            }
        };
        //enqueue the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void showImagePickDialog() {
        //options (camera , gallery) to show in dialog
        // opciones (cámara, galería) para mostrar en el diálogo
        String pickimagen = getString(R.string.pickimagen);
        String camerapermission = getString(R.string.camerapermission);
        String gallerypermission = getString(R.string.gallerypermission);
        String[] options = {camerapermission, gallerypermission};

        //dialog -- dialogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(pickimagen);

        //set options to dialog
        //establecer opciones para dialogo
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //item click handle
                //haga clic en el elemento del controlador
                if (which == 0) {
                    //camera clicked
                    //hacer clicl en camara
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                }
                if (which == 1) {
                    //gallery clicked
                    //hacer click en galeria
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }

                }
            }
        });
        //create and show dialog
        //crear y mostrar dialogo
        builder.create().show();
    }

    private void pickFromGallery() {
        //intent to pick image from gallery
        // intento de elegir una imagen de la galería
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent to pick image from camera
        // intento de tomar una imagen de la cámara
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    private boolean checkStoragePermission() {
        //check if storage permission is enable or not
        //return true if enable
        //return false if not enable

        // verifica si el permiso de almacenamiento está habilitado o no
        // devuelve verdadero si esta habilitado
        // devuelve falso si no está habilitado

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        // solicitar permiso de almacenamiento en tiempo de ejecución
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission() {
        //check if camera permission is enable or not
        //return true if enable
        //return false if not enable

        // verifica si el permiso de camara está habilitado o no
        // devuelve verdadero si esta habilitado
        // devuelve falso si no está habilitado

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        //request runtime camera permission
        // solicitar permiso de camara en tiempo de ejecución
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }


    private void checkUserStatus() {
        //get current user -- obtener usuario actual
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here -- El usuario ha iniciado sesión aquí.
            email = user.getEmail();
            uid = user.getUid();

        } else {
            //user not signed in, go to main activity -- usuario no ha iniciado sesión, vaya a la actividad principal
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        // muestra el cuadro de diálogo
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AddPostActivity.this);
        builder.setMessage(R.string.msnonbackpressed);
        builder.setPositiveButton(R.string.txtacept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                onBackPressed(); //goto previus activity-- ir a la actividad anterior

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

        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_chat).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    //handle permissions results
    // manejar resultados de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //this method is called when user press Allow or Deny from permissions request dialog
        //here we will handle permissions cases (allowed and denied)

        // se llama a este método cuando el usuario presiona Permitir o Denegar desde el diálogo de solicitud de permisos
        // aquí manejaremos los casos de permisos (permitidos y denegados)

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //both permission are granted
                        // se otorgan ambos permisos
                        pickFromCamera();
                    } else {

                        //camera or gallery or both permissions were denied
                        // cámara o galería o ambos permisos fueron denegados
                        String textpermissionsimagen = getString(R.string.textpermissionsimagen);
                        Toast.makeText(this, textpermissionsimagen, Toast.LENGTH_SHORT).show();
                    }

                } else {

                }

            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //storage permission granted
                        //permiso de almacenamiento otorgado
                        pickFromGallery();
                    } else {

                        //camera or gallery or both permissions were denied
                        // cámara o galería o ambos permisos fueron denegados
                        String textpermissionstorage = getString(R.string.textpermissionstorage);
                        Toast.makeText(this, textpermissionstorage, Toast.LENGTH_SHORT).show();
                    }
                } else {

                }
            }
            break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking image from camera or gallery
        // se llamará a este método después de seleccionar la imagen de la cámara o galería
        if (resultCode == RESULT_OK) {
            deleteIV();
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //Image is picked from gallery, get uri of image
                // La imagen se selecciona de la galería, obtén el uri de la imagen
                image_rui = data.getData();

                //set to ImageView
                //establecer en ImageView
                imageIv.setImageURI(image_rui);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image is picked from camera, get uri of image
                // la imagen se toma de la cámara, obtén el uri de la imagen

                //set to ImageView
                //establecer en ImageView
                imageIv.setImageURI(image_rui);

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void deleteIV() {
        if (imageIv != null) {
            imageIv.setVisibility(View.VISIBLE);
            closeimageBtn.setVisibility(View.VISIBLE);
            closeimageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Picasso.get().load(R.drawable.ic_delete_accentcolor).into(imageIv);
                    imageIv.setImageURI(null);
                    image_rui = null;
                    imageIv.setVisibility(View.GONE);
                    closeimageBtn.setVisibility(View.GONE);
                }
            });
        }
    }
}
