package io.onemfive.data.util;

public class JSONPretty {

    public static String toPretty(final String json, final int indention) {
        final char[] chars = json.toCharArray();
        final String newline = System.lineSeparator();

        StringBuilder sb = new StringBuilder();
        boolean begin_quotes = false;

        for (int i = 0, indent = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '\"') {
                sb.append(c);
                begin_quotes = !begin_quotes;
                continue;
            }

            if (!begin_quotes) {
                switch (c) {
                    case '{':
                    case '[':
                        sb.append(c + newline + String.format("%" + (indent += indention) + "s", ""));
                        continue;
                    case '}':
                    case ']':
                        sb.append(newline + ((indent -= indention) > 0 ? String.format("%" + indent + "s", "") : "") + c);
                        continue;
                    case ':':
                        sb.append(c + " ");
                        continue;
                    case ',':
                        sb.append(c + newline + (indent > 0 ? String.format("%" + indent + "s", "") : ""));
                        continue;
                    default:
                        if (Character.isWhitespace(c)) continue;
                }
            }

            sb.append(c + (c == '\\' ? "" + chars[++i] : ""));
        }

        return sb.toString();
    }
}
