package com.kauron.newssarcher;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private CheckBox stemmingCheck, stopwordsCheck;
    private QueryAdapter mAdapter;
    private ArrayList<Query> queries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context context = this;

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final String TAG = "TAG";
        db.getReference("server_url").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putString("pref_endpoint", dataSnapshot.getValue(String.class)).apply();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
        db.getReference("server_port").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putString("pref_port", String.valueOf(dataSnapshot.getValue(Integer.class)))
                        .apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
            }
        });


        queries = new ArrayList<>();
        ListView listView = (ListView) findViewById(R.id.listView);
        editText = (EditText) findViewById(R.id.editText);
        stemmingCheck = (CheckBox) findViewById(R.id.stemmingCheck);
        stopwordsCheck = (CheckBox) findViewById(R.id.stopwordsCheck);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    onButtonClick(null);
                    return true;
                }
                return false;
            }
        });

        mAdapter = new QueryAdapter(this, queries);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (queries.get(i).hasAnswer()) {
                    Toast.makeText(adapterView.getContext(), R.string.no_answer_yet,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                LayoutInflater inflater= LayoutInflater.from(adapterView.getContext());
                View mView=inflater.inflate(R.layout.my_dialog, null);

                TextView answerView = (TextView) mView.findViewById(R.id.answer_text);
                TextView shortAnswerView = (TextView) mView.findViewById(R.id.shortanswer_text);
                TextView optionsView = (TextView) mView.findViewById(R.id.options_text);

                answerView.setText(queries.get(i).getAnswer());
                shortAnswerView.setText(queries.get(i).getShortAnswer());
                optionsView.setText(queries.get(i).getOptions());

                final int[] position = new int[] {i};

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(adapterView.getContext());
                alertDialog.setTitle(queries.get(i).getQuery());
                alertDialog.setView(mView);
                alertDialog.setPositiveButton(R.string.close, null);
                alertDialog.setNeutralButton(R.string.copy_query, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editText.setText("");
                        editText.append(queries.get(position[0]).getQuery());
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.show();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                editText.getText().clear();
                editText.append(queries.get(i).getQuery());
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onButtonClick(View view) {
        // connect to the server
        String s = editText.getText().toString();
        if (s.isEmpty())
            return;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String endpoint = sharedPref.getString("pref_endpoint", "kauron.ddns.net");
        int port = Integer.valueOf(sharedPref.getString("pref_port", "2048"));

        Query q = new Query(s, endpoint, port, stopwordsCheck.isChecked(), stemmingCheck.isChecked(), this);
        new QueryTask().execute(q);
        queries.add(0, q);
        mAdapter.notifyDataSetChanged();
        editText.setText("");
        editText.requestFocus();
    }

    public void onSymbolClick(View view) {
        editText.append(((Button)view).getText().toString());
    }

    public void onBoolClick(View view) {
        String s = ((Button)view).getText().toString() + ' ';
        String text = editText.getText().toString();
        if (!text.isEmpty() && text.charAt(text.length() - 1) != ' ')
            s = ' ' + s;
        editText.append(s);
    }

    public void onFieldClick(View view) {
        String s = ((Button)view).getText().toString();
        String text = editText.getText().toString();
        if (!text.isEmpty() && text.charAt(text.length() - 1) != ' ')
            s = ' ' + s;
        editText.append(s);
    }


    private class QueryTask extends AsyncTask<Query, Void, Void> {
        @Override
        protected Void doInBackground(Query... queries) {
            Query q  = queries[0];
            String message = q.getQuery() + q.getShortOptions();

            TCPClient mTcpClient = new TCPClient(message, q.getEndpoint(), new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String response) {
                    MainActivity.this.queries.get(0).setAnswer(response);
                }
            });
            mTcpClient.run();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
