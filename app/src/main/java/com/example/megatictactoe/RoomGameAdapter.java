package com.example.megatictactoe;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.example.megatictactoe.roomOnlineActivity.mAuth;
import static com.example.megatictactoe.roomOnlineActivity.myRef_GameRoom;

public class RoomGameAdapter extends BaseAdapter {
    ArrayList<RoomGame> roomGames = new ArrayList<>();
    Context mContext ;
    RoomGameAdapter(ArrayList<RoomGame> roomGames,Context mContext){
        this.roomGames = roomGames;
        this.mContext = mContext;
    }
    @Override
    public int getCount() {
        return roomGames.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.room_item,null);
        TextView txt_roomId = view.findViewById(R.id.txt_roomId);
        txt_roomId.setText("Room Id: " + roomGames.get(position).getRoomId());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String uId = currentUser.getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                myRef_GameRoom = database.getReference("room/"+roomGames.get(position).getRoomId()+"/players/"+uId);
                myRef_GameRoom.setValue("Guest");
                Intent intent = new Intent(mContext, GameActivity.class);
                intent.putExtra("playModeId",2);// 0 is play vs player
                intent.putExtra("RoomId",roomGames.get(position).getRoomId());
                intent.putExtra("MoveFirst",false);
                mContext.startActivity(intent);
            }
        });
        return  view;
    }
}
