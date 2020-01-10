package io.onemfive.core.util.cpu;

public interface VIACPUInfo extends CPUInfo{

    /**
     * @return true if the CPU present in the machine is at least an 'c3' CPU
     */
    boolean IsC3Compatible();
    /**
     * @return true if the CPU present in the machine is at least an 'nano' CPU
     */
    boolean IsNanoCompatible();

}
