package com.example.megatictactoe;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class roomOnlineActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener{

    static FirebaseAuth mAuth;
    ImageButton btn_createNewRoom;
    ListView list_room;
    static DatabaseReference myRef_GameRoom = null;
    ArrayList<RoomGame> arrayListRoom = new ArrayList<>();
    RoomGameAdapter adapter;
    String RoomId;
    static Context contextRoomOnline;
    static ArrayList<RoomGame> arrayListRoom2Play = new ArrayList<>();
    DatabaseReference List_RoomFull;
    FirebaseDatabase database;

    // sate network
    private NetworkStateReceiver networkStateReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_online);
        initViews();
        // network state
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        database = FirebaseDatabase.getInstance();
        adapter = new RoomGameAdapter(arrayListRoom, this);
        list_room.setAdapter(adapter);
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                mAuth = FirebaseAuth.getInstance();
                signInAnonymously();
            }
        };
        thread.start();
        btn_createNewRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // get user id;
                    btn_createNewRoom.setVisibility(View.GONE);
                    String uId = currentUser.getUid();

                    RoomId = randomRoomId();
                    myRef_GameRoom = database.getReference("room/" + RoomId + "/players/" + uId);
                    myRef_GameRoom.setValue("Creat Room");
                    Intent intent = new Intent(roomOnlineActivity.this, GameActivity.class);
                    intent.putExtra("playModeId", 2);// 0 is play vs player
                    intent.putExtra("RoomId", RoomId);
                    intent.putExtra("MoveFirst", true);
                    startActivity(intent);
                }
            }
        });

        addEventRoomdataChange();
        EventRoomFullChange();
        contextRoomOnline = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void initViews() {
        btn_createNewRoom = findViewById(R.id.btn_createNewRoom);
        list_room = findViewById(R.id.list_room);
    }

    private void signInAnonymously() {
        // [START signin_anonymously]
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uId = user.getUid();

                        } else {
                            Toast.makeText(roomOnlineActivity.this, "connect erro .",
                                    Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    }
                });
        // [END signin_anonymously]
    }

    private void signOut() {
        mAuth.signOut();
    }

    void addEventRoomdataChange() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef_GameRoom = database.getReference("room");
        myRef_GameRoom.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> rooms = snapshot.getChildren();
                for (DataSnapshot room : rooms) {
                    if(!arrayListRoom2Play.contains(new RoomGame(room.getKey()))){
                        if(!arrayListRoom.contains(new RoomGame(room.getKey()))){
                            arrayListRoom.add(new RoomGame(room.getKey()));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                if(snapshot.getChildrenCount()==0){
                    addEventRoomdataChange();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // failed to read
            }
        });
    }

    final String data = "0123456789";

    String randomRoomId() {
        Random random = new Random();
        String rs = "";
        for (int i = 0; i < 5; i++) {
            rs += (data.charAt(random.nextInt(10)));
        }
        if (arrayListRoom.contains(rs))
            rs = randomRoomId();
        return rs;
    }

    @Override
    protected void onRestart() {
        btn_createNewRoom.setVisibility(View.VISIBLE);
        super.onRestart();
    }


    void EventRoomFullChange() {
        List_RoomFull = database.getReference("roomFull");
        List_RoomFull.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> roomsFull = snapshot.getChildren();
                arrayListRoom2Play.clear();
                for (DataSnapshot room_full : roomsFull) {
                    arrayListRoom2Play.add(new RoomGame(room_full.getKey()));
                    arrayListRoom.remove(new RoomGame(room_full.getKey()));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void networkAvailable() {
        // do nothing
    }

    @Override
    public void networkUnavailable() {
        Toast.makeText(roomOnlineActivity.this,"Conntect Internet Fail",Toast.LENGTH_LONG);
        Intent intent = new Intent(roomOnlineActivity.this,MainActivity.class);
        startActivity(intent);
    }
}