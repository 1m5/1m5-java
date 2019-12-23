package io.onemfive.data.util;

import java.io.*;

public interface NamedStreamable
{
    InputStream getInputStream() throws IOException;

    String getName();

    boolean isDirectory();

}
