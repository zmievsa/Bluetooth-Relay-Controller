package varabe.brc;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static varabe.brc.RelayController.COMMAND_CLOSE;
import static varabe.brc.RelayController.COMMAND_ONE_SECOND_BLINK;
import static varabe.brc.RelayController.COMMAND_OPEN;
import static varabe.brc.activity.MainActivity.COLOR_GRAY;
import static varabe.brc.activity.MainActivity.COLOR_RED;

/*
 * Holding button implementation is based on "oneSecondBlinkSequence" which, instead of sending
 * "COMMAND_SWITCH" twice (on press and on release), sends "COMMAND_ONE_SECOND_BLINK" continuously.
 * The reason is: if device turns off or bluetooth connection is broken while a user holds a button,
 * the corresponding relay channel will stay active which might be dangerous. So, in this
 * implementation, if relay board does not get any new requests, it turns a relay off automatically
 */
public class ButtonManager {
    private final View.OnTouchListener holdingButtonListener = new HoldingButtonListener();
    private final View.OnClickListener switchButtonListener = new SwitchButtonListener();
    private Timer timer = new Timer();

    private CommandOneSecondBlinkExecutorTask currentTask;
    private HashMap<Integer, RelayButton> buttons;
    private Set<Set<Integer>> mutuallyExclusiveButtonSets;
    private RelayController controller;

    public ButtonManager(RelayController controller) {
        this.buttons = new HashMap<>();
        this.mutuallyExclusiveButtonSets = new HashSet<>();
        this.controller = controller;
    }
    private Collection<RelayButton> getButtons() {
        return buttons.values();
    }

    public void addHoldingButton(View view) {
        view.setOnTouchListener(holdingButtonListener);
        addButton(view);
    }
    public void addSwitchButton(View view) {
        view.setOnClickListener(switchButtonListener);
        addButton(view);
    }
    private void addButton(View view) {
        buttons.put(view.getId(), new RelayButton(view));
    }

    public void connectMutuallyExclusiveButtons(Set<Integer> viewIds) {
        mutuallyExclusiveButtonSets.add(viewIds);
    }

    public void setEnabledAllButtons(boolean enabled) {
        for (RelayButton button: getButtons()) {
            setEnabled(button.getView(), enabled);
        }
    }
    public void setEnabledAllButtonsExcept(View view, boolean enabled) {
        for (RelayButton button: getButtons()) {
            if (!view.equals(button.getView())) {
                setEnabled(button.getView(), enabled);
            }
        }
    }
    public void setEnabled(View view, Boolean enabled) {
        if (view instanceof ImageView)
            setEnabled((ImageView) view, enabled);
        else if (view instanceof Button)
            view.setEnabled(enabled);
        else
            throw new UnsupportedOperationException("View of type \"" + view.getClass() + "\" is not supported");
    }
    private void setEnabled(ImageView view, Boolean enabled) {
        view.setEnabled(enabled);
        if (enabled)
            view.setColorFilter(null);
        else
            view.setColorFilter(Color.argb(255,150,150,150));
    }
    @Nullable
    private RelayButton getButtonByView(View view) {
        return buttons.get(view.getId());
    }
    // Timer management classes and methods
    private class HoldingButtonListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) { // removed check for currentTask == null
                view.setBackgroundColor(COLOR_RED);
                scheduleRelayBlinkSequence(view);
            } else if (action == MotionEvent.ACTION_UP) { // removed check for currentTask != null
                view.setBackgroundColor(COLOR_GRAY);
                stopRelayBlinkSequence(view);

            }
            return true;
        }
    }

    private class SwitchButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (((ColorDrawable) view.getBackground()).getColor() == COLOR_GRAY) {
                view.setBackgroundColor(COLOR_RED);
                controller.sendCommand(view, COMMAND_CLOSE);
            } else {
                view.setBackgroundColor(COLOR_GRAY);
                controller.sendCommand(view, COMMAND_OPEN);
            }
        }
    }

    private class CommandOneSecondBlinkExecutorTask extends TimerTask {
        private View view;

        CommandOneSecondBlinkExecutorTask(View view) {
            super();
            this.view = view;
        }

        public void run() {
            controller.sendCommand(view, COMMAND_ONE_SECOND_BLINK);
        }

        public View getView() {
            return view;
        }
    }

    private void scheduleRelayBlinkSequence(View view) {
        currentTask = new CommandOneSecondBlinkExecutorTask(view);
        timer.scheduleAtFixedRate(currentTask, 0, 400);
        setEnabledAllButtonsExcept(view, false);
    }

    private void stopRelayBlinkSequence(final View view) {
        if (currentTask.getView().equals(view)) {
            currentTask.cancel();
            new CountDownTimer(1000, 1000) {
                // When board is still evaluating the last 1 second command (which is very rare),
                // it will result in a bug that will leave one of the relays active. If we wait for
                // the board to finish, the bug has no chance of occurring. 1000 millis is the worst
                // case scenario
                @Override
                public void onTick(long l) {}

                @Override
                public void onFinish() {
                    currentTask = null;
                    setEnabledAllButtons(true);
                }
            }.start();

            controller.sendCommand(view, COMMAND_OPEN);
            setEnabled(view, false);
        }
    }
}
