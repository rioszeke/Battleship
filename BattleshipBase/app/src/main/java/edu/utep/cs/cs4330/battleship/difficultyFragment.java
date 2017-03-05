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

    private Button fragment_btn_1;
    private Button fragment_btn_2;
    private OnFragmentInteractionListener mListener;

    private final List<DifficultySelectListener> listeners = new ArrayList<>();

    public difficultyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_difficulty, container, false);
        fragment_btn_1 = (Button) view.findViewById(R.id.fragment_button_1);
        fragment_btn_2 = (Button) view.findViewById(R.id.fragment_button_2);
        fragment_btn_1.setOnClickListener(this);
        fragment_btn_2.setOnClickListener(this);
        return view;
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

    public void onClick(View v){
        switch(v.getId()){
            case R.id.fragment_button_1:
                //Fragment fragment1 = new BlankFragment();
                //moveToFragment(fragment1);
                notifyDifficulty("Easy");
                break;
            case R.id.fragment_button_2:
//                Fragment fragment2 = new BlankFragment2();
//                moveToFragment(fragment2);
                notifyDifficulty("Hard");
                break;
        }
    }

    public interface DifficultySelectListener{

        public void difficultySelected(String string);
    }

    /** Register the given listener. */
    public void addDifficultyListener(DifficultySelectListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void notifyDifficulty(String string){
        for (DifficultySelectListener listener: listeners) {
            listener.difficultySelected(string);
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
