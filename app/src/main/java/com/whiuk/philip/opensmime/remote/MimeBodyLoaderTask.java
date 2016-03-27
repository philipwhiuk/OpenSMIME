package com.whiuk.philip.opensmime.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.whiuk.philip.opensmime.OpenSMIME;
import korex.mail.MessagingException;
import korex.mail.internet.MimeBodyPart;

public class MimeBodyLoaderTask extends ContentLoaderTask<MimeBodyPart> {
    private File inputFile;

    public MimeBodyLoaderTask(final InputStream inputStream) {
        super(inputStream);
    }

    @Override
    protected MimeBodyPart doInBackground(Void... params) {
        try {
            InputStream inputStream = getInputStream();
            if(OpenSMIME.isDEBUG()) {
                inputFile = copyToFile(inputStream);
                inputStream = new FileInputStream(inputFile);
            }
            return new MimeBodyPart(inputStream);
        } catch (MessagingException | IOException e) {
            return null;
        }
    }
}
