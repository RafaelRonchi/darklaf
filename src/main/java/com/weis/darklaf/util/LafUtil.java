package com.weis.darklaf.util;

import com.weis.darklaf.DarkLaf;
import com.weis.darklaf.icons.EmptyIcon;
import com.weis.darklaf.icons.IconLoader;
import com.weis.darklaf.icons.UIAwareIcon;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.InsetsUIResource;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public final class LafUtil {
    private static final Logger LOGGER = Logger.getLogger(LafUtil.class.getName());
    private static final IconLoader ICON_LOADER = IconLoader.get();
    private static final String DUAL_KEY = "[dual]";
    private static final String AWARE_KEY = "[aware]";

    @NotNull
    public static Properties loadProperties(@NotNull final DarkLaf laf) {
        final Properties properties = new Properties();
        final String osSuffix = SystemInfo.isMac ? "mac" : SystemInfo.isWindows ? "windows" : "linux";
        try (InputStream stream = laf.getClass().getResourceAsStream(laf.getPrefix() + ".properties")) {
            properties.load(stream);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        try (InputStream stream = laf.getClass().getResourceAsStream(
                laf.getPrefix() + "_" + osSuffix + ".properties")) {
            properties.load(stream);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        return properties;
    }


    @Nullable
    public static Object parseValue(@NotNull final String key, @NotNull final String value) {
        if ("null".equals(value)) {
            return null;
        }
        Object returnVal = new LoadError("Could not parse value '" + value + "' for key '" + key + "'");
        if (key.endsWith("Insets")) {
            returnVal = parseInsets(value);
        } else if (key.endsWith(".border") || key.endsWith("Border")) {
            returnVal = parseObject(value);
        } else if (key.endsWith(".component") || key.endsWith("Component")) {
            returnVal = parseObject(value);
        } else if (key.endsWith("Renderer")) {
            returnVal = parseObject(value);
        } else if (key.endsWith(".font")) {
            returnVal = parseFont(value);
        } else if (key.endsWith(".icon") || key.endsWith("Icon")) {
            returnVal = parseIcon(value);
        } else if (key.endsWith("Size") || key.endsWith(".size")) {
            returnVal = parseSize(value);
        } else if ("null".equalsIgnoreCase(value)) {
            returnVal = null;
        }
        if (returnVal instanceof LoadError) {
            final Color color = ColorUtil.fromHex(value, null);
            final Integer invVal = getInteger(value);
            final Boolean boolVal = "true".equalsIgnoreCase(value)
                                    ? Boolean.TRUE
                                    : "false".equalsIgnoreCase(value) ? Boolean.FALSE : null;
            if (color != null) {
                return new ColorUIResource(color);
            } else if (invVal != null) {
                return invVal;
            } else if (boolVal != null) {
                return boolVal;
            }
        } else {
            return returnVal;
        }
        return value;
    }

    @NotNull
    private static DimensionUIResource parseSize(@NotNull final String value) {
        int[] dim = Arrays.stream(value.split(",", 2)).mapToInt(Integer::parseInt).toArray();
        return new DimensionUIResource(dim[0], dim[1]);
    }

    private static Icon parseIcon(@NotNull final String value) {
        String path = value;
        Dimension dim = new Dimension(16, 16);
        if (value.charAt(value.length() - 1) == ')') {
            int i = path.lastIndexOf('(');
            String dimVal = path.substring(i + 1, path.length() - 1);
            int[] values = Arrays.stream(dimVal.split(",", 2)).mapToInt(Integer::parseInt).toArray();
            dim.width = values[0];
            dim.height = values[1];
            path = path.substring(0, i);
        }
        if (path.charAt(path.length() - 1) == ']') {
            String tag = path.endsWith(DUAL_KEY) ? DUAL_KEY
                                                 : path.endsWith(AWARE_KEY) ? AWARE_KEY : null;
            if (tag == null) {
                throw new IllegalArgumentException("Invalid tag on icon path: '" + value + "'");
            }
            UIAwareIcon icon = ICON_LOADER.getUIAwareIcon(path.substring(0, path.length() - tag.length()),
                                                          dim.width, dim.height);
            if (tag.equals(DUAL_KEY)) {
                return icon.getDual();
            } else {
                return icon;
            }
        }
        if (path.equals("empty")) {
            return EmptyIcon.create(dim.width, dim.height);
        }
        return ICON_LOADER.getIcon(path, dim.width, dim.height);
    }

    @NotNull
    @Contract("_ -> new")
    private static Object parseFont(final String value) {
        try {
            final String[] decode = value.split("-");
            //noinspection MagicConstant
            return new Font(decode[0], Integer.parseInt(decode[1]), Integer.parseInt(decode[2]));
        } catch (@NotNull final Exception e) {
            return new Font("Monospaced", Font.PLAIN, 12);
        }
    }

    @NotNull
    private static Object parseObject(final String value) {
        try {
            return Class.forName(value).getDeclaredConstructor().newInstance();
        } catch (@NotNull final Exception e) {
            return new LoadError(e.getMessage());
        }
    }


    @NotNull
    private static Object parseInsets(final String value) {
        final List<String> numbers = StringUtil.split(value, ",");
        return new InsetsUIResource(
                Integer.parseInt(numbers.get(0)),
                Integer.parseInt(numbers.get(1)),
                Integer.parseInt(numbers.get(2)),
                Integer.parseInt(numbers.get(3)));
    }

    @Nullable
    private static Integer getInteger(@NotNull final String value) {
        try {
            return Integer.parseInt(value);
        } catch (@NotNull final NumberFormatException ignored) {
            return null;
        }
    }

    private static final class LoadError {

        @Contract(pure = true)
        private LoadError(final String message) {
        }

    }
}
