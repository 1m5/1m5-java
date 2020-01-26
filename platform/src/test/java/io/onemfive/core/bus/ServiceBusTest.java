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
package io.onemfive.core.bus;

import io.onemfive.core.client.ClientAppManager;
import io.onemfive.core.notification.NotificationService;
import io.onemfive.core.notification.SubscriptionRequest;
import io.onemfive.data.Envelope;
import io.onemfive.data.EventMessage;
import io.onemfive.data.Subscription;
import io.onemfive.data.TextMessage;
import io.onemfive.util.DLC;
import org.junit.*;

import java.util.Properties;

// TODO: Complete the test to remove ignore
@Ignore
public class ServiceBusTest {

    Properties properties;
    ClientAppManager clientAppManager;
    ServiceBus bus;

    @Before
    public void setup() {
        properties = new Properties();
        clientAppManager = new ClientAppManager(false);
        bus = new ServiceBus(properties, clientAppManager);
        bus.start(properties);
    }

    @Test
    public void send() {
        Envelope se = Envelope.documentFactory();
        SubscriptionRequest r = new SubscriptionRequest(EventMessage.Type.TEXT, new Subscription() {
            @Override
            public void notifyOfEvent(Envelope envelope) {
                Assert.assertEquals("Hello Gaia!", ((TextMessage)envelope.getMessage()).getText());
            }
        });
        DLC.addData(SubscriptionRequest.class, r, se);
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_SUBSCRIBE, se);
        bus.send(se);

        Envelope pe = Envelope.eventFactory(EventMessage.Type.TEXT);
        EventMessage m = (EventMessage)pe.getMessage();
        m.setMessage("Hello Gaia!");
        DLC.addRoute(NotificationService.class, NotificationService.OPERATION_PUBLISH, pe);


    }

    @After
    public void teardown() {
        clientAppManager.stop();
    }
}
