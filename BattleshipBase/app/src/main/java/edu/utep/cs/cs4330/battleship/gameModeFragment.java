package edu.utep.cs.cs4330.battleship;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;


public class gameModeFragment extends Fragment implements View.OnClickListener{

    private Button p2pBtn;
    private Button stratBtn;
    private final List<ModeSelectListener> listeners = new ArrayList<>();


    private OnFragmentInteractionListener mListener;

    public interface ModeSelectListener{

        public void ModeSelected(String mode);
    }

    public gameModeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_mode, container, false);
        p2pBtn = (Button) view.findViewById(R.id.p2pBtn);
        stratBtn = (Button) view.findViewById(R.id.stratBtn);
        p2pBtn.setOnClickListener(this);
        stratBtn.setOnClickListener(this);
        return view;

    }

    public void onClick(View view){
        switch(view.getId()){
            case R.id.p2pBtn:
                notifyButtonClick("p2p");
                break;
            case R.id.stratBtn:
                notifyButtonClick("strategy");
                break;
        }
    }

    protected void addButtonListener(ModeSelectListener listener){
        if(!listeners.contains(listener)){
            listeners.add(listener);
        }
    }

    public void notifyButtonClick(String string){
        for (ModeSelectListener listener: listeners) {
            listener.ModeSelected(string);
        }
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
