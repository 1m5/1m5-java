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
package io.onemfive.network.sensors.radio;

import io.onemfive.data.JSONSerializable;
import io.onemfive.data.content.Content;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class RadioDatagram implements JSONSerializable {

    private Logger LOG = Logger.getLogger(RadioDatagram.class.getName());

    public RadioPeer from;
    public RadioPeer to;
    public RadioPeer destination;
    public Content content;

    @Override
    public Map<String, Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        if(from!=null) m.put("from",from.toMap());
        if(to!=null) m.put("to",to.toMap());
        if(destination!=null) m.put("destination",destination.toMap());
        if(content!=null) m.put("content",content.toMap());
        return m;
    }

    @Override
    public void fromMap(Map<String, Object> m) {
        if(m.get("from")!=null) {
            from = new RadioPeer();
            from.fromMap((Map<String,Object>)m.get("from"));
        }
        if(m.get("to")!=null) {
            to = new RadioPeer();
            to.fromMap((Map<String,Object>)m.get("to"));
        }
        if(m.get("destination")!=null) {
            destination = new RadioPeer();
            destination.fromMap((Map<String,Object>)m.get("destination"));
        }
        if(m.get("content")!=null) {
            try {
                content = Content.newInstance(m);
            } catch (Exception e) {
                LOG.warning(e.getLocalizedMessage());
            }
            content.fromMap((Map<String,Object>)m.get("content"));
        }
    }
}
