package com.pentasecurity.core;

import com.pentasecurity.core.service.AmoChainCommunicator;
import org.junit.Test;


public class AmoChainCommunicatorTest {
    @Test
    public void getLatestBlockHeightTest() throws InterruptedException {
        String latestBlockHeight = AmoChainCommunicator.getLatestBlockHeight();
        System.out.println(latestBlockHeight);
    }
}
