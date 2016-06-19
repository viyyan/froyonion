package id.co.froyo.froyonion.helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.co.froyo.froyonion.R;

/**
 * Created by Fian on 6/9/16.
 */
public class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    private ArrayList<byte[]> scanDevices;
    private LayoutInflater mInflator;
    private Context context;
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();


    public LeDeviceListAdapter(Context mContext) {
        super();
        mLeDevices = new ArrayList<BluetoothDevice>();
        scanDevices = new ArrayList<byte[]>();
        mInflator = (LayoutInflater.from(mContext));
    }

    public void addDevice(BluetoothDevice device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public void addRecord(byte[] device) {
        if(!scanDevices.contains(device)) {
            scanDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int pos) {
        return mLeDevices.get(pos);
    }


    public double getDistance(int rssi, int txPower) {
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

    public void clear(){
//        mLeDevices.clear();
          scanDevices.clear();
    }



    @Override
    public int getCount() {
//        return mLeDevices.size();
        return scanDevices.size();
    }

    @Override
    public Object getItem(int position) {
//        return mLeDevices.get(position);
        return scanDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null) {
            view = mInflator.inflate(R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        HashMap<String,String> getScaned = getScanDevice(scanDevices.get(position));
        viewHolder.deviceName.setText(getScaned.get("UUID"));
        viewHolder.deviceAddress.setText(getScaned.get("major")+" - "+getScaned.get("minor"));
//        BluetoothDevice device = mLeDevices.get(position);
//        final String deviceName = device.getName();

//        if (deviceName != null && deviceName.length() > 0 )
//            viewHolder.deviceName.setText(deviceName);
//        else
//            viewHolder.deviceName.setText(R.string.unknown_device);
//        viewHolder.deviceAddress.setText(device.getAddress());

//        getDistance(device.)
        return view;
    }

    private class ViewHolder {
        TextView deviceAddress;
        TextView deviceName;
    }

    public HashMap<String, String> getScanDevice(byte[] scanRecord) {
        int startByte = 2;
        boolean patternFound = false;
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
            int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

            //Here is your Minor value
            int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);


            device.put("UUID", uuid);
            device.put("major", String.valueOf(major));
            device.put("minor", String.valueOf(minor));

        }
        return device;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++){
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


}
