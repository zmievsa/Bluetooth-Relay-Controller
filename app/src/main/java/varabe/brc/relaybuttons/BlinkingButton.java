package varabe.brc.relaybuttons;

import android.os.CountDownTimer;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import varabe.brc.RelayController;

import static varabe.brc.RelayController.COMMAND_ONE_SECOND_BLINK;
import static varabe.brc.RelayController.COMMAND_OPEN;

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
//        new CountDownTimer(1000, 1000) {
//            // When board is still evaluating the last 1 second command (which is very rare),
//            // it will result in a bug that will leave one of the relays active. If we wait for
//            // the board to finish, the bug has no chance of occurring. 1000 millis is the worst
//            // case scenario
//            @Override
//            public void onTick(long l) {}
//            @Override
//            public void onFinish() {
//                setEnabledAllButtons(true);
//            }
//        }.start();
    }
    private class oneSecondBlinkExecutorTask extends TimerTask {
        public void run() {
            getController().sendCommand(getView(), COMMAND_ONE_SECOND_BLINK);
        }
    }
}
