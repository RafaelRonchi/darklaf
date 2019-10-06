package com.weis.darklaf.ui.button;

import com.weis.darklaf.util.GraphicsContext;
import com.weis.darklaf.util.GraphicsUtil;
import com.weis.darklaf.util.SystemInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class DarkButtonUI extends BasicButtonUI {

    public static final int SQUARE_ARC_SIZE = 3;
    public static final int ARC_SIZE = 5;
    protected static final Rectangle viewRect = new Rectangle();
    protected static final Rectangle textRect = new Rectangle();
    protected static final Rectangle iconRect = new Rectangle();
    protected AbstractButton button;

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static ComponentUI createUI(final JComponent c) {
        return new DarkButtonUI();
    }

    @Override
    public void installUI(final JComponent c) {
        button = (AbstractButton) c;
        super.installUI(c);
    }

    @Override
    public void paint(final Graphics g, final JComponent c) {
        GraphicsContext config = new GraphicsContext(g);
        AbstractButton b = (AbstractButton) c;
        paintButton(g, c);

        String text = layout(b, c, SwingUtilities2.getFontMetrics(b, g),
                             b.getWidth(), b.getHeight());

        paintIcon(g, b, c);
        paintText(g, b, c, text);
        config.restore();
    }

    @Override
    protected void paintText(@NotNull final Graphics g, final JComponent c,
                             final Rectangle textRect, final String text) {
        AbstractButton button = (AbstractButton) c;
        ButtonModel model = button.getModel();
        g.setColor(getForeground(button));
        FontMetrics metrics = SwingUtilities2.getFontMetrics(c, g);
        int mnemonicIndex = button.getDisplayedMnemonicIndex();
        if (model.isEnabled()) {
            SwingUtilities2.drawStringUnderlineCharAt(c, g, text, mnemonicIndex,
                                                      textRect.x + this.getTextShiftOffset(),
                                                      textRect.y + metrics.getAscent() + getTextShiftOffset());
        } else {
            g.setColor(UIManager.getColor("Button.disabledText"));
            SwingUtilities2.drawStringUnderlineCharAt(c, g, text, -1,
                                                      textRect.x + getTextShiftOffset(),
                                                      textRect.y + metrics.getAscent() + getTextShiftOffset());
        }
    }

    protected Color getForeground(@NotNull final AbstractButton button) {
        Color fg = button.getForeground();
        if (fg instanceof UIResource && button instanceof JButton && ((JButton) button).isDefaultButton()) {
            Color selectedFg = UIManager.getColor("Button.selectedButtonForeground");
            if (selectedFg != null) {
                fg = selectedFg;
            }
        }
        return fg;
    }

    protected void paintButton(final Graphics g, @NotNull final JComponent c) {
        Graphics2D g2 = (Graphics2D) g;
        int borderSize = DarkButtonBorder.BORDER_SIZE;
        if (shouldDrawBackground(c)) {
            int arc = getArcSize(c);
            if (isShadowVariant(c)) {
                var b = (AbstractButton) c;
                if (b.isEnabled() && b.getModel().isRollover()) {
                    GraphicsUtil.setupAAPainting(g2);
                    g.setColor(getShadowColor(b));
                    if (isFullShadow(c)) {
                        g.fillRect(0, 0, c.getWidth(), c.getHeight());
                    } else {
                        g.fillRoundRect(borderSize, borderSize, c.getWidth() - 2 * borderSize,
                                        c.getHeight() - 2 * borderSize, arc, arc);
                    }
                }
            } else {
                g2.setColor(getBackgroundColor(c));
                if (isSquare(c) && !isForceRoundCorner(c)) {
                    g2.fillRect(borderSize, borderSize, c.getWidth() - 2 * borderSize,
                                c.getHeight() - 2 * borderSize - DarkButtonBorder.SHADOW_HEIGHT);
                } else {
                    g2.fillRoundRect(borderSize, borderSize, c.getWidth() - 2 * borderSize,
                                     c.getHeight() - 2 * borderSize - DarkButtonBorder.SHADOW_HEIGHT, arc, arc);
                }
            }
        }
    }

    protected String layout(@NotNull final AbstractButton b, final JComponent c, final FontMetrics fm,
                            final int width, final int height) {
        Insets i = b.getInsets();
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = width - (i.right + viewRect.x);
        viewRect.height = height - (i.bottom + viewRect.y);

        textRect.x = textRect.y = textRect.width = textRect.height = 0;
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

        // layout the text and icon
        return SwingUtilities.layoutCompoundLabel(
                b, fm, b.getText(), b.getIcon(),
                b.getVerticalAlignment(), b.getHorizontalAlignment(),
                b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
                viewRect, iconRect, textRect,
                b.getText() == null ? 0 : b.getIconTextGap());
    }

    protected void paintIcon(final Graphics g, @NotNull final AbstractButton b, final JComponent c) {
        if (b.getIcon() != null) {
            paintIcon(g, c, iconRect);
        }
    }

    protected void paintText(final Graphics g, final AbstractButton b, final JComponent c, final String text) {
        var context = GraphicsUtil.setupAntialiasing(g);
        if (text != null && !text.equals("")) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
                v.paint(g, textRect);
            } else {
                paintText(g, b, textRect, text);
            }
        }
        context.restore();
    }

    private boolean shouldDrawBackground(@NotNull final JComponent c) {
        if (isLabelButton(c)) return false;
        AbstractButton button = (AbstractButton) c;
        Border border = c.getBorder();
        return c.isEnabled() && border != null && button.isContentAreaFilled();
    }

    protected int getArcSize(final JComponent c) {
        return isSquare(c) ? SQUARE_ARC_SIZE : ARC_SIZE;
    }

    @Contract("null -> false")
    public static boolean isShadowVariant(final Component c) {
        if (isFullShadow(c)) return true;
        return c instanceof JButton
                && "shadow".equals(((JButton) c).getClientProperty("JButton.variant"));
    }

    protected Color getShadowColor(@NotNull final AbstractButton c) {
        var colorHover = c.getClientProperty("JButton.shadow.hover");
        var colorClick = c.getClientProperty("JButton.shadow.click");
        return c.getModel().isArmed() ? colorClick instanceof Color ? (Color) colorClick
                                                                    : UIManager.getColor("Button.shadow.click")
                                      : colorHover instanceof Color ? (Color) colorHover
                                                                    : UIManager.getColor("Button.shadow.hover");
    }

    @Contract("null -> false")
    public static boolean isFullShadow(final Component c) {
        return c instanceof JButton
                && "fullShadow".equals(((JButton) c).getClientProperty("JButton.variant"));
    }

    protected Color getBackgroundColor(@NotNull final JComponent c) {
        var defaultButton = (c instanceof JButton && (((JButton) c).isDefaultButton()));
        var rollOver = (c instanceof JButton && (((JButton) c).isRolloverEnabled()
                && (((JButton) c).getModel().isRollover())));
        var clicked = rollOver && ((JButton) c).getModel().isArmed();
        if (c.isEnabled()) {
            if (defaultButton) {
                if (clicked) {
                    return UIManager.getColor("Button.defaultFillColorClick");
                } else if (rollOver) {
                    return UIManager.getColor("Button.defaultFillColorRollOver");
                } else {
                    return UIManager.getColor("Button.defaultFillColor");
                }
            } else {
                if (clicked) {
                    return UIManager.getColor("Button.activeFillColorClick");
                } else if (rollOver) {
                    return UIManager.getColor("Button.activeFillColorRollOver");
                } else {
                    return UIManager.getColor("Button.activeFillColor");
                }
            }
        } else {
            return UIManager.getColor("Button.inactiveFillColor");
        }
    }

    @Contract("null -> false")
    public static boolean isSquare(final Component c) {
        return c instanceof JButton && "square".equals(((JButton) c).getClientProperty("JButton.buttonType"));
    }

    @Contract("null -> false")
    public static boolean isForceRoundCorner(final Component c) {
        return c instanceof JButton
                && Boolean.TRUE.equals(((JButton) c).getClientProperty("JButton.forceRoundCorner"));
    }

    @Contract("null -> false")
    public static boolean isLabelButton(final Component c) {
        return c instanceof JButton
                && "onlyLabel".equals(((JButton) c).getClientProperty("JButton.variant"));
    }

    @Override
    public void update(final Graphics g, final JComponent c) {
        super.update(g, c);
        if (c instanceof JButton && ((JButton) c).isDefaultButton() && !SystemInfo.isMac && !c.getFont().isBold()) {
            c.setFont(c.getFont().deriveFont(Font.BOLD));
        }
    }

    @Override
    public boolean contains(@NotNull final JComponent c, final int x, final int y) {
        if (isLabelButton(c)) {
            return super.contains(c, x, y);
        }
        if (!(x >= 0 && x <= c.getWidth() && y >= 0 && y <= c.getHeight())) return false;
        int bs = DarkButtonBorder.BORDER_SIZE;
        return new RoundRectangle2D.Float(bs, bs, c.getWidth() - 2 * bs, c.getWidth() - 2 * bs, getArcSize(c),
                                          getArcSize(c)).contains(x, y);
    }
}
