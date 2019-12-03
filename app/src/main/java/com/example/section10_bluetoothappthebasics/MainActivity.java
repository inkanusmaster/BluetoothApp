package com.example.section10_bluetoothappthebasics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView devicesListView;
    TextView statusTextView;
    Button searchButton;
    ArrayList<String> btDevicesArrayList;
    ArrayAdapter<String> btDevicesArrayAdapter;

    BluetoothAdapter bluetoothAdapter; // robimy bt adapter

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // robimy receiver do przetwarzania otrzymanych danych
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // pobieramy akcję do stringa (drugi parametr intent z metodą getAction())
            System.out.println("ACTION\n" + action); // on nam wydrukuje akcje które się wydarzą, oczywiście te które daliśmy w addAction

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { // jeśli skończono wyszukiwanie to enabluj button search
//                devicesListView.setAdapter(btDevicesArrayAdapter);
                statusTextView.setText("Searching finished");
                searchButton.setEnabled(true);

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) { // onReceive jest w trakcie szukania cały czas. Jeśli znajdzie device to...
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // robimy BluetoothDevice i możemy już wyciągać jakieś info o danym urządzeniu
                assert device != null;
                String name = device.getName(); // pobieramy name
                String address = device.getAddress(); // adres
                String rssi = String.valueOf(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)); // ale rssi już inaczej. z intentu
                if (name == null) {
                    System.out.println("ADDRESS: " + address + "     RSSI: " + rssi);
                    btDevicesArrayList.add("DEVICE: "+address+"   RSSI: "+rssi);
                } else {
                    System.out.println("NAME: " + name + "     RSSI: " + rssi);
                    btDevicesArrayList.add("DEVICE: "+name+"   RSSI: "+rssi);
                }
                devicesListView.setAdapter(btDevicesArrayAdapter);
            }

        }
    };

    @SuppressLint("SetTextI18n")
    public void search(View view) {
        btDevicesArrayAdapter.clear();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported!", Toast.LENGTH_SHORT).show();
        } else if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is not enabled!", Toast.LENGTH_SHORT).show();
        } else {
            statusTextView.setText("Searching..."); // po kliknięciu w search pojawia się napis searching...
            searchButton.setEnabled(false); // ... i wyszarza button Search

            bluetoothAdapter.startDiscovery(); // odpalamy discovery, ale musimy ustawić permission w manifeście na BLUETOOTH_ADMIN, BLUETOOTH i ACCESS_COARSE_LOCATION
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1; // żądanie dostępu do coarse_location (bluetooth). Inaczej nic nie wyszuka
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        devicesListView = findViewById(R.id.devicesListView);
        statusTextView = findViewById(R.id.statusTextView);
        searchButton = findViewById(R.id.searchButton);
        btDevicesArrayList = new ArrayList<>();
        btDevicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, btDevicesArrayList);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // ustawiamy bt adapter
        IntentFilter intentFilter = new IntentFilter(); // bluetooth pluje dużo rzeczy więc robimy intentfilter żeby określić co nas interesuje. Różne akcje mamy poniżej
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); // mówi o stanie adaptera, np został włączony.wyłączony
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND); // mówi czy znaleźliśmy urządzenie
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); // rozpoczęcie szukania
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); // kończy szukanie

        registerReceiver(broadcastReceiver, intentFilter); // coś znaleźliśmy, musimy to zarejestrować. Wrzucamy tu broadcastReceiver i intentfilter
    }
}