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
import com.pentasecurity.core.exception.InvalidIncomingProcessorException;
import com.pentasecurity.core.service.AmoChainCommunicator;
import com.pentasecurity.core.service.RegisterTransactionCreator;
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
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.*;

@Tags({"AMO Custom Processor for AMO Blockchain"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class AmoChainRegisterProcessor extends AbstractProcessor {
    /**
     * ** 기본적으로 private key 정보와 마켓 서버의 로그인 정보는 NiFi 사용자가 직접 Parameter Context에 sensitive 정보로
     * 입력을 해둔다.
     * - last height block을 조회한다.
     * - Register TX를 생성한다.
     * - Register TX를 sign한다.
     * - sign된 Register TX를 AMO 체인에 전송한다.
     */
    public static final String PRIVATE_KEY = "#{auth.private.key}";
    public static final String PARCEL_ID = "${parcel.id}";

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

    public static final PropertyDescriptor PROP_PARCEL_ID = new PropertyDescriptor.Builder()
            .name("parcel-id")
            .displayName("Parcel ID")
            .description("Specifies a parcel for register")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .sensitive(false)
            .defaultValue(PARCEL_ID)
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
        descriptors.add(PROP_PARCEL_ID);

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

//        String previousProcessorName = flowFile.getAttribute("previous.processor.name");
//        if (!previousProcessorName.equals("AmoStorageUploadProcessor")) {
//            throw new InvalidIncomingProcessorException("Invalid Incoming Processor");
//        }

        /**
         * - TX 생성
         * - TX Sign
         * - TX 전송
         * - FlowFile을 transfer한다.
         */
        try {
            String privateKeyString = context.getProperty(PROP_PRIVATE_KEY).evaluateAttributeExpressions(flowFile).getValue();
            String parcelId = context.getProperty(PROP_PARCEL_ID).evaluateAttributeExpressions(flowFile).getValue();
            logger.info("# parcel ID: " + parcelId);

            byte[] privateKey32Bytes = ECDSA.getPrivateKey32Bytes(privateKeyString);
            byte[] publicKey65Bytes = ECDSA.getPublicKey65Bytes(privateKeyString);

            int latestBlockHeight = Integer.parseInt(AmoChainCommunicator.getLatestBlockHeight());
            String sender = ECDSA.getAddressFromPrivateKeyString(privateKeyString);
            logger.info("# sender: " + sender);
            final BigInteger fee = new BigInteger("0");


            // TODO parcelID 검증 필요(비어있는지, 올바른 형식인지)
            RegisterTransactionCreator registerTransactionCreator = new RegisterTransactionCreator();
            Transaction amoTx = null;
            amoTx = registerTransactionCreator.createRegisterTx(sender, fee, latestBlockHeight, parcelId, null, null, null);
            String signedTx = registerTransactionCreator.create(privateKey32Bytes, publicKey65Bytes , amoTx);
            logger.info("# signed tx : " + signedTx);

            AmoChainCommunicator.requestRegisterTx(signedTx);

            session.putAttribute(flowFile, "tx.signed", signedTx);
            session.putAttribute(flowFile, "previous.processor.name", "AmoChainRegisterProcessor");
            session.transfer(flowFile, REL_SUCCESS);
        } catch (Exception e) {
            session.rollback();
            logger.error("Register Tx Processor Error happened: {}", e.getMessage());
            throw new ProcessException(e.getMessage());
        }
    }
}
