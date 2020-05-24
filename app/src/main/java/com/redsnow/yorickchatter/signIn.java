package com.redsnow.yorickchatter;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

public class signIn extends AppCompatActivity {

    TextInputLayout loginLayout, passwordLayout;
    BluetoothAdapter mBluetoothAdapter;

    String login, password;
    Button signInBtn;
    private BottomAppBar appbar;

    private final String[] PERMISSIONS = new String[] {Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE };
    private int REQUEST_PERMISSION_CODE = 3;

    private CustomToastHelper toastHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.signin_layout);
        __initAnimation();

        toastHelper = new CustomToastHelper(this);

        loginLayout = findViewById(R.id.loginTextInput_SignIn);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        loginLayout.getEditText().setText(mBluetoothAdapter.getName());

        if (!areAllPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_CODE);
        }

    }

    private void __initAnimation() {

        signInBtn = findViewById(R.id.signIn);

        AnimationDrawable mAnimationDrawable_SignIn = (AnimationDrawable) signInBtn.getBackground();

        mAnimationDrawable_SignIn.setEnterFadeDuration(10);

        mAnimationDrawable_SignIn.setExitFadeDuration(1600);

        mAnimationDrawable_SignIn.start();

    }

    private boolean areAllPermissionsGranted() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void onSignInClick(View view) throws IOException, JSONException {

        toastHelper = new CustomToastHelper(signIn.this);

        loginLayout = findViewById(R.id.loginTextInput_SignIn);
        passwordLayout = findViewById(R.id.passwordTextInput_SignIn);

        login = loginLayout.getEditText().getText().toString();

        File root = new File(Environment.getExternalStorageDirectory() + "/" + login + ".json");

        JSONObject user = new JSONObject(readFromJSON(root.getAbsolutePath()));

        if (loginLayout.getEditText().getText().toString().equals(user.getString("login"))
                    && passwordLayout.getEditText().getText().toString().equals(user.get("password"))) {

            toastHelper.initInformationToast(" <-- Welcome Home --> ");
            Intent homeIntent = new Intent(this, MainActivity.class);
            startActivity(homeIntent);

        } else {
            toastHelper.initErrorToast("Wrong. Check your data");
        }
    }

    private String readFromJSON (String file_path) throws IOException {
        String json = null;

        File file = new File(file_path);

        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        StringBuilder builder = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            builder.append(line).append("\n");
            line = reader.readLine();
        }
        reader.close();

        json = builder.toString();

        return json;
    }



}
