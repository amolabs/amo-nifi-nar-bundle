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
package com.pentasecurity.processors.request;

import com.pentasecurity.core.dto.market.AutoOrderFileData;
import com.pentasecurity.core.dto.market.BuyerAutoOrderData;
import com.pentasecurity.core.dto.market.JwtLoginPayload;
import com.pentasecurity.core.service.MarketCommunicator;
import com.pentasecurity.core.utils.JsonUtils;
import com.pentasecurity.core.utils.JwtUtils;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.*;

@Tags({"example"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class AutoRequestProcessor extends AbstractProcessor {
    /**
     *  - 마켓 서버 로그인 처리
     *  - 마켓 서버에서 자동 주문이 Enable(활성화) 된 auto order 조회
     *  - Enable 된 상품에 대한 auto order의 파일 목록 조회
     *  - 조회된 파일 목록에 대해서 마켓서버에게 주문(REQUEST TX) 요청
     */
    public static final String LOGIN_ID = "#{login.id}";
    public static final String LOGIN_PASSWORD = "#{login.password}";
    public static  final String BEARER = "Bearer ";

    public static final PropertyDescriptor PROP_LOGIN_ID = new PropertyDescriptor.Builder()
            .name("login-id")
            .displayName("Login ID")
            .description("Specifies a login id for authentication")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .sensitive(true)
            .defaultValue(LOGIN_ID)
            .build();

    public static final PropertyDescriptor PROP_LOGIN_PASSWORD = new PropertyDescriptor.Builder()
            .name("private-key")
            .displayName("Private Key")
            .description("Specifies a private key for authentication")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .sensitive(true)
            .defaultValue(LOGIN_PASSWORD)
            .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Success relationship")
            .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("Failure relationship")
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<>();
        descriptors.add(PROP_LOGIN_ID);
        descriptors.add(PROP_LOGIN_PASSWORD);

        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        final ComponentLog logger = getLogger();
        FlowFile flowFile = null;

        String loginId = context.getProperty(PROP_LOGIN_ID).getValue();
        String loginPassword = context.getProperty(PROP_LOGIN_PASSWORD).getValue();

        try {
            String accessToken = MarketCommunicator.requestLogin(loginId, loginPassword);
            String authorization = BEARER + accessToken;

            // accessToken 파싱
            String payload = JwtUtils.decodePayload(accessToken);
            JwtLoginPayload loginPayload = (JwtLoginPayload) JsonUtils.fromJson(payload, JwtLoginPayload.class);
            long buyerId = loginPayload.getBuyerId();

            // auto order 조회
            List<BuyerAutoOrderData> buyerAutoOrders =
                    MarketCommunicator.requestGetBuyerAutoOrders(authorization, buyerId);

            for (BuyerAutoOrderData autoOrder : buyerAutoOrders) {
                long buyerId2 = autoOrder.getBuyerId();
                long sellerId = autoOrder.getSellerId();
                long productId = autoOrder.getProductId();
                long orderId = autoOrder.getOrderId();

                List<AutoOrderFileData> autoOrderFiles =
                        MarketCommunicator.requestGetAutoOrderFiles(authorization, orderId);

                for (AutoOrderFileData file : autoOrderFiles) {
                    // 주문(Request TX 요청)
                    long fileId = file.getFileId();
                    MarketCommunicator.requestPostOrderFile(authorization, buyerId2, sellerId, productId, fileId);

                    flowFile = session.create();
                    session.transfer(flowFile, REL_SUCCESS);
                }
            }
            
            session.commit();
        } catch (Exception e) {
            logger.error("Auto Request Processor error happened: {}", e.getCause());
        }
    }
}
