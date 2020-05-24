package com.redsnow.yorickchatter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import com.redsnow.yorickchatter.signIn;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class registration extends AppCompatActivity {

    StrictMode.ThreadPolicy policy;

    private static final String ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm./?";
    private static final String MAIL_SENDER = "yorickchatter.companion@gmail.com";
    private static final String MAIN_PASSWORD = "hr05wfo.";


    private final String[] PERMISSIONS = new String[] {Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE };
    private final int PERMISSION_CODE = 1;

    private CustomToastHelper toastHelper;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    String login, pswrd;

    Button regBtn, logbtn;
    TextInputLayout loginLayout, passwordlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_registration);
        __initAnimation__();

        toastHelper = new CustomToastHelper(this);

        String defaultDeviceNameAndUserLogin = mBluetoothAdapter.getName();

        EditText loginText = findViewById(R.id.loginText);
        login = mBluetoothAdapter.getName();
        loginText.setText(login);

        if (!isInternetPermissionGranted()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);
        } else  {
            toastHelper.initInformationToast("All permissions is okay, starting");
        }

        toastHelper.initWarningToast("Your login: " + defaultDeviceNameAndUserLogin);


    }

    public void __initAnimation__() {

        regBtn = findViewById(R.id.registerButton);
        logbtn = findViewById(R.id.jumpToSignInButton);

        AnimationDrawable mAnimationDrawable_SignIn = (AnimationDrawable) logbtn.getBackground();
        AnimationDrawable mAnimationDrawable_SignUp = (AnimationDrawable) regBtn.getBackground();

        mAnimationDrawable_SignIn.setEnterFadeDuration(10);
        mAnimationDrawable_SignUp.setEnterFadeDuration(10);

        mAnimationDrawable_SignUp.setExitFadeDuration(1600);
        mAnimationDrawable_SignIn.setExitFadeDuration(1600);

        mAnimationDrawable_SignUp.start();
        mAnimationDrawable_SignIn.start();

    }

    public void onJumpButtonClick(View view) {
        toastHelper = new CustomToastHelper(registration.this);
        File root = new File(Environment.getExternalStorageDirectory() + "/" + login + ".json");

        if (root.exists()) {

            Intent signIn = new Intent(this, signIn.class);
            startActivity(signIn);

        } else {
            toastHelper.initErrorToast("Nah, you don`t have an account. Maybe you`ve changed you bluetooth name?");
        }
    }


    public void onRegBtnClick (View view) throws JSONException {
        EditText email = findViewById(R.id.emailText);

        passwordlayout = findViewById(R.id.passwordTextInput);
        regBtn = findViewById(R.id.registerButton);
        logbtn = findViewById(R.id.jumpToSignInButton);

        pswrd = Objects.requireNonNull(passwordlayout.getEditText()).getText().toString();

        JSONObject user = new JSONObject();

        Intent home = new Intent(this, MainActivity.class);

        if (areAllPermissionsGranted()) {
            try {
                if (email.getText().toString().equals("")) {
                    saveToJson(login, pswrd, user);
                    startActivity(home);
                } else if (!email.getText().toString().equals("")) {
                    saveToJson(login, pswrd, user);
                    sendEmail(email.getText().toString(), login, pswrd);
                    startActivity(home);
                }
            } catch (NullPointerException | IOException ex) {
                ex.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);
        }

    }

    private void saveToJson(String login, String password, JSONObject obj) throws JSONException, IOException {

        toastHelper = new CustomToastHelper(registration.this);

        Writer output = null;
        File root = new File(Environment.getExternalStorageDirectory() + "/" + login + ".json");
        obj.put("login", login).put("password", password);
        output = new BufferedWriter(new FileWriter(root));
        output.write(obj.toString());
        output.close();
    }

    private void sendEmail(String destination, String name, String password) {
        String senderEmail = "yorickchatter.companion@gmail.com";
        String senderPassword = "hr05wfo.";
        List<String> toEmaiList = Arrays.asList(destination.split("\\s*,\\s*"));

        String TO = destination.trim();
        String subject = "Yorick Chatter App Account Information";
        String message = String.format("Hello, %s! You have been successfully created an account in YorickChatter App. \n" +
                "Here is your account information: \n Login - %s\n Password - %s\n Have a good day!", name, name, password);

        new SendMailTask(registration.this).execute(senderEmail, senderPassword, toEmaiList, subject, message);

    }

    public void onGeneratePasswordClick (View view) {
        passwordlayout = findViewById(R.id.passwordTextInput);
        pswrd = passwordGenerator(8);
        passwordlayout.getEditText().setText(pswrd);
    }

    private String passwordGenerator(final int passwordSize) {

        final Random random = new Random();
        final StringBuilder stringBuilder = new StringBuilder(passwordSize);

        for (int i = 0; i < passwordSize; ++i) {
            stringBuilder.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return stringBuilder.toString();
    }

    private boolean isInternetPermissionGranted () {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean areAllPermissionsGranted() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
