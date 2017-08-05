/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.gecko.gfx;


public class DynamicToolbarAnimator {
    private static final String LOGTAG = "GeckoDynamicToolbarAnimator";

    public static enum PinReason {
        DISABLED(0),
        RELAYOUT(1),
        ACTION_MODE(2),
        FULL_SCREEN(3),
        CARET_DRAG(4),
        PAGE_LOADING(5),
        CUSTOM_TAB(6);

        public final int value;

        PinReason(final int aValue) {
            value = aValue;
        }
    }
}
