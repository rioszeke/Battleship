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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link difficultyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class difficultyFragment extends Fragment implements View.OnClickListener{

    private Button fragment_difficult_btn;
    private Button fragment_easy_btn;
    private Button fragment_done_btn;
    private SelectShipsView shipsView;
    private PlacedShipsView placedShipsView;

    private OnFragmentInteractionListener mListener;

    private final List<ButtonSelectListener> listeners = new ArrayList<>();

    public difficultyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_difficulty, container, false);
        fragment_difficult_btn = (Button) view.findViewById(R.id.fragment_difficult_btn);
        fragment_easy_btn = (Button) view.findViewById(R.id.fragment_easy_btn);
        fragment_done_btn = (Button) view.findViewById(R.id.fragment_done_btn);
        fragment_difficult_btn.setOnClickListener(this);
        fragment_easy_btn.setOnClickListener(this);
        fragment_done_btn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
//        try {
        shipsView = ((gameActivity) this.getActivity()).getSelectShipsView();
        placedShipsView = ((gameActivity) this.getActivity()).getPlacedShipsView();

//            playerBoard = ((gameActivity) this.getActivity()).getPlayerBoard();
//            opponentBoard = ((gameActivity) this.getActivity()).getOpponentBoard();

//        }catch(Exception e){
//            if(playerBoardView == null){
//                System.out.println("playerBoardView was null");
//            }
//            if(opponentBoardView == null){
//                System.out.println("opponentBoardView was null");
//            }
//            if(playerBoard == null){
//                System.out.println("playerBoard was null");
//            }
//            if(opponentBoard == null){
//                System.out.println("opponentBoard was null");
//            }
//        }

    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.fragment_easy_btn:
                //Fragment fragment1 = new BlankFragment();
                //moveToFragment(fragment1);
                notifyButtonClick("Easy");
                break;
            case R.id.fragment_difficult_btn:
//                Fragment fragment2 = new BlankFragment2();
//                moveToFragment(fragment2);
                notifyButtonClick("Difficult");
                break;
            case R.id.fragment_done_btn:
                notifyButtonClick("Done");
                break;
        }
    }

    public interface ButtonSelectListener{

        public void ButtonSelected(String string);
    }

    /** Register the given listener. */
    public void addButtonListener(ButtonSelectListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void notifyButtonClick(String string){
        for (ButtonSelectListener listener: listeners) {
            listener.ButtonSelected(string);
        }
    }
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
