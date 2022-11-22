package com.getmotivation.getmotivation.miosensore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.net.Proxy;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private long lastUpdate;
    Sensor sensor;
    Integer passi;
    double s0;
    double a;
    double t;
    double s;
    double a0;
    double delta_a;
    double v;
    double kcal;
    MaterialButton dati;
    MaterialTextView nomeDati;
    String kmPercorsi;
    Intent intent;
    public static final String CUSTOM_BROADCAST = "com.getmotivation.getmotivation.miosensore_CUSTOM_BROADCAST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        passi = 0;
        s0 = 0;
        s = 0;
        a = 0;
        t = 0;
        a0 = 0;
        delta_a = 0;
        v = 0;
        kcal = 0;
        kmPercorsi = String.valueOf(0);
        //mReceiver = new MyReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(CUSTOM_BROADCAST));

        dati = findViewById(R.id.dati);
        nomeDati = findViewById(R.id.nomeDati);
        nomeDati.setText(" PASSI: ");  //così setto il primo dato da vedere in fase di creazione
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lastUpdate = System.currentTimeMillis();
        dati.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nomeDati.getText().toString() == " PASSI: ") {
                    nomeDati.setText(" KM PERCORSI: ");
                    dati.setText(kmPercorsi);
                } else {
                    if (nomeDati.getText().toString() == " KM PERCORSI: ") {
                        nomeDati.setText(" KCAL CONSUMATE: ");
                        dati.setText(String.valueOf(kcal));
                    } else {
                        nomeDati.setText(" PASSI: ");
                        dati.setText(passi.toString());
                    }
                }
            }
        });
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerazione(sensorEvent);
        }
    }

    private void getAccelerazione(SensorEvent event) {
        float[] values = event.values;

        float x = values[0];
        float y = values[1];
        float z = values[2];

        double stimaRadiceSommaInQuadratura = Math.sqrt((x * x + y * y + z * z));
        a = stimaRadiceSommaInQuadratura;
        delta_a = a - a0;
        a0 = a;
        long actualTime = event.timestamp;
        //t= (actualTime-lastUpdate)*Math.pow(10,-3);
        if (delta_a > 6) {
            lastUpdate = actualTime;
            passi++;
            s = 0.0006 * passi;   //un passo equivale a 60cm , ho moltiplicato per 10^-3 per rappresentare la distanza in chilometri
            s = Math.ceil(s * 1000) / 1000;
            //s=((s0+(t*t*a))/1000); //ho considerato il percorso svolto rettilineo ed ho usato una semplice formula di fisica classica
            //s0=s*1000;  //questo perchè all'interno della formula s0 mi serve in metri
            //v= delta_a*t*3.6;
            kmPercorsi = String.valueOf(s);
            kcal = passi * 0.5 * Math.pow(10, -3) * 65;  //ho trovato questa formula su internet, 65 è il valore di default del peso
            kcal = Math.ceil(kcal * 1000) / 1000;
            intent = new Intent(CUSTOM_BROADCAST);
            intent.putExtra("passi", passi.toString());
            intent.putExtra("distanza", kmPercorsi);
            intent.putExtra("kcal", String.valueOf(kcal));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            if (nomeDati.getText().toString() == " PASSI: ") {
                dati.setText(passi.toString());
            } else {
                if (nomeDati.getText().toString() == " KM PERCORSI: ") {
                    dati.setText(kmPercorsi);
                } else {
                    dati.setText(String.valueOf(kcal));
                }
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getAction();
            String passi1 =intent.getStringExtra("passi");
            String kcal1 =intent.getStringExtra("kcal");
            String distanza1 =intent.getStringExtra("distanza");
            Toast.makeText(MainActivity.this,passi1 +"  "+ kcal1 + "  "+distanza1 , Toast.LENGTH_LONG).show();
        }
    };
}