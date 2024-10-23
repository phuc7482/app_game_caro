package com.example.megatictactoe;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.megatictactoe.GameActivity.DataRoom;
import static com.example.megatictactoe.GameActivity.RoomId;
import static com.example.megatictactoe.GameActivity.contextGameActivity;
import static com.example.megatictactoe.GameActivity.countDownTimer_GameOnline;
import static com.example.megatictactoe.GameActivity.currentUser;
import static com.example.megatictactoe.GameActivity.database;
import static com.example.megatictactoe.GameActivity.players;

public class serviceCheckAppClose extends Service {
    IBinder mBinder = new MyBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public class MyBinder extends Binder {
        serviceCheckAppClose getService() {
            return serviceCheckAppClose.this;
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try {
            players = database.getReference("room/" + RoomId + "/players");
            players.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot==null){
                        DataRoom = database.getReference("room");
                        DataRoom.child(RoomId).removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // fail to read do no thing in here
                }
            });
            database.getReference("room/" + RoomId + "/players").child(currentUser.getUid()).removeValue();

            try{
                countDownTimer_GameOnline.cancel();
            }catch (Exception e){

            }
        } catch (Exception e) {

        }
        super.onTaskRemoved(rootIntent);
    }
}
