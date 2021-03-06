package com.SandY.stomanage.HeadChapter;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.SandY.stomanage.Adapters.AdapterTextSubTextImage;
import com.SandY.stomanage.R;
import com.SandY.stomanage.dataObject.chapterObj;
import com.SandY.stomanage.dataObject.ItemObj;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WarehouseItemList extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_CODE = 819;

    String cid;
    chapterObj Class;

    EditText _search;
    ListView _itemslist;
    TextView _header;
    ImageButton _clear, _add;

    List<ItemObj> items;
    List<String> iid;
    List<String> itemName;
    List<String> quantity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_activity_listview_add_search);

        Intent intent = getIntent();
        cid = intent.getStringExtra("cid");

        downloadPermissions();
        attachFromXml();
        modifyActivity();
        setClicks();
        printItemList(_search.getText().toString());
        searchAction();
    }

    @Override
    protected void onResume() {
        super.onResume();
        printItemList(_search.getText().toString());
    }

    private void downloadPermissions(){
        if (ContextCompat.checkSelfPermission(WarehouseItemList.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){return;}
        else{
            if (ActivityCompat.shouldShowRequestPermissionRationale(WarehouseItemList.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                new AlertDialog.Builder(WarehouseItemList.this)
                        .setTitle(getResources().getString(R.string.perm_needed))
                        .setMessage(getResources().getString(R.string.storage_perm_message_write))
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(WarehouseItemList.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_CODE);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create().show();
            }
            else{
                ActivityCompat.requestPermissions(WarehouseItemList.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_CODE);
            }
        }
    }

    private void attachFromXml(){
        _search = (EditText) findViewById(R.id.searchText);
        _itemslist = (ListView) findViewById(R.id.itemslist);
        _header = (TextView) findViewById(R.id.header);
        _clear = (ImageButton) findViewById(R.id.clear);
        _add = findViewById(R.id.createNew);
    }

    private void modifyActivity(){
        DatabaseReference DBRef = FirebaseDatabase.getInstance().getReference().child("Chapters").child(cid);
        DBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Class = snapshot.getValue(chapterObj.class);
                _header.setText(Class.get_name() + " - " + getResources().getString(R.string.warehouses));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //TODO set error
            }
        });
        _search.setHint(getResources().getString(R.string.chapters_name));
        _clear.setVisibility(View.INVISIBLE);
    }

    private void printItemList(String search){
        DatabaseReference DBRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = DBRef.child("Warehouses").child(cid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items = new ArrayList<>();
                iid = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    iid.add(ds.getKey());
                    items.add(ds.getValue(ItemObj.class));
                }
                quantity = new ArrayList<>();
                itemName = new ArrayList<>();
                for (int i = 0; items.size() > i; i++) {
                    if (items.get(i).get_name().contains(search)) {
                        itemName.add(items.get(i).get_name());
                        quantity.add(Double.toString(items.get(i).get_quantity()));
                    }
                }
                AdapterTextSubTextImage adapter = new AdapterTextSubTextImage(WarehouseItemList.this, itemName, quantity, "Equipment\\" + cid, ".png", getResources().getDrawable(R.drawable.image_not_available));
                _itemslist.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //TODO set error
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

    private void setClicks(){
        _add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WarehouseItemList.this, NewItem.class);
                intent.putExtra("cid", cid);
                startActivity(intent);
            }
        });

        _itemslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Dialog dialog = new Dialog(WarehouseItemList.this);
                dialog.setContentView(R.layout.popup_textview_textview_edittext_button);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_background));
                }
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(true);
                dialog.getWindow().getAttributes().windowAnimations = R.style.popUpAnimation;
                dialog.show();

                TextView headerDialog = dialog.findViewById(R.id.header);
                TextView textDialog = dialog.findViewById(R.id.TextView);
                EditText fieldDialog = dialog.findViewById(R.id.EditText);
                Button updateDialog = dialog.findViewById(R.id.Button);

                headerDialog.setText(getResources().getString(R.string.update_quantity));
                textDialog.setText(itemName.get(position));
                fieldDialog.setText(quantity.get(position));
                updateDialog.setText(getResources().getString(R.string.update));

                updateDialog.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View v) {
                        if (fieldDialog.getText().toString().isEmpty()){
                            fieldDialog.setText("0");
                        }

//                        DatabaseReference DBRef = FirebaseDatabase.getInstance().getReference();
//                        DatabaseReference ref = DBRef.child("Warehouses").child(cid).child(eid.get(Ename.indexOf(EnameToPrint.get(position))));
//                        ref.setValue(Double.parseDouble(fieldDialog.getText().toString()));
//                        dialog.dismiss();
//                        printItemList(_search.getText().toString());

                    }
                });
            }
        });
    }

}
