package varabe.brc;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import static varabe.brc.RelayController.SUPPORTED_TAGS;

public class RelayButton {
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
    public void setEnabled(View view, Boolean enabled) {
        if (view instanceof ImageView)
            setEnabled((ImageView) view, enabled);
        else if (view instanceof Button)
            view.setEnabled(enabled);
        else
            throw new UnsupportedOperationException("View of type \"" + view.getClass() + "\" is not supported");
    }
    public void setEnabled(ImageView view, Boolean enabled) {
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
        else {
            String tag = tagObj.toString();
            for (String supportedTag: SUPPORTED_TAGS) {
                if (tag.equals(supportedTag))
                    return tag;
            }
            throw new UnsupportedOperationException("View tag '" + tag + "' is not supported (View ID: " + view.getId() + ")");
        }
    }
}
