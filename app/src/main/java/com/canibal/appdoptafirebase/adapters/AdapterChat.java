package com.canibal.appdoptafirebase.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.canibal.appdoptafirebase.R;
import com.canibal.appdoptafirebase.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.canibal.appdoptafirebase.R.color.colorPrimary;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {


    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layouts: row_chat_left.xml for receiver, row_chat_right.xml for sender
        //diseños de inflado: row_chat_left.xml para el receptor, row_chat_right.xml para el remitente
        if (i == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
            return new MyHolder(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        //get data -- obtener datos
        String message = chatList.get(i).getMessage();
        final String timeStamp = chatList.get(i).getTimestamp();
        String type = chatList.get(i).getType();

        //convert time stamp to dd/mm/yyyy hh:mm am/pm
        //convertir la marca de tiempo a dd/mm/aaaa hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        if (type.equals("text")) {
            //text message
            myHolder.messageTv.setVisibility(View.VISIBLE);
            myHolder.messageIv.setVisibility(View.GONE);

            myHolder.messageTv.setText(message);
        } else {
            //image message
            myHolder.messageTv.setVisibility(View.GONE);
            myHolder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(myHolder.messageIv);
        }

        //set data -- establecer datos
        myHolder.messageTv.setText(message);
        myHolder.timeTv.setText(dateTime);
        try {
            Picasso.get().load(imageUrl).into(myHolder.profileIv);
        } catch (Exception e) {

        }

        //click to show delete dialog -- Haga clic para mostrar el cuadro de diálogo Eliminar
        myHolder.messageLAyout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                myHolder.messageLAyout.setBackgroundResource(R.color.colorPrimaryopaci);
                //Show delete message confirm dialog -- mostrar eliminar mensaje confirmar diálogo
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("");
                builder.setMessage(R.string.deletemessage);
                //delete button  -- boton borrar
                builder.setPositiveButton(R.string.txtdelete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteMessage(i);
                        myHolder.messageLAyout.setBackground(null);

                    }
                });

                // cancel delete button -- boton cancelar
                builder.setNegativeButton(R.string.textBtn_cancel_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog -- descartar diaogo
                        dialog.dismiss();
                        myHolder.messageLAyout.setBackground(null);
                    }
                });
                //create and show dialog -- crear y mostrar dialogo
                builder.create().show();
                return false;
            }
        });

        myHolder.messageLAyout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myHolder.messageLAyout.setBackground(null);
            }
        });

        //set seen/delivered status of message -- establecer el estado visto / entregado del mensaje
        if (i == chatList.size() - 1) {
            if (chatList.get(i).isSeen()) {
                myHolder.isSeenTv.setText(R.string.txtseen);
            } else {
                myHolder.isSeenTv.setText(R.string.txtselivered);
            }
        } else {
            myHolder.isSeenTv.setVisibility(View.GONE);
        }

    }

    private void deleteMessage(int position) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        /*Logic:
        * Get timestamp of clicked message
        * Compare the timestamp of the clicked message with all messages in chats
        * where both values matches delete that message*
        *This will allow sender to delete his and receiver's message/

        /*Lógica:
        * Obtenga la marca de tiempo del mensaje en el que se hizo clic
        * Compare la marca de tiempo del mensaje cliqueado con todos los mensajes en los chats
        * donde ambos valores coinciden elimine ese mensaje
        * Esto permitirá al remitente eliminar su mensaje y el del receptor.*/

        final String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    /*if you want to allow sender to delete only his message then
                        compare sender value with current user's uid*
                        if they match means its the message of sender that is trying to delete*/

                    /*si desea permitir que el remitente elimine solo su mensaje, entonces
                        compare el valor del remitente con el uid del usuario actual *
                        si coinciden significa que es el mensaje del remitente que está tratando de eliminar */

                    if (ds.child("sender").getValue().equals(myUID)) {

                        /*We can do one of two things here
                         * 1) Remove the message form chats
                         * 2) set the value of message "this message was deleted..."
                         * so do whatever you want*/

                        /* Podemos hacer una de dos cosas aquí
                         * 1) Eliminar los chats del formulario de mensaje
                         * 2) establece el valor del mensaje "este mensaje fue eliminado ..."
                         * así que haz lo que quieras*/

                        //1) Remove the message from chats -- Eliminar los chats del formulario de mensaje
                        //ds.getRef().removeValue(); //now test this

                        //2) set the value of message "this message was deleted..." test this -- establece el valor del mensaje "este mensaje fue eliminado ..." prueba esto

                        ///////////////////////////////////////////////////////

                        HashMap<String, Object> hashMap = new HashMap<>();
                        String deletemsm = context.getString(R.string.deletemsm);
                        hashMap.put("message", deletemsm);
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, R.string.txtmessagedeleted, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(context, R.string.txtmessagefailedeleted, Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get currently signed in user -- obtener usuario actualmente conectado
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    //view holder class -- ver clase de titular
    class MyHolder extends RecyclerView.ViewHolder {

        //views -- vistas
        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLAyout; // for click listener to show delete

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views inicializar las vistas
            profileIv = itemView.findViewById(R.id.profileIv);
            messageIv = itemView.findViewById(R.id.messageIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeentTv);
            messageLAyout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
