package com.canibal.appdoptafirebase.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.canibal.appdoptafirebase.AdoptionDetailActivity;
import com.canibal.appdoptafirebase.ChatActivity;
import com.canibal.appdoptafirebase.PostDetailActivity;
import com.canibal.appdoptafirebase.R;
import com.canibal.appdoptafirebase.ReportsFoundDetailActivity;
import com.canibal.appdoptafirebase.ReportsLostDetailActivity;
import com.canibal.appdoptafirebase.ServiceDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class FirebaseMessaging extends FirebaseMessagingService {

    private static final String ADMIN_CHANNEL_ID = "admin_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //get current user from shared preferences
        //obtener el usuario actual de las preferencias compartidas
        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");

        /*Now there are tow  types of notificaions
            > notificationType ="PostNotification"
            > notificationType ="ChatNotification"*/

        /* Ahora hay dos tipos de notificaciones
            > notificationType = "PostNotification"
            > notificationType = "ChatNotification" */

        String notificationType = remoteMessage.getData().get("notificationType");
        if (notificationType.equals("PostNotification")) {
            //post Notification
            String sender = remoteMessage.getData().get("sender");
            String pId = remoteMessage.getData().get("pId");
            String pTitle = remoteMessage.getData().get("pTitle");
            String pDescription = remoteMessage.getData().get("pDescription");

            //if is same that has posted don't show notification
            // si es el mismo que ha publicado no mostrar notificación
            if (!sender.equals(savedCurrentUser)) {
                showPostNotification("" + pId, "" + pTitle, "" + pDescription);
            }

        } else if (notificationType.equals("AdoptionNotification")) {
            //post Notification
            String sender = remoteMessage.getData().get("sender");
            String pId = remoteMessage.getData().get("pId");
            String pTitle = remoteMessage.getData().get("pTitle");
            String pDescription = remoteMessage.getData().get("pDescription");

            //if is same that has posted don't show notification
            // si es el mismo que ha publicado no mostrar notificación
            if (!sender.equals(savedCurrentUser)) {
                showAdoptionNotification("" + pId, "" + pTitle, "" + pDescription);
            }

        } else if (notificationType.equals("ReportsLostNotification")) {
            //post Notification
            String sender = remoteMessage.getData().get("sender");
            String pId = remoteMessage.getData().get("pId");
            String pTitle = remoteMessage.getData().get("pTitle");
            String pDescription = remoteMessage.getData().get("pDescription");

            //if is same that has posted don't show notification
            // si es el mismo que ha publicado no mostrar notificación
            if (!sender.equals(savedCurrentUser)) {
                showReportLostNotification("" + pId, "" + pTitle, "" + pDescription);
            }

        } else if (notificationType.equals("ReportsFoundNotification")) {
            //post Notification
            String sender = remoteMessage.getData().get("sender");
            String pId = remoteMessage.getData().get("pId");
            String pTitle = remoteMessage.getData().get("pTitle");
            String pDescription = remoteMessage.getData().get("pDescription");

            //if is same that has posted don't show notification
            // si es el mismo que ha publicado no mostrar notificación
            if (!sender.equals(savedCurrentUser)) {
                showReportFoundNotification("" + pId, "" + pTitle, "" + pDescription);
            }

        } else if (notificationType.equals("ServiceNotification")) {
            //post Notification
            String sender = remoteMessage.getData().get("sender");
            String pId = remoteMessage.getData().get("pId");
            String pTitle = remoteMessage.getData().get("pTitle");
            String pDescription = remoteMessage.getData().get("pDescription");

            //if is same that has posted don't show notification
            // si es el mismo que ha publicado no mostrar notificación
            if (!sender.equals(savedCurrentUser)) {
                showServiceNotification("" + pId, "" + pTitle, "" + pDescription);
            }

        } else if (notificationType.equals("ChatNotification")) {
            //chat Notification
            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");
            FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fUser != null && sent.equals(fUser.getUid())) {
                if (!savedCurrentUser.equals(user)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sendOAndAboveNotification(remoteMessage);
                    } else {
                        sendNormalNotification(remoteMessage);
                    }
                }
            }
        }
    }

    //redirigir a posdetail
    private void showPostNotification(String pId, String pTitle, String pDescription) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = new Random().nextInt(3000);

        /*Apps targeting SDK 26 or above (Android O and above) must implement notification channels
         and add its notifications to at least one of the them
         Let's add check if verssion is oreo or higher then setup notification channel*/

        /*Las aplicaciones dirigidas al SDK 26 o superior (Android O y superior) deben implementar canales de notificación
         y agrega sus notificaciones al menos a una de ellas
         Agreguemos verificar si la versión es oreo o superior y luego configurar el canal de notificación*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupPostNotificationChannel(notificationManager);
        }

        //show post detail activity using post id when notification clicked
        // muestra la actividad detallada de la publicación usando la identificación de la publicación cuando se hace clic en la notificación
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("postId", pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //largeIcon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);

        //sound for notification
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(pDescription));


        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());


    }

    //redirigir a adoption detail
    private void showAdoptionNotification(String pId, String pTitle, String pDescription) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = new Random().nextInt(3000);

        /*Apps targeting SDK 26 or above (Android O and above) must implement notification channels
         and add its notifications to at least one of the them
         Let's add check if verssion is oreo or higher then setup notification channel*/

        /*Las aplicaciones dirigidas al SDK 26 o superior (Android O y superior) deben implementar canales de notificación
         y agrega sus notificaciones al menos a una de ellas
         Agreguemos verificar si la versión es oreo o superior y luego configurar el canal de notificación*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupPostNotificationChannel(notificationManager);
        }

        //show post detail activity using post id when notification clicked
        // muestra la actividad detallada de la publicación usando la identificación de la publicación cuando se hace clic en la notificación
        Intent intent = new Intent(this, AdoptionDetailActivity.class);
        intent.putExtra("postId", pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //largeIcon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);

        //sound for notification
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(pDescription));


        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());


    }

    //redirigir a reportlostdetail
    private void showReportLostNotification(String pId, String pTitle, String pDescription) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = new Random().nextInt(3000);

        /*Apps targeting SDK 26 or above (Android O and above) must implement notification channels
         and add its notifications to at least one of the them
         Let's add check if verssion is oreo or higher then setup notification channel*/

        /*Las aplicaciones dirigidas al SDK 26 o superior (Android O y superior) deben implementar canales de notificación
         y agrega sus notificaciones al menos a una de ellas
         Agreguemos verificar si la versión es oreo o superior y luego configurar el canal de notificación*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupPostNotificationChannel(notificationManager);
        }

        //show post detail activity using post id when notification clicked
        // muestra la actividad detallada de la publicación usando la identificación de la publicación cuando se hace clic en la notificación
        Intent intent = new Intent(this, ReportsLostDetailActivity.class);
        intent.putExtra("postId", pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //largeIcon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);

        //sound for notification
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(pDescription));


        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());


    }

    //redirigir a reportlostdetail
    private void showReportFoundNotification(String pId, String pTitle, String pDescription) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = new Random().nextInt(3000);

        /*Apps targeting SDK 26 or above (Android O and above) must implement notification channels
         and add its notifications to at least one of the them
         Let's add check if verssion is oreo or higher then setup notification channel*/

        /*Las aplicaciones dirigidas al SDK 26 o superior (Android O y superior) deben implementar canales de notificación
         y agrega sus notificaciones al menos a una de ellas
         Agreguemos verificar si la versión es oreo o superior y luego configurar el canal de notificación*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupPostNotificationChannel(notificationManager);
        }

        //show post detail activity using post id when notification clicked
        // muestra la actividad detallada de la publicación usando la identificación de la publicación cuando se hace clic en la notificación
        Intent intent = new Intent(this, ReportsFoundDetailActivity.class);
        intent.putExtra("postId", pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //largeIcon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);

        //sound for notification
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(pDescription));


        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());


    }

    private void showServiceNotification(String pId, String pTitle, String pDescription) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = new Random().nextInt(3000);

        /*Apps targeting SDK 26 or above (Android O and above) must implement notification channels
         and add its notifications to at least one of the them
         Let's add check if verssion is oreo or higher then setup notification channel*/

        /*Las aplicaciones dirigidas al SDK 26 o superior (Android O y superior) deben implementar canales de notificación
         y agrega sus notificaciones al menos a una de ellas
         Agreguemos verificar si la versión es oreo o superior y luego configurar el canal de notificación*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupPostNotificationChannel(notificationManager);
        }

        //show post detail activity using post id when notification clicked
        // muestra la actividad detallada de la publicación usando la identificación de la publicación cuando se hace clic en la notificación
        Intent intent = new Intent(this, ServiceDetailActivity.class);
        intent.putExtra("postId", pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //largeIcon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);

        //sound for notification
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(pDescription));


        //show notification
        notificationManager.notify(notificationID, notificationBuilder.build());


    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPostNotificationChannel(NotificationManager notificationManager) {
        //se agrego el txt en strings
        CharSequence channelName = getString(R.string.txtnewnotifiation);
        String channelDescription = "Device to device post notification";

        NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);

        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i > 0) {
            j = i;

        }
        notificationManager.notify(j, builder.build());
    }

    private void sendOAndAboveNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getONotifications(title, body, pIntent, defSoundUri, icon);

        int j = 0;
        if (i > 0) {
            j = i;

        }
        notification1.getManager().notify(j, builder.build());

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        //update user tokens
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //signed in, update token
            updateToken(s);
        }
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        ref.child(user.getUid()).setValue(token);

    }
}
