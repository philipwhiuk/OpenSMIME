package de.fau.cs.mad.smile_crypto;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.spongycastle.operator.bc.BcDigestCalculatorProvider;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class ListOwnCertificatesFragment extends Fragment {
    public ListOwnCertificatesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.task_layout);
        TextView myText = new TextView(getActivity());

        myText.setText(findCerts());
        linearLayout.addView(myText);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private String findCerts() {
        try {
            /*TODO: This lists own certs and certs from other people. Needs to be splitted into
             * part for own and a part for other certs. */
            String result = "My Certificates: ";
            String result_other = "\n Other Certificates: ";
            Log.d(SMileCrypto.LOG_TAG, "Find certs…");
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                Log.d(SMileCrypto.LOG_TAG, "Found certificate with alias: " + alias);
                Certificate c = ks.getCertificate(alias);
                KeyStore.Entry entry = ks.getEntry(alias, null);
                if (entry instanceof KeyStore.PrivateKeyEntry) {
                    ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
                    result += "\n\t · Alias: " + alias;
                    Log.d(SMileCrypto.LOG_TAG, "· Type: " + c.getType());
                    Log.d(SMileCrypto.LOG_TAG, "· HashCode: " + c.hashCode());
                    result += "\n\t\t – Type: " + c.getType();
                    result += "\n\t\t – HashCode: " + c.hashCode();
                    try {
                        //TODO: just for testing -- uses AsyncTask -- fails because of java.lang.NoClassDefFoundError: Failed resolution of: [Ljava/awt/datatransfer/DataFlavor;
                        //DecryptMail dM = new DecryptMail();
                        //dM.startEncDecMail();

                        // TODO: just a workaround for testing -- will throw an error (Network on main thread)
                        /* DecryptMail dM = new DecryptMail();
                        EncryptMail eM = new EncryptMail();
                        Properties props = System.getProperties();
                        Session session = Session.getDefaultInstance(props, null);

                        Address fromUser = new InternetAddress("\"Eric H. Echidna\"<eric@spongycastle.org>");
                        Address toUser = new InternetAddress("example@spongycastle.org");

                        MimeMessage body = new MimeMessage(session);
                        body.setFrom(fromUser);
                        body.setRecipient(Message.RecipientType.TO, toUser);
                        body.setSubject("example encrypted message");
                        MimeBodyPart messagePart = new MimeBodyPart();
                        MimeMultipart multipart = new MimeMultipart();
                        messagePart.setText("You message's string content goes here.", "utf-8");
                        multipart.addBodyPart(messagePart);
                        body.setContent(multipart);
                        Log.e(SMileCrypto.LOG_TAG, "Testmail: " + body.getContent().toString());
                        MimeMessage enc = eM.encrypt(alias, body);
                        Log.e(SMileCrypto.LOG_TAG, "Testmail enc: " + enc.getContent().toString());
                        MimeBodyPart dec = dM.decryptMail(alias, enc);
                        Log.e(SMileCrypto.LOG_TAG, "Testmail dec: " + dec.getContent().toString()); */
                    } catch (Exception ex) {
                        Log.e(SMileCrypto.LOG_TAG, "Error while encryspting/decrypting mail: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                } else {
                    //--> no private key available for this certificate
                    //currently there are no such entries because yet we cannot import the certs of
                    //others, e.g. by using their signature.
                    Log.d(SMileCrypto.LOG_TAG, "Not an instance of a PrivateKeyEntry");
                    Log.d(SMileCrypto.LOG_TAG, "· Type: " + c.getType());
                    Log.d(SMileCrypto.LOG_TAG, "· HashCode: " + c.hashCode());
                    result_other += "\n\t · Alias: " + alias;
                    result_other += "\n\t\t – Type: " + c.getType();
                    result_other += "\n\t\t – HashCode: " + c.hashCode();
                }
            }
            return result + result_other;
        } catch (Exception e){
            Log.e(SMileCrypto.LOG_TAG, "Error while finding certificate: " + e.getMessage());
            return null;
        }
    }
}