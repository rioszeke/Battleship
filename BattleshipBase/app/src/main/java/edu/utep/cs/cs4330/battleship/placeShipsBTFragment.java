package edu.utep.cs.cs4330.battleship;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class placeShipsBTFragment extends Fragment {


    public placeShipsBTFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_place_ships_bt, container, false);
    }

    @Override
    public void onResume(){
        super.onResume();
        ((gameActivityBT) this.getActivity()).setSelectShipsView();
        ((gameActivityBT) this.getActivity()).setPlacedShipsView();
    }

}
