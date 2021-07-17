package com.canibal.appdoptafirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.canibal.appdoptafirebase.adapters.AdapterComments;
import com.canibal.appdoptafirebase.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    //to get detail of user and post
    String hisUid, myUid, myEmail, postId, pLikes, hisDp, hisName, pImage;

    boolean mProcessLike = false;

    //progress bar
    ProgressDialog pd;


    //views -- vistas
    ImageView uPictureIv, pImageIv;
    TextView uNameTv, pTimeTiv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn, commentBtn, shareBtn;
    LinearLayout profileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        showToolbar("", true);

        //get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");


        // init views
        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTiv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        commentBtn = findViewById(R.id.commentBtn);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profileLayout);


        loadPostInfo();

        checkUserStatus();

        setLikes();

        pImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start PostDetailActivity
                Intent intent = new Intent(PostDetailActivity.this, DetailImageActivity.class);
                intent.putExtra("postId", postId); //will get detail of post using this id, its id of the post clicked
                startActivity(intent);
            }
        });

        //uNameTv click handle
        uNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*click to go to Dashboard2Activity or to go to ThereProfileActivity with uid, this uid is of clicked user
                 * which will be used to show user specific data/post*/

                /* haga clic para ir a Dashboard2Activity (profilefragment) o a ThereProfileActivity con uid, este uid es de usuario cliqueado
                 * que se utilizará para mostrar datos / publicaciones específicos del usuario */
                if (hisUid.equals(myUid)) {

                    Intent intent = new Intent(PostDetailActivity.this, Dashboard2Activity.class);
                    intent.putExtra("uid", hisUid);
                    startActivity(intent);

                } else {
                    Intent intent = new Intent(PostDetailActivity.this, ThereProfileActivity.class);
                    intent.putExtra("uid", hisUid);
                    startActivity(intent);
                }
            }
        });

        //Like button click handle
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();

            }
        });

        //comment button click handle
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostDetailActivity.this, PostCommentByActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("postType", "Posts");
                startActivity(intent);
            }
        });

        //more button click handle
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });


        //share button click handle
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle = pTitleTv.getText().toString().trim();
                String pDescription = pDescriptionTv.getText().toString().trim();

                BitmapDrawable bitmapDrawable = (BitmapDrawable) pImageIv.getDrawable();
                if (bitmapDrawable == null) {
                    //post without image
                    shareTextOnly(pTitle, pDescription);
                } else {
                    //post with image

                    //convert image to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);
                }
            }
        });

        //click like count to start PostLikedByActivity, and pass the post id
        pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostDetailActivity.this, PostLikedByActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("postType", "Posts");
                startActivity(intent);
            }
        });

        //click like count to start PostLikedByActivity, and pass the post id
        pCommentsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostDetailActivity.this, PostCommentByActivity.class);
                intent.putExtra("postId", postId);
                intent.putExtra("postType", "Posts");
                startActivity(intent);
            }
        });

    }

    private void addToHisNotifications(String hisUid, String pId, String notification, String postType) {
        String timestamp = "" + System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("postType", postType);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        //concatenate title and description to share
        String shareBody = pTitle + "\n" + pDescription;
        String Subjecttxt = getString(R.string.Subjecttxt);
        String sharetxt = getString(R.string.sharetxt);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, Subjecttxt); // en caso de que comparta a través de una aplicación de correo electrónico
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //texto para compartir
        startActivity(Intent.createChooser(sIntent, sharetxt)); //mensaje para mostrar en el diálogo de compartir

    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        //concatenate title and description to share
        String shareBody = pTitle + "\n" + pDescription;
        String Subjecttxt = getString(R.string.Subjecttxt);
        String sharetxt = getString(R.string.sharetxt);

        //first we will save this image in cache, get the saved image uri
        // primero guardaremos esta imagen en caché, obtendremos la uri de la imagen guardada
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, Subjecttxt);// en caso de que comparta a través de una aplicación de correo electrónico
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent, sharetxt)); //mensaje para mostrar en el diálogo de compartir


    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();//crear si no existe
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.canibal.appdoptafirebase.fileprovider",
                    file);

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void showMoreOptions() {
        //creating popup menu currently habing option delete, we will add more options later
        // creando un menú emergente que actualmente tiene la opción eliminar, agregaremos más opciones más tarde
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        // muestra la opción de eliminación solo en las publicaciones del usuario que ha iniciado sesión actualmente
        if (hisUid.equals(myUid)) {
            //add  items in menu
            String txtdelete = getString(R.string.txtdelete);
            String txtedit = getString(R.string.txtedit);

            popupMenu.getMenu().add(Menu.NONE, 0, 0, txtdelete);
            popupMenu.getMenu().add(Menu.NONE, 1, 0, txtedit);
        }

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0) {

                    //delete is clicked
                    // muestra el cuadro de diálogo eliminar
                    AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
                    builder.setTitle("");
                    builder.setMessage(R.string.msmdeletepost);
                    builder.setPositiveButton(R.string.txtdelete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //delete is clicked
                            //delete is clicked
                            beginDelete();
                            Toast.makeText(PostDetailActivity.this, R.string.txtdeletepost, Toast.LENGTH_SHORT).show();
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

                } else if (id == 1) {
                    //edit is clicked
                    // muestra el cuadro de diálogo editar
                    AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
                    builder.setTitle("");
                    builder.setMessage(R.string.msmeditpost);
                    builder.setPositiveButton(R.string.txtedit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //start AddPostActivity with key "editpost" and the id of the post clicked
                            // inicie AddPostActivity con la clave "editpost" y haga clic en la identificación de la publicación
                            Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                            intent.putExtra("key", "editPost");
                            intent.putExtra("editPostId", postId);
                            startActivity(intent);
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
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete() {
        //post can be with or without image
        // la publicación puede ser con o sin imagen

        if (pImage.equals("noImage")) {
            //post is without image
            // la publicación no tiene imagen
            deleteWithoutImage();
            onBackPressed();

        } else {
            //post is with image
            //la publicacion tiene imagen
            deleteWithImage();
            onBackPressed();

        }
    }

    private void deleteWithImage() {
        //progres bar
        final ProgressDialog pd = new ProgressDialog(this);
        String progresbardeleting = getString(R.string.progresbardeleting);
        pd.setMessage(progresbardeleting);

        /*Steps:
         * 1) Delete image using url
         * 2)Delete from database using post id*/

        /*Pasos:
         * 1) Eliminar imagen usando url
         * 2) Eliminar de la base de datos usando el ID de publicación */

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image delete, now delete database
                        // eliminar imagen, ahora eliminar base de datos

                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    ds.getRef().removeValue(); //remove values from firebase where pic matches -- elimina valores de firebase donde coincide la imagen
                                }
                                //deleted
                                Toast.makeText(PostDetailActivity.this, R.string.deletedpost, Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed, can't go further
                        // falló, no puedo ir más allá
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        String progresbardeleting = getString(R.string.progresbardeleting);
        pd.setMessage(progresbardeleting);

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue(); //remove values from firebase where pic matches -- elimina valores de firebase donde coincide la imagen
                }
                //deleted
                Toast.makeText(PostDetailActivity.this, R.string.deletedpost, Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void setLikes() {
        //when the details of posts is loading, also check if current user has liked it or not
        // cuando se cargan los detalles de las publicaciones, compruebe también si al usuario actual le ha gustado o no

        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).hasChild(myUid)) {
                    //user has liked this post
                    /*To indicate that the post is liked by this (SignedIn) user
                     * change drawable left icon of the button
                     *change text of like button from "like" to "liked" */

                    // al usuario le ha gustado esta publicación
                    /* Para indicar que la publicación le gusta a este usuario (SignedIn)
                     * Cambiar el icono izquierdo dibujable del botón
                     * cambia el texto del botón Me gusta de "Me gusta" a "Me gusta" */
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    String likebtn = getString(R.string.likebtn);
                    likeBtn.setText(likebtn);

                } else {
                    //user has not liked this post
                    /*To indicate that the post is not liked by this (SignedIn) user
                     * change drawable left icon of the button
                     *change text of like button from "liked" to "liked" */

                    // al usuario no le ha gustado esta publicación
                    /* Para indicar que la publicación no le gusta a este usuario (SignedIn)
                     * Cambiar el icono izquierdo dibujable del botón
                     * cambia el texto del botón Me gusta de "Me gusta" a "Me gusta" */
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    String likebtn = getString(R.string.likebtn);
                    likeBtn.setText(likebtn);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {
        //get total number of likes for the post, whose like button clicked
        //if currently signed in user has not liket it before
        //increase value by 1, otherwise decrease value by 1

        // obtener el número total de Me gusta para la publicación, en cuyo botón Me gusta hizo clic
        // si el usuario que ha iniciado sesión actualmente no lo ha marcado antes
        // aumenta el valor en 1, de lo contrario disminuye el valor en 1
        mProcessLike = true;
        //get id of the post clicked
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessLike) {
                    if (dataSnapshot.child(postId).hasChild(myUid)) {
                        //already liked, so remove like
                        postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) - 1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;

                    } else {
                        //no liked, like it
                        postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) + 1));
                        likesRef.child(postId).child(myUid).setValue("Liked"); //set any value
                        mProcessLike = false;

                        if (!hisUid.equals(myUid)) {
                            String txtnotifiliked = getString(R.string.txtnotifiliked);
                            addToHisNotifications("" + hisUid, "" + postId, txtnotifiliked, "Posts");
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {
        //get post using the id of the  post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep checking the posts until get the  required post
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescr = "" + ds.child("pDescr").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisName = "" + ds.child("uName").getValue();
                    String commentCount = "" + ds.child("pComments").getValue();


                    //convert timestamp to dd/mm/yyy hh:mm am/pm
                    //convierte la marca de tiempo a dd/mm/yyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                    String likebtn = getString(R.string.likebtn);
                    String commentbtn = getString(R.string.commentbtn);

                    //set data
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes + "\t" + likebtn);
                    pTimeTiv.setText(pTime);
                    pCommentsTv.setText(commentCount + "\t" + commentbtn);

                    uNameTv.setText(hisName);

                    // set image of the user who posted
                    //if there is no image i.e. pImage.equals("noImage") then hide ImageView
                    if (pImage.equals("noImage")) {
                        //hide Image Biew
                        pImageIv.setVisibility(View.GONE);
                    } else {
                        //show Image Biew
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(pImageIv);

                        } catch (Exception e) {

                        }
                    }

                    //set user image in comment part
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img).into(uPictureIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img).into(uPictureIv);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //user is signed in
            myEmail = user.getEmail();
            myUid = user.getUid();

        } else {
            //user not signed in, go to main activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        //check on start app --comprobar en la aplicación de inicio
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        //set online -- establecer en linea
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void showToolbar(String tittle, boolean upButton) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(tittle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide some menu items
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_chat).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
