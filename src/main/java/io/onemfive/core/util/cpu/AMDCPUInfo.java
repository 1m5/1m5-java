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

public interface AMDCPUInfo extends CPUInfo {
    /**
     * @return true if the CPU present in the machine is at least an 'k6' CPU
     */
    boolean IsK6Compatible();
    /**
     * @return true if the CPU present in the machine is at least an 'k6-2' CPU
     */
    boolean IsK6_2_Compatible();
    /**
     * @return true if the CPU present in the machine is at least an 'k6-3' CPU
     */
    boolean IsK6_3_Compatible();
    /**
     * @return true if the CPU present in the machine is at least an 'geode' CPU
     */
    boolean IsGeodeCompatible();
    /**
     * @return true if the CPU present in the machine is at least an 'k7' CPU (Atlhon, Duron etc. and better)
     */
    boolean IsAthlonCompatible();
    /**
     * @return true if the CPU present in the machine is at least an 'k8' CPU (Atlhon 64, Opteron etc. and better)
     */
    boolean IsAthlon64Compatible();
    /**
     * @return true if the CPU present in the machine is at least an 'k10' CPU
     */
    boolean IsK10Compatible();
    /**
     * @return true if the CPU present in the machine is at least an 'bobcat' CPU
     */
    boolean IsBobcatCompatible();
    /**
     * @return true if the CPU present in the machine is at least an 'jaguar' CPU
     */
    boolean IsJaguarCompatible();
    /**
     * @return true if the CPU present in the machine is at least a 'bulldozer' CPU
     */
    boolean IsBulldozerCompatible();
    /**
     * @return true if the CPU present in the machine is at least a 'piledriver' CPU
     */
    boolean IsPiledriverCompatible();
    /**
     * @return true if the CPU present in the machine is at least a 'steamroller' CPU
     */
    boolean IsSteamrollerCompatible();
    /**
     * @return true if the CPU present in the machine is at least a 'excavator' CPU
     */
    boolean IsExcavatorCompatible();

}
