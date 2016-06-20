package id.co.froyo.froyonion;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.froyo.froyonion.helper.AppController;
import id.co.froyo.froyonion.helper.CustomRequest;
import id.co.froyo.froyonion.helper.LeDeviceListAdapter;
import id.co.froyo.froyonion.helper.SessionManager;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private ListView listDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLeScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private Context mContext;
    private SessionManager sessionManager;
    private HashMap<String, String> userData;
    private TextView name, timeCheck, status, lastCheck;
    private Button mainButton;
    private ProgressBar progressBar;
    private String UUID = "UUID", major = "major", minor = "minor", dist = "distance";
    private String fixUUID = "CB10023F-A318-3394-4199-A8730C7C1AEC";
    private String fixMajor = "3", fixMinor = "284";
    private int range = 30; //meters
    private TextClock textClock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listDevices = (ListView) findViewById(R.id.list_devices);
        name = (TextView) findViewById(R.id.mainName);
        timeCheck = (TextView) findViewById(R.id.mainTime);
        status = (TextView) findViewById(R.id.mainStatus);
        lastCheck = (TextView) findViewById(R.id.lastCheck);
        lastCheck.setText("Click Checkin Button");

        textClock = (TextClock) findViewById(R.id.textCLock);

        status.setText("Kamu berada di luar area kantor");

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mainButton = (Button) findViewById(R.id.mainButton);

        mContext = this.getApplicationContext();
        sessionManager = new SessionManager(mContext);
        sessionManager.checkLogin();


        userData = sessionManager.getUserData();
        if(userData != null && userData.get(sessionManager.KEY_NAME) != null) {
            name.setText(userData.get(sessionManager.KEY_NAME));
        }
        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();



        mLeDeviceListAdapter = new LeDeviceListAdapter(mContext);
        listDevices.setAdapter(mLeDeviceListAdapter);
//        listDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(mContext, "Connecting to " + mLeDeviceListAdapter.getDevice(position), Toast.LENGTH_SHORT).show();
//                connectToDevice(mLeDeviceListAdapter.getDevice(position));
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_logout) {
            sessionManager.clearSession();
            return true;
        } else if (id== R.id.action_refresh) {
            scanLeDevice(true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled() ){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
        }


        if(sessionManager.isCheckedIn()){
            mainButton.setText("Checkout");
            mainButton.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorCheckout));
            mainButton.setOnClickListener(new OnChekoutClick());
            timeCheck.setText(sessionManager.getTimeChecked());
            lastCheck.setText("Checked in at:");
        } else {
            mainButton.setText("Checkin");
            mainButton.setBackgroundColor(ContextCompat.getColor(mContext,R.color.colorCheckin));
            mainButton.setOnClickListener(new OnCheckinClick());
            timeCheck.setText(sessionManager.getTimeChecked());
            lastCheck.setText("Checked out at:");
        }


    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        scanLeDevice(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // bluetooth not enabled.
                finish();
                return;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            status.setText("Scanning...");
            progressBar.setVisibility(View.VISIBLE);
            mainButton.setVisibility(View.GONE);

            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else  {
                        mLeScanner.stopScan(mScanCallback);
                    }
                }
            }, SCAN_PERIOD);

            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLeScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLeScanner.stopScan(mScanCallback);
            }
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            byte[] scanRecord = result.getScanRecord().getBytes();
            int rssi = result.getRssi();

            int powerTx = result.getScanRecord().getTxPowerLevel();
            HashMap<String, String> devices = getScanDevice(scanRecord, rssi, powerTx);
            double distance = calculateDistance(powerTx, rssi);
            if(devices.get(UUID) != null) {
                if (devices.get(UUID).equals(fixUUID) && devices.get(major).equals(fixMajor) && devices.get(minor).equals(fixMinor)) {
                    if (distance < range) {
                        Log.i("distanceOk", String.valueOf(distance));
                        status.setText("Kamu berada di area kantor");
                        mainButton.setVisibility(View.VISIBLE);

                    } else {
                        status.setText("Kamu berada di luar area kantor");
                        mainButton.setVisibility(View.GONE);
                    }
                }
            }
//            mLeDeviceListAdapter.addRecord(scanRecord);
//            mLeDeviceListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: "+ errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("onLeScan", device.toString());

//                    mLeDeviceListAdapter.addRecord(scanRecord);
//                    mLeDeviceListAdapter.notifyDataSetChanged();
//                    mLeDeviceListAdapter.addDevice(device);
//                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });

        }
    };


    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            Log.i("Gatt Connected", mGatt.getDevice().getAddress());
            scanLeDevice(false);
        } else {
            mGatt.disconnect();
            mGatt = device.connectGatt(this, false, gattCallback);
            Log.i("New Connected", mGatt.getDevice().getAddress());
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: "+status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServiceDiscovered", services.toString());
            gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
        }
    };

    public HashMap<String, String> getScanDevice(byte[] scanRecord, int rssi, int txPower) {
        int startByte = 2;
        boolean patternFound = false;
        double distance = calculateDistance(txPower, rssi);

        HashMap<String, String> device = new HashMap<>();
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 &&
                    ((int) scanRecord[startByte + 3] & 0xff) == 0x15) {
                patternFound = true;
                break;

            }
            startByte++;
        }
        if (patternFound) {
//                convert to hex String
            byte[] uuidBytes = new byte[16];
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
            String hexString = bytesToHex(uuidBytes);

//               UUID
            String uuid = hexString.substring(0, 8) + "-" +
                    hexString.substring(8, 12) + "-" +
                    hexString.substring(12, 16) + "-" +
                    hexString.substring(16, 20) + "-" +
                    hexString.substring(20, 32);
            //Here is your Major value
            int majorIn = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

            //Here is your Minor value
            int minorIn = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);


            device.put(UUID, uuid);
            device.put(major, String.valueOf(majorIn));
            device.put(minor, String.valueOf(minorIn));
            device.put(dist, String.valueOf(distance));

        }
        return device;
    }

    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++){
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

//    measure distance
    protected static double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine distance, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

    private class OnCheckinClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final String authToken  = userData.get(sessionManager.KEY_TOKEN);
            Log.i("token", authToken);
            String url = "https://api.froyonion.com/event/checkin";
            String tag = "json_checkin_obj_req";
            CustomRequest jsonObjectRequest = new CustomRequest(Request.Method.POST, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if(response.has("data")){
                                try {
                                    JSONObject data = response.getJSONObject("data");
                                    Log.i("data", data.toString());
                                    sessionManager.checkedIn(data.getString("createdAt"));
                                    onResume();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(mContext, "Poor Network", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof AuthFailureError) {
                        Toast.makeText(mContext, "Auth Token Expired", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(mContext, "Kesalahan Server", Toast.LENGTH_SHORT).show();
                    }
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer "+authToken);
                    return headers;
                }

            };
            AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag);
        }
    }

    private class OnChekoutClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final String authToken  = userData.get(sessionManager.KEY_TOKEN);
            Log.i("token", authToken);
            String url = "https://api.froyonion.com/event/checkout";
            String tag = "json_checkout_obj_req";
            CustomRequest jsonObjectRequest = new CustomRequest(Request.Method.POST, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if(response.has("data")){
                                try {
                                    JSONObject data = response.getJSONObject("data");
                                    Log.i("data", data.toString());
                                    sessionManager.checkedOut(data.getString("createdAt"));
                                    onResume();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(mContext, "Poor Network", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof AuthFailureError) {
                        Toast.makeText(mContext, "Auth Token Expired", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError) {
                        Toast.makeText(mContext, "Kesalahan Server", Toast.LENGTH_SHORT).show();
                    }
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
//                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer "+authToken);
                    return headers;
                }

            };
            AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag);
        }
    }
}
