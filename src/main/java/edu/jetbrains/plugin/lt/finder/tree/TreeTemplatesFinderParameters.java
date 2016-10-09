package edu.jetbrains.plugin.lt.finder.tree;

import com.intellij.openapi.util.Pair;
import edu.jetbrains.plugin.lt.finder.Parameters;

public class TreeTemplatesFinderParameters extends Parameters {
    static {
        TEXT_MAP.put(Name.DEPTH_MINIMUM, "Depth Minimum");
        TEXT_MAP.put(Name.LENGTH_MAXIMUM, "Length Maximum");
        TEXT_MAP.put(Name.MATCHES_MINIMUM, "Matches Minimum");
        TEXT_MAP.put(Name.LENGTH_MINIMUM, "Length Minimum");
        TEXT_MAP.put(Name.NODES_MINIMUM, "Nodes Minimum");
        TEXT_MAP.put(Name.PLACEHOLDERS_MAXIMUM, "Placeholders Maximum");
        TEXT_MAP.put(Name.TEMPLATES_TO_SHOW, "Templates to Show");
    }

    static {
        BOUNDS_MAP.put(Name.DEPTH_MINIMUM, Pair.create(4, 10));
        BOUNDS_MAP.put(Name.LENGTH_MAXIMUM, Pair.create(1000, 2000));
        BOUNDS_MAP.put(Name.MATCHES_MINIMUM, Pair.create(2, 100));
        BOUNDS_MAP.put(Name.LENGTH_MINIMUM, Pair.create(4, 200));
        BOUNDS_MAP.put(Name.NODES_MINIMUM, Pair.create(5, 50));
        BOUNDS_MAP.put(Name.PLACEHOLDERS_MAXIMUM, Pair.create(1, 10));
        BOUNDS_MAP.put(Name.TEMPLATES_TO_SHOW, Pair.create(0, 100));
    }

    static {
        DEFAULT_MAP.put(Name.DEPTH_MINIMUM, 5);
        DEFAULT_MAP.put(Name.LENGTH_MAXIMUM, 1488);
        DEFAULT_MAP.put(Name.MATCHES_MINIMUM, 10);
        DEFAULT_MAP.put(Name.LENGTH_MINIMUM, 36);
        DEFAULT_MAP.put(Name.NODES_MINIMUM, 8);
        DEFAULT_MAP.put(Name.PLACEHOLDERS_MAXIMUM, 3);
        DEFAULT_MAP.put(Name.TEMPLATES_TO_SHOW, 30);
    }


    public TreeTemplatesFinderParameters() {
        super();
    }

    @Override
    public Parameters.Name[] names() {
        return Name.values();
    }

    public enum Name implements Parameters.Name {
        MATCHES_MINIMUM,
        LENGTH_MINIMUM,
        LENGTH_MAXIMUM,
        DEPTH_MINIMUM,
        NODES_MINIMUM,
        PLACEHOLDERS_MAXIMUM,
        PLACEHOLDER_NODES_PERCENT_MAXIMUM,
        NODE_TEMPLATE_PERCENT,
        TEMPLATES_TO_SHOW
    }
}
