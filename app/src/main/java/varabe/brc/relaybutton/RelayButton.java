package varabe.brc.relaybutton;

import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import varabe.brc.RelayController;

import static varabe.brc.RelayController.COMMAND_CLOSE;
import static varabe.brc.RelayController.COMMAND_OPEN;
import static varabe.brc.RelayController.SUPPORTED_CHANNELS;
import static varabe.brc.activity.MainActivity.PRESSED_BUTTON_TEXT_COLOR;
import static varabe.brc.activity.MainActivity.PRESSED_DISABLED_BUTTON_COLOR;
import static varabe.brc.activity.MainActivity.RELEASED_BUTTON_COLOR;
import static varabe.brc.activity.MainActivity.PRESSED_BUTTON_COLOR;
import static varabe.brc.activity.MainActivity.RELEASED_BUTTON_TEXT_COLOR;
import static varabe.brc.activity.MainActivity.RELEASED_DISABLED_BUTTON_COLOR;

abstract public class RelayButton {
    public int getId() {
        return view.getId();
    }

    public String getRelayChannel() {
        return relayChannel;
    }

    public RelayController getController() {
        return controller;
    }

    public View getView() {
        return view;
    }

    private static ArrayList<RelayButton> buttons = new ArrayList<>();
    boolean isActivated;
    private String relayChannel;
    private View view;
    private RelayController controller;
    private int timeoutUntilReenabled;
    private boolean hasActiveTask;
    private MutuallyExclusiveButtonManager MEBManager;

    RelayButton(View view, String relayChannel, RelayController controller, int timeoutUntilReenabled) {
        this.view = view;
        this.relayChannel = relayChannel;
        this.controller = controller;
        this.timeoutUntilReenabled = timeoutUntilReenabled;
        this.hasActiveTask = false;
        isActivated = false;
        buttons.add(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RelayButton)
            return ((RelayButton) obj).getId() == getId();
        else
            return false;
    }

    void setMEBManager(MutuallyExclusiveButtonManager MEBManager) {
        this.MEBManager = MEBManager;
    }

    public void setEnabled(Boolean enabled) {
        if (!hasActiveTask) {
            if (view instanceof ImageView)
                setEnabled((ImageView) view, enabled);
            else if (view instanceof Button) {
                setEnabled((Button) view, enabled);
            }
            else
                throw new UnsupportedOperationException("View of type \"" + view.getClass() + "\" is not supported");
        }
    }
    private void setEnabled(ImageView view, Boolean enabled) {
        view.setEnabled(enabled);
        if (enabled)
            view.setColorFilter(null);
        else
            view.setColorFilter(Color.argb(255, 150, 150, 150));
    }

    private void setEnabled(Button button, boolean enabled) {
            view.setEnabled(enabled);
            if (enabled && isActivated)
                view.setBackgroundColor(PRESSED_BUTTON_COLOR);
            else if (!enabled && isActivated)
                view.setBackgroundColor(PRESSED_DISABLED_BUTTON_COLOR);
            else if (enabled)
                view.setBackgroundColor(RELEASED_BUTTON_COLOR);
            else
                view.setBackgroundColor(RELEASED_DISABLED_BUTTON_COLOR);
        }

    void onActivate() {
        activate();
        view.setBackgroundColor(PRESSED_BUTTON_COLOR);
        if (view instanceof Button)
            ((Button) view).setTextColor(PRESSED_BUTTON_TEXT_COLOR);
        setEnabledMutuallyExclusiveButtons(false);
        isActivated = true;
    }
    void onDeactivate() {
        deactivate();
        view.setBackgroundColor(RELEASED_BUTTON_COLOR);
        if (view instanceof Button)
            ((Button) view).setTextColor(RELEASED_BUTTON_TEXT_COLOR);
        setEnabledMutuallyExclusiveButtons(true);
        if (timeoutUntilReenabled > 0) {
            hasActiveTask = true;
            setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hasActiveTask = false;
                    setEnabled(true);
                }
            }, timeoutUntilReenabled);
        }
        isActivated = false;
    }
    void activate() {
        controller.sendCommand(relayChannel, COMMAND_CLOSE);
    }
    void deactivate() {
        controller.sendCommand(relayChannel, COMMAND_OPEN);
    }

    static String getRelayChannelFromViewTag(View view) {
        Object tagObj = view.getTag();
        if (tagObj == null)
            throw new UnsupportedOperationException("View tag is not set (View ID: " + view.getId() + ")");
        else {
            String tag = tagObj.toString();
            for (String supportedTag: SUPPORTED_CHANNELS) {
                if (tag.equals(supportedTag))
                    return tag;
            }
            throw new UnsupportedOperationException("View tag '" + tag + "' is not supported (View ID: " + view.getId() + ")");
        }
    }

    private void setEnabledMutuallyExclusiveButtons(boolean enabled) {
        if (MEBManager != null)
            MEBManager.setEnabledMutuallyExclusiveButtons(this, enabled);
    }

    public static void clearButtons() {
        buttons.clear();
    }
    public static void setEnabledAllButtons(boolean enabled) {
        for (RelayButton button: buttons) {
            button.setEnabled(enabled);
        }
    }
}
