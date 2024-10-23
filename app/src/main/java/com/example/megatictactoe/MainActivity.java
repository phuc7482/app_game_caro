package com.example.megatictactoe;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    ImageButton btnPlayVsPlay, btnExit, btnPlayOnline;
    ImageButton btnPlayVsComputer;
    private NetworkStateReceiver networkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        btnPlayVsPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("playModeId", 0);// 0 is play vs player
                startActivity(intent);
            }
        });
        btnPlayVsComputer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("playModeId", 1);// 1 is play vs computer
                startActivity(intent);
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAndRemoveTask();
            }
        });
    }

    private void initViews() {
        btnPlayVsPlay = findViewById(R.id.btn_playVsP);
        btnPlayVsComputer = findViewById(R.id.btn_playVsC);
        btnPlayOnline = findViewById(R.id.btn_playOnline);
        btnExit = findViewById(R.id.btn_exit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    @Override
    public void networkAvailable() {
        btnPlayOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, roomOnlineActivity.class);
                intent.putExtra("playModeId", 2);
                startActivity(intent);
            }
        });
    }

    @Override
    public void networkUnavailable() {
        btnPlayOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Connect Fail, Check and try again",Toast.LENGTH_SHORT).show();
            }
        });
    }
}