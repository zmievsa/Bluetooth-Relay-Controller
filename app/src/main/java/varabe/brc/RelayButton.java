package varabe.brc;

import android.view.View;

import static varabe.brc.RelayController.SUPPORTED_CHANNELS;

public class RelayButton {
    public String getRelayChannel() {
        return relayChannel;
    }

    public View getView() {
        return view;
    }

    public boolean hasCustomBehavior() {
        return customBehavior;
    }

    private String relayChannel;
    private View view;
    private boolean customBehavior;

    public RelayButton(View view, boolean hasCustomBehavior, String relayChannel) {
        this.view = view;
        this.customBehavior = hasCustomBehavior;
        this.relayChannel = relayChannel;
    }
    public RelayButton(View view, boolean hasCustomBehavior) {
        this(view, hasCustomBehavior, getRelayChannelFromViewTag(view));
    }
    public RelayButton(View view) {
        this(view, false, getRelayChannelFromViewTag(view));
    }
    private static String getRelayChannelFromViewTag(View view) {
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
}
