package com.canibal.appdoptafirebase.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.canibal.appdoptafirebase.AddAdoptionActivity;
import com.canibal.appdoptafirebase.AddPostActivity;
import com.canibal.appdoptafirebase.AddReportsFoundActivity;
import com.canibal.appdoptafirebase.AddReportsLostActivity;
import com.canibal.appdoptafirebase.AdoptionDetailActivity;
import com.canibal.appdoptafirebase.Dashboard2Activity;
import com.canibal.appdoptafirebase.PostCommentByActivity;
import com.canibal.appdoptafirebase.PostDetailActivity;
import com.canibal.appdoptafirebase.PostLikedByActivity;
import com.canibal.appdoptafirebase.R;
import com.canibal.appdoptafirebase.ReportsFoundDetailActivity;
import com.canibal.appdoptafirebase.ReportsLostDetailActivity;
import com.canibal.appdoptafirebase.ThereProfileActivity;
import com.canibal.appdoptafirebase.models.ModelAdoption;
import com.canibal.appdoptafirebase.models.ModelPost;
import com.canibal.appdoptafirebase.models.ModelReportsFound;
import com.canibal.appdoptafirebase.models.ModelReportsLost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterReportsFound extends RecyclerView.Adapter<AdapterReportsFound.MyHolder> {

    Context context;
    List<ModelReportsFound> reportsFoundList;

    String myUid;

    private DatabaseReference reportsFoundRef; // reference of posts

    boolean mProcessLike = false;

    public AdapterReportsFound(Context context, List<ModelReportsFound> reportsFoundList) {
        this.context = context;
        this.reportsFoundList = reportsFoundList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reportsFoundRef = FirebaseDatabase.getInstance().getReference().child("ReportsFound");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_reports_found, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        //get data
        //obtener datos
        final String uid = reportsFoundList.get(i).getUid();
        String uEmail = reportsFoundList.get(i).getuEmail();
        String uName = reportsFoundList.get(i).getuName();
        String uDp = reportsFoundList.get(i).getuDp();
        final String pId = reportsFoundList.get(i).getpId();
        final String pTitle = reportsFoundList.get(i).getpTitle();
        final String pAge = reportsFoundList.get(i).getpAge();
        final String pSpecies = reportsFoundList.get(i).getpSpecies();
        final String pRace = reportsFoundList.get(i).getpRace();
        final String pSex = reportsFoundList.get(i).getpSex();
        final String pState = reportsFoundList.get(i).getpState();
        final String pLocation = reportsFoundList.get(i).getpLocation();
        final String pDescription = reportsFoundList.get(i).getpDescr();
        final String pImage = reportsFoundList.get(i).getpImage();
        String pTimeStamp = reportsFoundList.get(i).getpTime();
        String pLikes = reportsFoundList.get(i).getpLikes(); //contenedor para el numero total de likes del post
        String pComments = reportsFoundList.get(i).getpComments(); //contenedor para el numero total de likes del post

        //convert timestamp to dd/mm/yyy hh:mm am/pm
        //convierte la marca de tiempo a dd/mm/yyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        String likebtn = context.getString(R.string.likebtn);
        String commentbtn = context.getString(R.string.commentbtn);

        //set data
        myHolder.uNameTv.setText(uName);
        myHolder.pTimeTv.setText(pTime);
        myHolder.pTitleTv.setText(pTitle);
        myHolder.pAgeTv.setText(pAge);
        myHolder.pRaceTv.setText(pRace);
        myHolder.pSexTv.setText(pSex);
        myHolder.pSpeciesTv.setText(pSpecies);
        myHolder.pCommentsTv.setText(pComments + "\t" + commentbtn); //e.g. 100 likes
        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(myHolder.uPictureIv);
        } catch (Exception e) {

        }

        //set post image
        //if there is no image i.e. pImage.equals("noImage") then hide ImageView
        if (pImage.equals("noImage")) {
            //hide Image View
            myHolder.pImageIv.setVisibility(View.GONE);
        } else {
            //show Image View
            myHolder.pImageIv.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(pImage).into(myHolder.pImageIv);

            } catch (Exception e) {

            }
        }

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start PostDetailActivity
                Intent intent = new Intent(context, ReportsFoundDetailActivity.class);
                intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                context.startActivity(intent);
            }
        });

        //handle buttons clicks
        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(myHolder.moreBtn, uid, myUid, pId, pImage);
            }
        });

        myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start PostCommentByActivity
                Intent intent = new Intent(context, PostCommentByActivity.class);
                intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                intent.putExtra("postType", "ReportsFound");
                context.startActivity(intent);


            }
        });
        myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*some posts contains only text, and some contains image and text so, we will handle them both*/
                /* algunas publicaciones contienen solo texto, y algunas contienen imágenes y texto, así que las manejaremos ambas */
                //get image from imageView
                BitmapDrawable bitmapDrawable = (BitmapDrawable) myHolder.pImageIv.getDrawable();
                if (bitmapDrawable == null) {
                    //post without image
                    shareTextOnly(pTitle, pSpecies, pRace, pLocation, pSex, pAge, pState, pDescription);
                } else {
                    //post with image

                    //convert image to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pSpecies, pRace, pLocation, pSex, pAge, pState, pDescription, bitmap);
                }
            }
        });

        myHolder.uNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*click to go to Dashboard2Activity or to go to ThereProfileActivity with uid, this uid is of clicked user
                 * which will be used to show user specific data/post*/

                /* haga clic para ir a Dashboard2Activity (profilefragment) o a ThereProfileActivity con uid, este uid es de usuario cliqueado
                 * que se utilizará para mostrar datos / publicaciones específicos del usuario */
                if (uid.equals(myUid)) {

                    Intent intent = new Intent(context, Dashboard2Activity.class);
                    intent.putExtra("uid", uid);
                    context.startActivity(intent);

                } else {
                    Intent intent = new Intent(context, ThereProfileActivity.class);
                    intent.putExtra("uid", uid);
                    context.startActivity(intent);
                }

            }
        });

        myHolder.pCommentsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostCommentByActivity.class);
                intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                intent.putExtra("postType", "ReportsFound");
                context.startActivity(intent);
            }
        });

    }


    private void addToHisNotifications(String hisUid, String pId, String notification, String postType) {
        String pTimeStamp = "" + System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("postType", postType);
        hashMap.put("timestamp", pTimeStamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(pTimeStamp).setValue(hashMap)
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

    private void shareTextOnly(String pTitle, String pSpecies, String pRace, String pLocation, String pSex, String pAge, String pState, String pDescription) {
        //concatenate title and description to share
        String txthintnameofpet = context.getString(R.string.txthintnameofpet);
        String txthintspeciesofpet = context.getString(R.string.txthintspeciesofpet);
        String txthintraceofpet = context.getString(R.string.txthintraceofpet);
        String txthintlocationofpet = context.getString(R.string.txthintlocationofpet);
        String txthintsexofpet = context.getString(R.string.txthintsexofpet);
        String txthintageofpet = context.getString(R.string.txthintageofpet);
        String txthintstateofpet = context.getString(R.string.txthintstateofpet);
        String txthintdescriptionfpet = context.getString(R.string.txthintdescriptionfpet);

        String shareBody = txthintnameofpet + "\t" + pTitle + "\n" + txthintspeciesofpet + "\t" + pSpecies + "\n" +
                txthintraceofpet + "\t" + pRace + "\n" + txthintlocationofpet + "\t" + pLocation + "\n" + txthintsexofpet + "\t" + pSex + "\n" +
                txthintageofpet + "\t" + pAge + "\n" + txthintstateofpet + "\t" + pState + "\n" + txthintdescriptionfpet + "\t" + pDescription;
        String Subjecttxt = context.getString(R.string.Subjecttxt);
        String sharetxt = context.getString(R.string.sharetxt);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, Subjecttxt); // en caso de que comparta a través de una aplicación de correo electrónico
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //texto para compartir
        context.startActivity(Intent.createChooser(sIntent, sharetxt)); //mensaje para mostrar en el diálogo de compartir


    }

    private void shareImageAndText(String pTitle, String pSpecies, String pRace, String pLocation, String pSex, String pAge, String pState, String pDescription, Bitmap bitmap) {
        //concatenate title and description to share
        //txt usados para el cuerpo de text body share
        String txthintnameofpet = context.getString(R.string.txthintnameofpet);
        String txthintspeciesofpet = context.getString(R.string.txthintspeciesofpet);
        String txthintraceofpet = context.getString(R.string.txthintraceofpet);
        String txthintlocationofpet = context.getString(R.string.txthintlocationofpet);
        String txthintsexofpet = context.getString(R.string.txthintsexofpet);
        String txthintageofpet = context.getString(R.string.txthintageofpet);
        String txthintstateofpet = context.getString(R.string.txthintstateofpet);
        String txthintdescriptionfpet = context.getString(R.string.txthintdescriptionfpet);

        String shareBody = txthintnameofpet + "\t" + pTitle + "\n" + txthintspeciesofpet + "\t" + pSpecies + "\n" +
                txthintraceofpet + "\t" + pRace + "\n" + txthintlocationofpet + "\t" + pLocation + "\n" + txthintsexofpet + "\t" + pSex + "\n" +
                txthintageofpet + "\t" + pAge + "\n" + txthintstateofpet + "\t" + pState + "\n" + txthintdescriptionfpet + "\t" + pDescription;
        String Subjecttxt = context.getString(R.string.Subjecttxt);
        String sharetxt = context.getString(R.string.sharetxt);

        //first we will save this image in cache, get the saved image uri
        // primero guardaremos esta imagen en caché, obtendremos la uri de la imagen guardada
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, Subjecttxt);// en caso de que comparta a través de una aplicación de correo electrónico
        sIntent.setType("image/png");
        context.startActivity(Intent.createChooser(sIntent, sharetxt)); //mensaje para mostrar en el diálogo de compartir


    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs();//crear si no existe
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.canibal.appdoptafirebase.fileprovider",
                    file);

        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    //add a key named "pLikes" to each post and set its value to "0" manually in firebase
    // agrega una clave llamada "pLikes" a cada publicación y establece su valor en "0" manualmente en firebase


    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        //creating popup menu currently habing option delete, we will add more options later
        // creando un menú emergente que actualmente tiene la opción eliminar, agregaremos más opciones más tarde
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        // muestra la opción de eliminación solo en las publicaciones del usuario que ha iniciado sesión actualmente
        if (uid.equals(myUid)) {
            //add  items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, R.string.txtdelete);
            popupMenu.getMenu().add(Menu.NONE, 1, 0, R.string.txtedit);
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, R.string.txtviewdetail);


        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == 0) {
                    //delete is clicked
                    // muestra el cuadro de diálogo eliminar
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("");
                    builder.setMessage(R.string.msmdeletepost);
                    builder.setPositiveButton(R.string.txtdelete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //delete is clicked
                            beginDelete(pId, pImage);
                            Toast.makeText(context, R.string.txtdeletepost, Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("");
                    builder.setMessage(R.string.msmeditpost);
                    builder.setPositiveButton(R.string.txtedit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //start AddPostActivity with key "editpost" and the id of the post clicked
                            // inicie AddPostActivity con la clave "editpost" y haga clic en la identificación de la publicación
                            Intent intent = new Intent(context, AddReportsFoundActivity.class);
                            intent.putExtra("key", "editPost");
                            intent.putExtra("editPostId", pId);
                            context.startActivity(intent);
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

                } else if (id == 2) {
                    // start PostDetailActivity
                    Intent intent = new Intent(context, ReportsLostDetailActivity.class);
                    intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
                    context.startActivity(intent);
                }


                return false;
            }
        });
        //show menu
        popupMenu.show();

    }

    private void beginDelete(String pId, String pImage) {
        //post can be with or without image
        // la publicación puede ser con o sin imagen

        if (pImage.equals("noImage")) {
            //post is without image
            // la publicación no tiene imagen
            deleteWithoutImage(pId);

        } else {
            //post is with image
            //la publicacion tiene imagen
            deleteWithImage(pId, pImage);

        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        //progres bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("" + R.string.progresbardeleting);

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

                        Query fquery = FirebaseDatabase.getInstance().getReference("ReportsFound").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    ds.getRef().removeValue(); //remove values from firebase where pic matches -- elimina valores de firebase donde coincide la imagen
                                }
                                //deleted
                                Toast.makeText(context, R.string.deletedpost, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void deleteWithoutImage(final String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("" + R.string.progresbardeleting);

        Query fquery = FirebaseDatabase.getInstance().getReference("ReportsFound").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue(); //remove values from firebase where pic matches -- elimina valores de firebase donde coincide la imagen
                }
                //deleted
                Toast.makeText(context, R.string.deletedpost, Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return reportsFoundList.size();
    }

    // view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //views from row_post.xml
        ImageView uPictureIv, pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pAgeTv, pRaceTv, pSexTv, pSpeciesTv, pCommentsTv;
        ImageButton commentBtn, shareBtn, moreBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pAgeTv = itemView.findViewById(R.id.pAgeTv);
            pRaceTv = itemView.findViewById(R.id.pRaceTv);
            pSexTv = itemView.findViewById(R.id.pSexTv);
            pSpeciesTv = itemView.findViewById(R.id.pSpeciesTv);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
        }
    }
}

