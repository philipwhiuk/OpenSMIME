package com.whiuk.philip.opensmime.crypto;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Binder;
import android.util.Log;

import com.whiuk.philip.opensmime.OpenSMIME;

import org.openintents.smime.util.SMimeApi;
import org.openintents.smime.util.SMimeError;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by philip on 28/03/2016.
 */
public class CheckPermission {

    private final Context mContext;
//    private final ApiDataAccessObject mApiDao;
    private PackageManager mPackageManager;

    public CheckPermission(Context context /*ApiDataAccessObject apiDao */) {
//        mApiDao = apiDao;
        mContext = context;
        mPackageManager = context.getPackageManager();
    }

    public boolean checkPermission() throws WrongPackageCertificateException, PackageManager.NameNotFoundException {

        if (isCallerAllowed()) {
            return true;
        } else {
            String packageName = getCurrentCallingPackage();
            Log.d(OpenSMIME.LOG_TAG, "isAllowed packageName: " + packageName);

            byte[] packageCertificate;
            packageCertificate = getPackageCertificate(packageName);
            Log.e(OpenSMIME.LOG_TAG, "Not allowed to use service! return PendingIntent for registration!");

            return false;
        }
    }

    /**
     * Returns package name associated with the UID, which is assigned to the process that sent you the
     * current transaction that is being processed :)
     *
     * @return package name
     */
    private String getCurrentCallingPackage() {
        String[] callingPackages = mPackageManager.getPackagesForUid(Binder.getCallingUid());

        // NOTE: No support for sharedUserIds
        // callingPackages contains more than one entry when sharedUserId has been used
        // No plans to support sharedUserIds due to many bugs connected to them:
        // http://java-hamster.blogspot.de/2010/05/androids-shareduserid.html
        String currentPkg = callingPackages[0];
        Log.d(OpenSMIME.LOG_TAG, "currentPkg: " + currentPkg);

        return currentPkg;
    }


    /**
     * Checks if process that binds to this service (i.e. the package name corresponding to the
     * process) is in the list of allowed package names.
     *
     * @return true if process is allowed to use this service
     * @throws WrongPackageCertificateException
     */
    private boolean isCallerAllowed() throws WrongPackageCertificateException {
        return isUidAllowed(Binder.getCallingUid());
    }

    private boolean isUidAllowed(int uid)
            throws WrongPackageCertificateException {

        String[] callingPackages = mPackageManager.getPackagesForUid(uid);

        // is calling package allowed to use this service?
        for (String currentPkg : callingPackages) {
            if (isPackageAllowed(currentPkg)) {
                return true;
            }
        }

        Log.e(OpenSMIME.LOG_TAG, "Uid is NOT allowed!");
        return false;
    }


    /**
     * Checks if packageName is a registered app for the API. Does not return true for own package!
     *
     * @throws WrongPackageCertificateException
     */
    private boolean isPackageAllowed(String packageName) throws WrongPackageCertificateException {
        Log.d(OpenSMIME.LOG_TAG, "isPackageAllowed packageName: " + packageName);

//TODO:
//        ArrayList<String> allowedPkgs = mApiDao.getRegisteredApiApps();
//        Log.d(OpenSMIME.LOG_TAG, "allowed: " + allowedPkgs);

        // check if package is allowed to use our service
        if (/*allowedPkgs.contains(packageName)*/true) {
            Log.d(OpenSMIME.LOG_TAG, "Package is allowed! packageName: " + packageName);

            // check package signature
            byte[] currentCert;
            try {
                currentCert = getPackageCertificate(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                throw new WrongPackageCertificateException(e.getMessage());
            }

// TODO:
//            byte[] storedCert = mApiDao.getApiAppCertificate(packageName);
            if (/*Arrays.equals(currentCert, storedCert)*/true) {
                Log.d(OpenSMIME.LOG_TAG,
                        "Package certificate is correct! (equals certificate from database)");
                return true;
            } else {
                throw new WrongPackageCertificateException(
                        "PACKAGE NOT ALLOWED! Certificate wrong! (Certificate not " +
                                "equals certificate from database)");
            }
        }

        Log.d(OpenSMIME.LOG_TAG, "Package is NOT allowed! packageName: " + packageName);
        return false;
    }

    private byte[] getPackageCertificate(String packageName) throws PackageManager.NameNotFoundException {
        @SuppressLint("PackageManagerGetSignatures") // we do check the byte array of *all* signatures
                PackageInfo pkgInfo = mContext.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
        // NOTE: Silly Android API naming: Signatures are actually certificates
        Signature[] certificates = pkgInfo.signatures;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (Signature cert : certificates) {
            try {
                outputStream.write(cert.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Should not happen! Writing ByteArrayOutputStream to concat certificates failed");
            }
        }

        // Even if an apk has several certificates, these certificates should never change
        // Google Play does not allow the introduction of new certificates into an existing apk
        // Also see this attack: http://stackoverflow.com/a/10567852
        return outputStream.toByteArray();
    }

    public static class WrongPackageCertificateException extends Exception {
        private static final long serialVersionUID = -8294642703122196028L;

        public WrongPackageCertificateException(String message) {
            super(message);
        }
    }
}
