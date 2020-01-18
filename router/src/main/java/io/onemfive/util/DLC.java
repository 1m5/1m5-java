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

import io.onemfive.data.*;
import io.onemfive.data.route.SimpleExternalRoute;
import io.onemfive.data.route.SimpleRoute;
import io.onemfive.network.NetworkPeer;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Location Constants
 *
 * @author objectorange
 */
public final class DLC {

    public static final String CONTENT = "CONTENT";
    public static final String ENTITY = "ENTITY";
    public static final String EXCEPTIONS = "EXCEPTIONS";

    public static boolean addRoute(Class service, String operation, Envelope envelope) {
        envelope.getDynamicRoutingSlip().addRoute(new SimpleRoute(service.getName(),operation));
        return true;
    }

    public static boolean addExternalRoute(Class service, String operation, Envelope envelope, NetworkPeer origination, NetworkPeer destination) {
        envelope.getDynamicRoutingSlip().addRoute(new SimpleExternalRoute(service.getName(), operation, origination, destination));
        return true;
    }

    public static boolean addContent(Object content, Envelope envelope) {
        Message m = envelope.getMessage();
        if(!(m instanceof DocumentMessage)) {
            return false;
        }
        ((DocumentMessage)m).data.get(0).put(CONTENT, content);
        return true;
    }

    public static Object getContent(Envelope envelope) {
        Message m = envelope.getMessage();
        if(!(m instanceof DocumentMessage)) {
            return null;
        }
        return ((DocumentMessage)m).data.get(0).get(CONTENT);
    }

    public static boolean addEntity(Object entity, Envelope envelope) {
        Message m = envelope.getMessage();
        if(!(m instanceof DocumentMessage)) {
            return false;
        }
        ((DocumentMessage)m).data.get(0).put(ENTITY, entity);
        return true;
    }

    public static Object getEntity(Envelope envelope) {
        Message m = envelope.getMessage();
        if(!(m instanceof DocumentMessage)) {
            return null;
        }
        return ((DocumentMessage)m).data.get(0).get(ENTITY);
    }

    public static boolean addException(Exception e, Envelope envelope) {
        Message m = envelope.getMessage();
        if(!(m instanceof DocumentMessage)) {
            return false;
        }
        List<Exception> exceptions = (List<Exception>)((DocumentMessage)m).data.get(0).get(EXCEPTIONS);
        if(exceptions == null) {
            exceptions = new ArrayList<>();
            ((DocumentMessage)m).data.get(0).put(EXCEPTIONS, exceptions);
        }
        exceptions.add(e);
        return true;
    }

    public static List<Exception> getExceptions(Envelope envelope) {
        Message m = envelope.getMessage();
        if(!(m instanceof DocumentMessage)) {
            return null;
        }
        List<Exception> exceptions = (List<Exception>)((DocumentMessage)m).data.get(0).get(EXCEPTIONS);
        if(exceptions == null) {
            exceptions = new ArrayList<>();
            ((DocumentMessage)m).data.get(0).put(EXCEPTIONS, exceptions);
        }
        return exceptions;
    }

    public static void addErrorMessage(String errorMessage, Envelope envelope) {
        envelope.getMessage().addErrorMessage(errorMessage);
    }

    public static List<String> getErrorMessages(Envelope envelope) {
        return envelope.getMessage().getErrorMessages();
    }

    public static boolean addData(Class clazz, Object object, Envelope envelope) {
        Message m = envelope.getMessage();
        if(!(m instanceof DocumentMessage)) {
            return false;
        }
        DocumentMessage dm = (DocumentMessage)m;
        dm.data.get(0).put(clazz.getName(), object);
        return true;
    }

    public static Object getData(Class clazz, Envelope envelope) {
        Message m = envelope.getMessage();
        if(!(m instanceof DocumentMessage)) {
            return null;
        }
        return ((DocumentMessage)m).data.get(0).get(clazz.getName());
    }

    public static boolean addNVP(String name, Object object, Envelope envelope){
        Message m = envelope.getMessage();
        if(!(m instanceof DocumentMessage)) {
            return false;
        }
        DocumentMessage dm = (DocumentMessage)m;
        dm.data.get(0).put(name, object);
        return true;
    }

    public static Object getValue(String name, Envelope envelope) {
        Message m = envelope.getMessage();
        if(!(m instanceof DocumentMessage)) {
            return null;
        }
        return ((DocumentMessage)m).data.get(0).get(name);
    }

    public static EventMessage getEventMessage(Envelope e) {
        Message m = e.getMessage();
        if(!(m instanceof EventMessage))
            return null;
        return (EventMessage)m;
    }
}
