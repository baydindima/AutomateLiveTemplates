package edu.jetbrains.plugin.lt.finder.element;

public class ElementValue {
    private static final String PLACEHOLDER_STRING = "#_#";

    private boolean isPlaceholder;
    private String value;

    public ElementValue() {
        isPlaceholder = false;
        value = null;
    }

    public void add(String text) {
        if (isPlaceholder) return;
        if (value == null) {
            value = text;
            return;
        }
        isPlaceholder = !value.equals(text);
    }

    public String get() {
        return isPlaceholder ? PLACEHOLDER_STRING : value;
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }
}
