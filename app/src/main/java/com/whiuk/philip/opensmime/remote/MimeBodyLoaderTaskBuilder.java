package com.whiuk.philip.opensmime.remote;

import korex.mail.internet.MimeBodyPart;

public class MimeBodyLoaderTaskBuilder extends AbstractContentLoaderTaskBuilder<MimeBodyPart> {

    public MimeBodyLoaderTask build() {
        return new MimeBodyLoaderTask(getInputStream());
    }
}
