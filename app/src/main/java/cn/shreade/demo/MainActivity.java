package cn.shreade.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import cn.shreade.sdk.Room;
import cn.shreade.sdk.chat.ICallMessage;
import cn.shreade.sdk.config.CallView;
import cn.shreade.sdk.online.Header;
import cn.shreade.sdk.share.IShareObserver;
import cn.shreade.sdk.view.VideoView;

public class MainActivity extends AppCompatActivity {
    public final static String TAG = "XXXLog_main_activity";

    private GridLayout videoList;
    private String nickname;
    private String roomName;
    private String wsUrl;

    private Room room;

    RecyclerView recyclerView;
    RCAdapter rcAdapter;
    ArrayList<cn.shreade.sdk.chat.Message> modelArrayList;
    //private ConcurrentHashMap<String, RCModel> modelArrayList;

    private ConcurrentHashMap<String, RelativeLayout> idRelativeLayoutMap;
    private ConcurrentHashMap<String, LinearLayout> idLinearLayoutMap;

    private ConcurrentHashMap<String, Integer> idQueueMap;

    Deque<LinearLayout> queue;

    private Handler mHandler;

    private final static int FILL_MEDIA = 1;
    private final static int REMOVE_MEDIA = 2;

    private final static int SHARE_SHOW_TEXT = 3;

    TextView chatSubmit;

    EditText chatInput;

    TextView showShare;

    ClickableSpan clickableSpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            Log.d(TAG, "==============landscape==============="); // 横屏
            setContentView(R.layout.activity_main_landscape);
        } else if (this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "==============portrait================"); // 竖屏
            setContentView(R.layout.activity_main);
        }

        idRelativeLayoutMap = new ConcurrentHashMap<>();
        idLinearLayoutMap = new ConcurrentHashMap<>();
        idQueueMap = new ConcurrentHashMap<>();

        mHandler = new MainHandler(Looper.myLooper(),this);

        recyclerView = this.findViewById(R.id.chat_list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);

        modelArrayList = new ArrayList<>();
        rcAdapter = new RCAdapter(this, modelArrayList);
        recyclerView.setAdapter(rcAdapter);

        init();
    }

    static class MainHandler extends Handler {

        WeakReference<MainActivity> mactivity;

        //构造函数，传来的是外部类的this
        public MainHandler(@NonNull Looper looper, MainActivity activity) {
            super(looper);//调用父类的显式指明的构造函数
            mactivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            MainActivity mainActivity = mactivity.get();
            switch (msg.what) {
                case FILL_MEDIA:
                    if (!mainActivity.queue.isEmpty()) {
                        VideoView videoView = (VideoView) msg.obj;

                        LinearLayout parent = mainActivity.queue.pollFirst();
                        Log.d(mainActivity.TAG, "add id: "+videoView.getId()+",name: " + videoView.getName()  + ",layout: " + parent.getId());
                        Log.d(TAG, "video view width: "+videoView.getWidthLayout()+",height: " + videoView.getHeightLayout() );
                        ViewGroup.LayoutParams lp = parent.getLayoutParams();
                        Log.d(TAG, "parent width: " + lp.width + ", height: " +lp.height);

                        LinearLayout parentView = videoView.getParentView();
                        if (mainActivity.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT) {
                            parentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        } else if (mainActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            parentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        }
                        //parentView.setGravity(Gravity.CENTER_HORIZONTAL);
                        //parentView.setHorizontalGravity(RelativeLayout.CENTER_IN_PARENT);

                        LinearLayout center = new LinearLayout(mainActivity);
                        center.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        center.setGravity(Gravity.CENTER);
                        center.addView(parentView);

                        TextView titleView = new TextView(mainActivity);
                        titleView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        titleView.setGravity(Gravity.CENTER);
                        titleView.setBackgroundColor(0xc0f0f0f0);
                        titleView.setText(videoView.getName());

                        RelativeLayout relativeLayout = new RelativeLayout(mainActivity);
                        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        //relativeLayout.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP);
                        //relativeLayout.setHorizontalGravity(RelativeLayout.CENTER_IN_PARENT);

                        relativeLayout.addView(center);
                        relativeLayout.addView(titleView);

                        parent.addView(relativeLayout);
                        mainActivity.idRelativeLayoutMap.put(videoView.getId(), relativeLayout);
                        mainActivity.idLinearLayoutMap.put(videoView.getId(), parent);
                        mainActivity.idQueueMap.put(videoView.getId(),parent.getId());
                    }
                    break;
                case REMOVE_MEDIA:
                    VideoView videoView = (VideoView) msg.obj;
                    if (mainActivity.idRelativeLayoutMap.containsKey(videoView.getId()) && mainActivity.idLinearLayoutMap.containsKey(videoView.getId())) {
                        Log.d(TAG, "remove id: " + videoView.getId() + ", name: "+videoView.getName());
                        LinearLayout parent = mainActivity.idLinearLayoutMap.get(videoView.getId());
                        RelativeLayout son = mainActivity.idRelativeLayoutMap.get(videoView.getId());

                        Log.d(TAG, "parent id: " + parent.getId() + ", son id: "+son.getId());
                        parent.removeView(son);

                        mainActivity.idRelativeLayoutMap.remove(videoView.getId());
                        mainActivity.idLinearLayoutMap.remove(videoView.getId());

                        int pos = mainActivity.idQueueMap.get(videoView.getId());
                        mainActivity.queue.addFirst(mainActivity.findViewById(pos));
                    }
                    break;
                case SHARE_SHOW_TEXT:
                    Header h = (Header) msg.obj;
                    SpannableStringBuilder style = new SpannableStringBuilder();
                    String str = h.getName() + "分享了屏幕";
                    /*
                    style.append(str);

                    style.setSpan(clickableSpan, str.length()-3, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    showShare.setText(style);

                    ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#BEFF5722"));
                    style.setSpan(foregroundColorSpan, str.length()-3, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    //配置给TextView
                    showShare.setMovementMethod(LinkMovementMethod.getInstance());
                    */
                    mainActivity.showShare.setText(str);
                    mainActivity.showShare.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void init() {
        Log.d(TAG, "init run");
        nickname = getIntent().getStringExtra("nickname");
        roomName = getIntent().getStringExtra("room_name");
        wsUrl = getIntent().getStringExtra("url");
        Log.d(TAG,"nickname: "+nickname+", room name: "+roomName+", url: "+wsUrl);

        queue = new LinkedList<>();

        queue.offerLast(findViewById(R.id.list_0_0));
        queue.offerLast(findViewById(R.id.list_0_1));
        queue.offerLast(findViewById(R.id.list_1_0));
        queue.offerLast(findViewById(R.id.list_1_1));
        queue.offerLast(findViewById(R.id.list_2_0));
        queue.offerLast(findViewById(R.id.list_2_1));

        chatSubmit = findViewById(R.id.chat_submit);
        chatInput = findViewById(R.id.chat_input);
        showShare = findViewById(R.id.btn_share_go);

        clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Log.d(TAG,"click 去看看");
            }
        };


        if (nickname == null || roomName == null || wsUrl == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }else{
            if (this.room == null) {
                this.room = new Room(MainActivity.this, nickname, roomName, "wss://" +wsUrl+"/wss");
                this.room.setLocalCallView(new CallView() {
                    @Override
                    public void add(VideoView v) {
                        super.add(v);

                        Log.d(TAG, "add local view");
                        Message msg = new Message();
                        msg.what=FILL_MEDIA;
                        msg.obj = v;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void remove(VideoView v) {
                        super.remove(v);
                        Log.d(TAG, "remove local view");
                        Message msg = new Message();
                        msg.what=REMOVE_MEDIA;
                        msg.obj = v;
                        mHandler.sendMessage(msg);
                    }
                });

                this.room.setRemoteCallView(new CallView() {
                    @Override
                    public void add(VideoView v) {
                        super.add(v);

                        Log.d(TAG, "add remote view, " + v.getId()  + ":" + v.getName());
                        Message msg = new Message();
                        msg.what=FILL_MEDIA;
                        msg.obj = v;
                        mHandler.sendMessage(msg);
                    }

                    @Override
                    public void remove(VideoView v) {
                        super.remove(v);
                        Log.d(TAG, "remove remote view, " + v.getId() + ":" + v.getName());
                        if (v.getId().equals(cn.shreade.sdk.config.Message.KIND_SHARE)) {
                            showShare.setVisibility(View.INVISIBLE);
                        }
                        Message msg = new Message();
                        msg.what=REMOVE_MEDIA;
                        msg.obj = v;
                        mHandler.sendMessage(msg);

                    }
                });

                this.room.setCallbackChat(new ICallMessage() {
                    @Override
                    public void onMessage(cn.shreade.sdk.chat.Message msg) {
                        //Log.d(TAG, "on chat message " + msg.name + ":" + Html.fromHtml(msg.context).toString()+":"+msg.createAt);
                        modelArrayList.add(msg);
                        //modelArrayList.add(rcAdapter.getItemCount() , msg);
                        rcAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(rcAdapter.getItemCount() -1);


                    }
                });

                this.room.setCallbackShare(new IShareObserver() {
                    @Override
                    public void onMessage(Header data) {
                        Log.d(TAG, "add share show text");
                        Message msg = new Message();
                        msg.what=SHARE_SHOW_TEXT;
                        msg.obj = data;
                        mHandler.sendMessageDelayed(msg, 500);
                    }
                });
                this.room.start();

                chatSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (room != null) {
                            if (chatInput.getText().toString().equals("")) {
                                return;
                            }

                            room.sendChatMessage(chatInput.getText().toString());

                            cn.shreade.sdk.chat.Message msg = new cn.shreade.sdk.chat.Message();
                            msg.name = room.getUserName();
                            msg.uid = room.getUserId();
                            msg.room = room.getRoomName();
                            SimpleDateFormat sdf3=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = new Date();
                            msg.createAt = sdf3.format(date);
                            msg.context = chatInput.getText().toString();
                            modelArrayList.add(msg);

                            chatInput.setText("");

                            //modelArrayList.add(rcAdapter.getItemCount() , msg);
                            rcAdapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(rcAdapter.getItemCount() -1);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "main 获得焦点");
        //
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        close();
        Log.d(TAG, "main pause");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "or: " + newConfig.orientation);
    }

    @Override
    protected void onStop() {
        super.onStop();
        close();
        Log.d(TAG, "main stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
        Log.d(TAG, "main destroy");
    }

    private void close() {
        if (this.room != null) {
            this.room.close();
            this.room = null;
        }


        Iterator<String> it = idRelativeLayoutMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            LinearLayout parent = idLinearLayoutMap.get(key);
            RelativeLayout son = idRelativeLayoutMap.get(key);
            parent.removeView(son);

            idRelativeLayoutMap.remove(key);
            idLinearLayoutMap.remove(key);
            idQueueMap.remove(key);
        }
    }
}