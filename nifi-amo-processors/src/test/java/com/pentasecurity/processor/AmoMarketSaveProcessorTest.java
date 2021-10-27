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


public class AmoMarketSaveProcessorTest {

    private TestRunner testRunner;

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(AmoMarketSaveProcessor.class);
    }

    // 테스트 할때는 주석 해제, 빌드 시에는 주석 처리
//    @Test
    public void testProcessor() throws NoSuchAlgorithmException {
        InputStream content = new ByteArrayInputStream("{\"contents\":\"not use\"}".getBytes());
        TestRunner runner = TestRunners.newTestRunner(new AmoMarketSaveProcessor());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(AmoMarketSaveProcessor.PROP_LOGIN_ID, "hjs6877@naver.com");
        runner.setProperty(AmoMarketSaveProcessor.PROP_LOGIN_PASSWORD, "1234");
        runner.setProperty(AmoMarketSaveProcessor.PROP_PRODUCT_ID, "1");
        runner.setProperty(AmoChainRegisterProcessor.PROP_PARCEL_ID,
                "00000001E68EB37B9A00F3220C3486E691E15B65CDB8AE72A2C2480EC2339D969ECD8242");
        runner.setProperty(AmoMarketSaveProcessor.PROP_PARCEL_PRICE, "0");

        runner.enqueue(content);
        runner.run(1);
        runner.assertQueueEmpty();

        List<MockFlowFile> results = runner.getFlowFilesForRelationship(AmoMarketSaveProcessor.REL_SUCCESS);
        assertTrue("1 match", results.size() == 1);

        MockFlowFile result = results.get(0);
        String resultValue = new String(runner.getContentAsByteArray(result));

        System.out.println(resultValue);

        System.out.println("Match: " + IOUtils.toString(runner.getContentAsByteArray(result), "UTF-8"));

        result.assertContentEquals("{\"contents\":\"not use\"}");
    }

}
