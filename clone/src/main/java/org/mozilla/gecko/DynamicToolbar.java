package org.mozilla.gecko;

import android.os.Bundle;

import org.mozilla.gecko.gfx.DynamicToolbarAnimator.PinReason;

public abstract class DynamicToolbar {
    private static final String LOGTAG = "DynamicToolbar";

    private static final String STATE_ENABLED = "dynamic_toolbar";
    private static final String CHROME_PREF = "browser.chrome.dynamictoolbar";

    // DynamicToolbar is enabled iff prefEnabled is true *and* accessibilityEnabled is false,
    // so it is disabled by default on startup. We do not enable it until we explicitly get
    // the pref from Gecko telling us to turn it on.
    private volatile boolean prefEnabled;
    private boolean accessibilityEnabled;
    // On some device we have to force-disable the dynamic toolbar because of
    // bugs in the Android code. See bug 1231554.

    private OnEnabledChangedListener enabledChangedListener;
    private boolean temporarilyVisible;

    public enum VisibilityTransition {
        IMMEDIATE,
        ANIMATE
    }

    /**
     * Listener for changes to the dynamic toolbar's enabled state.
     */
    public interface OnEnabledChangedListener {
        /**
         * This callback is executed on the UI thread.
         */
        public void onEnabledChanged(boolean enabled);
    }

    public DynamicToolbar() {
    }

    public static boolean isForceDisabled() {
        return true;
    }

    abstract public void destroy();

    abstract public void setEnabledChangedListener(OnEnabledChangedListener listener);

    abstract public void onSaveInstanceState(Bundle outState);

    abstract public void onRestoreInstanceState(Bundle savedInstanceState);

    abstract public boolean isEnabled();

    abstract public void setAccessibilityEnabled(boolean enabled);

    abstract public void setVisible(boolean visible, VisibilityTransition transition);

    abstract public void setPinned(boolean pinned, PinReason reason);
}
