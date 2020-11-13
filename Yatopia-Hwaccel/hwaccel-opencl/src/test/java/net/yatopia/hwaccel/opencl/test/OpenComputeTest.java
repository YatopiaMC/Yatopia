package net.yatopia.hwaccel.opencl.test;

import net.yatopia.hwaccel.opencl.OpenCompute;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpenComputeTest {

    @Before
    public void initializeOpenCL() {
        OpenCompute.init();
        if(OpenCompute.getActiveInstance() == null)
            System.out.println("[WARN] OpenCL service not started");
    }

    @Test
    public void placeholder() {

    }

    @After
    public void closeOpenCL() {
        OpenCompute.release();
    }

}