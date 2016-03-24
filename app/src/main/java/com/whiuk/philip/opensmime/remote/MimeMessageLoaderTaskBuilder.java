package com.whiuk.philip.opensmime.remote;


import korex.mail.internet.MimeMessage;

public class MimeMessageLoaderTaskBuilder extends AbstractContentLoaderTaskBuilder<MimeMessage> {
    @Override
    public ContentLoaderTask<MimeMessage> build() {
        return new MimeMessageLoaderTask(getInputStream());
    }
}
