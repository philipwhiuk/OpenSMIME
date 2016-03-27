package com.whiuk.philip.opensmime.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import com.whiuk.philip.opensmime.KeyInfo;
import com.whiuk.philip.opensmime.R;
import com.whiuk.philip.opensmime.OpenSMIME;
import com.whiuk.philip.opensmime.crypto.KeyManagement;
import com.whiuk.philip.opensmime.ui.adapter.KeyAdapter;

/**
 * Display all certificates. User can use a search bar to find certificates.
 */
public class SearchActivity extends ActionBarActivity {
    private KeyAdapter adapter;
    private KeyManagement keyManager;

    private List<KeyInfo> cards;

    private Toolbar toolbar;
    private String searchQuery;
    private EditText searchEt;
    private ArrayList<KeyInfo> cardsFiltered;
    private String edText;

    private static String STATE_SEARCH = "searchquery";
    private static String STATE_EDTEXT = "edtext";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        toolbar.setTitle(R.string.title_activity_search);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(STATE_SEARCH);
            edText = savedInstanceState.getString(STATE_EDTEXT);
        } else {
            searchQuery = "";
            edText = "";
        }

        try {
            keyManager = KeyManagement.getInstance();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | NoSuchProviderException | CertificateException e) {
            if(OpenSMIME.isDEBUG()) {
                Log.e(OpenSMIME.LOG_TAG, "Error while getting KeyManagement instance. " + e.getMessage());
            }
            showErrorPrompt();
        }

        cards = keyManager.getAllCertificates();

        //Staggered grid view
        RecyclerView gRecyclerView = (RecyclerView) this.findViewById(R.id.card_list);
        gRecyclerView.setHasFixedSize(false);
        gRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new KeyAdapter(this, cards);
        //adapter.addKey(cards);
        gRecyclerView.setAdapter(adapter);

        searchEt = (EditText) toolbar.findViewById(R.id.search_bar);
        searchEt.addTextChangedListener(new SearchWatcher());
        searchEt.setText(edText);
        searchEt.requestFocus();

        ImageView image = (ImageView) toolbar.findViewById(R.id.remove_search);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEt.setText("");
                adapter.addKey(keyManager.getAllCertificates());
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_SEARCH, searchQuery);
        savedInstanceState.putString(STATE_EDTEXT, edText);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        adapter.addKey(keyManager.getAllCertificates());
        super.onResume();
    }

    /**
     * Listen for user input.
     */
    private class SearchWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence c, int i, int i2, int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            searchQuery = searchEt.getText().toString();
            if(OpenSMIME.isDEBUG()) {
                Log.d(OpenSMIME.LOG_TAG, "Search for: " + searchQuery);
            }
            if (!searchQuery.equals("")) {
                cardsFiltered = performSearch(cards, searchQuery);
                adapter.switchCards(cardsFiltered);
            } else {
                adapter.switchCards(cards);
            }
        }

    }

    /**
     * Search fpr certificates matching a given string.
     * @param cardList The certificates to search.
     * @param query The string to search after.
     * @return A list containing all matches.
     */
    private ArrayList<KeyInfo> performSearch(List<KeyInfo> cardList, String query) {
        String[] queryByWords = query.toLowerCase().split("\\s+");

        ArrayList<KeyInfo> cardsFiltered = new ArrayList<KeyInfo>();

        for (KeyInfo ki : cardList) {
            StringBuilder stringBuilder = new StringBuilder();
            Resources res = getResources();
            for (String fmt : res.getStringArray(R.array.dateSearchFormats)) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(fmt);
                DateTime termination = ki.getTerminationDate();
                if (termination != null) {
                    stringBuilder.append(termination.toString(dateTimeFormatter));
                }
                stringBuilder.append(" ");
                DateTime validAfter = ki.getValidAfter();
                if (validAfter != null) {
                    stringBuilder.append(validAfter.toString(dateTimeFormatter));
                }
                stringBuilder.append(" ");
            }

            String mail = ki.getMail();
            if (mail != null) {
                stringBuilder.append(mail);
                stringBuilder.append(" ");
            }

            String name = ki.getContact();
            if(name != null) {
                stringBuilder.append(name);
                stringBuilder.append(" ");
            }

            String content = stringBuilder.toString().toLowerCase();

            int numberOfMatches = queryByWords.length;

            for (String word : queryByWords) {
                if(OpenSMIME.isDEBUG()) {
                    Log.d(OpenSMIME.LOG_TAG, "Search for " + word + " in " + content);
                }

                if (content.contains(word)) {
                    if(OpenSMIME.isDEBUG()) {
                        Log.d(OpenSMIME.LOG_TAG, "Found");
                    }
                    numberOfMatches--;
                } else {
                    if(OpenSMIME.isDEBUG()) {
                        Log.d(OpenSMIME.LOG_TAG, "Not found");
                    }
                    break;
                }

                if (numberOfMatches == 0) {
                    if(OpenSMIME.isDEBUG()) {
                        Log.d(OpenSMIME.LOG_TAG, "Found complete query");
                    }
                    cardsFiltered.add(ki);
                }

            }

        }

        return cardsFiltered;
    }

    /**
     * Show prompt for internal errors.
     */
    private void showErrorPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
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
