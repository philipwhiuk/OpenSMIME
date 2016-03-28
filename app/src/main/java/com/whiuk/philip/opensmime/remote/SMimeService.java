package com.whiuk.philip.opensmime.remote;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.openintents.smime.util.SMimeError;
import org.spongycastle.cms.CMSException;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.x509.CertPathReviewerException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.whiuk.philip.opensmime.OpenSMIME;
import com.whiuk.philip.opensmime.crypto.CheckPermission;
import com.whiuk.philip.opensmime.crypto.KeyManagement;
import com.whiuk.philip.opensmime.remote.operation.CryptoOperation;
import org.openintents.smime.ISMimeService;
import org.openintents.smime.util.SMimeApi;
import korex.mail.MessagingException;
import korex.mail.internet.AddressException;
import korex.mail.internet.InternetAddress;

public class SMimeService extends Service {

    private Map<Long, ParcelFileDescriptor> mOutputPipeMap = new HashMap<Long, ParcelFileDescriptor>();

    private long createKey(int id) {
        int callingPid = Binder.getCallingPid();
        return ((long) callingPid << 32) | ((long) id & 0xFFFFFFFL);
    }

    private final ISMimeService.Stub mBinder = new ISMimeService.Stub() {
        @Override
        public ParcelFileDescriptor createOutputPipe(int outputPipeId) throws RemoteException {

            try {
                ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
                mOutputPipeMap.put(createKey(outputPipeId), pipe[1]);
                return pipe[0];
            } catch (IOException e) {
                Log.e(OpenSMIME.LOG_TAG, "IOException in OpenPgpService2", e);
                return null;
            }
        }

        @Override
        public Intent execute(Intent data, ParcelFileDescriptor input, int outputPipeId) throws RemoteException {
            long key = createKey(outputPipeId);
            ParcelFileDescriptor output = mOutputPipeMap.get(key);
            mOutputPipeMap.remove(key);
            return processRequest(data, input, output);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Intent processRequest(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        Intent result = new Intent();
        result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
        CryptoOperation operation = null;
        String action = data.getAction();

        try {
            CryptoOperationBuilder cryptoOperationBuilder = new CryptoOperationBuilder();
            cryptoOperationBuilder.setData(data);
            cryptoOperationBuilder.setInput(input);
            cryptoOperationBuilder.setOutput(output);

            switch (action) {
                case SMimeApi.ACTION_CHECK_PERMISSION: {
                    return checkPermission();
                }
                case SMimeApi.ACTION_GET_SIGN_CERTIFICATE_ID: {
//TODO:                    return checkPermission();
                }
                case SMimeApi.ACTION_SIGN:
                    operation = cryptoOperationBuilder.createSignOperation();
                    break;
                case SMimeApi.ACTION_ENCRYPT:
                    operation = cryptoOperationBuilder.createEncryptOperation();
                    break;
                case SMimeApi.ACTION_ENCRYPT_AND_SIGN:
                    operation = cryptoOperationBuilder.createSignAndEncryptOperation();
                    break;
                case SMimeApi.ACTION_DECRYPT_VERIFY:
                    operation = cryptoOperationBuilder.createDecryptAndVerifyOperation();
                    break;
                case SMimeApi.ACTION_VERIFY:
                    operation = cryptoOperationBuilder.createVerifyOperation();
                    break;
                case SMimeApi.HAS_PRIVATE_KEY:
                    return checkPrivateKey(data);
                case SMimeApi.HAS_PUBLIC_KEY:
                    return checkPublicKey(data);
                default:
                    if(OpenSMIME.isDEBUG()) {
                        Log.d(OpenSMIME.LOG_TAG, "Unknown operation " + action);
                    }
            }

            if (operation != null) {
                operation.execute();
                result = operation.getResult();
            }
        } catch (IOException | GeneralSecurityException | CertPathReviewerException | CMSException |
                OperatorCreationException | SMIMEException | ExecutionException | InterruptedException | MessagingException e) {
            result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
            if(OpenSMIME.isDEBUG()) {
                Log.e(OpenSMIME.LOG_TAG, "Exception while doing crypto stuff", e);
            }
        } finally {
            if (operation != null) {
                try {
                    operation.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    private Intent checkPermission() {
        CheckPermission checkPermission = new CheckPermission(getApplicationContext());
        Intent result = new Intent();

        try {
            if (checkPermission.checkPermission()) {
                result.putExtra(SMimeApi.RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
                return result;
            } else {
                return permissionRequired();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(OpenSMIME.LOG_TAG, "Should not happen, returning!", e);
            // return error
            result.putExtra(SMimeApi.RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
            result.putExtra(SMimeApi.RESULT_ERROR,
                    new SMimeError(SMimeError.GENERIC_ERROR, e.getMessage()));
            return result;
        } catch (CheckPermission.WrongPackageCertificateException e) {
            return permissionRequired();
        }
    }

    private Intent permissionRequired() {
        Intent result = new Intent();
//      return PendingIntent to be executed by client
//      PendingIntent pi = piFactory.createRegisterPendingIntent(data,
//      packageName, packageCertificate);
        result.putExtra(SMimeApi.RESULT_CODE, SMimeApi.RESULT_CODE_USER_INTERACTION_REQUIRED);
//      result.putExtra(SMimeApi.RESULT_INTENT, pi);
        return result;
    }

    private Intent checkPublicKey(Intent data) {
        String address = data.getStringExtra(SMimeApi.EXTRA_IDENTITY);
        return checkKey(address, false);
    }

    private Intent checkPrivateKey(Intent data) {
        String address = data.getStringExtra(SMimeApi.EXTRA_IDENTITY);
        return checkKey(address, true);

    }

    private Intent checkKey(String address, boolean includeOwn) {
        Intent intent = new Intent();
        try {
            KeyManagement keyManagement = KeyManagement.getInstance();
            InternetAddress email = new InternetAddress(address);
            if(keyManagement.getKeyInfoByAddress(email, includeOwn).size() > 0) {
                intent.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
            }
            intent.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);

        } catch (CertificateException | NoSuchAlgorithmException | NoSuchProviderException | KeyStoreException | IOException e) {
            Log.e(OpenSMIME.LOG_TAG, "Error getting KeyManagement instance", e);
            intent.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
        } catch (AddressException e) {
            Log.e(OpenSMIME.LOG_TAG, "Error creating email address", e);
            intent.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
        }
        return intent;
    }

}
