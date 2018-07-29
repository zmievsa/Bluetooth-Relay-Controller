package varabe.brc;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import static varabe.brc.RelayController.SUPPORTED_CHANNELS;

public class RelayButton {
    private String relayChannel;
    private View view;
    private boolean customBehavior;

    public RelayButton(View view, boolean hasCustomBehavior, String relayChannel) {
        this.view = view;
        this.customBehavior = hasCustomBehavior;
        checkRelayChannelIsSupported(relayChannel);
        this.relayChannel = relayChannel;
        view.setTag(relayChannel); // Very important that channels are stored as tags
    }
    public RelayButton(View view, boolean hasCustomBehavior) {
        this(view, hasCustomBehavior, getRelayChannelFromViewTag(view));
    }
    public RelayButton(View view) {
        this(view, false, getRelayChannelFromViewTag(view));
    }
    public String getRelayChannel() {
        return relayChannel;
    }
    public View getView() {
        return view;
    }
    public boolean hasCustomBehavior() {
        return customBehavior;
    }
    public void setEnabled(Boolean enabled) {
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
    private static String getRelayChannelFromViewTag(View view) {
        Object tagObj = view.getTag();
        if (tagObj == null)
            throw new UnsupportedOperationException("View tag is not set (View ID: " + view.getId() + ")");
        else
            return tagObj.toString();
    }
    private static void checkRelayChannelIsSupported(String channel) {
        for (String supportedChannel: SUPPORTED_CHANNELS) {
            if (channel.equals(supportedChannel))
                return;
        }
        throw new UnsupportedOperationException("Relay channel '" + channel + "' is not supported");
    }
}
