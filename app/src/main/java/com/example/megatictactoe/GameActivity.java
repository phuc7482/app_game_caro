package com.example.megatictactoe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.megatictactoe.ControllPlayGame.MoveCount;
import static com.example.megatictactoe.roomOnlineActivity.arrayListRoom2Play;
import static com.example.megatictactoe.roomOnlineActivity.mAuth;

public class GameActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener{

    ArrayList<CellIndex> cellEmty = new ArrayList<>();
    ArrayList<CellIndex> cellPlayer1 = new ArrayList<>();
    ArrayList<CellIndex> cellPlayer2 = new ArrayList<>();

    RelativeLayout game_board_view, layoutWaitingPlayer, layout_turn, layout_Time;
    ImageView btnNewGame, btnUndo, btnBack;
    static TextView txt_turnPlayer, txt_CountDownTime;
    static int ModePlay = 1; // 0 is mean play vs play ,1 is mean play vs computer,2 play online// this need get inten extra
    private int defaultBoardSize = 30;
    static ZoomManager zoomManager = null;
    ControllPlayGame controllPlayGame = null;
    static TableLayout boardGame;
    boolean uiLocked = true;
    static int idPlayer = 1; // 1 is mean player1 move first,0 is mean player2 move first or computer move first
    static int idPlayerWin = -1;
    static boolean MoveFirst = true;

    static String RoomId = "";// if mode is play online

    DatabaseReference RematchNewGame;
    DatabaseReference DataGame;
    static DatabaseReference players;
    static DatabaseReference DataRoom;
    static FirebaseDatabase database;

    static FirebaseUser currentUser;

    boolean waitingPlayer = true;

    // countDown timer for online mode;
    static Context contextGameActivity;
    static CountDownTimer countDownTimer_GameOnline;
    long delay;

    private NetworkStateReceiver networkStateReceiver;
    boolean turnNewGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        try {
            ModePlay = getIntent().getIntExtra("playModeId", 1);
            RoomId = getIntent().getStringExtra("RoomId");
            MoveFirst = getIntent().getBooleanExtra("MoveFirst", true);
        } catch (Exception e) {
            // do nothing
        }
        initViews();
        // network state
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        layoutWaitingPlayer.setVisibility(View.GONE);
        CreateNewGame();
        countDownTimer_GameOnline = new CountDownTimer(20000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                txt_CountDownTime.setText(millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                // this is your turn
                if (MoveCount == 0) {
                    if (MoveFirst) {
                        Toast.makeText(GameActivity.this, "You Lose", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GameActivity.this, "You Win", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (idPlayer == 1) {
                        Toast.makeText(GameActivity.this, "You Lose", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GameActivity.this, "You Win", Toast.LENGTH_SHORT).show();
                    }
                }

                try {
                    controllPlayGame.blockBoardGame();
                } catch (Exception e) {

                }
            }
        };
        controllPlayGame = new ControllPlayGame(cellPlayer1, cellPlayer2, cellEmty, boardGame, GameActivity.this, RoomId);
        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ModePlay == 2) {
                    RematchNewGame = database.getReference("room/" + RoomId + "/RematchNewGame/ValueRemacth/");
                    RematchNewGame.setValue(currentUser.getUid());
                    countDownTimer_GameOnline.cancel();
                }

                CreateNewGame();
            }
        });
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    controllPlayGame.undo();
                } catch (Exception e) {

                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ModePlay==2){
                    LeaveRoom();
                }
                Intent intent = new Intent(GameActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        if (ModePlay == 2) {
            currentUser = mAuth.getCurrentUser();
            database = FirebaseDatabase.getInstance();
            RematchNewGame = database.getReference("room/" + RoomId + "/RematchNewGame/ValueRemacth/");
            RematchNewGame.setValue("False");
            addEventNewGameDataChange();
            btnUndo.setEnabled(false);
            layout_turn.setVisibility(View.VISIBLE);
            layout_Time.setVisibility(View.VISIBLE);
            // neu move first thi set your turn else competitor
            if (MoveFirst) {
                txt_turnPlayer.setText("Your Turn (X)");
                delay = 3000;
            } else {
                txt_turnPlayer.setText("Competitor Turn (O)");
                delay = 2700;
            }

            RoomEvent();
        } else
            waitingPlayer = false;
        startService(new Intent(GameActivity.this,serviceCheckAppClose.class));
        contextGameActivity = this;
    }

    private void initViews() {
        boardGame = findViewById(R.id.boardTableLayout);
        btnNewGame = findViewById(R.id.btn_newGame);
        btnUndo = findViewById(R.id.btn_undo);
        btnBack = findViewById(R.id.btn_back);
        layoutWaitingPlayer = findViewById(R.id.layoutWaitingPlayer);
        layout_turn = findViewById(R.id.layout_turn);
        layout_Time = findViewById(R.id.layout_Time);
        txt_CountDownTime = findViewById(R.id.txt_CountDownTime);
        txt_turnPlayer = findViewById(R.id.txt_turnPlayer);
    }

    public void InitGameBoard(int defaultBoardSize) {
        TableLayout tableLayout = (TableLayout) findViewById(R.id.boardTableLayout);
        tableLayout.setBackgroundResource(R.drawable.paper_background);
        for (int row = 0; row < defaultBoardSize; row++) {
            TableRow tableRow = new TableRow(this);
            for (int column = 0; column < defaultBoardSize; column++) {
                RelativeLayout relativeLayout = new RelativeLayout(this);
                relativeLayout.setLayoutParams(new TableRow.LayoutParams(this.zoomManager.GetCurrentCellSize(), this.zoomManager.GetCurrentCellSize()));
                relativeLayout.setGravity(1);
                relativeLayout.setBackgroundResource(R.drawable.cell_border);

                ImageView imageView = new ImageView(this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(-1, -1);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setLayoutParams(layoutParams);
                relativeLayout.addView(imageView);
                int finalColumn = column;
                int finalRow = row;
                relativeLayout.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (!waitingPlayer) {
                            controllPlayGame.PlayMove(new CellIndex(finalRow, finalColumn));
                        }
                    }
                });
                tableRow.addView(relativeLayout);
            }
            tableLayout.addView(tableRow);
        }
    }

    public void CenterScrollbarsOnLoad() {
        final ScrollView scrollView = (ScrollView) findViewById(R.id.vertical_scrollbar);
        final HorizontalScrollView horizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontal_scrollbar);
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.scrollTo(0, (scrollView.getChildAt(0).getHeight() / 2) - (scrollView.getHeight() / 2));
                scrollView.invalidate();
                horizontalScrollView.scrollTo((horizontalScrollView.getChildAt(0).getWidth() / 2) - (horizontalScrollView.getWidth() / 2), 0);
                horizontalScrollView.invalidate();
                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(500);
                            boolean unused = GameActivity.this.uiLocked = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }

    public void CreateNewGame() {
        cellEmty.clear();
        boardGame.removeAllViews();
        ImageView winningLine = findViewById(R.id.winningLine);
        winningLine.setBackgroundResource(android.R.color.transparent);
        for (int row = 0; row < defaultBoardSize; row++) {
            for (int colum = 0; colum < defaultBoardSize; colum++) {
                cellEmty.add(new CellIndex(row, colum));
            }
        }
        if (zoomManager == null) {
            this.zoomManager = new ZoomManager(this, this.defaultBoardSize, boardGame, (ScrollView) findViewById(R.id.vertical_scrollbar), (HorizontalScrollView) findViewById(R.id.horizontal_scrollbar), (ImageView) findViewById(R.id.winningLine));
        }
        InitGameBoard(this.defaultBoardSize);
        CenterScrollbarsOnLoad();
        if (controllPlayGame != null) {
            controllPlayGame.newGame(cellEmty);
        }
        if (ModePlay == 2) {

            if (MoveFirst) {
                {
                    txt_turnPlayer.setText("Your Turn (X)");
                    idPlayer = 1;
                }
            } else
                txt_turnPlayer.setText("Competitor Turn (O)");
        }
    }

    void addEventNewGameDataChange() {
        RematchNewGame = database.getReference("/room/" + RoomId + "/RematchNewGame/ValueRemacth");
        RematchNewGame.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = (String) snapshot.getValue();
                if (value != null) {
                    if ((!value.contains("False")) && (value != currentUser.getUid())) {
                        AlertDialog dialog = new AlertDialog.Builder(GameActivity.this)
                                .setTitle("Rematch Game!")
                                .setMessage("competitor want to play again with you ?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        RematchNewGame = database.getReference("room/" + RoomId + "/RematchNewGame");
                                        RematchNewGame.setValue("Yes");
                                        DataGame = database.getReference("room/" + RoomId + "/Game");
                                        DataGame.setValue(null);
                                        countDownTimer_GameOnline.cancel();
                                        countDownTimer_GameOnline.start();
                                        CreateNewGame();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        LeaveRoom();
                                        onBackPressed();
                                    }
                                })
                                .create();

                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                final Button NoButtonDialog = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                                final CharSequence NoButtonDialogText = NoButtonDialog.getText();
                                new CountDownTimer(15000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        NoButtonDialog.setText(String.format(
                                                Locale.getDefault(), "%s (%d)",
                                                NoButtonDialogText,
                                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1 //add one so it never displays zero
                                        ));
                                    }
                                    @Override
                                    public void onFinish() {
                                        if (((AlertDialog) dialog).isShowing()) {
                                            // call leavRoom method
                                            LeaveRoom();
                                            dialog.dismiss();
                                            onBackPressed();
                                        }
                                    }
                                }.start();
                            }
                        });

                        dialog.show();
                    }
                } else {
                    Toast.makeText(GameActivity.this,"Waiting for player answer",Toast.LENGTH_SHORT);
                    countDownTimer_GameOnline.cancel();
                    countDownTimer_GameOnline.start();
                    CreateNewGame();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // fail to read
            }
        });
    }

    void RoomEvent() {
        players = database.getReference("room/" + RoomId + "/players");
        players.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Iterable<DataSnapshot> Players = snapshot.getChildren();
                ArrayList<String> keyUid = new ArrayList<>();
                for (DataSnapshot player : Players) {
                    keyUid.add(player.getKey());
                }
                if (keyUid.size() == 1) {
                    // show this room again , remove
                    database.getReference("roomFull").child(RoomId).removeValue();
                    // have someone lave room or create new room
                    String uID_play = keyUid.get(0);
                    if (uID_play != null) {
                        if (currentUser.getUid().equals(uID_play)) {
                            MoveFirst = true;
                            waitingPlayer = true;
                            try {
                                // delete data game
                                DataGame = database.getReference("room/" + RoomId + "/Game");
                                DataGame.setValue(null);
                                CreateNewGame();
                                layoutWaitingPlayer.setVisibility(View.VISIBLE);
                                countDownTimer_GameOnline.cancel();
                            } catch (Exception e) {

                            }
                        }
                    }

                } else if (keyUid.size() == 2) {
                    // need add Room is full to dont show to list room online
                    DatabaseReference RoomFull = database.getReference("roomFull/"+RoomId);
                    RoomFull.setValue(RoomId);
                    waitingPlayer = false;
                    layoutWaitingPlayer.setVisibility(View.GONE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            countDownTimer_GameOnline.start();
                        }
                    }, delay);

                }
                else if(keyUid.size()==0){
                    // delete this room;
                    DataRoom = database.getReference("room");
                    DataRoom.child(RoomId).removeValue();
                }
                if(snapshot.getChildrenCount()==0){
                    DataRoom = database.getReference("room");
                    DataRoom.child(RoomId).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // fail to read do no thing in here
            }
        });
    }

    @Override
    public void onBackPressed() {
        // remove player from room;
        try {
            try {
                Thread thread = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LeaveRoom();
                            }
                        });
                    }
                };
                thread.start();
            }catch (Exception e){

            }
        }catch (Exception e){
            //

        }finally {
            Intent intent = new Intent(GameActivity.this,roomOnlineActivity.class);
            startActivity(intent);
        }


    }
    void LeaveRoom(){
        try {
            database.getReference("room/" + RoomId + "/players").child(currentUser.getUid()).removeValue();
            try{
                countDownTimer_GameOnline.cancel();
            }catch (Exception e){

            }
        } catch (Exception e) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CountDownTimer countDownTimer  = new CountDownTimer(120000, 1000) {

                            @Override
                            public void onTick(long millisUntilFinished) {
                                // do nothing in here
                            }

                            @Override
                            public void onFinish() {
                                LeaveRoom();
                                Intent intent = new Intent(GameActivity.this,MainActivity.class);
                                startActivity(intent);
                            }
                        }.start();
                    }
                });
            }
        };
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LeaveRoom();
                    }
                });
            }
        };
        thread.start();
    }

    @Override
    public void networkAvailable() {
        // do nothing
    }

    @Override
    public void networkUnavailable() {
        // leve room and back to main menu
        Toast.makeText(GameActivity.this,"Conntect Internet Fail",Toast.LENGTH_LONG);
        Intent intent = new Intent(GameActivity.this,MainActivity.class);
        startActivity(intent);
    }
}