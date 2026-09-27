package io.github.piscescup.fabricmc.config;

public enum BroadcastLanguage {
    EN_US("en_us"),
    ZH_CN("zh_cn");

    private final String code;

    BroadcastLanguage(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static BroadcastLanguage fromCode(String code) {
        for (BroadcastLanguage language : values()) {
            if (language.code.equalsIgnoreCase(code)) {
                return language;
            }
        }

        throw new IllegalArgumentException("Unsupported language: " + code);
    }
}
