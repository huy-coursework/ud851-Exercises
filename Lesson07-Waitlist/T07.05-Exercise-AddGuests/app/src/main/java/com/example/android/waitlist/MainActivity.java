package com.example.android.waitlist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.android.waitlist.data.WaitlistContract;
import com.example.android.waitlist.data.WaitlistDbHelper;

import static com.example.android.waitlist.data.WaitlistContract.WaitlistEntry.COLUMN_GUEST_NAME;
import static com.example.android.waitlist.data.WaitlistContract.WaitlistEntry.COLUMN_PARTY_SIZE;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private GuestListAdapter mAdapter;
    private SQLiteDatabase mDb;

    private EditText mNewGuestNameEditText;
    private EditText mNewPartySizeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView waitlistRecyclerView;

        // Set local attributes to corresponding views
        waitlistRecyclerView = (RecyclerView) this.findViewById(R.id.all_guests_list_view);

        // Set the Edit texts to the corresponding views using findViewById
        mNewGuestNameEditText = (EditText) findViewById(R.id.person_name_edit_text);
        mNewPartySizeEditText = (EditText) findViewById(R.id.party_count_edit_text);

        // Set layout for the RecyclerView, because it's a list we are using the linear layout
        waitlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Create a DB helper (this will create the DB if run for the first time)
        WaitlistDbHelper dbHelper = new WaitlistDbHelper(this);

        // Keep a reference to the mDb until paused or killed. Get a writable database
        // because you will be adding restaurant customers
        mDb = dbHelper.getWritableDatabase();

        // Get all guest info from the database and save in a cursor
        Cursor cursor = getAllGuests();

        // Create an adapter for that cursor to display the data
        mAdapter = new GuestListAdapter(this, cursor);

        // Link the adapter to the RecyclerView
        waitlistRecyclerView.setAdapter(mAdapter);

    }

    /**
     * This method is called when user clicks on the Add to waitlist button
     *
     * @param view The calling view (button)
     */
    public void addToWaitlist(View view) {

        // First thing, check if any of the EditTexts are empty, return if so
        String guestName = mNewGuestNameEditText.getText().toString();
        String partySizeString = mNewPartySizeEditText.getText().toString();
        if (guestName.isEmpty() || partySizeString.isEmpty()) {
            return;
        }

        // Create an integer to store the party size and initialize to 1
        int partySize = 1;

        // Parse mNewPartySizeEditText.getText to an integer
        try {
            partySize = Integer.parseInt(partySizeString);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error found parsing party size: " + e.getMessage(), e);
        }

        // Call addNewGuest with the guest name and party size
        addGuest(guestName, partySize);

        // Update the cursor by passing in getAllGuests()
        mAdapter.swapCursor(getAllGuests());

        // To make the UI look nice, call .getText().clear() on both EditTexts, also call
        // clearFocus() on mNewPartySizeEditText
        mNewGuestNameEditText.getText().clear();
        mNewPartySizeEditText.getText().clear();
        mNewPartySizeEditText.clearFocus();
    }



    /**
     * Query the mDb and get all guests from the waitlist table
     *
     * @return Cursor containing the list of guests
     */
    private Cursor getAllGuests() {
        return mDb.query(
                WaitlistContract.WaitlistEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                WaitlistContract.WaitlistEntry.COLUMN_TIMESTAMP
        );
    }

    public void addGuest(String guestName, int partySize) {
        ContentValues guestValues = new ContentValues();
        guestValues.put(COLUMN_GUEST_NAME, guestName);
        guestValues.put(COLUMN_PARTY_SIZE, partySize);
        mDb.insert(WaitlistContract.WaitlistEntry.TABLE_NAME, null, guestValues);
    }
}