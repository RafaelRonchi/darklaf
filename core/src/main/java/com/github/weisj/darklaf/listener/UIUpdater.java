/*
 * MIT License
 *
 * Copyright (c) 2019-2022 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.weisj.darklaf.listener;

import java.util.function.Consumer;

import javax.swing.*;

import com.github.weisj.darklaf.components.DynamicUI;

/**
 * Listener that updates the component ui after a new laf has been installed. This listener only
 * needs to be used with the top most component. This listener isn't needed for components that are
 * part of a visible ui hierarchy.
 */
public final class UIUpdater implements Consumer<JComponent> {

    private static final String KEY_UPDATER = "JComponent.uiUpdaterLister";

    /**
     * Creates and registers a new {@link UIUpdater} with the given component.
     *
     * @param component the component.
     */
    public static void registerComponent(final JComponent component) {
        if (component == null) return;
        removeComponent(component);
        UIUpdater updater = new UIUpdater();
        component.putClientProperty(KEY_UPDATER, updater);
        DynamicUI.registerCallback(component, updater, false);
    }

    /**
     * Remove the registered {@link UIUpdater} from the component.
     *
     * @param component the component to unregister.
     */
    public static void removeComponent(final JComponent component) {
        if (component == null) return;
        Object updater = component.getClientProperty(KEY_UPDATER);
        if (updater instanceof UIUpdater) {
            removeComponent(component, (UIUpdater) updater);
        }
    }

    private static void removeComponent(final JComponent component, final UIUpdater updater) {
        component.putClientProperty(KEY_UPDATER, null);
        DynamicUI.removeCallback(component, updater);
    }

    private UIUpdater() {}

    @Override
    public void accept(final JComponent c) {
        SwingUtilities.updateComponentTreeUI(c);
    }
}
