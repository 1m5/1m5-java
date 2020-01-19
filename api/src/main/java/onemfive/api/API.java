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
package onemfive.api;

import io.onemfive.data.Envelope;
import io.onemfive.util.DLC;
import io.onemfive.util.Multipart;
import okhttp3.*;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class API {

    private static final Logger LOG = Logger.getLogger(API.class.getName());

    private static URL url;

    private static ConnectionSpec httpSpec;
    private static OkHttpClient httpClient;

    private static ExecutorService executorService;
    private static boolean isInitialized = false;

    public static Envelope send(Envelope e) {
        if(!isInitialized || !init()) {
            handleFailure(e, "Unable to initialize API.");
            return e;
        }
        e.setAction(Envelope.Action.POST);
        Map<String,Object> h = e.getHeaders();
        Map<String,String> hStr = new HashMap<>();
        if(h.containsKey(Envelope.HEADER_CONTENT_DISPOSITION) && h.get(Envelope.HEADER_CONTENT_DISPOSITION)!=null) {
            hStr.put(Envelope.HEADER_CONTENT_DISPOSITION,(String)h.get(Envelope.HEADER_CONTENT_DISPOSITION));
        }
        if(h.containsKey(Envelope.HEADER_CONTENT_TYPE) && h.get(Envelope.HEADER_CONTENT_TYPE)!=null) {
            hStr.put(Envelope.HEADER_CONTENT_TYPE, (String)h.get(Envelope.HEADER_CONTENT_TYPE));
        }
        if(h.containsKey(Envelope.HEADER_CONTENT_TRANSFER_ENCODING) && h.get(Envelope.HEADER_CONTENT_TRANSFER_ENCODING)!=null) {
            hStr.put(Envelope.HEADER_CONTENT_TRANSFER_ENCODING, (String)h.get(Envelope.HEADER_CONTENT_TRANSFER_ENCODING));
        }
        if(h.containsKey(Envelope.HEADER_USER_AGENT) && h.get(Envelope.HEADER_USER_AGENT)!=null) {
            hStr.put(Envelope.HEADER_USER_AGENT, (String)h.get(Envelope.HEADER_USER_AGENT));
        }

        ByteBuffer bodyBytes = null;
        CacheControl cacheControl = null;
        if(e.getMultipart() != null) {
            // handle file upload
            Multipart m = e.getMultipart();
            hStr.put(Envelope.HEADER_CONTENT_TYPE, "multipart/form-data; boundary=" + m.getBoundary());
            try {
                bodyBytes = ByteBuffer.wrap(m.finish().getBytes());
            } catch (IOException e1) {
                handleFailure(e, "IOException caught while building HTTP body with multipart: "+e1.getLocalizedMessage());
                return e;
            }
            cacheControl = new CacheControl.Builder().noCache().build();
        }

        Headers headers = Headers.of(hStr);

        String json = e.toJSON();
        if(bodyBytes == null) {
            bodyBytes = ByteBuffer.wrap(json.getBytes());
        } else {
            bodyBytes.put(json.getBytes());
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse((String) h.get(Envelope.HEADER_CONTENT_TYPE)), bodyBytes.array());

        okhttp3.Request.Builder b = new okhttp3.Request.Builder().url(url);
        if(cacheControl != null)
            b = b.cacheControl(cacheControl);
        b = b.headers(headers);
        switch(e.getAction()) {
            case POST: {b = b.post(requestBody);break;}
            case PUT: {b = b.put(requestBody);break;}
            case DELETE: {b = b.delete(requestBody);break;}
            case GET: {b = b.get();break;}
            default: {
                handleFailure(e, "Envelope.action must be set to ADD, UPDATE, REMOVE, or VIEW");
                return e;
            }
        }
        Request req = b.build();
        if(req == null) {
            handleFailure(e, "okhttp3 builder didn't build request.");
            return e;
        }
        Response response = null;
        LOG.info("Sending http request, host="+url.getHost());
        if(httpClient == null) {
            handleFailure(e, "httpClient was not set up.");
            return e;
        }
        try {
            response = httpClient.newCall(req).execute();
            if(!response.isSuccessful()) {
                handleFailure(e, "HTTP request not successful: "+response.code());
                return e;
            }
        } catch (IOException e2) {
            handleFailure(e, e2.getLocalizedMessage());
            return e;
        }

        LOG.info("Received http response.");
        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            LOG.info(responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }
        ResponseBody responseBody = response.body();
        Envelope eResponse = null;
        if(responseBody != null) {
            try {
                DLC.addContent(responseBody.bytes(),e);
                eResponse = new Envelope();
                json = responseBody.toString();
                LOG.info(json);
                eResponse.fromJSON(json);
            } catch (IOException e1) {
                handleFailure(e, e1.getLocalizedMessage());
                return e;
            } finally {
                responseBody.close();
            }
        }
        return eResponse == null ? e : eResponse;
    }

    public static boolean sendAsync(final Envelope e, final APICallback cb) {
        if(!isInitialized || !init())
            return false;
        executorService.execute(() -> {
            send(e);
            cb.reply(e);
        });
        return true;
    }

    protected static void handleFailure(Envelope e, String message) {
        LOG.warning(message);
        e.getMessage().addErrorMessage(message);
    }

    /**
     * Initialize API
     */
    public static boolean init() {
        try {
            url = new URL("http://localhost:2017");
            LOG.info("Setting up http spec....");
            httpSpec = new ConnectionSpec
                    .Builder(ConnectionSpec.CLEARTEXT)
                    .build();
            LOG.info("Setting up http client...");
            httpClient = new OkHttpClient.Builder()
                    .connectionSpecs(Collections.singletonList(httpSpec))
                    .retryOnConnectionFailure(true)
                    .followRedirects(false)
                    .build();
            executorService = Executors.newFixedThreadPool(4);
        } catch (Exception e) {
            LOG.warning("Exception caught initializing API: " + e.getLocalizedMessage());
            isInitialized = false;
            return false;
        }
        isInitialized = true;
        return true;
    }

}
