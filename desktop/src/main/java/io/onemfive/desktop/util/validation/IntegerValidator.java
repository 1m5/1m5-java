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
package io.onemfive.desktop.util.validation;

import io.onemfive.util.Res;

public class IntegerValidator extends InputValidator {

    private int intValue;

    public ValidationResult validate(String input) {
        ValidationResult validationResult = super.validate(input);
        if (!validationResult.isValid)
            return validationResult;

        if (!isInteger(input))
            return new ValidationResult(false, Res.get("validation.notAnInteger"));

        if (isBelowMinValue(intValue))
            return new ValidationResult(false, Res.get("validation.btc.toSmall", Integer.MIN_VALUE));

        if (isAboveMaxValue(intValue))
            return new ValidationResult(false, Res.get("validation.btc.toLarge", Integer.MAX_VALUE));

        return validationResult;
    }

    private boolean isBelowMinValue(int intValue) {
        return intValue < Integer.MIN_VALUE;
    }

    private boolean isAboveMaxValue(int intValue) {
        return intValue > Integer.MAX_VALUE;
    }

    private boolean isInteger(String input) {
        try {
            intValue = Integer.parseInt(input);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
