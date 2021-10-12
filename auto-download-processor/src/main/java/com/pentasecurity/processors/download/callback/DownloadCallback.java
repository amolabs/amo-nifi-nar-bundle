package com.pentasecurity.processors.download.callback;

import com.pentasecurity.core.utils.CryptoUtils;
import org.apache.nifi.processor.io.StreamCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadCallback implements StreamCallback {
    private String dataHex;

    public DownloadCallback(String dataHex) {
        this.dataHex = dataHex;
    }

    @Override
    public void process(InputStream inputStream, OutputStream outputStream) throws IOException {
        outputStream.write(CryptoUtils.hexToBytes(dataHex));
    }
}
