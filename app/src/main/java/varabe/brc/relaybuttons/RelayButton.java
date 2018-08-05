package varabe.brc.relaybuttons;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import varabe.brc.RelayController;

import static varabe.brc.RelayController.SUPPORTED_CHANNELS;

public class RelayButton {
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

    private String relayChannel;
    private View view;
    private RelayController controller;

    RelayButton(View view, String relayChannel, RelayController controller) {
        this.view = view;
        this.relayChannel = relayChannel;
        this.controller = controller;

    }
    public RelayButton(View view, RelayController controller) {
        this(view, getRelayChannelFromViewTag(view), controller);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RelayButton)
            return ((RelayButton) obj).getId() == getId();
        else
            return false;
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

    public void onPress() {
        ;
    }
    public void onRelease() {
        ;
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
}
