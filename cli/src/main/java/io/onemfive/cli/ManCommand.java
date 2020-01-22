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

package io.onemfive.cli;

public abstract class ManCommand implements CLICommand {

    // TODO: Support wrapping
    protected String help() {
        StringBuilder sb = new StringBuilder();
        sb.append("MAN(1)\t\t\t\tManual pager utils\t\t\t\tMAN(1)");
        sb.append("NAME\n");
        sb.append(getName()+"\n");
        sb.append("SYNOPSIS\n");
        sb.append(getSynopsis()+"\n");
        sb.append("DESCRIPTION\n");
        sb.append(getDescription()+"\n");
        return sb.toString();
    }

    public abstract String getName();
    protected abstract String getSynopsis();
    protected abstract String getDescription();
}
