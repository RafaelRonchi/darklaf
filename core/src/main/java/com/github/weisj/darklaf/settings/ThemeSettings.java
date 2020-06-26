/*
 * MIT License
 *
 * Copyright (c) 2020 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package com.github.weisj.darklaf.settings;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.*;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.components.DefaultButton;
import com.github.weisj.darklaf.icons.IconLoader;
import com.github.weisj.darklaf.theme.Theme;
import com.github.weisj.darklaf.theme.event.ThemePreferenceChangeEvent;
import com.github.weisj.darklaf.theme.event.ThemePreferenceListener;
import com.github.weisj.darklaf.theme.info.AccentColorRule;
import com.github.weisj.darklaf.theme.info.FontSizeRule;
import com.github.weisj.darklaf.theme.info.PreferredThemeStyle;
import com.github.weisj.darklaf.util.DarkUIUtil;
import com.github.weisj.darklaf.util.LazyValue;
import com.github.weisj.darklaf.util.ResourceUtil;

public class ThemeSettings implements ThemePreferenceListener {

    private static final LazyValue<ThemeSettings> instance = new LazyValue<>(ThemeSettings::new);
    private static final LazyValue<Icon> icon = new LazyValue<>(() -> UIManager.getIcon("ThemeSettings.icon"));
    private final ResourceBundle resourceBundle = ResourceUtil.getResourceBundle("theme_settings");

    private final LazyValue<ThemeSettingsPanel> settingsPanel;

    private JDialog dialog;

    private final SettingsConfiguration savedConfiguration;
    private final SettingsConfiguration currentConfiguration;

    public static ThemeSettings getInstance() {
        return instance.get();
    }

    public static boolean isInitialized() {
        return instance.isInitialized();
    }

    /**
     * Show the settings as a dialog.
     *
     * @param  parent                the parent component.
     * @throws IllegalStateException If {@link #getSettingsPanel()} is already in use.
     */
    public static void showSettingsDialog(final Component parent) {
        showSettingsDialog(parent, Dialog.ModalityType.MODELESS);
    }

    /**
     * Show the settings as a dialog.
     *
     * @param  parent                the parent component.
     * @param  modalityType          the modality type.
     * @throws IllegalStateException If {@link #getSettingsPanel()} is already in use.
     */
    public static void showSettingsDialog(final Component parent, final Dialog.ModalityType modalityType) {
        getInstance().showDialog(parent, modalityType);
    }

    protected ThemeSettings() {
        LafManager.addThemePreferenceChangeListener(this);
        currentConfiguration = new DefaultSettingsConfiguration();
        savedConfiguration = new DefaultSettingsConfiguration();
        settingsPanel = new LazyValue<>(() -> {
            ThemeSettingsPanel panel = new ThemeSettingsPanel(resourceBundle);
            panel.loadConfiguration(currentConfiguration);
            return panel;
        });
    }

    protected JDialog createDialog(final Window parent) {
        JDialog dialog = new JDialog(parent);
        dialog.setIconImage(IconLoader.createFrameIcon(getIcon(), dialog));
        dialog.setTitle(getTitle());

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(settingsPanel.get(), BorderLayout.CENTER);
        contentPane.add(createButtonPanel(), BorderLayout.SOUTH);
        dialog.setContentPane(contentPane);

        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationByPlatform(true);
        dialog.setLocationRelativeTo(parent);
        return dialog;
    }

    /**
     * Show the settings as a dialog.
     *
     * @param  parent                the parent component.
     * @param  modal                 the modality type.
     * @throws IllegalStateException If {@link #getSettingsPanel()} is already in use.
     */
    public void showDialog(final Component parent, final Dialog.ModalityType modal) {
        Window parentWindow = DarkUIUtil.getWindow(getSettingsPanel());
        if (parentWindow != null && parentWindow != DarkUIUtil.getWindow(dialog)) {
            throw new IllegalStateException("Can't show dialog while settings panel is used elsewhere");
        }
        if (dialog != null && dialog.isVisible()) {
            dialog.requestFocus();
            return;
        }
        refresh();
        Window window = DarkUIUtil.getWindow(parent);
        dialog = createDialog(window);
        dialog.setModalityType(modal);
        dialog.setVisible(true);
    }

    /**
     * Returns the settings panel for custom use. Note that this is a shared resource and while it is used {@link
     * #showDialog(Component, java.awt.Dialog.ModalityType)} can't be called. Note that all communication to the
     * settings panel should happen through this class by calling {@link #peek()}, {@link #save()},{@link
     * #refresh()},{@link #revert()} or {@link #apply()}
     *
     * @return the settings panel.
     */
    public JComponent getSettingsPanel() {
        return settingsPanel.get();
    }

    /**
     * Returns whether the option to follow the system settings is enabled.
     *
     * @return true if enabled.
     * @see    #isSystemPreferencesEnabled() (boolean)
     * @see    #isThemeFollowsSystem()
     * @see    #isAccentColorFollowsSystem()
     * @see    #isSelectionColorFollowsSystem()
     * @see    #isFontSizeFollowsSystem()
     */
    public boolean isSystemPreferencesEnabled() {
        return currentConfiguration.isSystemPreferencesEnabled();
    }

    /**
     * Returns whether the accent color follows the system settings.
     *
     * @return true if accent color follows system settings.
     * @see    #setAccentColorFollowsSystem(boolean)
     */
    public boolean isAccentColorFollowsSystem() {
        return currentConfiguration.isAccentColorFollowsSystem();
    }

    /**
     * Returns whether the font size follows the system settings.
     *
     * @return true if font size follows system settings.
     * @see    #setFontSizeFollowsSystem(boolean)
     */
    public boolean isFontSizeFollowsSystem() {
        return currentConfiguration.isFontSizeFollowsSystem();
    }

    /**
     * Returns whether the selection color follows the system settings.
     *
     * @return true if selection color follows system settings.
     * @see    #setSelectionColorFollowsSystem(boolean)
     */
    public boolean isSelectionColorFollowsSystem() {
        return currentConfiguration.isSelectionColorFollowsSystem();
    }

    /**
     * Returns whether the theme follows the system settings.
     *
     * @return true if theme follows system settings.
     * @see    #setThemeFollowsSystem(boolean)
     */
    public boolean isThemeFollowsSystem() {
        return currentConfiguration.isThemeFollowsSystem();
    }

    /**
     * Get the currently selected accent color rule. This is not the same as the rule of {@link LafManager#getTheme()}
     * as the current settings might not have been applied.
     *
     * @return the current selected accent color rule.
     * @see    #setAccentColorRule(AccentColorRule)
     */
    public AccentColorRule getAccentColorRule() {
        return currentConfiguration.getAccentColorRule();
    }

    /**
     * Get the currently selected font size rule. This is not the same as the rule of {@link LafManager#getTheme()} as
     * the current settings might not have been applied.
     *
     * @return the current selected font size rule.
     * @see    #setFontSizeRule(FontSizeRule)
     */
    public FontSizeRule getFontSizeRule() {
        return currentConfiguration.getFontSizeRule();
    }

    /**
     * Get the currently selected theme. This is not the same as {@link LafManager#getTheme()} as the current settings
     * might not have been applied.
     *
     * @return the current selected theme.
     * @see    #setTheme(Theme)
     */
    public Theme getTheme() {
        return currentConfiguration.getTheme();
    }

    /**
     * Enables the option to follow system preferences.
     *
     * @param enabled true if enabled.
     * @see           #setAccentColorFollowsSystem(boolean)
     * @see           #setSelectionColorFollowsSystem(boolean)
     * @see           #setFontSizeFollowsSystem(boolean)
     * @see           #setThemeFollowsSystem(boolean)
     */
    public void setEnabledSystemPreferences(final boolean enabled) {
        currentConfiguration.setEnabledSystemPreferences(enabled);
        updateSettingsPanel();
    }

    /**
     * Sets whether the accent color should follow the system settings. This only works if {@link
     * #isSelectionColorFollowsSystem()} is true.
     *
     * @param accentColorFollowsSystem true if accent color should follow system.
     * @see                            #isAccentColorFollowsSystem()
     */
    public void setAccentColorFollowsSystem(final boolean accentColorFollowsSystem) {
        currentConfiguration.setAccentColorFollowsSystem(accentColorFollowsSystem);
        updateSettingsPanel();
    }

    /**
     * Sets whether the font size should follow the system settings. This only works if {@link
     * #isSelectionColorFollowsSystem()} is true.
     *
     * @param fontSizeFollowsSystem true if font size should follow system.
     * @see                         #isFontSizeFollowsSystem()
     */
    public void setFontSizeFollowsSystem(final boolean fontSizeFollowsSystem) {
        currentConfiguration.setFontSizeFollowsSystem(fontSizeFollowsSystem);
        updateSettingsPanel();
    }

    /**
     * Sets whether the selection color should follow the system settings. This only works if {@link
     * #isSelectionColorFollowsSystem()} is true.
     *
     * @param selectionColorFollowsSystem true if selection color should follow system.
     * @see                               #isSelectionColorFollowsSystem()
     */
    public void setSelectionColorFollowsSystem(final boolean selectionColorFollowsSystem) {
        currentConfiguration.setSelectionColorFollowsSystem(selectionColorFollowsSystem);
        updateSettingsPanel();
    }

    /**
     * Sets whether the theme should follow the system settings. This only works if {@link
     * #isSelectionColorFollowsSystem()} is true.
     *
     * @param themeFollowsSystem true if theme should follow system.
     * @see                      #isThemeFollowsSystem()
     */
    public void setThemeFollowsSystem(final boolean themeFollowsSystem) {
        currentConfiguration.setThemeFollowsSystem(themeFollowsSystem);
        updateSettingsPanel();
    }

    /**
     * Sets the font accent color rule. The theme is not updated until {@link #apply()} is called.
     *
     * @param accentColorRule the accent color rule
     * @see                   #getAccentColorRule()
     */
    public void setAccentColorRule(final AccentColorRule accentColorRule) {
        currentConfiguration.setAccentColorRule(accentColorRule);
        updateSettingsPanel();
    }

    /**
     * Sets the font size rule. The theme is not updated until {@link #apply()} is called.
     *
     * @param fontSizeRule the font size rule.
     * @see                #getFontSizeRule()
     */
    public void setFontSizeRule(final FontSizeRule fontSizeRule) {
        currentConfiguration.setFontSizeRule(fontSizeRule);
        updateSettingsPanel();

    }

    /**
     * Sets the theme. The theme is not updated until {@link #apply()} is called.
     *
     * @param theme the theme.
     * @see         #getTheme()
     */
    public void setTheme(final Theme theme) {
        currentConfiguration.setTheme(theme);
    }

    protected Component createButtonPanel() {
        JButton ok = new DefaultButton(resourceBundle.getString("dialog_ok"));
        ok.setDefaultCapable(true);
        ok.addActionListener(e -> {
            apply();
            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
        });

        JButton cancel = new JButton(resourceBundle.getString("dialog_cancel"));
        cancel.addActionListener(e -> {
            revert();
            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
        });
        JButton apply = new JButton(resourceBundle.getString("dialog_apply"));
        apply.addActionListener(e -> {
            apply();
        });

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(ok);
        box.add(cancel);
        box.add(apply);
        box.setBorder(settingsPanel.get().getBorder());
        return box;
    }

    /**
     * Updates all values according to the current settings.
     */
    public void refresh() {
        currentConfiguration.setEnabledSystemPreferences(LafManager.isPreferenceChangeReportingEnabled());
        updateSettingsPanel();
    }

    private void updateSettingsPanel() {
        settingsPanel.ifPresent(p -> p.loadConfiguration(currentConfiguration));
    }

    private void fetchFromSettingsPanel() {
        settingsPanel.ifPresent(ThemeSettingsPanel::updateConfiguration);
    }

    /**
     * Saves the settings.
     */
    public void save() {
        fetchFromSettingsPanel();
        LafManager.enabledPreferenceChangeReporting(currentConfiguration.isSystemPreferencesEnabled());
        savedConfiguration.load(currentConfiguration);
    }

    public void setConfiguration(final SettingsConfiguration configuration) {
        this.currentConfiguration.load(configuration);
    }

    /**
     * Saves the settings and updates the laf. This is them same as calling {@link #save()} and {@link #peek()}.
     */
    public void apply() {
        save();
        peek();
    }

    /**
     * Sets the theme according to the selected options. Does not save the settings.
     */
    public void peek() {
        applyTheme(getEffectiveTheme());
    }

    private Theme getEffectiveTheme() {
        return getEffectiveTheme(LafManager.getPreferredThemeStyle());
    }

    private Theme getEffectiveTheme(final PreferredThemeStyle themeStyle) {
        Theme baseTheme = getEffectiveBaseTheme(themeStyle);
        if (baseTheme == null) return null;
        FontSizeRule fontSizeRule = getEffectiveFontSizeRule(baseTheme, themeStyle);
        AccentColorRule accentColorRule = getEffectiveAccentColorRule(baseTheme);
        return baseTheme.derive(fontSizeRule, accentColorRule);
    }

    private Theme getEffectiveBaseTheme(final PreferredThemeStyle preferredThemeStyle) {
        return isThemeFollowsSystem()
                ? LafManager.themeForPreferredStyle(preferredThemeStyle)
                : getTheme();
    }

    private FontSizeRule getEffectiveFontSizeRule(final Theme theme, final PreferredThemeStyle preferredThemeStyle) {
        if (theme == null) return FontSizeRule.getDefault();
        return isFontSizeFollowsSystem()
                ? preferredThemeStyle.getFontSizeRule()
                : getFontSizeRule();
    }

    private AccentColorRule getEffectiveAccentColorRule(final Theme theme) {
        if (theme == null) return AccentColorRule.getDefault();
        return currentConfiguration.getAccentColorRule();
    }

    /**
     * Revert the settings to the last time they have been saved.
     *
     * @see #save()
     */
    public void revert() {
        currentConfiguration.load(savedConfiguration);
        refresh();
    }

    protected void applyTheme(final Theme theme) {
        if (theme == null) return;
        if (LafManager.getTheme().appearsEqualTo(theme)) return;
        SwingUtilities.invokeLater(() -> {
            LafManager.installTheme(theme);
            refresh();
        });
    }

    /**
     * Returns the recommended display icon.
     *
     * @return the icon
     */
    public static Icon getIcon() {
        return icon.get();
    }

    /**
     * Returns the recommended display title.
     *
     * @return the title
     */
    public String getTitle() {
        return resourceBundle.getString("title");
    }

    @Override
    public void themePreferenceChanged(final ThemePreferenceChangeEvent e) {
        refresh();
        applyTheme(getEffectiveTheme(e.getPreferredThemeStyle()));
    }
}
