package varabe.brc.relaybuttons;

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
public class BlinkingButton extends RelayButton {

    private TimerTask task;
    private Timer timer = new Timer();

    public BlinkingButton(View view, boolean hasCustomBehavior, RelayController controller) {
        super(view, hasCustomBehavior, getRelayChannelFromViewTag(view), controller);
    }

    @Override
    public void onPress() {
        super.onPress();
        scheduleRelayBlinkSequence();
    }
    @Override
    public void onRelease() {
        super.onRelease();
        stopRelayBlinkSequence();
    }

    private void scheduleRelayBlinkSequence() {
        task = new oneSecondBlinkExecutorTask();
        timer.scheduleAtFixedRate(task, 0, 400);
    }

    private void stopRelayBlinkSequence() {
        task.cancel();
        getController().sendCommand(getView(), COMMAND_OPEN);
        // setEnabled(false);
    }
    private class oneSecondBlinkExecutorTask extends TimerTask {
        public void run() {
            getController().sendCommand(getView(), COMMAND_ONE_SECOND_BLINK);
        }
    }
}
