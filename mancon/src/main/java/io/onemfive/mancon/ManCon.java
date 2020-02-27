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
package io.onemfive.mancon;

import io.onemfive.util.Config;

import java.util.*;
import java.util.logging.Logger;

/**
 * 1M5 ManCon Node
 * Tests and reports on network parameters within defined IP ranges
 * providing real-time status updates for 1M5 ManCon recommendations.
 */
public class ManCon {

    private static Logger LOG = Logger.getLogger(ManCon.class.getName());

    private static ManCon instance;
    public static boolean isRunning = true;

    public static Properties config;

    public static void main(String[] args) throws Exception {
        config = new Properties();
        try {
            config.putAll(Config.loadFromClasspath("1m5-mancon.config", null, false));
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
            System.exit(-1);
        }

    }

}
