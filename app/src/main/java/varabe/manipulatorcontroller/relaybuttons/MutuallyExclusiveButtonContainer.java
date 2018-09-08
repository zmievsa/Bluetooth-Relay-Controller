package varabe.manipulatorcontroller.relaybuttons;

import java.util.ArrayList;

public class MutuallyExclusiveButtonContainer {
    private RelayButton[] buttons;
    private ArrayList<RelayButton> passiveButtons;
    private int timeout;

    public MutuallyExclusiveButtonContainer(RelayButton[] buttons, int timeoutInMillis) {
        // Timeout stands for the time between the release of a button and enabling of mutually
        // exclusive buttons
        this.buttons = buttons;
        this.passiveButtons = new ArrayList<>();
        this.timeout = timeoutInMillis;
    }

    public RelayButton[] getButtons() {
        return buttons;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isPassive(RelayButton button) {
        return passiveButtons.contains(button);
    }

    public void setPassiveButton(RelayButton button) {
        // Makes the button passive which means that it won't disable other ME buttons but will be
        // disabled by them
        passiveButtons.add(button);
    }
}
