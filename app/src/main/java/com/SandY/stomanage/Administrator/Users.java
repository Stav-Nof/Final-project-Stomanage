package com.SandY.stomanage.Administrator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.SandY.stomanage.Adapters.AdapterTextSubText;
import com.SandY.stomanage.R;
import com.SandY.stomanage.dataObject.UserObj;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;


public class Users extends AppCompatActivity {

    ImageButton _new;
    EditText _search;
    ListView _itemslist;
    TextView _header;
    ImageButton _clear;

    ArrayList <String> usersNames;
    ArrayList <String> troops;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_activity_listview_add_search);

        attachFromXml();
        modifyActivity();
        setClicks();
        printItemList(_search.getText().toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        printItemList(_search.getText().toString());
    }


    private void attachFromXml() {
        _new = (ImageButton) findViewById(R.id.createNew);
        _search = (EditText) findViewById(R.id.searchText);
        _itemslist = (ListView) findViewById(R.id.itemslist);
        _header = (TextView) findViewById(R.id.header);
        _clear = (ImageButton) findViewById(R.id.clear);
    }

    private void modifyActivity(){
        _header.setText(getResources().getString(R.string.users));
        _search.setHint(getResources().getString(R.string.user_name));
        _clear.setVisibility(View.INVISIBLE);

    }

    private void printItemList(String search){
        DatabaseReference DBRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = DBRef.child("Users");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usersNames = new ArrayList<>();
                troops = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    UserObj user = ds.getValue(UserObj.class);
                    if (user.getFirstName().contains(search) || user.getFirstName().contains(search) || user.getTroop().contains(search) || user.getLeadership().contains(search)){
                        usersNames.add(user.getFirstName() + " " + user.getLastName());
                        troops.add(user.getTroop());
                    }
                }
                AdapterTextSubText adapter = new AdapterTextSubText(Users.this, usersNames, troops);
                _itemslist.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        ref.addListenerForSingleValueEvent(valueEventListener);
    }

    private void setClicks(){
        _new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Users.this, NewUser.class);
                startActivity(intent);
            }
        });
    }
}