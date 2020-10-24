package net.yatopia.hwaccel.test.opencl;

import junit.framework.TestCase;
import net.yatopia.hwaccel.opencl.OpenCompute;

public class OpenComputeTest extends TestCase {

    public void testInitDefault() {
        OpenCompute.init();
        if(OpenCompute.getActiveInstance() == null)
            System.out.println("[WARN] OpenCL service not started");
        OpenCompute.release();
    }

}