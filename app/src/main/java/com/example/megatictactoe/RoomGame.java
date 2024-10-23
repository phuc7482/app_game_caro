package com.example.megatictactoe;

import androidx.annotation.Nullable;

public class RoomGame {
    private String roomId;

    RoomGame(String roomId){
        this.roomId = roomId;
    }
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof RoomGame){
            RoomGame toCompare = (RoomGame) obj;
            if(toCompare.getRoomId().equals(this.roomId)  ){
                return true;
            }
            else
                return  false;
        }
        return false;
    }
}
