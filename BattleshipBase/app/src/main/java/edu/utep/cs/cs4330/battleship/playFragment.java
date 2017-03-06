package edu.utep.cs.cs4330.battleship;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class playFragment extends Fragment {

    private BoardView playerBoardView;
    private BoardView opponentBoardView;
    private TextView numShots;
    private Board playerBoard;
    private Board opponentBoard;

    public playFragment() {
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
//        playerBoardView = ((gameActivity)this.getActivity()).getPlayerBoardView();
//        opponentBoardView = ((gameActivity)this.getActivity()).getOpponentBoardView();
//
//        playerBoardView.setBoard(((gameActivity)this.getActivity()).getPlayerBoard(), true);
//        opponentBoardView.setBoard(((gameActivity)this.getActivity()).getOpponentBoard(), false);
//
//        numShots = ((gameActivity)this.getActivity()).getShotsView();
    }

    @Override
    public void onResume(){
        super.onResume();
//        try {
            playerBoardView = ((gameActivity) this.getActivity()).getPlayerBoardView();
            opponentBoardView = ((gameActivity) this.getActivity()).getOpponentBoardView();

//            playerBoard = ((gameActivity) this.getActivity()).getPlayerBoard();
//            opponentBoard = ((gameActivity) this.getActivity()).getOpponentBoard();

            numShots = ((gameActivity) this.getActivity()).getShotsView();
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
}
