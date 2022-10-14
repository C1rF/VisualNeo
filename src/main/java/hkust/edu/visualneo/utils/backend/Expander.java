package hkust.edu.visualneo.utils.backend;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static hkust.edu.visualneo.utils.backend.Consts.NEW_LINE;

public class Expander {

    private static final int MAX_INDENT = 16;

    private static final String COLON = ": ";
    private static final String COMMA = ", ";
    private static final String STACK = "| ";
    private static final String FOCUS = "|-";
    private static final String EMPTY = "None";

    private final StringBuilder builder = new StringBuilder();

    private int indent;

    public String expand(Expandable expandable) {
        builder.append(expandable);
        elaborate(expandable);
        String expansion = builder.toString();
        clear();
        return expansion;
    }

    private void elaborate(Expandable expandable) {
        elaborate(expandable.expand());
    }

    private void elaborate(Map<?, ?> map) {
        if (map.isEmpty())
            builder.append(COLON).append(EMPTY);

        else {
            increaseIndent();
            map.forEach((key, value) -> {
                newLine();
                builder.append(key);

                if (value instanceof Expandable expandable)
                    elaborate(expandable);

                else if (value instanceof Map<?, ?> subMap) {
                    elaborate(subMap);

                }
                else if (value instanceof Collection<?> collection) {
                    if (collection.isEmpty())
                        builder.append(COLON).append(EMPTY);

                    else if (collection.iterator().next() instanceof Expandable) {
                        increaseIndent();
                        collection.forEach(element -> {
                            newLine();
                            builder.append(element);
                            elaborate((Expandable) element);
                        });
                        decreaseIndent();
                    }

                    else if (collection.iterator().next() instanceof Map<?, ?>) {
                        increaseIndent();
                        collection.forEach(element -> {
                            newLine();
                            builder.append(element);
                            elaborate((Map<?, ?>) element);
                        });
                        decreaseIndent();
                    }

                    else {
                        builder.append(COLON);
                        Iterator<?> iter = collection.iterator();
                        builder.append(iter.next());
                        while (iter.hasNext())
                            builder.append(COMMA).append(iter.next());
                    }
                }

                else
                    builder.append(COLON).append(value == null ? EMPTY : value);
            });
            decreaseIndent();
        }
    }

    private void increaseIndent() {
        if (++indent > MAX_INDENT)
            indent = MAX_INDENT;
    }

    private void decreaseIndent() {
        if (--indent < 0)
            indent = 0;
    }

    private void increaseIndent(int delta) {
        if ((indent += delta) > MAX_INDENT)
            indent = MAX_INDENT;
    }

    private void decreaseIndent(int delta) {
        if ((indent -= delta) < 0)
            indent = 0;
    }

    private void newLine() {
        builder.append(NEW_LINE);
        if (indent > 0) {
            builder.append(STACK.repeat(indent - 1));
            builder.append(FOCUS);
        }
    }

    private void clear() {
        builder.setLength(0);
        indent = 0;
    }
}
