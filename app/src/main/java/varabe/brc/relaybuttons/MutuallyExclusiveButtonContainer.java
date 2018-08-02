package varabe.brc.relaybuttons;

public class MutuallyExclusiveButtonContainer {
    private RelayButton[] buttons;
    private int timeout;

    MutuallyExclusiveButtonContainer(RelayButton[] buttons, int timeoutInMillis) {
        // Timeout stands for the time between the release of a button and enabling of mutually
        // exclusive buttons
        this.buttons = buttons;
        this.timeout = timeoutInMillis;
    }

    public RelayButton[] getButtons() {
        return buttons;
    }

    public int getTimeout() {
        return timeout;
    }
}
