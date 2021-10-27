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
import com.pentasecurity.core.dto.market.BuyerAutoOrderData;
import com.pentasecurity.core.dto.market.JwtLoginPayload;
import com.pentasecurity.core.service.AmoStorageCommunicator;
import com.pentasecurity.core.service.MarketCommunicator;
import com.pentasecurity.core.utils.JsonUtils;
import com.pentasecurity.core.utils.JwtUtils;
import com.pentasecurity.processor.callback.DownloadCallback;
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
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.util.*;

@Tags({"AMO Custom Processor for Auto Download"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class AmoAutoDownloadProcessor extends AbstractProcessor {
    /**
     *  - 마켓 서버 로그인 처리
     *  - ORDER TYPE 이 AUTO 인 주문 중에서 ORDER_GRANT 이고, isDownloaded가 false 상태인 주문 목록을 조회
     *  - AMO Storage 서버에 인증 토큰 요청
     *  - AMO Storage 서버에 주문한 데이터 파일 다운로드를 요청
     *  - 1건 당 다운로드가 완료되면 마켓 서버의 주문의 isDownloaded의 값을 true로 업데이트
     */
    public static final String LOGIN_ID = "#{login.id}";
    public static final String LOGIN_PASSWORD = "#{login.password}";
    public static  final String BEARER = "Bearer ";
    public static final String PRIVATE_KEY = "#{auth.private.key}";

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
    public void onTrigger(final ProcessContext context, final ProcessSession session) {
        final ComponentLog logger = getLogger();
        FlowFile flowFile = null;
        // TODO implement
        String loginId = context.getProperty(PROP_LOGIN_ID).getValue();
        String loginPassword = context.getProperty(PROP_LOGIN_PASSWORD).getValue();
        String privateKeyString = context.getProperty(PROP_PRIVATE_KEY).getValue();

        try {
            // 마켓 서버 로그인
            String marketAccessToken = MarketCommunicator.requestLogin(loginId, loginPassword);
            String authorization = BEARER + marketAccessToken;

            // accessToken 파싱
            String payload = JwtUtils.decodePayload(marketAccessToken);
            JwtLoginPayload loginPayload = (JwtLoginPayload) JsonUtils.fromJson(payload, JwtLoginPayload.class);
            long buyerId = loginPayload.getBuyerId();

            // ORDER TYPE 이 AUTO 인 주문 중에서 ORDER_GRANT 이고, isDownloaded가 false 상태인 주문 목록을 조회
            List<BuyerAutoOrderData> buyerAutoOrders =
                    MarketCommunicator.requestGetBuyerAutoOrders(authorization, buyerId);
            BCECPrivateKey privateKey = (BCECPrivateKey) ECDSA.getPrivateKeyFromHexString(privateKeyString);
            BCECPrivateKey privateKeyNew = ECDSA.getPrivateKey(privateKey.getEncoded());

            BCECPublicKey publicKey = (BCECPublicKey) ECDSA.getPublicKeyFromPrivateKey(privateKey);
            BCECPublicKey publicKeyNew = (BCECPublicKey) ECDSA.getPublicKey(publicKey.getEncoded());
            String address = ECDSA.getAddressFromPublicKey(publicKeyNew);
            byte[] publicKey65Bytes = ECDSA.convertPubicKeyTo65Bytes(publicKeyNew);

            for (BuyerAutoOrderData buyerAutoOrder : buyerAutoOrders) {
                for (String parcelId : buyerAutoOrder.getParcelIds()) {
                    // AMO Storage 서버에 인증 토큰 요청
                    String storageAccessToken = AmoStorageCommunicator.requestAuthToken(address, parcelId);
                    // TODO signature 부분 수정 필요(AmoStorageUploadProcessor와 같게 작성해야됨)
                    byte[] signature = ECDSA.getSignature(privateKeyNew, storageAccessToken);

                    // AMO Storage 서버에 다운로드 요청
                    String dataHex = AmoStorageCommunicator.requestDownload(parcelId,
                            storageAccessToken, publicKey65Bytes, signature);

                    // 마켓 서버의 주문의 isDownloaded의 값을 true로 업데이트
                    MarketCommunicator.requestPatchOrder(authorization, buyerAutoOrder.getAutoOrderId());


                    flowFile = session.create();
                    flowFile = session.write(flowFile, new DownloadCallback(dataHex));
                    session.transfer(flowFile, REL_SUCCESS);
                }
            }
            session.commit();
        } catch (Exception e) {
            session.rollback();
            logger.error("Auto Download Processor error happened: {}", e.getCause());
            throw new ProcessException(e.getMessage());
        }
    }
}
