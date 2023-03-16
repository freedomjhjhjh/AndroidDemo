package cn.shreade.demo;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.Manifest;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import pub.devrel.easypermissions.EasyPermissions;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "XXXLog_login_activity";

    public static String[] permissionsREAD = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
    };

    private EditText etNickname;
    private EditText etRoomName;
    private EditText etServiceUrl;

    private RadioButton rbAgree;
    private Button btSubmit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etNickname = findViewById(R.id.login_nick_name);
        etRoomName = findViewById(R.id.login_room_name);
        etServiceUrl = findViewById(R.id.login_service_url);
        rbAgree = findViewById(R.id.login_agree);
        btSubmit = findViewById(R.id.login_submit);

        setViewEventListener();
    }

    private void setViewEventListener() {
        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname = etNickname.getText()==null?"":etNickname.getText().toString();
                String roomName = etRoomName.getText()==null?"":etRoomName.getText().toString();
                String url = etServiceUrl.getText()==null?"":etServiceUrl.getText().toString();
                requestAllPower();
                if (rbAgree.isChecked() && nickname != null && roomName != null && url != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("nickname", nickname);
                    intent.putExtra("room_name", roomName);
                    intent.putExtra("url",url);
                    startActivity(intent);
                }
            }
        });
    }

    public void requestAllPower() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PERMISSION_GRANTED) {
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "login 获得焦点");
        //
        //init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "login pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "login stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "login destroy");
    }
}
