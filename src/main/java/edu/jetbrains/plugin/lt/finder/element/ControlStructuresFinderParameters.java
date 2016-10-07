package edu.jetbrains.plugin.lt.finder.element;

import com.intellij.openapi.util.Pair;
import edu.jetbrains.plugin.lt.finder.Parameters;

public class ControlStructuresFinderParameters extends Parameters {
    static {
        TEXT_MAP.put(Name.MATCHES_MINIMUM, "Matches Minimum");
        TEXT_MAP.put(Name.LENGTH_MINIMUM, "Length Minimum");
        TEXT_MAP.put(Name.TEMPLATES_TO_SHOW, "Templates to Show");
    }

    static {
        BOUNDS_MAP.put(Name.MATCHES_MINIMUM, Pair.create(50, 1500));
        BOUNDS_MAP.put(Name.LENGTH_MINIMUM, Pair.create(1, 100));
        BOUNDS_MAP.put(Name.TEMPLATES_TO_SHOW, Pair.create(0, 200));
    }

    static {
        DEFAULT_MAP.put(Name.MATCHES_MINIMUM, 200);
        DEFAULT_MAP.put(Name.LENGTH_MINIMUM, 3);
        DEFAULT_MAP.put(Name.TEMPLATES_TO_SHOW, 30);
    }


    public ControlStructuresFinderParameters() {
        super();
    }

    @Override
    public Parameters.Name[] names() {
        return Name.values();
    }

    public enum Name implements Parameters.Name {
        MATCHES_MINIMUM,
        LENGTH_MINIMUM,
        TEMPLATES_TO_SHOW
    }
}
