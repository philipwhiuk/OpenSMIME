package com.whiuk.philip.opensmime.remote.operation;

import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.spongycastle.cms.CMSException;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.x509.CertPathReviewerException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

import com.whiuk.philip.opensmime.SMileCrypto;
import com.whiuk.philip.opensmime.crypto.CryptoParams;
import com.whiuk.philip.opensmime.remote.MimeBodyLoaderTaskBuilder;
import korex.mail.MessagingException;
import korex.mail.internet.AddressException;
import korex.mail.internet.MimeBodyPart;

abstract class MimeBodyCryptoOperation extends CryptoOperation<MimeBodyPart> {
    MimeBodyCryptoOperation(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws IOException, AddressException, CertificateException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        super(data, input, output, new MimeBodyLoaderTaskBuilder());
    }

    @Override
    public void execute() throws MessagingException, IOException, OperatorCreationException, GeneralSecurityException, SMIMEException, CMSException, CertPathReviewerException, ExecutionException, InterruptedException {
        final MimeBodyPart source = preProcess();
        final CryptoParams cryptoParams = cryptoParamsLoaderTask.get();
        final MimeBodyPart processed = process(source, cryptoParams);
        if (processed != null && outputStream != null) {
            final File targetFile = getOutputFile();
            if(SMileCrypto.isDEBUG()) {
                processed.writeTo(new FileOutputStream(targetFile));
            }

            processed.writeTo(outputStream);
        } else {
            Log.wtf(SMileCrypto.LOG_TAG, "processed or outputstream was null, cannot write to output");
        }
    }
}

