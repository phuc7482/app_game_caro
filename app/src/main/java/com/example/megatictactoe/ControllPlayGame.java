package com.example.megatictactoe;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.megatictactoe.GameActivity.ModePlay;
import static com.example.megatictactoe.GameActivity.MoveFirst;
import static com.example.megatictactoe.GameActivity.countDownTimer_GameOnline;
import static com.example.megatictactoe.GameActivity.idPlayer;
import static com.example.megatictactoe.GameActivity.idPlayerWin;
import static com.example.megatictactoe.GameActivity.txt_turnPlayer;
import static com.example.megatictactoe.GameActivity.zoomManager;
import static com.example.megatictactoe.roomOnlineActivity.mAuth;

public class ControllPlayGame {
    // gamebroad have 30 row and 30 colum
    private ArrayList<CellIndex> cellPlay1 = new ArrayList<>();
    private ArrayList<CellIndex> cellPlay2 = new ArrayList<>(); // if model play is play vs pc cell play2 is mean cell computer move
    private ArrayList<CellIndex> cellEmty = new ArrayList<>();// this is all cell but is emty all
    private TableLayout BoardGameTableLayout;
    private Context mContext;
    ArrayList<CellIndex> WinningLine = new ArrayList<>();
    Thread threadPlayMove;
    DatabaseReference gameBoard;
    String RoomId;
    static int MoveCount = 0;
    // blockUI;


    ControllPlayGame(ArrayList<CellIndex> cellPlayer1, ArrayList<CellIndex> cellPlayer2, ArrayList<CellIndex> cellEmty, TableLayout BoardGameTableLayout, Context Context, String RoomId) {
        this.cellPlay1 = cellPlayer1;
        this.cellPlay2 = cellPlayer2;
        this.cellEmty = cellEmty;
        this.BoardGameTableLayout = BoardGameTableLayout;
        this.mContext = Context;
        this.RoomId = RoomId;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        gameBoard = database.getReference("room/" + RoomId + "/Game/");
        addEventGameBoardDataChange();
    }

    public void PlayMove(CellIndex indexCellMove) {
        threadPlayMove = new Thread() {

            @Override
            public void run() {
                super.run();
                if (cellEmty.contains(indexCellMove)) {
                    TableRow row = (TableRow) BoardGameTableLayout.getChildAt(indexCellMove.getIndexRow());
                    RelativeLayout relativeLayout = (RelativeLayout) row.getChildAt(indexCellMove.getIndexColumn());
                    // watch PvsCActivity line 59 to know why use this line to get Image view and set background
                    ImageView imageView = (ImageView) relativeLayout.getChildAt(0);
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (idPlayer == 1) {
                                if(ModePlay==2){
                                    if (MoveFirst || MoveCount>0){
                                        imageView.setBackgroundResource(R.drawable.x_black);
                                        cellPlay1.add(indexCellMove);
                                        cellEmty.remove(indexCellMove);
                                        // need add indexCellMove to fireBase if modePlay is online
                                        WinningLine.clear();
                                        if (checkWinner(indexCellMove) == 1) {
                                            setImageWinline();
                                            blockBoardGame();
                                            idPlayerWin = 1;
                                            if (ModePlay == 1) {
                                                Toast.makeText(mContext, "Player Win", Toast.LENGTH_LONG).show();
                                            } else if (ModePlay == 0) {
                                                Toast.makeText(mContext, "Player 1 Win", Toast.LENGTH_LONG).show();
                                            } else if (ModePlay == 2) {
                                                countDownTimer_GameOnline.cancel();
                                                Toast.makeText(mContext, "you Win", Toast.LENGTH_LONG).show();
                                                // set to new game
                                                MoveCount = 0;
                                                MoveFirst = false;
                                            }
                                        } else if (checkWinner(indexCellMove) == -1) {
                                            countDownTimer_GameOnline.cancel();
                                            Toast.makeText(mContext, "Tie", Toast.LENGTH_LONG).show();
                                        }else {
                                            countDownTimer_GameOnline.cancel();
                                            countDownTimer_GameOnline.start();
                                        }
                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        String row = "";
                                        String colum = "";
                                        if (indexCellMove.getIndexRow() < 10)
                                            row = "0" + indexCellMove.getIndexRow();
                                        else
                                            row = String.valueOf(indexCellMove.getIndexRow());
                                        if (indexCellMove.getIndexColumn() < 10)
                                            colum = "0" + indexCellMove.getIndexColumn();
                                        else
                                            colum = String.valueOf(indexCellMove.getIndexColumn());


                                        String cellIndex = row + colum + currentUser.getUid();
                                        gameBoard = database.getReference("room/" + RoomId + "/Game/" + cellIndex);
                                        gameBoard.setValue(currentUser.getUid());
                                        txt_turnPlayer.setText("Competitor Turn (O)");
                                        idPlayer = -1;
                                        MoveCount++;
                                        if (checkWinner(indexCellMove) == 1) {

                                            return;
                                        } else if (checkWinner(indexCellMove) == -1) {

                                            return;
                                        }

                                    }
                                }else {
                                    imageView.setBackgroundResource(R.drawable.x_black);
                                    cellPlay1.add(indexCellMove);
                                    cellEmty.remove(indexCellMove);

                                    WinningLine.clear();
                                    if (checkWinner(indexCellMove) == 1) {
                                        setImageWinline();
                                        blockBoardGame();
                                        idPlayerWin = 1;
                                        if (ModePlay == 1) {
                                            Toast.makeText(mContext, "Player Win", Toast.LENGTH_LONG).show();
                                        } else if (ModePlay == 0) {
                                            Toast.makeText(mContext, "Player 1 Win", Toast.LENGTH_LONG).show();
                                        } else if (ModePlay == 2) {
                                            countDownTimer_GameOnline.cancel();
                                            Toast.makeText(mContext, "you Win", Toast.LENGTH_LONG).show();
                                            MoveCount = 0;
                                            MoveFirst = false;
                                        }
                                    } else if (checkWinner(indexCellMove) == -1) {
                                        Toast.makeText(mContext, "Tie", Toast.LENGTH_LONG).show();
                                    } else {
                                        idPlayer = 0;
                                        if (ModePlay == 1) {
                                            ComputerMove();
                                        }
                                    }
                                    MoveCount++;
                                }


                            } else if(idPlayer==0){
                                imageView.setBackgroundResource(R.drawable.o_red);
                                cellPlay2.add(indexCellMove);
                                cellEmty.remove(indexCellMove);
                                // need add indexCellMove to fireBase if modePlay is online
                                WinningLine.clear();
                                if (checkWinner(indexCellMove) == 0) {
                                    setImageWinline();
                                    blockBoardGame();
                                    idPlayerWin = 0;

                                    if (ModePlay == 1) {
                                        Toast.makeText(mContext, "Computer Win", Toast.LENGTH_LONG).show();
                                    } else if (ModePlay == 0) {
                                        Toast.makeText(mContext, "Player 2 Win", Toast.LENGTH_LONG).show();
                                    } else if (ModePlay == 2) {
                                        countDownTimer_GameOnline.cancel();
                                        Toast.makeText(mContext, "You Lose", Toast.LENGTH_LONG).show();
                                        MoveCount = 0;
                                        MoveFirst = true;
                                        return;
                                    }

                                } else if (checkWinner(indexCellMove) == -1) {
                                    Toast.makeText(mContext, "Tie", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if(ModePlay==2){
                                    txt_turnPlayer.setText("Your Turn (X)");
                                    countDownTimer_GameOnline.cancel();
                                    countDownTimer_GameOnline.start();
                                }
                                idPlayer = 1;// turn player1  move
                                MoveCount++;
                            }

                        }
                    });
                }
            }
        };
        threadPlayMove.start();
    }

    // listen value of database firebase change(Game data change)
    void addEventGameBoardDataChange() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        gameBoard = database.getReference("room/" + RoomId + "/Game/");
        gameBoard.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> GameData = snapshot.getChildren();
                for (DataSnapshot cell : GameData) {
                    String CellIndex_Value = cell.getKey();

                    int row = Integer.parseInt(CellIndex_Value.substring(0, 2));
                    int column = Integer.parseInt(CellIndex_Value.substring(2, 4));
                    String uId = CellIndex_Value.substring(4);

                    CellIndex cellCheck = new CellIndex(row, column);
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (cellEmty.contains(cellCheck) && (!currentUser.getUid().equals(uId))) {
                        idPlayer = 0;
                        MoveCount++;
                        PlayMove(cellCheck);
                        countDownTimer_GameOnline.cancel();
                        countDownTimer_GameOnline.start();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // fail to read data
            }
        });
    }


    private void setImageWinline() {
        int currentCellSize = zoomManager.GetCurrentCellSize();
        // need to * with cell size
        int widthImage = 1;
        int heightImage = 1;

        // index to margin (need * with cell size to)
        int top = -1;
        int left = -1;
        // image resoure id
        int imageId = -1;
        // horizontal
        if (WinningLine.get(0).getIndexRow() == WinningLine.get(4).getIndexRow()) {
            widthImage = 5;
            top = WinningLine.get(0).getIndexRow();
            left = WinningLine.get(0).getIndexColumn();
            imageId = R.drawable.win_line_hoziontal;

        }
        // vertical
        else if (WinningLine.get(0).getIndexColumn() == WinningLine.get(4).getIndexColumn()) {
            heightImage = 5;
            top = WinningLine.get(0).getIndexRow();
            left = WinningLine.get(0).getIndexColumn();
            imageId = R.drawable.win_line_vertical;
        }
        // Main cross
        else if (WinningLine.get(0).getIndexColumn() < WinningLine.get(4).getIndexColumn()) {
            heightImage = 5;
            widthImage = 5;
            top = WinningLine.get(0).getIndexRow();
            left = WinningLine.get(0).getIndexColumn();
            imageId = R.drawable.win_line_main_cross;
        } else {
            heightImage = 5;
            widthImage = 5;
            top = WinningLine.get(0).getIndexRow();
            left = WinningLine.get(4).getIndexColumn();
            imageId = R.drawable.win_line_sub_cross;
        }
        // sub cross
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(new TableRow.LayoutParams(currentCellSize * widthImage, currentCellSize * heightImage));
        layoutParams.topMargin = top * currentCellSize;
        layoutParams.leftMargin = left * currentCellSize;
        ImageView imageView = ((GameActivity) mContext).findViewById(R.id.winningLine);
        imageView.setBackgroundResource(imageId);
        imageView.setLayoutParams(layoutParams);

    }


    public int checkWinHorizontal(CellIndex lastCellIndexMove) {
        // row and colum last move
        int row = lastCellIndexMove.getIndexRow();
        int column = lastCellIndexMove.getIndexColumn();
        ArrayList<CellIndex> ArrayCellCheck = new ArrayList<>();

        if (idPlayer != 1) {
            ArrayCellCheck = cellPlay2;
        } else {
            ArrayCellCheck = cellPlay1;
        }
        // case 1;
        if (ArrayCellCheck.contains(new CellIndex(row, column - 1)) &&
                ArrayCellCheck.contains(new CellIndex(row, column - 2)) &&
                ArrayCellCheck.contains(new CellIndex(row, column - 3)) &&
                ArrayCellCheck.contains(new CellIndex(row, column - 4))) {


            WinningLine.add(new CellIndex(row, column - 4));
            WinningLine.add(new CellIndex(row, column - 3));
            WinningLine.add(new CellIndex(row, column - 2));
            WinningLine.add(new CellIndex(row, column - 1));
            WinningLine.add(lastCellIndexMove);

            return idPlayer;
        }
        // case 2:
        else if (
                ArrayCellCheck.contains(new CellIndex(row, column + 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column + 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column + 3)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column + 4))) {
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row, column + 1));
            WinningLine.add(new CellIndex(row, column + 2));
            WinningLine.add(new CellIndex(row, column + 3));
            WinningLine.add(new CellIndex(row, column + 4));
            return idPlayer;
        }
        // case 3:
        else if (
                ArrayCellCheck.contains(new CellIndex(row, column - 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column + 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column + 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column + 3))) {

            WinningLine.add(new CellIndex(row, column - 1));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row, column + 1));
            WinningLine.add(new CellIndex(row, column + 2));
            WinningLine.add(new CellIndex(row, column + 3));
            return idPlayer;
        }
        // case 4:
        else if (
                ArrayCellCheck.contains(new CellIndex(row, column - 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column - 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column + 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column + 2))) {
            WinningLine.add(new CellIndex(row, column - 2));
            WinningLine.add(new CellIndex(row, column - 1));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row, column + 1));
            WinningLine.add(new CellIndex(row, column + 2));
            return idPlayer;
        }
        // case 5
        else if (
                ArrayCellCheck.contains(new CellIndex(row, column - 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column - 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column - 3)) &&
                        ArrayCellCheck.contains(new CellIndex(row, column + 1))) {
            WinningLine.add(new CellIndex(row, column - 3));
            WinningLine.add(new CellIndex(row, column - 2));
            WinningLine.add(new CellIndex(row, column - 1));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row, column + 1));
            return idPlayer;
        }
        if (cellEmty.size() == 0) {
            return -1;
        }

        return -999;
    }

    public int checkWinVertical(CellIndex lastCellIndexMove) {
        // row and colum last move
        int row = lastCellIndexMove.getIndexRow();
        int column = lastCellIndexMove.getIndexColumn();
        ArrayList<CellIndex> ArrayCellCheck = new ArrayList<>();
        if (idPlayer != 1) {
            ArrayCellCheck = cellPlay2;
        } else {
            ArrayCellCheck = cellPlay1;
        }
        // case 1;
        if (ArrayCellCheck.contains(new CellIndex(row - 1, column)) &&
                ArrayCellCheck.contains(new CellIndex(row - 2, column)) &&
                ArrayCellCheck.contains(new CellIndex(row - 3, column)) &&
                ArrayCellCheck.contains(new CellIndex(row - 4, column))) {
            // use for set image wining line
            WinningLine.add(new CellIndex(row - 4, column));
            WinningLine.add(new CellIndex(row - 3, column));
            WinningLine.add(new CellIndex(row - 2, column));
            WinningLine.add(new CellIndex(row - 1, column));
            WinningLine.add(lastCellIndexMove);
            return idPlayer;
        }
        // case 2:
        else if (
                ArrayCellCheck.contains(new CellIndex(row + 1, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 2, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 3, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 4, column))) {
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column));
            WinningLine.add(new CellIndex(row + 2, column));
            WinningLine.add(new CellIndex(row + 3, column));
            WinningLine.add(new CellIndex(row + 4, column));
            return idPlayer;
        }
        // case 3:
        else if (
                ArrayCellCheck.contains(new CellIndex(row - 1, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 1, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 2, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 3, column))) {
            WinningLine.add(new CellIndex(row - 1, column));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column));
            WinningLine.add(new CellIndex(row + 2, column));
            WinningLine.add(new CellIndex(row + 3, column));
            return idPlayer;
        }
        // case 4:
        else if (
                ArrayCellCheck.contains(new CellIndex(row - 1, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row - 2, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 1, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 2, column))) {
            WinningLine.add(new CellIndex(row - 2, column));
            WinningLine.add(new CellIndex(row - 1, column));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column));
            WinningLine.add(new CellIndex(row + 2, column));
            return idPlayer;
        }
        // case 5
        else if (
                ArrayCellCheck.contains(new CellIndex(row - 1, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row - 2, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row - 3, column)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 1, column))) {

            WinningLine.add(new CellIndex(row - 3, column));
            WinningLine.add(new CellIndex(row - 2, column));
            WinningLine.add(new CellIndex(row - 1, column));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column));
            return idPlayer;
        }
        if (cellEmty.size() == 0) {
            return -1;
        }

        return -999;
    }

    public int checkWinMainCross(CellIndex lastCellIndexMove) {
        // row and colum last move
        int row = lastCellIndexMove.getIndexRow();
        int column = lastCellIndexMove.getIndexColumn();
        ArrayList<CellIndex> ArrayCellCheck = new ArrayList<>();
        if (idPlayer != 1) {
            ArrayCellCheck = cellPlay2;
        } else {
            ArrayCellCheck = cellPlay1;
        }
        // case 1;
        if (ArrayCellCheck.contains(new CellIndex(row - 1, column - 1)) &&
                ArrayCellCheck.contains(new CellIndex(row - 2, column - 2)) &&
                ArrayCellCheck.contains(new CellIndex(row - 3, column - 3)) &&
                ArrayCellCheck.contains(new CellIndex(row - 4, column - 4))) {
            WinningLine.add(new CellIndex(row - 4, column - 4));
            WinningLine.add(new CellIndex(row - 3, column - 3));
            WinningLine.add(new CellIndex(row - 2, column - 2));
            WinningLine.add(new CellIndex(row - 1, column - 1));
            WinningLine.add(lastCellIndexMove);


            return idPlayer;
        }
        // case 2:
        else if (
                ArrayCellCheck.contains(new CellIndex(row + 1, column + 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 2, column + 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 3, column + 3)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 4, column + 4))) {
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column + 1));
            WinningLine.add(new CellIndex(row + 2, column + 2));
            WinningLine.add(new CellIndex(row + 3, column + 3));
            WinningLine.add(new CellIndex(row + 4, column + 4));


            return idPlayer;
        }
        // case 3:
        else if (
                ArrayCellCheck.contains(new CellIndex(row - 1, column - 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 1, column + 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 2, column + 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 3, column + 3))) {

            WinningLine.add(new CellIndex(row - 1, column - 1));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column + 1));
            WinningLine.add(new CellIndex(row + 2, column + 2));
            WinningLine.add(new CellIndex(row + 3, column + 3));

            return idPlayer;
        }
        // case 4:
        else if (
                ArrayCellCheck.contains(new CellIndex(row - 1, column - 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row - 2, column - 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 1, column + 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 2, column + 2))) {
            WinningLine.add(new CellIndex(row - 2, column - 2));
            WinningLine.add(new CellIndex(row - 1, column - 1));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column + 1));
            WinningLine.add(new CellIndex(row + 2, column + 2));
            return idPlayer;
        }
        // case 5
        else if (
                ArrayCellCheck.contains(new CellIndex(row - 1, column - 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row - 2, column - 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row - 3, column - 3)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 1, column + 1))) {
            WinningLine.add(new CellIndex(row - 2, column - 2));
            WinningLine.add(new CellIndex(row - 3, column - 3));
            WinningLine.add(new CellIndex(row - 1, column - 1));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column + 1));
            return idPlayer;
        }

        if (cellEmty.size() == 0) {
            return -1;
        }

        return -999;
    }

    public int checkWinSubCross(CellIndex lastCellIndexMove) {
        // row and colum last move
        int row = lastCellIndexMove.getIndexRow();
        int column = lastCellIndexMove.getIndexColumn();
        ArrayList<CellIndex> ArrayCellCheck = new ArrayList<>();

        if (idPlayer != 1) {
            ArrayCellCheck = cellPlay2;
        } else {
            ArrayCellCheck = cellPlay1;
        }
        // case 1;
        if (ArrayCellCheck.contains(new CellIndex(row - 1, column + 1)) &&
                ArrayCellCheck.contains(new CellIndex(row - 2, column + 2)) &&
                ArrayCellCheck.contains(new CellIndex(row - 3, column + 3)) &&
                ArrayCellCheck.contains(new CellIndex(row - 4, column + 4))) {

            WinningLine.add(new CellIndex(row - 4, column + 4));
            WinningLine.add(new CellIndex(row - 3, column + 3));
            WinningLine.add(new CellIndex(row - 2, column + 2));
            WinningLine.add(new CellIndex(row - 1, column + 1));

            WinningLine.add(lastCellIndexMove);

            return idPlayer;
        }
        // case 2:
        else if (
                ArrayCellCheck.contains(new CellIndex(row + 1, column - 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 2, column - 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 3, column - 3)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 4, column - 4))) {

            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column - 1));
            WinningLine.add(new CellIndex(row + 2, column - 2));
            WinningLine.add(new CellIndex(row + 3, column - 3));
            WinningLine.add(new CellIndex(row + 4, column - 4));
            return idPlayer;
        }
        // case 3:
        else if (
                ArrayCellCheck.contains(new CellIndex(row - 1, column + 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 1, column - 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 2, column - 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 3, column - 3))) {
            WinningLine.add(new CellIndex(row - 1, column + 1));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column - 1));
            WinningLine.add(new CellIndex(row + 2, column - 2));
            WinningLine.add(new CellIndex(row + 3, column - 3));
            return idPlayer;
        }
        // case 4:
        else if (
                ArrayCellCheck.contains(new CellIndex(row - 1, column + 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row - 2, column + 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 1, column - 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 2, column - 2))) {
            WinningLine.add(new CellIndex(row - 2, column + 2));
            WinningLine.add(new CellIndex(row - 1, column + 1));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column - 1));
            WinningLine.add(new CellIndex(row + 2, column - 2));
            return idPlayer;
        }
        // case 5
        else if (
                ArrayCellCheck.contains(new CellIndex(row - 1, column + 1)) &&
                        ArrayCellCheck.contains(new CellIndex(row - 2, column + 2)) &&
                        ArrayCellCheck.contains(new CellIndex(row - 3, column + 3)) &&
                        ArrayCellCheck.contains(new CellIndex(row + 1, column - 1))) {
            WinningLine.add(new CellIndex(row - 2, column + 2));
            WinningLine.add(new CellIndex(row - 3, column + 3));
            WinningLine.add(new CellIndex(row - 1, column + 1));
            WinningLine.add(lastCellIndexMove);
            WinningLine.add(new CellIndex(row + 1, column - 1));
            return idPlayer;
        }
        if (cellEmty.size() == 0) {
            return -1;
        }

        return -999;
    }

    int checkWinner(CellIndex indexCellMove) {
        if (checkWinHorizontal(indexCellMove) != -999) {
            return checkWinHorizontal(indexCellMove);
        } else if (checkWinVertical(indexCellMove) != -999) {
            return checkWinVertical(indexCellMove);
        } else if (checkWinMainCross(indexCellMove) != -999) {
            return checkWinMainCross(indexCellMove);
        } else if (checkWinSubCross(indexCellMove) != -999) {
            return checkWinSubCross(indexCellMove);
        }
        return -999;
    }

    public void ComputerMove() {
        // AI to make its turn

        CellIndex cellMove = null;
        long MaxScore = -99999;
        if (cellEmty.size() == 900) {
            cellMove = new CellIndex(15, 15);
        } else {
            // find cell have max score
            for (int i = 0; i < cellEmty.size(); i++) {
                if (!ignore(cellEmty.get(i))) {
                    long ATK_Score = ATK_Horizontal(cellEmty.get(i)) + ATK_Vertical(cellEmty.get(i)) + ATK_MainCross(cellEmty.get(i)) + ATK_SubCross(cellEmty.get(i));
                    long DEF_Score = DEF_Horizontal(cellEmty.get(i)) + DEF_Vertical(cellEmty.get(i)) + DEF_MainCross(cellEmty.get(i)) + DEF_SubCross(cellEmty.get(i));
                    long tempScore = Math.max(ATK_Score, DEF_Score);
                    if (MaxScore < tempScore) {
                        MaxScore = tempScore;
                        cellMove = cellEmty.get(i);
                    }
                }
            }
        }
        if (cellMove != null) {
            PlayMove(cellMove);
        }

    }


    // check cell need to ignore or need check

    // checkAlphaBetaHorizontal
    // computer level range 2-5
    boolean checkAlphaBetaHorizontal(CellIndex indexCellCheck, int ComputerLevel) {

        // 30 is default size board
        // check  right;
        if (indexCellCheck.getIndexColumn() + 5 < 30) {
            for (int i = 1; i < ComputerLevel; i++) {
                if (cellPlay1.contains(new CellIndex(indexCellCheck.getIndexRow(), indexCellCheck.getIndexColumn() + i)) ||
                        cellPlay2.contains(new CellIndex(indexCellCheck.getIndexRow(), indexCellCheck.getIndexColumn() + i))) {
                    // if meet cell not emty return false is mean this cell dont need to ignore
                    return false;
                }
            }
        }

        // check  left;
        if (indexCellCheck.getIndexColumn() >= 4) {
            for (int i = 1; i < ComputerLevel; i++) {
                if (cellPlay1.contains(new CellIndex(indexCellCheck.getIndexRow(), indexCellCheck.getIndexColumn() - i)) ||
                        cellPlay2.contains(new CellIndex(indexCellCheck.getIndexRow(), indexCellCheck.getIndexColumn() - i))) {
                    // if meet cell not emty return false is mean this cell dont need to ignore
                    return false;
                }
            }
        }
        // if all cell emty -> need to ignore
        return true;
    }

    boolean checkAlphaBetaVertical(CellIndex indexCellCheck, int ComputerLevel) {

        // 30 is default size board
        // check  under;
        if (indexCellCheck.getIndexRow() + 5 < 30) {
            for (int i = 1; i < ComputerLevel; i++) {
                if (cellPlay1.contains(new CellIndex(indexCellCheck.getIndexRow() + i, indexCellCheck.getIndexColumn())) ||
                        cellPlay2.contains(new CellIndex(indexCellCheck.getIndexRow() + i, indexCellCheck.getIndexColumn()))) {
                    // if meet cell not emty return false is mean this cell dont need to ignore
                    return false;
                }
            }
        }
        // check  up;
        if (indexCellCheck.getIndexRow() >= 4) {
            for (int i = 1; i < ComputerLevel; i++) {
                if (cellPlay1.contains(new CellIndex(indexCellCheck.getIndexRow() - i, indexCellCheck.getIndexColumn())) ||
                        cellPlay2.contains(new CellIndex(indexCellCheck.getIndexRow() - i, indexCellCheck.getIndexColumn()))) {
                    // if meet cell not emty return false is mean this cell dont need to ignore
                    return false;
                }
            }
        }


        // if all cell emty -> need to ignore
        return true;
    }

    boolean checkAlphaBetaMainCross(CellIndex indexCellCheck, int ComputerLevel) {

        // 30 is default size board
        // check  up to down;
        if (indexCellCheck.getIndexRow() + 5 < 30 && indexCellCheck.getIndexColumn() + 5 < 30) {
            for (int i = 1; i < ComputerLevel; i++) {
                if (cellPlay1.contains(new CellIndex(indexCellCheck.getIndexRow() + i, indexCellCheck.getIndexColumn() + i)) ||
                        cellPlay2.contains(new CellIndex(indexCellCheck.getIndexRow() + i, indexCellCheck.getIndexColumn() + i))) {
                    // if meet cell not emty return false is mean this cell dont need to ignore
                    return false;
                }
            }
        }
        // check down to up;
        if (indexCellCheck.getIndexRow() >= 4 && indexCellCheck.getIndexColumn() >= 4) {
            for (int i = 1; i < ComputerLevel; i++) {
                if (cellPlay1.contains(new CellIndex(indexCellCheck.getIndexRow() - i, indexCellCheck.getIndexColumn() - i)) ||
                        cellPlay2.contains(new CellIndex(indexCellCheck.getIndexRow() - i, indexCellCheck.getIndexColumn() - i))) {
                    // if meet cell not emty return false is mean this cell dont need to ignore
                    return false;
                }
            }
        }


        // if all cell emty -> need to ignore
        return true;
    }

    boolean checkAlphaBetaSubCross(CellIndex indexCellCheck, int ComputerLevel) {

        // 30 is default size board
        // check  up to down;
        if (indexCellCheck.getIndexRow() + 5 < 30 && indexCellCheck.getIndexColumn() >= 4) {
            for (int i = 1; i < ComputerLevel; i++) {
                if (cellPlay1.contains(new CellIndex(indexCellCheck.getIndexRow() + i, indexCellCheck.getIndexColumn() - i)) ||
                        cellPlay2.contains(new CellIndex(indexCellCheck.getIndexRow() + i, indexCellCheck.getIndexColumn() - i))) {
                    // if meet cell not emty return false is mean this cell dont need to ignore
                    return false;
                }
            }
        }
        // check down to up;
        if (indexCellCheck.getIndexRow() >= 4 && indexCellCheck.getIndexColumn() + 5 < 30) {
            for (int i = 1; i < ComputerLevel; i++) {
                if (cellPlay1.contains(new CellIndex(indexCellCheck.getIndexRow() - i, indexCellCheck.getIndexColumn() + i)) ||
                        cellPlay2.contains(new CellIndex(indexCellCheck.getIndexRow() - i, indexCellCheck.getIndexColumn() + i))) {
                    // if meet cell not emty return false is mean this cell dont need to ignore
                    return false;
                }
            }
        }


        // if all cell emty -> need to ignore
        return true;
    }

    boolean ignore(CellIndex CellCheckIgnore) {
        int computerLevel = 5;
        if (cellPlay2.size() < 3)
            computerLevel = 2;
        if (checkAlphaBetaHorizontal(CellCheckIgnore, computerLevel) && checkAlphaBetaVertical(CellCheckIgnore, computerLevel) &&
                checkAlphaBetaMainCross(CellCheckIgnore, computerLevel) && checkAlphaBetaSubCross(CellCheckIgnore, computerLevel))
            return true;
        return false;
    }

    // AI in here
    // if Computer have 0 cell, plus 0 , 1 cell plus 3, and 2 is 3*8=24, 3 cell .. 24*8
    long[] ATK = {0, 4, 25, 246, 7300, 6561, 59049};
    long[] DEF = {0, 3, 24, 243, 2197, 19773, 177957}; // * 9

    //calculator ATK score

    long ATK_Horizontal(CellIndex Cell_Caulator) {
        long ATK_Score = 0;

        int NumberCell_OurSide = 0;
        int NumberCell_EnemySide = 0;

        int NumberRightEmtyCel = 0;
        int NumberLeftEmtyCell = 0;

        ArrayList<Integer> ArrayCellEmtyRight = new ArrayList<>();
        int row = Cell_Caulator.getIndexRow();
        int column = Cell_Caulator.getIndexColumn();
        // right side
        for (int i = 1; i <= 4 && column + 5 < 30; i++) {
            if (cellPlay1.contains(new CellIndex(row, column + i))) {
                NumberCell_EnemySide++;
                break;
            } else if (cellPlay2.contains(new CellIndex(row, column + i)))
                NumberCell_OurSide++;
            else {
                NumberRightEmtyCel++;
                ArrayCellEmtyRight.add(i);
            }
        }
        // left side
        for (int i = 1; i <= 4 && column >= 4; i++) {
            if (cellPlay1.contains(new CellIndex(row, column - i))) {
                NumberCell_EnemySide++;
                break;
            } else if (cellPlay2.contains(new CellIndex(row, column - i)))
                NumberCell_OurSide++;
            else
                NumberLeftEmtyCell++;
        }
        ATK_Score -= DEF[NumberCell_EnemySide];
        ATK_Score += ATK[NumberCell_OurSide];
        if (NumberCell_OurSide == 4) {
            ATK_Score *= 6;
        }
        if (NumberCell_OurSide == 3 && ((NumberLeftEmtyCell == 4 || NumberLeftEmtyCell == 1))) {
            ATK_Score *= 3;

        } else if (NumberCell_OurSide == 3 && NumberLeftEmtyCell == 3 && NumberRightEmtyCel > 0)
            ATK_Score *= 5;
        else if (NumberCell_OurSide == 3 && ((NumberLeftEmtyCell == 0 && NumberRightEmtyCel > 0) || (NumberRightEmtyCel == 0 && NumberLeftEmtyCell > 0))) {
            ATK_Score *= 4;
        }
        return ATK_Score;
    }

    long ATK_Vertical(CellIndex Cell_Caulator) {
        long ATK_Score = 0;
        int NumberCell_OurSide = 0;
        int NumberCell_EnemySide = 0;

        int NumberRightEmtyCel = 0;
        int NumberLeftEmtyCell = 0;

        ArrayList<Integer> ArrayCellEmtyRight = new ArrayList<>();

        int row = Cell_Caulator.getIndexRow();
        int column = Cell_Caulator.getIndexColumn();
        // down side
        for (int i = 1; i <= 4 && row + 5 < 30; i++) {
            if (cellPlay1.contains(new CellIndex(row + i, column))) {
                NumberCell_EnemySide++;
                break;
            } else if (cellPlay2.contains(new CellIndex(row + i, column)))
                NumberCell_OurSide++;
            else {
                NumberRightEmtyCel++;
                ArrayCellEmtyRight.add(i);
            }
        }
        // up side
        for (int i = 1; i <= 4 && row >= 4; i++) {
            if (cellPlay1.contains(new CellIndex(row - i, column))) {
                NumberCell_EnemySide++;
                break;
            } else if (cellPlay2.contains(new CellIndex(row - i, column)))
                NumberCell_OurSide++;
            else
                NumberLeftEmtyCell++;
        }

        ATK_Score -= DEF[NumberCell_EnemySide];
        ATK_Score += ATK[NumberCell_OurSide];
        if (NumberCell_OurSide == 4) {
            ATK_Score *= 6;
        }
        if (NumberCell_OurSide == 3 && ((NumberLeftEmtyCell == 4 || NumberLeftEmtyCell == 1))) {
            ATK_Score *= 3;

        } else if (NumberCell_OurSide == 3 && NumberLeftEmtyCell == 3 && NumberRightEmtyCel > 0)
            ATK_Score *= 5;
        else if (NumberCell_OurSide == 3 && ((NumberLeftEmtyCell == 0 && NumberRightEmtyCel > 0) || (NumberRightEmtyCel == 0 && NumberLeftEmtyCell > 0))) {
            ATK_Score *= 4;
        }
        return ATK_Score;
    }

    long ATK_MainCross(CellIndex Cell_Caulator) {
        long ATK_Score = 0;
        int NumberCell_OurSide = 0;
        int NumberCell_EnemySide = 0;

        int NumberRightEmtyCel = 0;
        int NumberLeftEmtyCell = 0;

        ArrayList<Integer> ArrayCellEmtyRight = new ArrayList<>();

        int row = Cell_Caulator.getIndexRow();
        int column = Cell_Caulator.getIndexColumn();
        // down side
        for (int i = 1; i <= 4 && row + 5 < 30 && column + 5 < 30; i++) {
            if (cellPlay1.contains(new CellIndex(row + i, column + i))) {
                NumberCell_EnemySide++;
                break;
            } else if (cellPlay2.contains(new CellIndex(row + i, column + i)))
                NumberCell_OurSide++;
            else {
                NumberRightEmtyCel++;
                ArrayCellEmtyRight.add(i);
            }
        }
        // up side
        for (int i = 1; i <= 4 && row >= 4 && column >= 4; i++) {
            if (cellPlay1.contains(new CellIndex(row - i, column - i))) {
                NumberCell_EnemySide++;
                break;
            } else if (cellPlay2.contains(new CellIndex(row - i, column - i)))
                NumberCell_OurSide++;
            else
                NumberLeftEmtyCell++;
        }

        ATK_Score -= DEF[NumberCell_EnemySide];
        ATK_Score += ATK[NumberCell_OurSide];
        if (NumberCell_OurSide == 4) {
            ATK_Score *= 6;
        }
        if (NumberCell_OurSide == 3 && ((NumberLeftEmtyCell == 4 || NumberLeftEmtyCell == 1))) {
            ATK_Score *= 3;

        } else if (NumberCell_OurSide == 3 && NumberLeftEmtyCell == 3 && NumberRightEmtyCel > 0)
            ATK_Score *= 5;
        else if (NumberCell_OurSide == 3 && ((NumberLeftEmtyCell == 0 && NumberRightEmtyCel > 0) || (NumberRightEmtyCel == 0 && NumberLeftEmtyCell > 0))) {
            ATK_Score *= 4;
        }
        return ATK_Score;
    }

    long ATK_SubCross(CellIndex Cell_Caulator) {
        long ATK_Score = 0;
        int NumberCell_OurSide = 0;
        int NumberCell_EnemySide = 0;

        int NumberRightEmtyCel = 0;
        int NumberLeftEmtyCell = 0;

        ArrayList<Integer> ArrayCellEmtyRight = new ArrayList<>();

        int row = Cell_Caulator.getIndexRow();
        int column = Cell_Caulator.getIndexColumn();
        // down side
        for (int i = 1; i <= 4 && row + 5 < 30 && column >= 4; i++) {
            if (cellPlay1.contains(new CellIndex(row + i, column - i))) {
                NumberCell_EnemySide++;
                break;
            } else if (cellPlay2.contains(new CellIndex(row + i, column - i)))
                NumberCell_OurSide++;
            else {
                NumberRightEmtyCel++;
                ArrayCellEmtyRight.add(i);
            }
        }
        // up side
        for (int i = 1; i <= 4 && row >= 4 && column + 5 < 30; i++) {
            if (cellPlay1.contains(new CellIndex(row - i, column + i))) {
                NumberCell_EnemySide++;
                break;
            } else if (cellPlay2.contains(new CellIndex(row - i, column + i)))
                NumberCell_OurSide++;
            else
                NumberLeftEmtyCell++;
        }

        ATK_Score -= DEF[NumberCell_EnemySide];
        ATK_Score += ATK[NumberCell_OurSide];
        if (NumberCell_OurSide == 4) {
            ATK_Score *= 6;
        }
        if (NumberCell_OurSide == 3 && ((NumberLeftEmtyCell == 4 || NumberLeftEmtyCell == 1))) {
            ATK_Score *= 3;

        } else if (NumberCell_OurSide == 3 && NumberLeftEmtyCell == 3 && NumberRightEmtyCel > 0)
            ATK_Score *= 5;
        else if (NumberCell_OurSide == 3 && ((NumberLeftEmtyCell == 0 && NumberRightEmtyCel > 0) || (NumberRightEmtyCel == 0 && NumberLeftEmtyCell > 0))) {
            ATK_Score *= 4;
        }

        return ATK_Score;
    }


    // calculator DEF score


    long DEF_Horizontal(CellIndex Cell_Caulator) {
        long DEF_Score = 0;
        int NumberCell_OurSide = 0;
        int NumberCell_EnemySide = 0;


        int NumberRightEmtyCel = 0;
        int NumberLeftEmtyCell = 0;

        ArrayList<Integer> ArrayCellEmtyRight = new ArrayList<>();
        ArrayList<Integer> ArrayCellEmtyLeft = new ArrayList<>();

        int row = Cell_Caulator.getIndexRow();
        int column = Cell_Caulator.getIndexColumn();
        // right side
        for (int i = 1; i <= 4 && column + 5 < 30; i++) {
            if (cellPlay1.contains(new CellIndex(row, column + i))) {
                NumberCell_EnemySide++;
            } else if (cellPlay2.contains(new CellIndex(row, column + i))) {
                NumberCell_OurSide++;
                break;
            } else {
                NumberRightEmtyCel++;
                ArrayCellEmtyRight.add(i);
            }
        }
        // left side
        for (int i = 1; i <= 4 && column >= 4; i++) {
            if (cellPlay1.contains(new CellIndex(row, column - i))) {
                NumberCell_EnemySide++;
            } else if (cellPlay2.contains(new CellIndex(row, column - i))) {
                NumberCell_OurSide++;
                break;
            } else {
                NumberLeftEmtyCell++;
                ArrayCellEmtyLeft.add(i);

            }
        }

        DEF_Score += DEF[NumberCell_EnemySide];
        if (NumberCell_EnemySide == 4) {
            DEF_Score *= 3;
        }
        if (NumberCell_EnemySide == 3 && ((NumberLeftEmtyCell == 4 || NumberLeftEmtyCell == 0) && NumberCell_OurSide == 0 && NumberRightEmtyCel == 1)) {
            if (ArrayCellEmtyRight.get(0) == 4) {
                DEF_Score *= 3;
            }

        }
        return DEF_Score;
    }

    long DEF_Vertical(CellIndex Cell_Caulator) {
        long DEF_Score = 0;
        int NumberCell_OurSide = 0;
        int NumberCell_EnemySide = 0;

        int NumberRightEmtyCel = 0;
        int NumberLeftEmtyCell = 0;

        ArrayList<Integer> ArrayCellEmtyRight = new ArrayList<>();
        ArrayList<Integer> ArrayCellEmtyLeft = new ArrayList<>();

        int row = Cell_Caulator.getIndexRow();
        int column = Cell_Caulator.getIndexColumn();
        // down side
        for (int i = 1; i <= 4 && row + 5 < 30; i++) {
            if (cellPlay1.contains(new CellIndex(row + i, column))) {
                NumberCell_EnemySide++;
            } else if (cellPlay2.contains(new CellIndex(row + i, column))) {
                NumberCell_OurSide++;
                break;
            } else {
                NumberRightEmtyCel++;
                ArrayCellEmtyRight.add(i);
            }
        }
        // up side
        for (int i = 1; i <= 4 && row >= 4; i++) {
            if (cellPlay1.contains(new CellIndex(row - i, column))) {
                NumberCell_EnemySide++;
            } else if (cellPlay2.contains(new CellIndex(row - i, column))) {
                NumberCell_OurSide++;
                break;
            } else {
                NumberLeftEmtyCell++;
                ArrayCellEmtyLeft.add(i);

            }
        }

        DEF_Score += DEF[NumberCell_EnemySide];

        if (NumberCell_EnemySide == 4) {
            DEF_Score *= 3;
        }
        if (NumberCell_EnemySide == 3 && ((NumberLeftEmtyCell == 4 || NumberLeftEmtyCell == 0) && NumberCell_OurSide == 0 && NumberRightEmtyCel == 1)) {
            if (ArrayCellEmtyRight.get(0) == 4) {
                DEF_Score *= 3;
            }

        }

        return DEF_Score;
    }

    long DEF_MainCross(CellIndex Cell_Caulator) {
        long DEF_Score = 0;
        int NumberCell_OurSide = 0;
        int NumberCell_EnemySide = 0;

        int NumberRightEmtyCel = 0;
        int NumberLeftEmtyCell = 0;

        ArrayList<Integer> ArrayCellEmtyRight = new ArrayList<>();
        ArrayList<Integer> ArrayCellEmtyLeft = new ArrayList<>();

        int row = Cell_Caulator.getIndexRow();
        int column = Cell_Caulator.getIndexColumn();
        // down side
        for (int i = 1; i <= 4 && row + 5 < 30 && column + 5 < 30; i++) {
            if (cellPlay1.contains(new CellIndex(row + i, column + i))) {
                NumberCell_EnemySide++;
            } else if (cellPlay2.contains(new CellIndex(row + i, column + i))) {
                NumberCell_OurSide++;
                break;
            } else {
                NumberRightEmtyCel++;
                ArrayCellEmtyRight.add(i);
            }
        }
        // up side
        for (int i = 1; i <= 4 && row >= 4 && column >= 4; i++) {
            if (cellPlay1.contains(new CellIndex(row - i, column - i))) {
                NumberCell_EnemySide++;
            } else if (cellPlay2.contains(new CellIndex(row - i, column - i))) {
                NumberCell_OurSide++;
                break;
            } else {
                NumberLeftEmtyCell++;
                ArrayCellEmtyLeft.add(i);

            }
        }

        DEF_Score += DEF[NumberCell_EnemySide];
        if (NumberCell_EnemySide == 4) {
            DEF_Score *= 3;
        }
        if (NumberCell_EnemySide == 3 && ((NumberLeftEmtyCell == 4 || NumberLeftEmtyCell == 0) && NumberCell_OurSide == 0 && NumberRightEmtyCel == 1)) {
            if (ArrayCellEmtyRight.get(0) == 4) {
                DEF_Score *= 3;
            }

        }
        return DEF_Score;
    }

    long DEF_SubCross(CellIndex Cell_Caulator) {
        long DEF_Score = 0;
        int NumberCell_OurSide = 0;
        int NumberCell_EnemySide = 0;

        int NumberRightEmtyCel = 0;
        int NumberLeftEmtyCell = 0;

        ArrayList<Integer> ArrayCellEmtyRight = new ArrayList<>();
        ArrayList<Integer> ArrayCellEmtyLeft = new ArrayList<>();

        int row = Cell_Caulator.getIndexRow();
        int column = Cell_Caulator.getIndexColumn();
        // down side
        for (int i = 1; i <= 4 && row + 5 < 30 && column >= 4; i++) {
            if (cellPlay1.contains(new CellIndex(row + i, column - i))) {
                NumberCell_EnemySide++;
            } else if (cellPlay2.contains(new CellIndex(row + i, column - i))) {
                NumberCell_OurSide++;
                break;
            } else {
                NumberRightEmtyCel++;
                ArrayCellEmtyRight.add(i);
            }
        }
        // up side
        for (int i = 1; i <= 4 && row >= 4 && column + 5 < 30; i++) {
            if (cellPlay1.contains(new CellIndex(row - i, column + i))) {
                NumberCell_EnemySide++;
            } else if (cellPlay2.contains(new CellIndex(row - i, column + i))) {
                NumberCell_OurSide++;
                break;
            } else {
                NumberLeftEmtyCell++;
                ArrayCellEmtyLeft.add(i);

            }
        }
        DEF_Score += DEF[NumberCell_EnemySide];
        if (NumberCell_EnemySide == 4) {
            DEF_Score *= 3;
        }
        if (NumberCell_EnemySide == 3 && ((NumberLeftEmtyCell == 4 || NumberLeftEmtyCell == 0) && NumberCell_OurSide == 0 && NumberRightEmtyCel == 1)) {
            if (ArrayCellEmtyRight.get(0) == 4) {
                DEF_Score *= 3;
            }

        }
        return DEF_Score;
    }


    void blockBoardGame() {
        for (int row = 0; row < 30; row++) {
            for (int column = 0; column < 30; column++) {
                TableRow rowLayout = (TableRow) BoardGameTableLayout.getChildAt(row);
                RelativeLayout relativeLayout = (RelativeLayout) rowLayout.getChildAt(column);
                relativeLayout.setOnClickListener(null);
            }
        }

    }

    void newGame(ArrayList<CellIndex> CellEmty) {
        cellPlay1.clear();
        cellPlay2.clear();
        cellEmty = CellEmty;
    }

    void undo() {
        if (idPlayer == 0) {
            // computer or player 2
            CellIndex temp = cellPlay1.get(cellPlay1.size() - 1);
            cellPlay1.remove(temp);
            cellEmty.add(temp);
            TableRow rowLayout = (TableRow) BoardGameTableLayout.getChildAt(temp.getIndexRow());
            RelativeLayout relativeLayout = (RelativeLayout) rowLayout.getChildAt(temp.getIndexColumn());
            ImageView imageView = (ImageView) relativeLayout.getChildAt(0);
            imageView.setBackgroundResource(android.R.color.transparent);
            idPlayer = 1;
        } else {

            CellIndex temp = cellPlay2.get(cellPlay2.size() - 1);
            cellPlay2.remove(temp);
            cellEmty.add(temp);
            TableRow rowLayout = (TableRow) BoardGameTableLayout.getChildAt(temp.getIndexRow());
            RelativeLayout relativeLayout = (RelativeLayout) rowLayout.getChildAt(temp.getIndexColumn());
            ImageView imageView = (ImageView) relativeLayout.getChildAt(0);
            imageView.setBackgroundResource(android.R.color.transparent);
            idPlayer = 0;
        }
    }

}
