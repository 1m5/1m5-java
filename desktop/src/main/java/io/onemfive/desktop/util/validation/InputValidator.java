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

import java.math.BigInteger;

public class InputValidator {

    public ValidationResult validate(String input) {
        return validateIfNotEmpty(input);
    }

    protected ValidationResult validateIfNotEmpty(String input) {
        //trim added to avoid empty input
        if (input == null || input.trim().length() == 0)
            return new ValidationResult(false, Res.get("validation.empty"));
        else
            return new ValidationResult(true);
    }

    public static class ValidationResult {
        public final boolean isValid;
        public final String errorMessage;

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        public ValidationResult(boolean isValid) {
            this(isValid, null);
        }

        public ValidationResult and(ValidationResult next) {
            if (this.isValid)
                return next;
            else
                return this;
        }

        @Override
        public String toString() {
            return "validationResult {" +
                    "isValid=" + isValid +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }

    protected boolean isPositiveNumber(String input) {
        try {
            return input != null && new BigInteger(input).compareTo(BigInteger.ZERO) >= 0;
        } catch (Throwable t) {
            return false;
        }
    }

    protected boolean isNumberWithFixedLength(String input, int length) {
        return isPositiveNumber(input) && input.length() == length;
    }

    protected boolean isNumberInRange(String input, int minLength, int maxLength) {
        return isPositiveNumber(input) && input.length() >= minLength && input.length() <= maxLength;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isStringWithFixedLength(String input, int length) {
        return input != null && input.length() == length;
    }

    protected boolean isStringInRange(String input, int minLength, int maxLength) {
        return input != null && input.length() >= minLength && input.length() <= maxLength;
    }
}
