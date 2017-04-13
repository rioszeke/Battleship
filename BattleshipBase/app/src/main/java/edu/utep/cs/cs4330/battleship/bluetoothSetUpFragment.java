package edu.utep.cs.cs4330.battleship;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class bluetoothSetUpFragment extends Fragment implements AdapterView.OnItemClickListener{

    private ListView pairView;
    private Button enableBtn;
    private Button settingsBtn;

    private ArrayAdapter<String> pairList;

    private List<listItemSelectListener> listeners;

    public bluetoothSetUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        pairList = new ArrayAdapter<String>(getActivity(), R.layout.pair_text);
        pairList.add("Zeke's phone");
        pairList.add("David's phone");
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_bluetooth_set_up, container, false);
        pairView = (ListView) view.findViewById(R.id.pair_List_View);
        enableBtn = (Button) view.findViewById(R.id.enable_BT_btn);
        settingsBtn = (Button) view.findViewById(R.id.settings_btn);
        pairView.setOnItemClickListener(this);
        pairView.setAdapter(pairList);
        return view;
    }

    public void addToPairList(String pairTxt){
        pairList.add(pairTxt);
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
        Log.d("btSetUpFrag.onItemClick", "clicked item: " + id +" at position: " + position);
        String msg = "clicked item: "+id+" at position: " + position;
        toast(msg);
        String itemName;
        notifyListItemSelected(position, id);
    }



    protected void toast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }


}
