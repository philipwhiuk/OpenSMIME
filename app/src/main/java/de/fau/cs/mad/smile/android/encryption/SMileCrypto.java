package de.fau.cs.mad.smile.android.encryption;

import android.app.Application;

public class SMileCrypto extends Application {

    public static interface ApplicationAware {
        void initializeComponent(Application application);
    }

    public static Application app = null;
    public static final String LOG_TAG = "SMile-crypto";

    public static int EXIT_STATUS;

    //success
    public static final int STATUS_SUCCESS = 0;

    //problems with certificates/passphrases
    public static final int STATUS_NO_PASSPHRASE_AVAILABLE = 1;
    public static final int STATUS_NO_CERTIFICATE_FOUND = 2;
    public static final int STATUS_INVALID_CERTIFICATE_STORED = 3; // no p12 was saved, invalid p12 was saved, no private key from p12
    public static final int STATUS_CERTIFICATE_ALREADY_IMPORTED = 4;
    public static final int STATUS_WRONG_PASSPHRASE = 5;

    //problems with MimeMessage (e.g. reading, creating…)
    public static final int STATUS_NO_VALID_MIMEMESSAGE_IN_FILE = 10;
    public static final int STATUS_NO_VALID_MIMEMESSAGE = 11;
    public static final int STATUS_NO_RECIPIENTS_FOUND = 12;

    //problems with decryption/encryption/signing
    public static final int STATUS_DECRYPTION_FAILED = 15;

    // other problems
    public static final int STATUS_INVALID_PARAMETER = 20; // e.g. null as param, could not be resolved to smth working
    public static final int STATUS_ERROR_ASYNC_TASK = 21;

    //unknown error -- status code is the answer ;-)
    public static final int STATUS_UNKNOWN_ERROR = 42;

    /**
     * If this is enabled there will be additional logging information sent to
     * Log.d, including protocol dumps.
     * TODO: Controlled by Preferences at run-time
     */
    public static boolean DEBUG = true;

}