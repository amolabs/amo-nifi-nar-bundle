/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pentasecurity.processor;

import com.pentasecurity.core.utils.CryptoUtils;
import org.apache.commons.io.IOUtils;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.Assert.assertTrue;


public class AmoChainRegisterProcessorTest {

    private TestRunner testRunner;

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(AmoChainRegisterProcessor.class);
    }

    @Test
    public void testProcessor() throws NoSuchAlgorithmException {
        InputStream content = new ByteArrayInputStream("{\"contents\":\"not use\"}".getBytes());
        TestRunner runner = TestRunners.newTestRunner(new AmoChainRegisterProcessor());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(AmoChainRegisterProcessor.PROP_PRIVATE_KEY, "269d46c9cfafe86be88fea3887422b520f7a9e8db829c2f8582200806e8d337a");
        runner.setProperty(AmoChainRegisterProcessor.PROP_PARCEL_ID, "00000001199F8E29A5A47543089BF2C13DD23C1915C6BAC5FF4DF56B60BE2EFB26B960D4");

        runner.enqueue(content);
        runner.run(1);
        runner.assertQueueEmpty();

        List<MockFlowFile> results = runner.getFlowFilesForRelationship(AmoStorageUploadProcessor.REL_SUCCESS);
        assertTrue("1 match", results.size() == 1);

        MockFlowFile result = results.get(0);
        String resultValue = new String(runner.getContentAsByteArray(result));

        System.out.println(resultValue);

        System.out.println("Match: " + IOUtils.toString(runner.getContentAsByteArray(result), "UTF-8"));

        result.assertContentEquals("{\"contents\":\"not use\"}");
    }

}