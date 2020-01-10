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
 * An interface for classes that provide lowlevel information about Intel CPU's
 */
public interface IntelCPUInfo extends CPUInfo {

    /**
     * @return true if the CPU is at least a Pentium CPU.
     */
    boolean IsPentiumCompatible();

    /**
     * @return true if the CPU is at least a Pentium which implements the MMX instruction/feature set.
     */
    boolean IsPentiumMMXCompatible();

    /**
     * @return true if the CPU implements at least the p6 instruction set (Pentium II or better).
     * Please note that an PentimPro CPU causes/should cause this method to return false (due to that CPU using a
     * very early implementation of the p6 instruction set. No MMX etc.)
     */
    boolean IsPentium2Compatible();

    /**
     * @return true if the CPU implements at least a Pentium III level of the p6 instruction/feature set.
     */
    boolean IsPentium3Compatible();

    /**
     * Supports the SSE 2 instructions. Does not necessarily support SSE 3.
     * https://en.wikipedia.org/wiki/Pentium_4
     * @return true if the CPU implements at least a Pentium IV level instruction/feature set.
     */
    boolean IsPentium4Compatible();

    /**
     * @return true if the CPU implements at least a Pentium M level instruction/feature set.
     */
    boolean IsPentiumMCompatible();

    /**
     * Supports the SSE 2 and SSE 3 instructions.
     * https://en.wikipedia.org/wiki/Atom_processor
     * @return true if the CPU implements at least a Atom level instruction/feature set.
     */
    boolean IsAtomCompatible();

    /**
     * Supports the SSE 3 instructions.
     * @return true if the CPU implements at least a Core2 level instruction/feature set.
     */
    boolean IsCore2Compatible();

    /**
     * Supports the SSE 3, 4.1, 4.2 instructions.
     * In general, this requires 45nm or smaller process.
     *
     * This is the Nehalem architecture.
     * ref: https://en.wikipedia.org/wiki/Nehalem_%28microarchitecture%29
     *
     * @return true if the CPU implements at least a Corei level instruction/feature set.
     */
    boolean IsCoreiCompatible();

    /**
     * Supports the SSE 3, 4.1, 4.2 instructions.
     * Supports the AVX 1 instructions.
     * In general, this requires 32nm or smaller process.
     * @return true if the CPU implements at least a SandyBridge level instruction/feature set.
     */
    boolean IsSandyCompatible();

    /**
     * Supports the SSE 3, 4.1, 4.2 instructions.
     * Supports the AVX 1 instructions.
     * In general, this requires 22nm or smaller process.
     *
     * UNUSED, there is no specific GMP build for Ivy Bridge,
     * and this is never called from NativeBigInteger.
     * Ivy Bridge is a successor to Sandy Bridge, so use IsSandyCompatible().
     *
     * @return true if the CPU implements at least a IvyBridge level instruction/feature set.
     */
    boolean IsIvyCompatible();

    /**
     * Supports the SSE 3, 4.1, 4.2 instructions.
     * Supports the AVX 1, 2 instructions.
     * Supports the BMI 1, 2 instructions.
     *
     * WARNING - GMP 6 uses the BMI2 MULX instruction for the "coreihwl" binaries.
     * Only Core i3/i5/i7 Haswell processors support BMI2.
     *
     * Requires support for all 6 of these Corei features: FMA3 MOVBE ABM AVX2 BMI1 BMI2
     * Pentium/Celeron Haswell processors do NOT support BMI2 and are NOT compatible.
     * Those processors will be Sandy-compatible if they have AVX 1 support,
     * and Corei-compatible if they do not.
     *
     * ref: https://software.intel.com/en-us/articles/how-to-detect-new-instruction-support-in-the-4th-generation-intel-core-processor-family
     * ref: https://en.wikipedia.org/wiki/Haswell_%28microarchitecture%29
     *
     * In general, this requires 22nm or smaller process.
     * @return true if the CPU implements at least a Haswell level instruction/feature set.
     */
    boolean IsHaswellCompatible();

    /**
     * Supports the SSE 3, 4.1, 4.2 instructions.
     * Supports the AVX 1, 2 instructions.
     * In general, this requires 14nm or smaller process.
     *
     * NOT FULLY USED as of GMP 6.0.
     * All GMP coreibwl binaries are duplicates of binaries for older technologies,
     * so we do not distribute any. However, this is called from NativeBigInteger.
     *
     * Broadwell is supported in GMP 6.1 and requires the ADX instructions.
     *
     * Requires support for all 7 of these Corei features: FMA3 MOVBE ABM AVX2 BMI1 BMI2 ADX
     * Pentium/Celeron Broadwell processors that do not support these instruction sets are not compatible.
     * Those processors will be Sandy-compatible if they have AVX 1 support,
     * and Corei-compatible if they do not.
     *
     * @return true if the CPU implements at least a Broadwell level instruction/feature set.
     */
    boolean IsBroadwellCompatible();
}
