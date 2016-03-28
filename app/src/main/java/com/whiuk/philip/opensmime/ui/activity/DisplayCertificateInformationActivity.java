package com.whiuk.philip.opensmime.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.IETFUtils;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.whiuk.philip.opensmime.App;
import com.whiuk.philip.opensmime.KeyInfo;
import com.whiuk.philip.opensmime.R;
import com.whiuk.philip.opensmime.OpenSMIME;
import com.whiuk.philip.opensmime.crypto.KeyManagement;
import com.whiuk.philip.opensmime.ui.activity.items.AbstractCertificateInfoItem;
import com.whiuk.philip.opensmime.ui.activity.items.CertificateInformationItem;
import com.whiuk.philip.opensmime.ui.activity.items.CryptographicInformationItem;
import com.whiuk.philip.opensmime.ui.activity.items.PersonalAndCAInformationItem;
import com.whiuk.philip.opensmime.ui.activity.items.ValidityItem;
import com.whiuk.philip.opensmime.ui.adapter.ExpandableCertificateListAdapter;
import com.whiuk.philip.opensmime.utilities.Utils;


/**
 * Display extended information about a certificate.
 */
public class DisplayCertificateInformationActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private String name;
    private String alias;
    private KeyInfo keyInfo;
    private HashMap<String, List<AbstractCertificateInfoItem>> listDataChild;
    private List<String> listDataHeader;
    ExpandableCertificateListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(OpenSMIME.isDEBUG()) {
            Log.d(OpenSMIME.LOG_TAG, "Started DisplayCertificateInformationActivity.");
        }
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            if(OpenSMIME.isDEBUG()) {
                Log.e(OpenSMIME.LOG_TAG, "intent was null.");
            }
            showErrorPrompt();
        }
        this.alias = extras.getString("Alias");
        if (this.alias == null) {
            if(OpenSMIME.isDEBUG()) {
                Log.e(OpenSMIME.LOG_TAG, "Called without alias.");
            }
            finish();
        }
        if(OpenSMIME.isDEBUG()) {
            Log.d(OpenSMIME.LOG_TAG, "Called with alias: " + alias);
        }
        this.name = extras.getString("Name");

        setContentView(R.layout.activity_display_certificate_information);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(this.name); //if (name == null) --> set later
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ExpandableListView expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        getKeyInfo();

        //TODO: workaround to prevent crash
        if (listDataHeader == null || listDataChild == null) {
            if(OpenSMIME.isDEBUG()) {
                Log.e(OpenSMIME.LOG_TAG, "ListDataHeader/ListDataChild was null.");
            }
            finish();
        } else {
            listAdapter = new ExpandableCertificateListAdapter(this, listDataHeader, listDataChild);

            // setting list adapter
            expListView.setAdapter(listAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_display_certificate_information, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_delete) {
            deleteKey(this.keyInfo);
        } else if (id == R.id.action_export) {
            exportCertificate();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the key info representing the certificate with the specified alias.
     */
    private void getKeyInfo() {
        try {
            KeyManagement keyManagement = KeyManagement.getInstance();
            KeyInfo keyInfo = keyManagement.getKeyInfo(this.alias);
            this.keyInfo = keyInfo;
            extractCertificateInformation(keyInfo);
        } catch (Exception e) {
            if(OpenSMIME.isDEBUG()) {
                Log.e(OpenSMIME.LOG_TAG, "Error: " + e.getMessage());
            }
            showErrorPrompt();
        }
    }

    /**
     * Extract certificate information for display.
     * @param keyInfo The key info representing the certificate.
     */
    private void extractCertificateInformation(KeyInfo keyInfo) {
        if (this.name == null) {
            this.name = keyInfo.getContact();
            if(OpenSMIME.isDEBUG()) {
                Log.d(OpenSMIME.LOG_TAG, "Name was null, set name to: " + this.name);
            }
            toolbar.setTitle(this.name);
            setSupportActionBar(toolbar);
        }
        listDataHeader = new ArrayList<>();
        listDataHeader.add(getString(R.string.personal));
        listDataChild = new HashMap<>();
        generatePersonalInformation(keyInfo);
        generateCaInformation(keyInfo);
        generateValidityInformation(keyInfo);
        generatingCertificateInformation(keyInfo);
        generateCryptographicInformation(keyInfo);
    }

    /**
     * Extract personal information.
     * @param keyInfo The key info representing the certificate
     */
    private void generatePersonalInformation(KeyInfo keyInfo) {
        if(OpenSMIME.isDEBUG()) {
            Log.d(OpenSMIME.LOG_TAG, "Setting personal information");
        }
        LinkedHashMap<String, String[]> data = new LinkedHashMap<>();
        X509Certificate certificate = keyInfo.getCertificate();
        if (certificate == null) {
            if(OpenSMIME.isDEBUG()) {
                Log.e(OpenSMIME.LOG_TAG, "Certificate was null -- abort.");
            }
            return;
        }
        X500Name x500name = null;
        try {
            x500name = new JcaX509CertificateHolder(certificate).getSubject();
            parseX500Name(data, x500name);
            ArrayList<AbstractCertificateInfoItem> pers = new ArrayList<>();
            PersonalAndCAInformationItem persI = new PersonalAndCAInformationItem();
            persI.buildComplex(data);
            pers.add(persI);
            listDataChild.put(listDataHeader.get(0), pers);
        } catch (CertificateEncodingException e) {
            if(OpenSMIME.isDEBUG()) {
                Log.d(OpenSMIME.LOG_TAG, "Error with certificate encoding: " + e.getMessage());
            }
            Toast.makeText(App.getContext(), getString(R.string.failed_extract), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Extract cryptographic information.
     * @param keyInfo The key info representing the certificate
     */
    private void generateCryptographicInformation(KeyInfo keyInfo) {
        if(OpenSMIME.isDEBUG()) {
            Log.d(OpenSMIME.LOG_TAG, "Setting cryptographic information");
        }
        listDataHeader.add(getString(R.string.cryptographic));
        HashMap<String, String> cryptographicInfo = new HashMap<>();
        PublicKey publicKey = keyInfo.getCertificate().getPublicKey();
        if (publicKey instanceof RSAPublicKey) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            String modulus = rsaPublicKey.getModulus().toString(16);
            String exponent = rsaPublicKey.getPublicExponent().toString(16);
            cryptographicInfo.put("Public Key", "RSAPublicKey");
            cryptographicInfo.put("Modulus", modulus);
            cryptographicInfo.put("Exponent", exponent);
            cryptographicInfo.put("Signature Algorithm", keyInfo.getCertificate().getSigAlgName());
            cryptographicInfo.put("Signature", new BigInteger(keyInfo.getCertificate().getSignature()).toString(16));
        } else {
            if(OpenSMIME.isDEBUG()) {
                Log.d(OpenSMIME.LOG_TAG, "Not an instance of RSAPublicKey.");
            }
            cryptographicInfo.put("Public Key", keyInfo.getCertificate().getPublicKey().toString());
            cryptographicInfo.put("Signature Algorithm", keyInfo.getCertificate().getSigAlgName());
            cryptographicInfo.put("Signature", new BigInteger(keyInfo.getCertificate().getSignature()).toString(16));
        }
        ArrayList<AbstractCertificateInfoItem> crypto = new ArrayList<>();
        CryptographicInformationItem cryptographicInformationItem = new CryptographicInformationItem();
        cryptographicInformationItem.build(cryptographicInfo);
        crypto.add(cryptographicInformationItem);
        listDataChild.put(listDataHeader.get(4), crypto);
    }

    /**
     * Extract certificate information.
     * @param keyInfo The key info representing the certificate
     */
    private void generatingCertificateInformation(KeyInfo keyInfo) {
        if(OpenSMIME.isDEBUG()) {
            Log.d(OpenSMIME.LOG_TAG, "Setting certificate information");
        }
        listDataHeader.add(getString(R.string.certificate));
        HashMap<String, String> certificateInfo = new HashMap<>();
        certificateInfo.put("Thumbprint", keyInfo.getThumbprint());
        BigInteger serialNumber = keyInfo.getCertificate().getSerialNumber();
        certificateInfo.put("Serial number", serialNumber.toString(16));
        certificateInfo.put("Version", Integer.toString(keyInfo.getCertificate().getVersion()));
        ArrayList<AbstractCertificateInfoItem> cert = new ArrayList<>();
        CertificateInformationItem certificateInformationItem = new CertificateInformationItem();
        certificateInformationItem.build(certificateInfo);
        cert.add(certificateInformationItem);
        listDataChild.put(listDataHeader.get(3), cert);
    }

    /**
     * Extract validity information.
     * @param keyInfo The key info representing the certificate
     */
    private void generateValidityInformation(KeyInfo keyInfo) {
        if(OpenSMIME.isDEBUG()) {
            Log.d(OpenSMIME.LOG_TAG, "Setting validity information");
        }
        DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMMM yyyy HH:mm:ss");
        listDataHeader.add(getString(R.string.validity));
        HashMap<String, String> validity = new HashMap<>();
        validity.put("Startdate", keyInfo.getValidAfter().toString(fmt));
        validity.put("Enddate", keyInfo.getTerminationDate().toString(fmt));
        ArrayList<AbstractCertificateInfoItem> val = new ArrayList<>();
        ValidityItem validityItem = new ValidityItem();
        validityItem.build(validity);
        val.add(validityItem);
        listDataChild.put(listDataHeader.get(2), val);
    }

    /**
     * Extract ca information.
     * @param keyInfo The key info representing the certificate
     */
    private void generateCaInformation(KeyInfo keyInfo) {
        X500Name x500name;
        if(OpenSMIME.isDEBUG()) {
            Log.d(OpenSMIME.LOG_TAG, "Setting ca information");
        }
        X509Certificate certificate = keyInfo.getCertificate();
        listDataHeader.add(getString(R.string.CA));
        LinkedHashMap<String, String[]> cadata = new LinkedHashMap<>();

        try {
            x500name = new JcaX509CertificateHolder(certificate).getIssuer();
            parseX500Name(cadata, x500name);
            ArrayList<AbstractCertificateInfoItem> pers = new ArrayList<>();
            PersonalAndCAInformationItem persI = new PersonalAndCAInformationItem();
            persI.buildComplex(cadata);
            pers.add(persI);
            listDataChild.put(listDataHeader.get(1), pers);
        } catch (CertificateEncodingException e) {
            if(OpenSMIME.isDEBUG()) {
                Log.d(OpenSMIME.LOG_TAG, "Error with certificate encoding: " + e.getMessage());
            }
            Toast.makeText(App.getContext(), getString(R.string.failed_extract), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Extracts data from a X500Name. The searched fields are defined in utils.
     * @param data The LinkedHashMap to fill with the extracted data.
     * @param x500name The name to parse.
     */
    private void parseX500Name(LinkedHashMap<String, String[]> data, X500Name x500name) {
        Resources res = getResources();
        String[] keys = res.getStringArray(R.array.info_keys);
        String[] entries = res.getStringArray(R.array.info_names);
        for (int i = 0; i < Utils.asn1ObjectIdentifiers.length && i < keys.length && i < entries.length; ++i) {
            RDN[] rdns = x500name.getRDNs(Utils.asn1ObjectIdentifiers[i]);
            if (rdns.length > 0) {
                String[] values = new String[2];
                values[0] = entries[i];
                values[1] = IETFUtils.valueToString(rdns[0].getFirst().getValue());
                data.put(keys[i], values);
            }
        }
    }

    /**
     * Delete the key represented by a key info.
     * @param keyInfo The certificate to be deleted.
     */
    private void deleteKey(final KeyInfo keyInfo) {
        final KeyManagement keyManagement;
        try {
            keyManagement = KeyManagement.getInstance();
        } catch (Exception e) {
            showErrorPrompt();
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if (keyInfo.getAlias().startsWith(OpenSMIME.KEY_PREFIX_OWN)) {
            alertDialogBuilder.setTitle(getString(R.string.alert_header_start) + keyInfo.getContact() + getString(R.string.alert_header_end));
            alertDialogBuilder
                    .setMessage(getString(R.string.alert_content))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.erase), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Boolean success = keyManagement.deleteKey(keyInfo.getAlias());
                            if (success)
                                Toast.makeText(App.getContext(),
                                        R.string.certificate_deleted, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            alertDialogBuilder.create().show();
        } else {
            alertDialogBuilder
                    .setMessage(getString(R.string.alert_header_start) + keyInfo.getContact() + getString(R.string.alert_header_end))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.erase), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Boolean success = keyManagement.deleteKey(keyInfo.getAlias());
                            if (success)
                                Toast.makeText(App.getContext(),
                                        R.string.certificate_deleted, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            alertDialogBuilder.create().show();
        }
    }

    /**
     * Export a certificate.
     */
    private void exportCertificate() {
        if(OpenSMIME.isDEBUG()) {
            Log.d(OpenSMIME.LOG_TAG, "Try to export certificate.");
        }
        if (this.alias.contains("_own_")) {
            exportOwnCertificate();
        } else if (this.alias.contains("_other_")) {
            exportOtherCertificate();
        } else {
            //this should not happen
            if(OpenSMIME.isDEBUG()) {
                Log.e(OpenSMIME.LOG_TAG, "Tried to export certificate with invalid alias: " + alias);
            }
        }
    }

    /**
     * Helper to export certificates containing private keys.
     */
    private void exportOwnCertificate() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.alert_header_export));
        alertDialogBuilder
                .setMessage(getString(R.string.alert_export))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.export), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String dst = KeyManagement.copyP12ToSDCard(alias);
                        if (dst == null) {
                            Toast.makeText(App.getContext(),
                                    getString(R.string.certificate_export_fail), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(App.getContext(),
                                    getString(R.string.certificate_export_success) + dst, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }

    /**
     * Helper to export public certificates.
     */
    private void exportOtherCertificate() {
        String dst = KeyManagement.copyCertificateToSDCard(keyInfo.getCertificate(), alias);
        if (dst == null) {
            Toast.makeText(App.getContext(),
                    getString(R.string.certificate_export_fail), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(App.getContext(),
                    getString(R.string.certificate_export_success) + dst, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show prompt for internal errors.
     */
    private void showErrorPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayCertificateInformationActivity.this);
        builder.setTitle(getResources().getString(R.string.error));
        if(OpenSMIME.isDEBUG()) {
            Log.e(OpenSMIME.LOG_TAG, "EXIT_STATUS: " + OpenSMIME.EXIT_STATUS);
        }
        builder.setMessage(getResources().getString(R.string.internal_error));
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }
}
