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
package io.onemfive.network.sensors.tor.control;

import java.io.PrintWriter;
import java.util.Iterator;

public class DebuggingEventHandler implements EventHandler {

    private final PrintWriter out;

    public DebuggingEventHandler(PrintWriter p) {
        out = p;
    }

    public void circuitStatus(String status, String circID, String path) {
        out.println("Circuit "+circID+" is now "+status+" (path="+path+")");
    }
    public void streamStatus(String status, String streamID, String target) {
        out.println("Stream "+streamID+" is now "+status+" (target="+target+")");
    }
    public void orConnStatus(String status, String orName) {
        out.println("OR connection to "+orName+" is now "+status);
    }
    public void bandwidthUsed(long read, long written) {
        out.println("Bandwidth usage: "+read+" bytes read; "+
                written+" bytes written.");
    }
    public void newDescriptors(java.util.List<String> orList) {
        out.println("New descriptors for routers:");
        for (Iterator<String> i = orList.iterator(); i.hasNext(); )
            out.println("   "+i.next());
    }
    public void message(String type, String msg) {
        out.println("["+type+"] "+msg.trim());
    }

    public void hiddenServiceEvent(String type, String msg) {
        out.println("hiddenServiceEvent: HS_DESC " + msg.trim());
    }

    public void hiddenServiceFailedEvent(String reason, String msg) {
        out.println("hiddenServiceEvent: HS_DESC " + msg.trim());
    }

    public void hiddenServiceDescriptor(String descriptorId, String descriptor, String msg) {
        out.println("hiddenServiceEvent: HS_DESC_CONTENT " + msg.trim());
    }

    public void unrecognized(String type, String msg) {
        out.println("unrecognized event ["+type+"] "+msg.trim());
    }

    @Override
    public void timeout() {
        out.println("The control connection to tor did not provide a response within one minute of waiting.");
    }

}