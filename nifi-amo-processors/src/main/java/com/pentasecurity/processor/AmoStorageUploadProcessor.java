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
import com.pentasecurity.core.exception.InvalidIncomingProcessorException;
import com.pentasecurity.core.service.AmoStorageCommunicator;
import com.pentasecurity.core.utils.CryptoUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Tags({"AMO Custom Processor for AMO Storage"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class AmoStorageUploadProcessor extends AbstractProcessor {
    /**
     * ** 기본적으로 private key 정보와 마켓 서버의 로그인 정보는 NiFi 사용자가 직접 Parameter Context에 sensitive 정보로
     * 입력을 해둔다.
     * - AMO Storage에 인증 토큰 요청
     *  - 토큰 요청 시 필요한 정보
     *      - owner 정보: private key에서 address 정보를 유도한다.
     *      -  content hash 정보: flow file을 받아서 hash로 변환한다.
     * - Signature 생성
     *  - private key를 이용해서 publickey를 유도한다.
     *  - private key와 publickey를 이용해서 인증 토큰에 대한 Signature를 생성한다.
     * - 데이터 업로드
     *  - header에 다음 정보를 포함한다
     *      - 인증 토큰
     *      - Public key
     *      - Signature
     *      - response 정보로 받은 Parcel ID를 Attribute에 추가한다.
     */
    public static final String PRIVATE_KEY = "#{auth.private.key}";

    // TODO 사용자가 임의로 수정 불가능하도록 하는 방법은?
    public static final PropertyDescriptor PROP_PRIVATE_KEY = new PropertyDescriptor.Builder()
            .name("private-key")
            .displayName("Private Key")
            .description("Specifies a private key for authentication")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .sensitive(true)
            .defaultValue(PRIVATE_KEY)
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

    /**
     * PropertyDescriptor와 Relationship을 Collection에 추가한다.
     *
     * @param context
     */
    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<>();
        descriptors.add(PROP_PRIVATE_KEY);

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
        FlowFile flowFile = session.get();
        if ( flowFile == null ) {
            return;
        }

        String previousProcessorName = flowFile.getAttribute("previous.processor.name");
        if (StringUtils.isNotEmpty(previousProcessorName) && (previousProcessorName.equals("AmoChainRegisterProcessor") ||
                previousProcessorName.equals("AmoMarketSaveProcessor"))) {
            throw new InvalidIncomingProcessorException("Invalid Incoming Processor");
        }

        String privateKeyString = context.getProperty(PROP_PRIVATE_KEY).evaluateAttributeExpressions(flowFile).getValue();

        // TODO session.exportTO가 정상적으로 동작하지 않을 시, 사용한다.
        try {
//            session.read(flowFile, inputStream -> {
//                StringWriter writer = new StringWriter();
//                IOUtils.copy(inputStream, writer, "UTF-8");
//                String content = writer.toString();
//
//            });

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            session.exportTo(flowFile, outputStream);
            final String content = outputStream.toString("UTF-8");

            ECPrivateKey ecPrivateKey = (ECPrivateKey) ECDSA.generateECDSAPrivateKey(Hex.decode(privateKeyString));
            BCECPublicKey publicKey = (BCECPublicKey) ECDSA.getPublicKeyFromPrivateKey(ecPrivateKey);
            byte[] publicKey65Bytes = ECDSA.convertPubicKeyTo65Bytes(publicKey);


            String address = ECDSA.getAddressFromPublicKey(publicKey);
            logger.info("# address: " + address);

            byte[] sha256 = CryptoUtils.sha256(content);
            String hashContent = CryptoUtils.bytesToHex(sha256);
            String accessToken = AmoStorageCommunicator.requestAuthToken(address, hashContent);

            byte[] signature = ECDSA.sign(ecPrivateKey, accessToken.getBytes(StandardCharsets.UTF_8));


            String parcelId = AmoStorageCommunicator.requestUpload(address,
                    hashContent, accessToken, publicKey65Bytes, signature, outputStream.toByteArray());

            session.putAttribute(flowFile, "parcel.id", parcelId);
            session.putAttribute(flowFile, "previous.processor.name", "AmoStorageUploadProcessor");
            session.transfer(flowFile, REL_SUCCESS);
        } catch (Exception e) {
//            session.rollback();
            logger.error("Upload Processor Error happened: " + e.getMessage());
            e.printStackTrace();
            logger.error(e.getStackTrace().toString());
            throw new ProcessException(e.getMessage());
        }
    }
}
