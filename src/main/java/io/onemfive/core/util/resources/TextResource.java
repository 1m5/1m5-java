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
package io.onemfive.core.util.resources;

import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;

public abstract class TextResource extends ResourceBundle {

    public static Logger LOG = Logger.getLogger(TextResource.class.getName());
    
    public static String getText(ResourceBundle catalog, String msgid) {
        String result = null;
        try {
            result = (String) catalog.getObject(msgid);
        } catch (MissingResourceException e) {
            LOG.warning(e.getLocalizedMessage());
        }
        return result;
    }

    public static String getText(ResourceBundle catalog, String msgid, String msgidPlural, long n) {
        String result = getText(catalog,msgid,n);
        if (result != null)
            return result;
        return (n != 1 ? msgidPlural : msgid);
    }

    private static String getText(ResourceBundle catalog, String msgid, long n) {

        do {
            // Try catalog itself.
            LOG.fine("catalog: "+catalog);
            Method handleGetObjectMethod = null;
            Method getParentMethod = null;
            try {
                handleGetObjectMethod = catalog.getClass().getMethod("handleGetObject", new Class[] { java.lang.String.class });
                getParentMethod = catalog.getClass().getMethod("getParent", new Class[0]);
            } catch (NoSuchMethodException e) {
            } catch (SecurityException e) {
            }
            LOG.fine("handleGetObject = "+(handleGetObjectMethod!=null)+", getParent = "+(getParentMethod!=null));
            if (handleGetObjectMethod != null
                    && Modifier.isPublic(handleGetObjectMethod.getModifiers())
                    && getParentMethod != null) {
                // A GNU gettext created class.
                Method lookupMethod = null;
                Method pluralEvalMethod = null;
                try {
                    lookupMethod = catalog.getClass().getMethod("lookup", new Class[] { java.lang.String.class });
                    pluralEvalMethod = catalog.getClass().getMethod("pluralEval", new Class[] { Long.TYPE });
                } catch (NoSuchMethodException e) {
                } catch (SecurityException e) {
                }
                LOG.fine("lookup = "+(lookupMethod!=null)+", pluralEval = "+(pluralEvalMethod!=null));
                if (lookupMethod != null && pluralEvalMethod != null) {
                    // A GNU gettext created class with plural handling.
                    Object localValue = null;
                    try {
                        localValue = lookupMethod.invoke(catalog, new Object[] { msgid });
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.getTargetException().printStackTrace();
                    }
                    if (localValue != null) {
                        LOG.fine("localValue = "+localValue);
                        if (localValue instanceof String)
                            // Found the value. It doesn't depend on n in this case.
                            return (String)localValue;
                        else {
                            String[] pluralforms = (String[])localValue;
                            long i = 0;
                            try {
                                i = ((Long) pluralEvalMethod.invoke(catalog, new Object[] { new Long(n) })).longValue();
                                if (!(i >= 0 && i < pluralforms.length))
                                    i = 0;
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.getTargetException().printStackTrace();
                            }
                            return pluralforms[(int)i];
                        }
                    }
                } else {
                    // A GNU gettext created class without plural handling.
                    Object localValue = null;
                    try {
                        localValue = handleGetObjectMethod.invoke(catalog, new Object[] { msgid });
                    } catch (IllegalAccessException e) {
                        LOG.warning(e.getLocalizedMessage());
                    } catch (InvocationTargetException e) {
                        LOG.warning(e.getLocalizedMessage());
                    }
                    if (localValue != null) {
                        // Found the value. It doesn't depend on n in this case.
                        LOG.fine("localValue = "+localValue);
                        return (String)localValue;
                    }
                }
                Object parentCatalog = catalog;
                try {
                    parentCatalog = getParentMethod.invoke(catalog, new Object[0]);
                } catch (IllegalAccessException e) {
                    LOG.warning(e.getLocalizedMessage());
                } catch (InvocationTargetException e) {
                    LOG.warning(e.getLocalizedMessage());
                }
                if (parentCatalog != catalog)
                    catalog = (ResourceBundle)parentCatalog;
                else
                    break;
            } else
                // Not a GNU gettext created class.
                break;
        } while (catalog != null);

        if (catalog != null) {
            Object value;
            try {
                value = catalog.getObject(msgid);
            } catch (MissingResourceException e) {
                value = null;
            }
            if (value != null)
                return (String)value;
        }
        return null;
    }
}
