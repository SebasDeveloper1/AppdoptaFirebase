package com.canibal.appdoptafirebase;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

public class HelpActivity extends AppCompatActivity {

    ActionBar actionBar;

    //wigets
    CardView reportContent, termsandconditionscontent, aboutcontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_help);

        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.txthelp);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        //init
        reportContent = findViewById(R.id.reportcontent);
        termsandconditionscontent = findViewById(R.id.termsandconditionscontent);
        aboutcontent = findViewById(R.id.aboutcontent);

        reportContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //password change dialog with custom layout having  currentPassword, newPassword and update button
                //inflate layout for dialog
                View view = LayoutInflater.from(HelpActivity.this).inflate(R.layout.dialog_report_problem, null);
                final EditText affairEt = view.findViewById(R.id.affairEt);
                final EditText reportEt = view.findViewById(R.id.reportEt);
                Button sendreportBtn = view.findViewById(R.id.sendreportBtn);
                FloatingActionButton dialogcloseBtn = view.findViewById(R.id.dialogcloseBtn);

                final AlertDialog.Builder builder = new AlertDialog.Builder(HelpActivity.this);
                builder.setView(view); //set view to dialog

                final AlertDialog dialog = builder.create();
                dialog.show();

                sendreportBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!affairEt.getText().toString().isEmpty() && !reportEt.getText().toString().isEmpty()) {
                            // strings
                            final String email = "sebastianyasul@gmail.com";
                            final String affair = affairEt.getText().toString().trim();
                            final String report = reportEt.getText().toString().trim();

                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                            intent.putExtra(Intent.EXTRA_SUBJECT, affair);
                            intent.putExtra(Intent.EXTRA_TEXT, report);
                            //intent.setType("message/rfc822");

                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                String txtmsmreport2 = getString(R.string.txtmsmreport2);
                                Toast.makeText(HelpActivity.this, txtmsmreport2, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            String txtmsmreport = getString(R.string.txtmsmreport);
                            Toast.makeText(HelpActivity.this, txtmsmreport, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialogcloseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        termsandconditionscontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //password change dialog with custom layout having  currentPassword, newPassword and update button
                //inflate layout for dialog
                View view = LayoutInflater.from(HelpActivity.this).inflate(R.layout.dialog_terms_conditions, null);
                FloatingActionButton dialogcloseBtn = view.findViewById(R.id.dialogcloseBtn);

                final AlertDialog.Builder builder = new AlertDialog.Builder(HelpActivity.this);
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

        aboutcontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //password change dialog with custom layout having  currentPassword, newPassword and update button
                //inflate layout for dialog
                View view = LayoutInflater.from(HelpActivity.this).inflate(R.layout.dialog_about, null);
                FloatingActionButton dialogcloseBtn = view.findViewById(R.id.dialogcloseBtn);

                final AlertDialog.Builder builder = new AlertDialog.Builder(HelpActivity.this);
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

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}