package varabe.brc;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

public class ButtonStateManager {
    private Set<WeakReference<View>> buttonSet; // Should I use weakreference here?
    private Set<Set<View>> mutuallyExclusiveButtonSets;

    public ButtonStateManager() {
        this.buttonSet = new HashSet<>();
        this.mutuallyExclusiveButtonSets = new HashSet<>();
    }
    public Set<WeakReference<View>> getButtonSet() {
        return buttonSet;
    }
    public void addButton(View button) {
        buttonSet.add(new WeakReference<>(button));
    }
    public void connectMutuallyExclusiveButtons(Set<View> views) {
        mutuallyExclusiveButtonSets.add(views);
    }
    public void setEnabledAllButtons(boolean enabled) {
        for (WeakReference buttonReference: buttonSet) {
            View button = (View) buttonReference.get();
            setEnabled( button, enabled);
        }
    }
    public void setEnabledAllButtonsExcept(View view, boolean enabled) {
        for (WeakReference buttonReference: buttonSet) {
            View button = (View) buttonReference.get();
            if (!view.equals(button)) {
                setEnabled(button, enabled);
            }
        }
    }
}
