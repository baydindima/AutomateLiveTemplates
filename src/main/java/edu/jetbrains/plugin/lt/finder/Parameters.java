package edu.jetbrains.plugin.lt.finder;

import com.intellij.openapi.util.Pair;

import java.util.HashMap;
import java.util.Map;

public abstract class Parameters {
    protected static final Map<Name, String> TEXT_MAP;
    protected static final Map<Name, Pair<Integer, Integer>> BOUNDS_MAP;
    protected static final Map<Name, Integer> DEFAULT_MAP;

    static {
        TEXT_MAP = new HashMap<>();
    }

    static {
        BOUNDS_MAP = new HashMap<>();
    }

    static {
        DEFAULT_MAP = new HashMap<>();
    }

    private Map<Name, Integer> values;

    public Parameters() {
        values = new HashMap<>();
    }

    public static String getText(Name name) {
        return TEXT_MAP.get(name);
    }

    public static Pair<Integer, Integer> getBounds(Name name) {
        return BOUNDS_MAP.get(name);
    }

    public static int getDefault(Name name) {
        return DEFAULT_MAP.get(name);
    }

    public abstract Name[] names();

    public void setParameter(Name name, int value) {
        values.put(name, value);
    }

    public int getParameter(Name name) {
        Integer value = values.get(name);
        return value != null ? value : getDefault(name);
    }

    public interface Name {
    }
}
