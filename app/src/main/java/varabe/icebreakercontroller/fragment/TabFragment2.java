package varabe.icebreakercontroller.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import varabe.icebreakercontroller.R;
import varabe.icebreakercontroller.RelayController;
import varabe.icebreakercontroller.activity.MainActivity;
import varabe.icebreakercontroller.relaybutton.HoldButton;
import varabe.icebreakercontroller.relaybutton.MutuallyExclusiveButtonContainer;
import varabe.icebreakercontroller.relaybutton.MutuallyExclusiveButtonManager;
import varabe.icebreakercontroller.relaybutton.RelayButton;
import varabe.icebreakercontroller.relaybutton.SwitchButton;
import varabe.icebreakercontroller.relaybutton.TimerButton;

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