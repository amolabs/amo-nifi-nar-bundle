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

import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;


public class AmoAutoGrantProcessorTest {

    private TestRunner testRunner;

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(AmoAutoGrantProcessor.class);
    }

    // 테스트 할때는 주석 해제, 빌드 시에는 주석 처리
    @Test
    public void testProcessor() throws NoSuchAlgorithmException {
//        InputStream content = new ByteArrayInputStream("{\"contents\":\"not use\"}".getBytes());
        TestRunner runner = TestRunners.newTestRunner(new AmoAutoGrantProcessor());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(AmoAutoGrantProcessor.PROP_PRIVATE_KEY,
                "269d46c9cfafe86be88fea3887422b520f7a9e8db829c2f8582200806e8d337a");
        runner.setProperty(AmoAutoGrantProcessor.PROP_LOGIN_ID, "hjs6877@naver.com");
        runner.setProperty(AmoAutoGrantProcessor.PROP_LOGIN_PASSWORD, "1234");

//        runner.enqueue(content);
        runner.run(1);
//        runner.assertQueueEmpty();

    }

}
