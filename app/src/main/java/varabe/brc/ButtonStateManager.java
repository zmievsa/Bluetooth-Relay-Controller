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

    public ButtonStateManager() {
        this.buttonSet = new HashSet<>();
    }
    public void addButton(View button) {
        buttonSet.add(new WeakReference<>(button));
    }
    public Set<WeakReference<View>> getButtonSet() {
        return buttonSet;
    }
    public void setEnabledAllButtons(boolean enabled) {
        for (WeakReference buttonReference: buttonSet) {
            View button = (View) buttonReference.get();
            setEnabled( button, enabled);
        }
    }
    public void setEnabledAllButtonsExcept(View view, boolean enabled) {
        // Might need to be optimized
        for (WeakReference buttonReference: buttonSet) {
            View button = (View) buttonReference.get();
            if (!view.equals(button)) {
                setEnabled(button, enabled);
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
}
