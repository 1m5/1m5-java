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

/**
 * An interface for classes that provide lowlevel information about CPU's
 */

public interface CPUInfo
{
    /**
     * @return A string indicating the vendor of the CPU.
     */
    String getVendor();

    /**
     * @return A string detailing what type of CPU that is present in the machine. I.e. 'Pentium IV' etc.
     * @throws UnknownCPUException If for any reason the retrieval of the requested information
     * failed. The message encapsulated in the execption indicates the
     * cause of the failure.
     */
    String getCPUModelString() throws UnknownCPUException;

    /**
     * @return true iff the CPU supports the MMX instruction set.
     */
    boolean hasMMX();

    /**
     * @return true iff the CPU supports the SSE instruction set.
     */
    boolean hasSSE();

    /**
     * @return true iff the CPU supports the SSE2 instruction set.
     */
    boolean hasSSE2();

    /**
     * @return true iff the CPU supports the SSE3 instruction set.
     */
    boolean hasSSE3();

    /**
     * @return true iff the CPU supports the SSE4.1 instruction set.
     */
    boolean hasSSE41();

    /**
     * @return true iff the CPU supports the SSE4.2 instruction set.
     */
    boolean hasSSE42();

    /**
     * AMD K10 only. Not supported on Intel.
     * ref: https://en.wikipedia.org/wiki/SSE4.2#SSE4a
     *
     * @return true iff the CPU supports the SSE4A instruction set.
     */
    boolean hasSSE4A();

    /**
     * @return true iff the CPU supports the AVX instruction set.
     */
    boolean hasAVX();

    /**
     * @return true iff the CPU supports the AVX2 instruction set.
     */
    boolean hasAVX2();

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
    boolean hasAVX512();

    /**
     *
     * Intel Multi-Precision Add-Carry Instruction Extensions
     * Available in Broadwell.
     * Unused until GMP 6.1.
     *
     * @return true iff the CPU supports the ADX instruction set.
     */
    boolean hasADX();

    /**
     * @return true iff the CPU supports TBM.
     */
    boolean hasTBM();

    /**
     * @return true iff the CPU supports the AES-NI instruction set.
     */
    boolean hasAES();

    /**
     * @return true iff the CPU supports the 64-bit support
     */
    boolean hasX64();

    /**
     * @return true iff the CPU supports the BMI1 instruction set.
     */
    boolean hasBMI1();

    /**
     * @return true iff the CPU supports the BMI2 instruction set.
     */
    boolean hasBMI2();

    /**
     * @return true iff the CPU supports the FMA3 instruction set.
     */
    boolean hasFMA3();

    /**
     * @return true iff the CPU supports the MOVBE instruction set.
     */
    boolean hasMOVBE();

    /**
     * Also known as LZCNT
     * @return true iff the CPU supports the ABM instruction set.
     */
    boolean hasABM();
}
