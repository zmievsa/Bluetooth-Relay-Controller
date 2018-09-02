package varabe.icebreakercontroller.relaybutton;

import android.os.Handler;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import varabe.icebreakercontroller.RelayController;
import varabe.icebreakercontroller.activity.MainActivity;

public class TimerButton extends SwitchButton {
    private int timeoutUntilInactive;

    public TimerButton(View view, String relayChannel, RelayController controller,
                      int timeoutUntilReenabled, int timeoutUntilInactive) {
        super(view, relayChannel, controller, timeoutUntilReenabled);
        this.timeoutUntilInactive = timeoutUntilInactive;
    }
    public TimerButton(View view, RelayController controller, int timeoutUntilReenabled, int timeoutUntilInactive) {
        this(view, getRelayChannelFromViewTag(view), controller, timeoutUntilReenabled, timeoutUntilInactive);
    }
    public TimerButton(View view, RelayController controller, int timeoutUntilInactive) {
        this(view, controller, 0, timeoutUntilInactive);
    }

    @Override
    void onActivate() {
        super.onActivate();
        setEnabled(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onDeactivate();
                setEnabled(true);
            }
        }, timeoutUntilInactive);
    }
}
