package hkust.edu.visualneo.utils.backend;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class TreePrinter {

    public static final String NEW_LINE = System.lineSeparator();
    private static final String COLON = ": ";
    private static final String COMMA = ", ";
    private static final String PREFIX = "├─";
    private static final String LAST_PREFIX = "└─";
    private static final String CHILDREN_PREFIX = "│ ";
    private static final String LAST_CHILDREN_PREFIX = "  ";
    private static final String NONE = "None";

    private final StringBuilder buffer = new StringBuilder();

    public String print(Object name, Map<?, ?> tree) {
        print(name, tree, "", "");
        return flush();
    }

    private void print(Object name, Object value, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(name);

        if (value instanceof Map<?, ?> subTree) {
            if (subTree.isEmpty()) {
                buffer.append(COLON).append(NONE);
                buffer.append(NEW_LINE);
            }
            else {
                buffer.append(NEW_LINE);

                Iterator<? extends Map.Entry<?, ?>> it = subTree.entrySet().iterator();
                while (true) {
                    Map.Entry<?, ?> next = it.next();
                    if (it.hasNext())
                        print(next.getKey(),
                              next.getValue(),
                              childrenPrefix + PREFIX,
                              childrenPrefix + CHILDREN_PREFIX);
                    else {
                        print(next.getKey(),
                              next.getValue(),
                              childrenPrefix + LAST_PREFIX,
                              childrenPrefix + LAST_CHILDREN_PREFIX);
                        break;
                    }
                }
            }
        }
        else {
            buffer.append(COLON);

            if (value instanceof Collection<?> contents) {
                if (contents.isEmpty())
                    buffer.append(NONE);
                else {
                    Iterator<?> it = contents.iterator();
                    while (true) {
                        Object next = it.next();
                        if (it.hasNext())
                            buffer.append(next).append(COMMA);
                        else {
                            buffer.append(next);
                            break;
                        }
                    }
                }
            }
            else
                buffer.append(value == null ? NONE : value);

            buffer.append(NEW_LINE);
        }
    }

    private String flush() {
        String str = buffer.toString();
        buffer.setLength(0);
        return str;
    }
}
