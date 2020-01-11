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

import java.util.Map;
import java.util.Random;

/**
 * Events
 *
 * @author objectorange
 */
public final class EventMessage extends BaseMessage {

    public enum Type {
        EMAIL,
        ERROR,
        EXCEPTION,
        STATUS_BUS,
        STATUS_CLIENT,
        STATUS_DID,
        STATUS_SENSOR,
        STATUS_SERVICE,
        TEXT
    }

    private Long id = new Random(4763128720251739L).nextLong();
    private String type;
    private String name;
    private Object message;

    public EventMessage(String type){this.type = type;}

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    @Override
    public Map<String, Object> toMap() {
        return super.toMap();
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        super.fromMap(m);
    }
}
