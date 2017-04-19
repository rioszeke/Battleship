package edu.utep.cs.cs4330.battleship;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class bluetoothSetUpFragment extends Fragment implements AdapterView.OnItemClickListener{

    private ListView pairView;
    private Button enableBtn;
    private Button settingsBtn;
    private BluetoothAdapter bt;
    private ArrayAdapter<String> pairList;
    private ArrayList<String> pairs;
    private List<listItemSelectListener> listeners = new ArrayList<>();

    public bluetoothSetUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bt = BluetoothAdapter.getDefaultAdapter();
        pairs = new ArrayList<String>();
        pairList = new ArrayAdapter<String>(getActivity(), R.layout.pair_text,pairs);
        View view =  inflater.inflate(R.layout.fragment_bluetooth_set_up, container, false);
        pairView = (ListView) view.findViewById(R.id.pair_List_View);
        enableBtn = (Button) view.findViewById(R.id.enable_BT_btn);
        settingsBtn = (Button) view.findViewById(R.id.settings_btn);
        enableBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                enableClicked(v);
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                settingsClicked(v);
            }
        });
        pairView.setOnItemClickListener(this);
        if(isBTEnabled()) {
            Set<BluetoothDevice> devices = bt.getBondedDevices();
            for (BluetoothDevice device : devices) {
                pairs.add(device.getName());
            }
        }

        pairView.setAdapter(pairList);

        return view;
    }
    private Boolean isBTEnabled(){
        return bt != null && bt.isEnabled();
    }
    public void enableClicked(View view){
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (bt != null && !bt.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(enableBtIntent, 1);
        }

    }
    public ArrayList<String> getPairs(){
        return pairs;
    }
    public void settingsClicked(View view){
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent);

    }


    public ArrayAdapter<String> getPairList(){
        return pairList;
    }

    public interface listItemSelectListener{

        public void listItemSelected(int position, long id);
    }

    public void addListItemSelectListener(listItemSelectListener listener){
        if(!listeners.contains(listener)){
            listeners.add(listener);
        }
    }

    public void notifyListItemSelected(int position, long id){
        for(listItemSelectListener listener : listeners){
            listener.listItemSelected(position, id);
        }
    }



    public void onItemClick(AdapterView<?> l, View v, int position, long id){
        String deviceName = pairs.get(position);
        Set<BluetoothDevice> devices = bt.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getName().equals(deviceName)) {
                Log.d(TAG,deviceName);
                final BluetoothDevice clientDevice = device;
                //Server
//                AsyncTask.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            BluetoothServerSocket serverSocket
//                                    = bt.listenUsingRfcommWithServiceRecord(MY_NAME, MY_UUID);
//                            BluetoothSocket socket = serverSocket.accept();
//                            running = true;
////                            while(running){
////                                //maintain connection
////                            }
//                            socket.close();
//                            serverSocket.close();
//                        } catch (IOException e) {/*already a server so move on*/)}                    }
//                });
                notifyListItemSelected(position, id);
//                AsyncTask.execute(new Runnable() {
//                  @Override
//                  public void run() {
//                      BluetoothSocket socket = null;
//                      try {
//                          socket = clientDevice.createRfcommSocketToServiceRecord(MY_UUID);
//                          socket.connect();
//                          socket.close();
//                      } catch (IOException e) {
//                      }
//                  }
//            }

            }
        }

    }



    protected void toast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isBTEnabled()) {
            Set<BluetoothDevice> devices = bt.getBondedDevices();
            for (BluetoothDevice device : devices) {
                if(!pairs.contains(device.getName())) {
                    pairs.add(device.getName());
                }
            }
            pairList.notifyDataSetChanged();
        }
    }
}
