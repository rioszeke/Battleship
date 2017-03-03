package edu.utep.cs.cs4330.battleship;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Fragment class which extends Dialog Fragment. This class is meant
 * to act as a prompt to end game or continue
 */

public class MyDialogFragment extends DialogFragment {

    private Button yesButton;
    private Button noButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_prompt_dialog, container, false);
        getDialog().setTitle("NewGamePrompt");

        yesButton = (Button) rootView.findViewById(R.id.yesButton);
        noButton = (Button) rootView.findViewById(R.id.noButton);

        yesButton.setEnabled(true);
        noButton.setEnabled(true);

        return rootView;
    }


}
