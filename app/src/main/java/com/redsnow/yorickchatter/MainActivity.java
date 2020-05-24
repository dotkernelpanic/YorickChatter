package com.redsnow.yorickchatter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final int     MESSAGE_STATE_CHANGE    = 1;
    public static final int     MESSAGE_READ            = 2;
    public static final int     MESSAGE_WRITE           = 3;
    public static final int     MESSAGE_DEVICE_OBJECT   = 4;
    public static final int     MESSAGE_TOAST           = 5;
    public static final String  DEVICE_OBJECT           = "device_name";

    private Dialog dialog;


    private TextInputLayout TIL;

    private BluetoothDevice connectingDevice;

    private TextView status; private ImageButton btnSend; private EditText editText;

    private ListView list;

    private ArrayAdapter<String>    chatAdapter;
    private ArrayList<String>       chatMessages;

    private ChatController chatController;

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private final int       PERMISSION_CODE = 2;
    private final String[]  PERMISSIONS     = new String[] {Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION };

    private BottomNavigationView botNav;
    private BluetoothAdapter     mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private int REQUEST_ENABLE = 1;

    private CustomToastHelper toastHelper;

    private ListView paired, nonPaired;
    private  int tap_counter = 0;

    private ArrayAdapter<String> deviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.home_activity);

        toastHelper = new CustomToastHelper(this);

        botNav = findViewById(R.id.bottom_nav);
        status = findViewById(R.id.status);
        TIL = findViewById(R.id.textInputLayout);
        btnSend = findViewById(R.id.sendBtn);
        editText = findViewById(R.id.editText);
        list = findViewById(R.id.list);


        botNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.scanMenu:
                        scan();
                        return true;
                    case R.id.bluetoothOFFMenu:
                        if (mBluetoothAdapter.isEnabled()) {
                            mBluetoothAdapter.disable();
                            toastHelper.initInformationToast("You have been successfully powered off bluetooth");
                        } else if (!mBluetoothAdapter.isEnabled()) {
                            mBluetoothAdapter.enable();
                            toastHelper.initInformationToast("Bluetooth is working now");
                        }
                        return true;
                }
                return false;
            }
        });


        if (arePermissionsGranted()) {
            if (isBTSupported()) {
                Intent blueToothEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(blueToothEnableIntent, REQUEST_ENABLE);
            }
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (Objects.requireNonNull(TIL.getEditText()).getText().toString().equals("")) {
                        toastHelper.initErrorToast("You can not send empty message");
                    } else {
                        sendMessage(TIL.getEditText().getText().toString());
                        TIL.getEditText().setText("");
                    }
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            }
        });

        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        list.setAdapter(chatAdapter);

    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            toastHelper = new CustomToastHelper(MainActivity.this);
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatController.STATE_CONNECTED:
                            setStatus("Successfully connected to: " + connectingDevice.getName());
                            break;
                        case ChatController.STATE_CONNECTING:
                            setStatus("Connecting");
                            break;
                        case ChatController.STATE_LISTEN:
                        case  ChatController.STATE_NONE:
                            setStatus("Not Connected");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuffer = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuffer);
                    chatMessages.add("You: " + writeMessage);
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;

                    String readMessage = new String(readBuffer, 0, msg.arg1);
                    chatMessages.add(connectingDevice.getName() + ": " + readMessage);
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    toastHelper.initWarningToast("Connected to " + connectingDevice.getName());
                    break;
                case MESSAGE_TOAST:
                    toastHelper.initWarningToast(msg.getData().getString("toast"));
                    break;
            }
            return false;
        }
    });


    private void scan() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();

        toastHelper = new CustomToastHelper(MainActivity.this);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_scan_bluetooth);
        dialog.setTitle(getString(R.string.dialogTitle));

        nonPaired = dialog.findViewById(R.id.discoveredList);
        paired = dialog.findViewById(R.id.pairedDevicesList);

        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        paired.setAdapter(pairedDevicesAdapter);
        nonPaired.setAdapter(deviceAdapter);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.suchEmpty));
        }

        paired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothAdapter.cancelDiscovery();
                try {
                    String info = ((TextView) view).getText().toString();
                    String address = info.substring(info.length() - 17);

                    connectToDevice(address);
                    dialog.dismiss();
                } catch (IllegalArgumentException e) {
                    tap_counter++;
                    e.printStackTrace();
                    if (tap_counter <= 3)
                        toastHelper.initInformationToast("Im getting angry");
                    else
                        toastHelper.initErrorToast("STOP TAPING, JEZ, ITS NOT WORKING");
                }
            }
        });

        nonPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothAdapter.cancelDiscovery();
                try {
                    String info = ((TextView) view).getText().toString();
                    String address = info.substring(info.length() - 17);

                    connectToDevice(address);
                    toastHelper.initWarningToast("Connecting to: " + address);
                    dialog.dismiss();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    if (tap_counter <= 3)
                        toastHelper.initInformationToast("Im getting angry");
                    else
                        toastHelper.initErrorToast("STOP TAPING, JEZ, ITS NOT WORKING");
                }
            }
        });

        dialog.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                assert device != null;
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    deviceAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }  else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (deviceAdapter.getCount() == 0) {
                    deviceAdapter.add(getString(R.string.suchEmpty));
                }
            }
        }
    };


    private boolean isBTSupported() {
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                return true;
            }
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        toastHelper = new CustomToastHelper(MainActivity.this);
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    chatController = new ChatController(this, handler);
                } else {
                    toastHelper.initErrorToast("Bluetooth not working");
                    finish();
                }
        }
    }

    private void sendMessage(String message) {
        toastHelper = new CustomToastHelper(MainActivity.this);
        if (chatController.getState() != ChatController.STATE_CONNECTED) {
            toastHelper.initWarningToast("Connection lost");
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatController.write(send);
        }
    }

    private boolean arePermissionsGranted() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void setStatus(String s) {
        status.setText(s);
    }

    private void connectToDevice(String address) {
        mBluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        chatController.connect(device);
    }


    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            chatController = new ChatController(this, handler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (chatController != null) {
            if (chatController.getState() == ChatController.STATE_NONE) {
                chatController.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (chatController != null) {
            chatController.stop();
        }
    }

}
