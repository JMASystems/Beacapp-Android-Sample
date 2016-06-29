package jp.co.jmas.beacappandroidsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.beacapp.BeaconEventListener;
import com.beacapp.FireEventListener;
import com.beacapp.JBCPException;
import com.beacapp.JBCPManager;
import com.beacapp.ShouldUpdateEventsListener;
import com.beacapp.UpdateEventsListener;
import com.beacapp.service.BeaconEvent;

import org.json.JSONObject;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private JBCPManager jbcpManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permissions[] = {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
            };
            boolean needPermission = false;
            for (String permission : permissions) {
                if (checkSelfPermission(permission) ==
                        PackageManager.PERMISSION_GRANTED) {
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            permission)) {
                        needPermission = true;
                    } else {
                        needPermission = true;
                    }
                }
            }
            if (needPermission) {
                showExplanationDialog(permissions, 0);
            } else {
                activate();
            }
        }
    }
    private void activate()
    {
        //通信が走るため、別スレッドでの処理にする
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    jbcpManager = JBCPManager.getManager(MainActivity.this,
                            "アクティベーションキー",
                            "シークレットキー",
                            null);
                } catch (JBCPException e) {
                    return;
                }

                if (jbcpManager == null){
                    return;
                }
                // リスナーを登録
                jbcpManager.setUpdateEventsListener(updateEventsListener);
                jbcpManager.setShouldUpdateEventsListener(shouldUpdateEventsListener);
                jbcpManager.setFireEventListener(fireEventListener);

                // デバッグ用に使う
                /*
                JBCPManager.SCAN_MODE = 2;
                jbcpManager.setBeaconEventListener(beaconEventListener);
                */

                // イベントを更新する
                jbcpManager.startUpdateEvents();
            }
        }).start();
    }
    private void showExplanationDialog(final String permissions[], final int requestCode) {
        ActivityCompat.requestPermissions(MainActivity.this,
                permissions,requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (0 == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activate();
            }
        }
    }

    // リスナーを生成
    private UpdateEventsListener updateEventsListener = new UpdateEventsListener() {
        @Override
        public void onProgress(int i, int i1) {
                //何もしない

        }

        @Override
        public void onFinished(JBCPException e) {
            if (e != null)
            {
                jbcpManager.startScan();
            }

        }
    };

    // リスナーを生成
    private ShouldUpdateEventsListener shouldUpdateEventsListener = new ShouldUpdateEventsListener() {
        @Override
        public boolean shouldUpdate(Map<String, Object> map) {
            return true;
        }
    };

    // リスナーを生成
    private FireEventListener fireEventListener = new FireEventListener() {
        @Override
        public void fireEvent(JSONObject jsonObject) {
            JSONObject action_data = jsonObject.optJSONObject("action_data");
            String action = action_data.optString("action");


            // URLの場合
            if(action.equals("jbcp_open_url"))
            {
                Log.d("DEBUG",action_data.optString("url"));
            }
            //画像の場合
            else if(action.equals("jbcp_open_image"))
            {
                Log.d("DEBUG",action_data.optString("image"));
            }
            //カスタムの場合
            else if(action.equals("jbcp_custom_key_value"))
            {
                Log.d("DEBUG",action_data.optString("key_values"));
            }
            //テキストの場合
            else if(action.equals("jbcp_open_text"))
            {
                Log.d("DEBUG",action_data.optString("text"));
            }

        }
    };

    //　リスナーを生成
    public BeaconEventListener beaconEventListener = new BeaconEventListener() {
        @Override
        public boolean targetBeaconDetected(BeaconEvent beaconEvent) {
            // CMSで検知対象に登録されているBeacon
            Log.d("DEBUG",beaconEvent.uuid);
            return false;
        }

        @Override
        public boolean nonTargetBeaconDetected(BeaconEvent beaconEvent) {
            // CMSで検知対象になっていないBeacon
            Log.d("DEBUG",beaconEvent.uuid);
            return false;
        }
    };
}
