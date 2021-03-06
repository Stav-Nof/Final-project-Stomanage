package com.SandY.stomanage.Administrator;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.SandY.stomanage.Adapters.AdapterTextSubText;
import com.SandY.stomanage.GlobalConstants;
import com.SandY.stomanage.R;
import com.SandY.stomanage.dataObject.ClassObj;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Regiment extends AppCompatActivity {

    TextView _header;
    EditText _search;
    ImageButton _clear, _share, _new;
    ListView _itemslist;

    String cid;
    String className;

    List<String> keys;
    List<String> names;
    List<String> ages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_activity_listview_add_search);

        Intent intent = getIntent();
        className = intent.getStringExtra("className");
        cid = intent.getStringExtra("cid");

        attachFromXml();
        modifyActivity();
        printItemList(_search.getText().toString());
        setClicks();
        searchAction();
    }

    private void attachFromXml(){
        _header = findViewById(R.id.header);
        _clear = findViewById(R.id.clear);
        _search = findViewById(R.id.searchText);
        _share = findViewById(R.id.share);
        _new = findViewById(R.id.createNew);
        _itemslist = findViewById(R.id.itemslist);
    }

    private void modifyActivity(){
        _header.setText(getResources().getString(R.string.Class) + " - " + className);
        _search.setHint(getResources().getString(R.string.class_name));
        _clear.setVisibility(View.INVISIBLE);
    }

    private void printItemList(String search){
        DatabaseReference DBRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = DBRef.child("Regiment").child(cid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                keys = new ArrayList<>();
                names = new ArrayList<>();
                ages = new ArrayList<>();
                for(DataSnapshot ds : snapshot.getChildren()) {
                    ClassObj regiment = ds.getValue(ClassObj.class);
                    String key = ds.getKey();
                    if (regiment.get_name().contains(search) || regiment.get_ageGroup().contains(search)){
                        keys.add(key);
                        names.add(regiment.get_name());
                        ages.add(regiment.get_ageGroup());
                    }
                }
                AdapterTextSubText adapter = new AdapterTextSubText(Regiment.this, names, ages);
                _itemslist.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void setClicks() {
        _new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newRegiment();
            }
        });

        _clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _search.setText("");
            }
        });

        _itemslist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(Regiment.this)
                        .setTitle(getResources().getString(R.string.delete) + " " + names.get(position) )
                        .setMessage(getResources().getString(R.string.delete_regiment_message))
                        .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ClassObj.deletFromDB(cid, keys.get(position));
                                dialog.dismiss();
                                printItemList(_search.getText().toString());
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
                return false;
            }
        });
    }

    private void searchAction(){
        _search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                printItemList(_search.getText().toString());
                if (_search.getText().toString().equals("")) _clear.setVisibility(View.INVISIBLE);
                else _clear.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void newRegiment() {
        Dialog dialog = new Dialog(Regiment.this);
        dialog.setContentView(R.layout.popup_text_spinner_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.getWindow().getAttributes().windowAnimations = R.style.popUpAnimation;
        dialog.show();

        EditText name = dialog.findViewById(R.id.EditText);
        name.setHint(getResources().getString(R.string.class_name));

        Spinner ageGroup = dialog.findViewById(R.id.Spinner);
        GlobalConstants.ageGroup[] ageGroups = GlobalConstants.ageGroup.values();
        ArrayList<String> ages = new ArrayList();
        ages.add(getResources().getString(R.string.select_age_error));
        for (int i = 0; i < ageGroups.length; i++){
            ages.add(ageGroups[i].toString());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ages);
        ageGroup.setAdapter(adapter);

        Button add = dialog.findViewById(R.id.Button);
        add.setText(getResources().getString(R.string.add));

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().isEmpty()){
                    name.setError(getResources().getString(R.string.enter_age_group));
                    name.requestFocus();
                    return;
                }
                if (ageGroup.getSelectedItemPosition() == 0){
                    Toast.makeText(Regiment.this, getResources().getString(R.string.select_age_error), Toast.LENGTH_LONG).show();
                    return;
                }
                _search.setText("");
                String ageGroupS = ageGroup.getSelectedItem().toString();
                String nameS = name.getText().toString();
                if (ages.contains(ageGroupS)){
//                    ageGroup.setError(getResources().getString(R.string.age_group_already_exists)); //TODO
                    ageGroup.requestFocus();
                    return;
                }
                if (names.contains(nameS)){
                    name.setError(getResources().getString(R.string.name_already_exists));
                    name.requestFocus();
                    return;
                }
                ClassObj Regiment = new ClassObj(nameS, ageGroupS);
                Regiment.WriteNewToDB(cid);
                dialog.dismiss();
                printItemList(_search.getText().toString());
            }
        });
    }
}
