package com.pentasecurity.core.service;

import com.pentasecurity.core.dto.Transaction;

public abstract class TransactionCreator {
    protected abstract Transaction createSignedTx();

    protected abstract Transaction createUnsignedTx();

    protected Transaction sign() {
        return null;
    }

    protected Transaction serializeUnsignedTx() {
        return null;
    }

    protected Transaction serializeSignedTx() {
        return null;
    }
}
