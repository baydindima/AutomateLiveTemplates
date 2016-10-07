package edu.jetbrains.plugin.lt.finder;

import java.util.HashMap;
import java.util.Map;

public class Modes {
    private static final Map<Name, String> TEXT_MAP;

    static {
        TEXT_MAP = new HashMap<>();
        TEXT_MAP.put(Name.TEMPLATES_FINDER, "Templates");
        TEXT_MAP.put(Name.CONTROL_STRUCTURES_FINDER, "Control Structures");
    }

    public static String getText(Name name) {
        return TEXT_MAP.get(name);
    }

    public enum Name {
        TEMPLATES_FINDER,
        CONTROL_STRUCTURES_FINDER
    }

    public static class ModeHolder {
        private Name mode;

        public ModeHolder(Name mode) {
            this.mode = mode;
        }

        public Name getMode() {
            return mode;
        }

        public void setMode(Name mode) {
            this.mode = mode;
        }
    }
}
