package com.github.beelzebu.matrix.api.i18n;

import com.github.beelzebu.matrix.api.config.AbstractConfig;
import com.github.beelzebu.matrix.api.util.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Beelzebu
 */
public final class I18n {

    public static final String DEFAULT_LOCALE = "en";
    private static I18n instance;
    private final Map<String, AbstractConfig> messagesMap;

    public I18n(Map<String, AbstractConfig> messagesMap) {
        this.messagesMap = messagesMap;
        instance = this;
    }

    public static String tl(Message message, String locale) {
        if (instance == null) {
            return StringUtils.replace(message.getDefault());
        }
        if (locale == null || locale.equals("")) {
            locale = DEFAULT_LOCALE;
        }
        return StringUtils.replace(getMessagesFile(locale).getString(message.getPath(), message.getDefault()));
    }

    public static String[] tls(Message message, String locale) {
        if (instance == null) {
            return StringUtils.replace(message.getDefaults());
        }
        if (locale == null || locale.equals("")) {
            locale = DEFAULT_LOCALE;
        }
        List<String> lines = getMessagesFile(locale).getStringList(message.getPath());
        if (lines == null || lines.isEmpty()) {
            return StringUtils.replace(message.getDefaults());
        }
        return StringUtils.replace(lines.toArray(new String[0]));
    }

    public static AbstractConfig getMessagesFile(String locale) {
        return Optional.ofNullable(instance.messagesMap.get(locale)).orElse(instance.messagesMap.get(DEFAULT_LOCALE));
    }
}
