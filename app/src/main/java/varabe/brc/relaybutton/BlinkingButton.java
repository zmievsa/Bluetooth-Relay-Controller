package varabe.brc.relaybutton;

import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import varabe.brc.RelayController;

import static varabe.brc.RelayController.COMMAND_ONE_SECOND_BLINK;
import static varabe.brc.RelayController.COMMAND_OPEN;

/*
 * Blinking button implementation is based on "oneSecondBlinkSequence" which, instead of sending
 * "COMMAND_SWITCH" twice (on press and on release), sends "COMMAND_ONE_SECOND_BLINK" continuously.
 * The reason is: if device turns off or bluetooth connection is broken while user holds a button,
 * the corresponding relay channel will stay active which might be dangerous. So, in this
 * implementation, if relay board does not get any new requests, it turns a relay off automatically
 */
public class BlinkingButton extends HoldButton {

    private TimerTask task;

    public BlinkingButton(View view, RelayController controller, int timeout) {
        super(view, getRelayChannelFromViewTag(view), controller, timeout);
    }
    public BlinkingButton(View view, RelayController controller) {
        this(view, controller, 0);
    }

    @Override
    public void activate() {
        scheduleRelayBlinkSequence();
    }
    @Override
    public void deactivate() {
        stopRelayBlinkSequence();
    }

    private void scheduleRelayBlinkSequence() {
        task = new oneSecondBlinkExecutorTask();
        new Timer().scheduleAtFixedRate(task, 0, 400); // Might need to be optimized
    }

    private void stopRelayBlinkSequence() {
        task.cancel();
        getController().sendCommand(getView(), COMMAND_OPEN);
    }
    private class oneSecondBlinkExecutorTask extends TimerTask {
        public void run() {
            getController().sendCommand(getView(), COMMAND_ONE_SECOND_BLINK);
        }
    }
}
