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
import varabe.icebreakercontroller.relaybutton.RelayButton;
import varabe.icebreakercontroller.relaybutton.SwitchButton;

public class TabFragment1 extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_fragment_1, container, false);
        RelayController controller = MainActivity.getRelayController();
        RelayButton enableDisableButton = new SwitchButton(v.findViewById(R.id.enableDisableButton), controller);
        RelayButton suckOutButton = new SwitchButton(v.findViewById(R.id.suckOutButton), controller);
        RelayButton starterButton = new HoldButton(v.findViewById(R.id.starterButton), controller);
        RelayButton lightButton = new SwitchButton(v.findViewById(R.id.lightButton), controller);
        RelayButton beaconButton = new SwitchButton(v.findViewById(R.id.beaconButton), controller);
        return v;
    }
}