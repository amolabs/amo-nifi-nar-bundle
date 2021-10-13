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

import com.pentasecurity.core.exception.InvalidIncomingProcessorException;
import com.pentasecurity.core.service.MarketCommunicator;
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

import java.util.*;

@Tags({"AMO Custom Processor for AMO Market"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class AmoMarketSaveProcessor extends AbstractProcessor {
    /**
     * - Market Server 서버 로그인 처리
     * - Market에 저장할 Parcel 정보 생성
     * - 생성한 Parcel 정보를 Market에 저장
     * @param context
     */
    public static final String LOGIN_ID = "#{login.id}";
    public static final String LOGIN_PASSWORD = "#{login.password}";
    public static final String PRODUCT_ID = "#{product.id}";
    public static final String PARCEL_PRICE = "#{parcel.price}";

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

    public static final PropertyDescriptor PROP_PRODUCT_ID = new PropertyDescriptor.Builder()
            .name("product-id")
            .displayName("Product ID")
            .description("Specifies a product id for parcel")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .sensitive(true)
            .defaultValue(PRODUCT_ID)
            .build();

    public static final PropertyDescriptor PROP_PARCEL_PRICE = new PropertyDescriptor.Builder()
            .name("parcel-price")
            .displayName("Parcel Price")
            .description("Specifies price for parcel")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .sensitive(true)
            .defaultValue(PARCEL_PRICE)
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
        descriptors.add(PROP_LOGIN_ID);
        descriptors.add(PROP_LOGIN_PASSWORD);
        descriptors.add(PROP_PRODUCT_ID);
        descriptors.add(PROP_PARCEL_PRICE);

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
        if (!previousProcessorName.equals("AmoChainRegisterProcessor")) {
            throw new InvalidIncomingProcessorException("Invalid Incoming Processor");
        }

        String loginId = context.getProperty(PROP_LOGIN_ID).getValue();
        String loginPassword = context.getProperty(PROP_LOGIN_PASSWORD).getValue();
        long productId = Long.parseLong(context.getProperty(PROP_PRODUCT_ID).getValue());
        String parcelPrice = context.getProperty(PROP_PARCEL_PRICE).getValue();
        String parcelId = flowFile.getAttribute("parcel.id");

        try {
            String accessToken = MarketCommunicator.requestLogin(loginId, loginPassword);
            MarketCommunicator.requestSaveParcel(accessToken, parcelId, productId, parcelPrice);

            session.putAttribute(flowFile, "previous.processor.name", "AmoMarketSaveProcessor");
            session.transfer(flowFile, REL_SUCCESS);
        } catch (Exception e) {
            session.rollback();
            logger.error("Register Tx Processor Error happened: {}", e.getCause());
            throw new ProcessException(e.getMessage());
        }

    }
}
