package edu.utep.cs.cs4330.battleship;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class playFragmentBT extends Fragment {

    public playFragmentBT() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_play, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
    }

    @Override
    public void onResume(){
        super.onResume();
        ((gameActivityBT)this.getActivity()).setPlayerBoardView();
        ((gameActivityBT)this.getActivity()).setOpponentBoardView();
        ((gameActivityBT)this.getActivity()).setShotsView();

    }

}
