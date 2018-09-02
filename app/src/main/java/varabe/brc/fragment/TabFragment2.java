package varabe.brc.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import varabe.brc.R;
import varabe.brc.RelayController;
import varabe.brc.activity.MainActivity;
import varabe.brc.relaybutton.HoldButton;
import varabe.brc.relaybutton.MutuallyExclusiveButtonContainer;
import varabe.brc.relaybutton.MutuallyExclusiveButtonManager;
import varabe.brc.relaybutton.RelayButton;
import varabe.brc.relaybutton.SwitchButton;
import varabe.brc.relaybutton.TimerButton;

public class TabFragment2 extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_fragment_2, container, false);
        RelayController controller = MainActivity.getRelayController();
        RelayButton reverseButton = new HoldButton(v.findViewById(R.id.reverseButton), controller);
        RelayButton WBISupplyButton = new SwitchButton(v.findViewById(R.id.WBISupplyButton), controller);
        RelayButton vibratorButton = new TimerButton(v.findViewById(R.id.vibratorButton), controller, 60000, 7000);

        MutuallyExclusiveButtonContainer MEBContainer = new MutuallyExclusiveButtonContainer(
                new RelayButton[] {reverseButton, WBISupplyButton}, 0
        );
        new MutuallyExclusiveButtonManager().connectMutuallyExclusiveButtons(MEBContainer);
        MainActivity activity = (MainActivity) getActivity();
        if (activity.getDeviceName() == null)
            // This operation is done to disable all buttons and set Actionbar subtitle to nothing
            activity.setDeviceName(null);
        return v;
    }
}