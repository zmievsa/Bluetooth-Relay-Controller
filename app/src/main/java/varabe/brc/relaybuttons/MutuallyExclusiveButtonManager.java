package varabe.brc.relaybuttons;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public class MutuallyExclusiveButtonManager {
    private ArrayList<MutuallyExclusiveButtonContainer> containers;

    public MutuallyExclusiveButtonManager() {
        this.containers = new ArrayList<>();
    }

    public void connectMutuallyExclusiveButtons(MutuallyExclusiveButtonContainer container) {
        containers.add(container);
        for (RelayButton button: container.getButtons()) {
            button.setMEBManager(this); // TODO: REMOVE RECURSIVE DEPENDENCIES
        }
    }

    public void setEnabledMutuallyExclusiveButtons(RelayButton queriedButton, boolean enabled) {
        MutuallyExclusiveButtonContainer container = getMutuallyExclusiveButtons(queriedButton);
        if (container != null && !container.isPassive(queriedButton)) {
            if (enabled && container.getTimeout() > 0) {
                enableMutuallyExclusiveButtonsAfterTimeout(queriedButton, container);
            }
            else {
                for (RelayButton button : container.getButtons()) {
                    if (!button.equals(queriedButton))
                        button.setEnabled(enabled);
                }
            }
        }
    }
    @Nullable
    private MutuallyExclusiveButtonContainer getMutuallyExclusiveButtons(RelayButton queriedButton) {
        for (MutuallyExclusiveButtonContainer container : containers) {
            for (RelayButton button : container.getButtons()) {
                if (queriedButton.equals(button)) return container;
            }
        }
        return null;
    }
    private void enableMutuallyExclusiveButtonsAfterTimeout(
            RelayButton enablingButton, @NonNull final MutuallyExclusiveButtonContainer container) {
        int timeout = container.getTimeout();
        enablingButton.setEnabled(false);
        new CountDownTimer(timeout, timeout) { // Means that it won't call onTick()
            public void onTick(long l) {}
            public void onFinish() {
                for (RelayButton button : container.getButtons()) {
                    button.setEnabled(true);
                }
            }
        }.start();
    }
}
