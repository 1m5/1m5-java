package io.onemfive.util;

import java.io.IOException;
import java.io.InputStream;

public interface NamedStreamable
{
    InputStream getInputStream() throws IOException;

    String getName();

    boolean isDirectory();

}
