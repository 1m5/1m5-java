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
package io.onemfive.neo4j;

import io.onemfive.util.JSONParser;
import org.neo4j.graphdb.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class GraphUtil {

    private static Logger LOG = Logger.getLogger(GraphUtil.class.getName());

    public static void updateProperties(PropertyContainer c, Map<String,Object> a) {
        Set<String> keys = a.keySet();
        for(String key : keys) {
            Object o = a.get(key);
            if(o instanceof String || o instanceof Number)
                c.setProperty(key,o);
            else if(o instanceof Map || o instanceof Collection) {
                c.setProperty(key,JSONParser.toString(o));
            } else {
                c.setProperty(key,o.toString());
            }
        }
    }

    public static Map<String,Object> getAttributes(PropertyContainer c) {
        Map<String,Object> a = new HashMap<>();
        for (String key : c.getPropertyKeys()) {
            Object o = c.getProperty(key);
            if(o instanceof String) {
                String str = (String)o;
                if(str.startsWith("{")) {
                    o = JSONParser.parse(str);
                    a.put(key, o);
                } else {
                    a.put(key, str);
                }
            } else {
                a.put(key, o);
            }
        }
        return a;
    }

}
