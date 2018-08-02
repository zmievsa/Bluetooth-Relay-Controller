package varabe.brc.relaybuttons;

import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;

import varabe.brc.RelayController;

import static varabe.brc.RelayController.COMMAND_CLOSE;
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
    private ArrayList<RelayButton[]> mutuallyExclusiveButtonSets;
    private RelayController controller;

    public ButtonManager(RelayController controller) {
        this.buttons = new HashMap<>();
        this.mutuallyExclusiveButtonSets = new ArrayList<>();
        this.controller = controller;
    }
    private Collection<RelayButton> getButtons() {
        return buttons.values();
    }
    private RelayButton getButtonByView(View view) {
        return buttons.get(view.getId());
    }

    public void addHoldButton(RelayButton button) {
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
    public void connectMutuallyExclusiveButtons(RelayButton[] buttons) {
        mutuallyExclusiveButtonSets.add(buttons);
    }
    private void disableMutuallyExclusiveButtons(RelayButton queriedButton) {
        RelayButton[] buttons = findMutuallyExclusiveButtons(queriedButton);
        for (RelayButton button : buttons) {
            button.setEnabled(false);
        }
    }
    private void enableMutuallyExclusiveButtons(RelayButton queriedButton) {
        RelayButton[] buttons = findMutuallyExclusiveButtons(queriedButton);
        for (RelayButton button : buttons) {
            button.setEnabled(true);
        }
    }
    private RelayButton[] findMutuallyExclusiveButtons(RelayButton queriedButton) {
        for (RelayButton[] buttonArray : mutuallyExclusiveButtonSets) {
            for (RelayButton button : buttonArray) {
                if (queriedButton.equals(button))
                    return buttonArray;
            }
        }
        return new RelayButton[0]; // Might need to be optimized
    }

    // Enabling/disabling buttons
    public void setEnabledAllButtons(boolean enabled) {
        for (RelayButton button: getButtons()) {
            button.setEnabled(enabled);
        }
    }
    public void setEnabledAllButtonsExcept(View view, boolean enabled) {
        for (RelayButton button: getButtons()) {
            if (!view.equals(button.getView())) {
                button.setEnabled(enabled);
            }
        }
    }
    // onTouch and onClick listeners
    private class HoldButtonListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                view.setBackgroundColor(COLOR_RED);
                getButtonByView(view).onPress();
                disableMutuallyExclusiveButtons(getButtonByView(view));
            } else if (action == MotionEvent.ACTION_UP) {
                view.setBackgroundColor(COLOR_GRAY);
                getButtonByView(view).onRelease();
                enableMutuallyExclusiveButtons(getButtonByView(view));
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
                disableMutuallyExclusiveButtons(getButtonByView(view));
            } else {
                view.setBackgroundColor(COLOR_GRAY);
                controller.sendCommand(view, COMMAND_OPEN);
                getButtonByView(view).onRelease();
                enableMutuallyExclusiveButtons(getButtonByView(view));
            }
        }
    }
}
