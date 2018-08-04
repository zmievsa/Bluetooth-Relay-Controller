package varabe.brc.relaybuttons;

import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;

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
    private MutuallyExclusiveButtonManager MEBManager;
    private RelayController controller;

    public ButtonManager(RelayController controller) {
        this.buttons = new HashMap<>();
        this.MEBManager = new MutuallyExclusiveButtonManager();
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
        buttons.put(button.getId(), button);
    }

    public void connectMutuallyExclusiveButtons(MutuallyExclusiveButtonContainer container) {
        MEBManager.addContainer(container);
    }

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
                MEBManager.setEnabledMutuallyExclusiveButtons(getButtonByView(view), false);
            } else if (action == MotionEvent.ACTION_UP) {
                view.setBackgroundColor(COLOR_GRAY);
                getButtonByView(view).onRelease();
                MEBManager.setEnabledMutuallyExclusiveButtons(getButtonByView(view), true);
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
                MEBManager.setEnabledMutuallyExclusiveButtons(getButtonByView(view), false);
            } else {
                view.setBackgroundColor(COLOR_GRAY);
                controller.sendCommand(view, COMMAND_OPEN);
                getButtonByView(view).onRelease();
                MEBManager.setEnabledMutuallyExclusiveButtons(getButtonByView(view), true);
            }
        }
    }
}
