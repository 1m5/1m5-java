/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.util;

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
