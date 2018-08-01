package varabe.brc;

import android.view.View;

import java.util.HashSet;
import java.util.Set;

public class ButtonStateManager {
    private Set<View> buttonSet; // Should I use weakreference here?
    private Set<Set<View>> mutuallyExclusiveButtonSets;

    public ButtonStateManager() {
        this.buttonSet = new HashSet<>();
        this.mutuallyExclusiveButtonSets = new HashSet<>();
    }
    public Set<View> getButtonSet() {
        return buttonSet;
    }
    public void addButton(View button) {
        buttonSet.add(button);
    }
    public void connectMutuallyExclusiveButtons(Set<View> views) {
        mutuallyExclusiveButtonSets.add(views);
    }
    public void setEnabledAllButtons(boolean enabled) {
        for (View button: buttonSet) {
            setEnabled( button, enabled);
        }
    }
    public void setEnabledAllButtonsExcept(View view, boolean enabled) {
        for (View button: buttonSet) {
            if (!view.equals(button)) {
                setEnabled(button, enabled);
            }
        }
    }
}
