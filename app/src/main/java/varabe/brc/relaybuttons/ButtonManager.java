package varabe.brc.relaybuttons;

import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import varabe.brc.RelayController;

import static varabe.brc.RelayController.COMMAND_CLOSE;
import static varabe.brc.RelayController.COMMAND_ONE_SECOND_BLINK;
import static varabe.brc.RelayController.COMMAND_OPEN;
import static varabe.brc.activity.MainActivity.COLOR_GRAY;
import static varabe.brc.activity.MainActivity.COLOR_RED;

/*
 * Hold button implementation is based on "oneSecondBlinkSequence" which, instead of sending
 * "COMMAND_SWITCH" twice (on press and on release), sends "COMMAND_ONE_SECOND_BLINK" continuously.
 * The reason is: if device turns off or bluetooth connection is broken while a user holds a button,
 * the corresponding relay channel will stay active which might be dangerous. So, in this
 * implementation, if relay board does not get any new requests, it turns a relay off automatically
 */
public class ButtonManager {
    private final View.OnTouchListener holdingButtonListener = new HoldButtonListener();
    private final View.OnClickListener switchButtonListener = new SwitchButtonListener();
    private Timer timer = new Timer();

    private HashMap<Integer, RelayButton> buttons;
    private Set<Set<RelayButton>> mutuallyExclusiveButtonSets;
    private RelayController controller;

    public ButtonManager(RelayController controller) {
        this.buttons = new HashMap<>();
        this.mutuallyExclusiveButtonSets = new HashSet<>();
        this.controller = controller;
    }
    private Collection<RelayButton> getButtons() {
        return buttons.values();
    }
    @Nullable
    private RelayButton getButtonByView(View view) {
        return buttons.get(view.getId());
    }

    public void addHoldingButton(RelayButton button) {
        button.getView().setOnTouchListener(holdingButtonListener);
        addButton(button);
    }
    public void addSwitchButton(RelayButton button) {
        button.getView().setOnClickListener(switchButtonListener);
        addButton(button);
    }

    private void addButton(RelayButton button) {
        buttons.put(button.getView().getId(), button);
    }

    // Mutually exclusive buttons
    public void connectMutuallyExclusiveButtons(Set<RelayButton> buttons) {
        mutuallyExclusiveButtonSets.add(buttons);
    }
    private void disableMutuallyExclusiveButtons(RelayButton queriedButton) {
        Set<RelayButton> buttons = findMutuallyExclusiveButtons(queriedButton);
        for (RelayButton button : buttons) {
            button.setEnabled(false);
        }
    }
    private Set<RelayButton> findMutuallyExclusiveButtons(RelayButton queriedButton) {
        for (Set<RelayButton> buttonSet : mutuallyExclusiveButtonSets) {
            for (RelayButton button : buttonSet) {
                if (queriedButton.equals(button))
                    return buttonSet;
            }
        }
        return new HashSet<>(); // Might need to be optimized
    }

    // Enabling/disabling buttons
    public void setEnabledAllButtons(boolean enabled) {
        for (RelayButton button: getButtons()) {
            setEnabled(button, enabled);
        }
    }
    public void setEnabledAllButtonsExcept(View view, boolean enabled) {
        for (RelayButton button: getButtons()) {
            if (!view.equals(button.getView())) {
                setEnabled(button, enabled);
            }
        }
    }
    public void setEnabled(RelayButton button, boolean enabled) {
        button.setEnabled(enabled);
    }
    // Timer management classes and methods
    private class HoldButtonListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) { // removed check for currentTask == null
                view.setBackgroundColor(COLOR_RED);
                getButtonByView(view).onPress();
            } else if (action == MotionEvent.ACTION_UP) { // removed check for currentTask != null
                view.setBackgroundColor(COLOR_GRAY);
                getButtonByView(view).onRelease();
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
                getButtonByView(view).onPress();
            } else {
                view.setBackgroundColor(COLOR_GRAY);
                controller.sendCommand(view, COMMAND_OPEN);
                getButtonByView(view).onRelease();
            }
        }
    }
}
