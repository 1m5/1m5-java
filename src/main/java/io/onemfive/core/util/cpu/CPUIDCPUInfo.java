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

class CPUIDCPUInfo implements CPUInfo
{
    public String getVendor()
    {
        return CPUID.getCPUVendorID();
    }

    public boolean hasMMX()
    {
        return (CPUID.getEDXCPUFlags() & (1 << 23)) != 0; //EDX Bit 23
    }

    public boolean hasSSE(){
        return (CPUID.getEDXCPUFlags() & (1 << 25)) != 0; //EDX Bit 25
    }

    public boolean hasSSE2()
    {
        return (CPUID.getEDXCPUFlags() & (1 << 26)) != 0; //EDX Bit 26
    }

    public boolean hasSSE3()
    {
        return (CPUID.getECXCPUFlags() & (1 << 0)) != 0; //ECX Bit 0
    }

    public boolean hasSSE41()
    {
        return (CPUID.getECXCPUFlags() & (1 << 19)) != 0; //ECX Bit 19
    }

    public boolean hasSSE42()
    {
        return (CPUID.getECXCPUFlags() & (1 << 20)) != 0; //ECX Bit 20
    }

    public boolean hasSSE4A()
    {
        return (CPUID.getExtendedECXCPUFlags() & (1 << 6)) != 0; //Extended ECX Bit 6
    }

    /**
     * @return true iff the CPU supports the AVX instruction set.
     */
    public boolean hasAVX()
    {
        int ecx = CPUID.getECXCPUFlags();
        return (ecx & (1 << 28)) != 0 && //AVX: ECX Bit 28
                (ecx & (1 << 27)) != 0;   //XSAVE enabled by OS: ECX Bit 27
    }

    /**
     * @return true iff the CPU supports the AVX2 instruction set.
     */
    public boolean hasAVX2() {
        return (CPUID.getExtendedEBXFeatureFlags() & (1 << 5)) != 0; //Extended EBX Feature Bit 5
    }

    /**
     * Does the CPU supports the AVX-512 Foundation instruction set?
     *
     * Quote wikipedia:
     *
     * AVX-512 consists of multiple extensions not all meant to be supported
     * by all processors implementing them. Only the core extension AVX-512F
     * (AVX-512 Foundation) is required by all implementations.
     *
     * ref: https://en.wikipedia.org/wiki/AVX-512
     *
     * @return true iff the CPU supports the AVX-512 Foundation instruction set.
     */
    public boolean hasAVX512()
    {
        return (CPUID.getExtendedEBXFeatureFlags() & (1 << 16)) != 0; //Extended EBX Bit 16
    }

    /**
     *
     * Intel Multi-Precision Add-Carry Instruction Extensions
     * Available in Broadwell.
     *
     * @return true iff the CPU supports the ADX instruction set.
     */
    public boolean hasADX()
    {
        return (CPUID.getExtendedEBXFeatureFlags() & (1 << 19)) != 0; //Extended EBX Bit 19
    }

    /**
     * @return true iff the CPU supports TBM.
     */
    public boolean hasTBM()
    {
        return (CPUID.getECXCPUFlags() & (1 << 21)) != 0; //ECX Bit 21
    }

    /**
     * @return true iff the CPU supports the AES-NI instruction set.
     */
    public boolean hasAES() {
        return (CPUID.getECXCPUFlags() & (1 << 25)) != 0; //ECX Bit 25
    }

    /**
     * @return true iff the CPU supports the 64-bit support
     */
    public boolean hasX64() {
        return (CPUID.getExtendedEDXCPUFlags() & (1 << 29)) != 0; //Extended EDX Bit 29
    }

    /**
     * @return true iff the CPU supports the BMI1 instruction set.
     */
    public boolean hasBMI1() {
        return (CPUID.getExtendedEBXFeatureFlags() & (1 << 3)) != 0; // Extended EBX Feature Bit 3
    }

    /**
     * @return true iff the CPU supports the BMI2 instruction set.
     */
    public boolean hasBMI2() {
        return (CPUID.getExtendedEBXFeatureFlags() & (1 << 8)) != 0; // Extended EBX Feature Bit 8
    }

    /**
     * @return true iff the CPU supports the FMA3 instruction set.
     */
    public boolean hasFMA3() {
        return (CPUID.getECXCPUFlags() & (1 << 12)) != 0; // ECX Bit 12
    }

    /**
     * @return true iff the CPU supports the MOVBE instruction set.
     */
    public boolean hasMOVBE() {
        return (CPUID.getECXCPUFlags() & (1 << 22)) != 0; // ECX Bit 22
    }

    /**
     * Also known as LZCNT
     */
    public boolean hasABM() {
        return (CPUID.getExtendedECXCPUFlags() & (1 << 5)) != 0; // Extended ECX Bit 5
    }

    @Override
    public String getCPUModelString() throws UnknownCPUException {
        throw new UnknownCPUException("Class CPUIDCPUInfo cannot indentify CPUs");
    }
}
