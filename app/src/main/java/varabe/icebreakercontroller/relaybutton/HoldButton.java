package varabe.icebreakercontroller.relaybutton;

import android.view.MotionEvent;
import android.view.View;

import varabe.icebreakercontroller.RelayController;

public class HoldButton extends RelayButton {

    public HoldButton(View view, String relayChannel, RelayController controller, int timeout) {
        super(view, relayChannel, controller, timeout);
        view.setOnTouchListener(new HoldButtonListener());
    }
    public HoldButton(View view, RelayController controller, int timeout) {
        this(view, getRelayChannelFromViewTag(view), controller, timeout);
    }
    public HoldButton(View view, RelayController controller) {
        this(view, controller, 0);
    }

    private class HoldButtonListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                onActivate();
            } else if (action == MotionEvent.ACTION_UP) {
                onDeactivate();
            }
            return true;
        }
    }
}
