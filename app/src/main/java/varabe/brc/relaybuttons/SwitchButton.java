package varabe.brc.relaybuttons;

import android.view.View;

import varabe.brc.RelayController;

public class SwitchButton extends RelayButton {
    private boolean isActivated;

    public SwitchButton(View view, String relayChannel, RelayController controller, int timeout) {
        super(view, relayChannel, controller, timeout);
        isActivated = false;
        view.setOnClickListener(new SwitchButtonListener());
    }
    public SwitchButton(View view, RelayController controller, int timeout) {
        this(view, getRelayChannelFromViewTag(view), controller, timeout);
    }
    public SwitchButton(View view, RelayController controller) {
        this(view, controller, 0);
    }

    private class SwitchButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (isActivated) {
                onDeactivate();
            } else {
                onActivate();
            }
            isActivated = !isActivated;
        }
    }
}
