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

import com.pentasecurity.core.crypto.ECDSA;
import com.pentasecurity.core.dto.chain.Transaction;
import com.pentasecurity.core.dto.market.AutoOrderFileData;
import com.pentasecurity.core.dto.market.JwtLoginPayload;
import com.pentasecurity.core.dto.market.SellerAutoOrderData;
import com.pentasecurity.core.dto.market.SellerOrderData;
import com.pentasecurity.core.service.AmoChainCommunicator;
import com.pentasecurity.core.service.GrantTransactionCreator;
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
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.math.BigInteger;
import java.util.*;

@Tags({"AMO Custom Processor for Auto Grant"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class AmoAutoGrantProcessor extends AbstractProcessor {
    /**
     *  - 마켓 서버 로그인 처리
     *  - ORDER TYPE이 AUTO인 주문 중에서 ORDER_REQUEST 상태인 주문 목록을 조회
     *  - 주문 목록에 대해서 체인에게 GRANT TX를 요청
     *  - 마켓 서버에게 주문 목록의 ORDER_GRANT 업데이트 요청
     */
    public static final String PRIVATE_KEY = "#{auth.private.key}";
    public static final String LOGIN_ID = "#{login.id}";
    public static final String LOGIN_PASSWORD = "#{login.password}";
    public static  final String BEARER = "Bearer ";

    public static final PropertyDescriptor PROP_PRIVATE_KEY = new PropertyDescriptor.Builder()
            .name("private-key")
            .displayName("Private Key")
            .description("Specifies a private key for authentication")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .sensitive(true)
            .defaultValue(PRIVATE_KEY)
            .build();

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
            .name("login-password")
            .displayName("Login Password")
            .description("Specifies a login password key for authentication")
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
        descriptors.add(PROP_PRIVATE_KEY);
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
    protected Collection<ValidationResult> customValidate(final ValidationContext context) {

        return null;
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        final ComponentLog logger = getLogger();
        FlowFile flowFile = session.create();

        String privateKeyString = context.getProperty(PROP_PRIVATE_KEY).evaluateAttributeExpressions(flowFile).getValue();
        String loginId = context.getProperty(PROP_LOGIN_ID).getValue();
        String loginPassword = context.getProperty(PROP_LOGIN_PASSWORD).getValue();

        try {
            // 마켓 서버 로그인
            String accessToken = MarketCommunicator.requestLogin(loginId, loginPassword);
            String authorization = BEARER + accessToken;

            // accessToken 파싱
            String payload = JwtUtils.decodePayload(accessToken);
            JwtLoginPayload loginPayload = (JwtLoginPayload) JsonUtils.fromJson(payload, JwtLoginPayload.class);
            long sellerId = loginPayload.getSellerId();

            byte[] privateKey32Bytes = ECDSA.getPrivateKey32Bytes(privateKeyString);
            byte[] publicKey65Bytes = ECDSA.getPublicKey65Bytes(privateKeyString);
            int latestBlockHeight = Integer.parseInt(AmoChainCommunicator.getLatestBlockHeight());
            String sender = ECDSA.getAddressFromPrivateKeyString(privateKeyString);
            logger.info("# sender: " + sender);
            final BigInteger fee = new BigInteger("0");

            /**
             * Grant TX를 보낼 주문 목록을 조회
             *  - 조건: 해당 판매자, Order Type AUTO, ORDER_REQUEST 주문
             */
            List<SellerOrderData> sellerOrderDataList =
                    MarketCommunicator.requestGetSellerOrders(authorization, sellerId);
            for (SellerOrderData sellerOrder : sellerOrderDataList) {
                long orderId = sellerOrder.getOrderId();
                String recipient = sellerOrder.getRecipient();
                for (String parcelId : sellerOrder.getParcelIds()) {
                    // 주문 목록에 대해서 체인에게 GRANT TX를 요청
                    GrantTransactionCreator grantTransactionCreator = new GrantTransactionCreator();
                    Transaction amoTx = null;
                    amoTx = grantTransactionCreator.createGrantTx(sender,
                            fee, latestBlockHeight, parcelId, null, recipient);
                    String signedTx = grantTransactionCreator.create(privateKey32Bytes, publicKey65Bytes, amoTx);
                    logger.info("#### signed tx : " + signedTx);

                    AmoChainCommunicator.requestGrantTx(signedTx);

                    // 마켓 서버에게 주문 목록의 ORDER_GRANT 업데이트 요청
                    MarketCommunicator.requestPatchOrder(authorization, orderId);
                }
            }

            session.remove(flowFile);
        } catch (Exception e) {
            logger.error("Auto Grant Processor error happened: {}", e.getMessage());
            throw new ProcessException(e.getMessage());
        }

    }
}
