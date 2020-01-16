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
package io.onemfive.data;

import io.onemfive.util.JSONParser;
import io.onemfive.util.JSONPretty;

import java.util.HashMap;
import java.util.Map;

public abstract class ServiceMessage implements JSONSerializable {
    public static int NO_ERROR = -1;
    public static int REQUEST_REQUIRED = 0;

    public int statusCode = NO_ERROR;
    public String errorMessage;
    public Exception exception;
    public String type;

    public Map<String,Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        m.put("statusCode", statusCode +"");
        if(errorMessage!=null) m.put("errorMessage",errorMessage);
        if(exception!=null) m.put("exception",exception.getLocalizedMessage());
        m.put("type",getClass().getName());
        return m;
    }

    public void fromMap(Map<String,Object> m) {
        if(m.get("statusCode")!=null) this.statusCode = Integer.parseInt((String)m.get("statusCode"));
        if(m.get("errorMessage")!=null) this.errorMessage = (String)m.get("errorMessage");
        if(m.get("exception")!=null) {
            exception = new Exception((String)m.get("exception"));
        }
        if(m.get("type")!=null) type = (String)m.get("type");
    }

    @Override
    public String toJSON() {
        return JSONPretty.toPretty(JSONParser.toString(toMap()), 4);
    }

    @Override
    public void fromJSON(String json) {
        fromMap((Map<String,Object>)JSONParser.parse(json));
    }
}
