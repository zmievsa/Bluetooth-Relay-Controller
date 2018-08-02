package varabe.brc.relaybuttons;

import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
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

public class ButtonManager {
    private final View.OnTouchListener holdingButtonListener = new HoldButtonListener();
    private final View.OnClickListener switchButtonListener = new SwitchButtonListener();
    private Timer timer = new Timer();

    private HashMap<Integer, RelayButton> buttons;
    private ArrayList<MutuallyExclusiveButtonContainer> mutuallyExclusiveButtonContainers;
    private RelayController controller;

    public ButtonManager(RelayController controller) {
        this.buttons = new HashMap<>();
        this.mutuallyExclusiveButtonContainers = new ArrayList<>();
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
    public void connectMutuallyExclusiveButtons(RelayButton[] buttons, int timeoutInMillis) {
        mutuallyExclusiveButtonContainers.add(new MutuallyExclusiveButtonContainer(buttons, timeoutInMillis));
    }
    private void setEnabledMutuallyExclusiveButtons(RelayButton queriedButton, boolean enabled) {
        // Refactor me, please :(
        final MutuallyExclusiveButtonContainer container = findMutuallyExclusiveButtons(queriedButton);
        if (container != null) {
            int timeout = container.getTimeout();
            if (enabled && timeout > 0) {
                queriedButton.setEnabled(false);
                new CountDownTimer(timeout, timeout) { // Means that it won't call onTick()
                    public void onTick(long l) {}
                    public void onFinish() {
                        for (RelayButton button : container.getButtons()) {
                            button.setEnabled(true);
                        }
                    }
                }.start();
            }
            else {
                for (RelayButton button : container.getButtons()) {
                    if (!button.equals(queriedButton))
                        button.setEnabled(enabled);
                }
            }
        }
    }
    private MutuallyExclusiveButtonContainer findMutuallyExclusiveButtons(RelayButton queriedButton) {
        for (MutuallyExclusiveButtonContainer container : mutuallyExclusiveButtonContainers) {
            for (RelayButton button : container.getButtons()) {
                if (queriedButton.equals(button))
                    return container;
            }
        }
        return null;
    }

    // Enabling/disabling buttons
    public void setEnabledAllButtons(boolean enabled) {
        setEnabledAllButtonsExcept(enabled, null);
    }
    private void setEnabledAllButtonsExcept(boolean enabled, RelayButton exception) {
        for (RelayButton button: getButtons()) {
            if (!button.equals(exception))
                button.setEnabled(enabled);
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
                setEnabledMutuallyExclusiveButtons(getButtonByView(view), false);
            } else if (action == MotionEvent.ACTION_UP) {
                view.setBackgroundColor(COLOR_GRAY);
                getButtonByView(view).onRelease();
                setEnabledMutuallyExclusiveButtons(getButtonByView(view), true);
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
                setEnabledMutuallyExclusiveButtons(getButtonByView(view), false);
            } else {
                view.setBackgroundColor(COLOR_GRAY);
                controller.sendCommand(view, COMMAND_OPEN);
                getButtonByView(view).onRelease();
                setEnabledMutuallyExclusiveButtons(getButtonByView(view), true);
            }
        }
    }
}
