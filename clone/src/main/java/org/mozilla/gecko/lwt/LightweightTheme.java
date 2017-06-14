/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.gecko.lwt;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public class LightweightTheme {

    public static interface OnChangeListener {
        // The View should change its background/text color.
        public void onLightweightThemeChanged();

        // The View should reset to its default background/text color.
        public void onLightweightThemeReset();
    }

    private final List<OnChangeListener> mListeners;

    public LightweightTheme(Application application) {
        mListeners = new ArrayList<OnChangeListener>();
    }

    public void addListener(final OnChangeListener listener) {
        // Don't inform the listeners that attached late.
        // Their onLayout() will take care of them before their onDraw();
        mListeners.add(listener);
    }

    public void removeListener(OnChangeListener listener) {
        mListeners.remove(listener);
    }


    /**
     * A lightweight theme is enabled only if there is an active bitmap.
     *
     * @return True if the theme is enabled.
     */
    public boolean isEnabled() {
        return false;
    }

    /**
     * Based on the luminance of the domanint color, a theme is classified as light or dark.
     *
     * @return True if the theme is light.
     */
    public boolean isLightTheme() {
        return false;
    }
}
