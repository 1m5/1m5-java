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
package io.onemfive.cli.commands;

import io.onemfive.cli.CLICommand;
import io.onemfive.cli.CLICommandFactory;
import io.onemfive.cli.CommandException;
import io.onemfive.cli.ManCommand;

import java.util.List;

public class Help implements CLICommand {

    @Override
    public String execute(List<String> params) throws CommandException {
        StringBuilder sb = new StringBuilder();
        sb.append("Following commands are available:");
        String h;
        for(CLICommand c : CLICommandFactory.getCommands()) {
            if(c instanceof ManCommand) {
                h = ((ManCommand)c).getName();
                if (h != null)
                    sb.append("\n\t"+h);
            }
        }
        return sb.toString();
    }

}
