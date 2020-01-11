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
package io.onemfive.ext.komodo;

import io.onemfive.core.BaseService;
import io.onemfive.data.Envelope;
import io.onemfive.data.Request;
import io.onemfive.data.route.Route;
import io.onemfive.util.DLC;

import java.util.logging.Logger;

public class KomodoService extends BaseService {

    private static final Logger LOG = Logger.getLogger(KomodoService.class.getName());

    public static final String OPERATION_SPEND = "SPEND";

    @Override
    public void handleDocument(Envelope e) {
        LOG.warning("Komodo not yet implemented.");
        Route route = e.getRoute();
        String operation = route.getOperation();
        switch(operation) {
            case OPERATION_SPEND: {
                Request request = (Request) DLC.getData(Request.class,e);
//                if(!bitcoin.send(request)) {
//                    LOG.warning("Issue sending BTC to "+request.base58To);
//                }
                break;
            }
            default: deadLetter(e); // Operation not supported
        }
    }
}
