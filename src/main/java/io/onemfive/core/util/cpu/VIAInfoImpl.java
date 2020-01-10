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
package io.onemfive.core.util.cpu;

class VIAInfoImpl extends CPUIDCPUInfo implements VIACPUInfo {

    private static boolean isC3Compatible;
    private static boolean isNanoCompatible;

    // If modelString != null, the cpu is considered correctly identified.
    private static final String smodel = identifyCPU();

    public boolean IsC3Compatible(){ return isC3Compatible; }

    public boolean IsNanoCompatible(){ return isNanoCompatible; }

    public String getCPUModelString()
    {
        if (smodel != null)
            return smodel;
        throw new UnknownCPUException("Unknown VIA CPU; Family="+CPUID.getCPUFamily() + '/' + CPUID.getCPUExtendedFamily()+
                ", Model="+CPUID.getCPUModel() + '/' + CPUID.getCPUExtendedModel());
    }


    public boolean hasX64()
    {
        return false;
    }


    private static String identifyCPU()
    {
        // http://en.wikipedia.org/wiki/Cpuid
        String modelString = null;
        int family = CPUID.getCPUFamily();
        int model = CPUID.getCPUModel();
        if (family == 15) {
            family += CPUID.getCPUExtendedFamily();
            model += CPUID.getCPUExtendedModel() << 4;
        }

        if (family == 6) {
            isC3Compatible = true; // Possibly not optimal
            switch (model) {
                case 5:
                    modelString = "Cyrix M2";
                    break;
                case 6:
                    modelString = "C5 A/B";
                    break;
                case 7:
                    modelString = "C5 C";
                    break;
                case 8:
                    modelString = "C5 N";
                    break;
                case 9:
                    modelString = "C5 XL/P";
                    break;
                case 10:
                    modelString = "C5 J";
                    break;
                case 15:
                    isNanoCompatible = true;
                    modelString = "Nano";
                    break;
                default:
                    modelString = "Via model " + model;
                    break;
            }
        }
        return modelString;
    }
}
