package io.github.piscescup.fabricmc.group;

import io.github.piscescup.fabricmc.config.GroupConfig;
import io.github.piscescup.fabricmc.exception.InvalidGroupException;

import java.text.Normalizer;
import java.util.Locale;

/**
 *
 * @author REN YuanTong
 * @since
 */
public record Group(String name, int id) {
    public static final int MAX_NAME_LENGTH = 64;

    public Group {
        name = validateName(name);
        if (id <= 0) {
            throw InvalidGroupException.id(id);
        }
    }

    public static String validateName(String name) {
        if (name == null) {
            throw InvalidGroupException.name(null);
        }

        String trimmed = name.strip();
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFKC).strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        if (normalized.isEmpty() || length > MAX_NAME_LENGTH) {
            throw InvalidGroupException.name(name);
        }

        for (int offset = 0; offset < normalized.length();) {
            int codePoint = normalized.codePointAt(offset);
            if (codePoint == '/' || Character.isISOControl(codePoint)) {
                throw InvalidGroupException.name(name);
            }
            offset += Character.charCount(codePoint);
        }
        return trimmed;
    }

    /**
     *
     * @author REN YuanTong
     * @since
     */
    public record JoinResult(Group group, boolean joined) {}
}