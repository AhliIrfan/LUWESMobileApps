package com.example.luwesmobileapps;

import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter {
    private final Activity context;
    public static CheckBox checkBox;
    //to store the list of device name
    private final ArrayList nameArray;
    //to store the list of device address
    private final ArrayList addressArray;

    public DeviceListAdapter(Activity context, ArrayList nameArrayParam, ArrayList addressArrayParam){

        super(context,R.layout.device_listview , nameArrayParam);

        this.context=context;
        this.nameArray = nameArrayParam;
        this.addressArray = addressArrayParam;

    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.device_listview, null,true);

        //this code gets references to objects in the listview_row.xml file

        checkBox = (CheckBox) rowView.findViewById(R.id.checkBoxItem);

//        if(showCheckBox){
//            checkBox.setVisibility(VISIBLE);
//            checkBox.setChecked(checkBoxState[position]);
////            if(checkedCheckBox)
////                checkBox.setChecked(true);
////            else
////                checkBox.setChecked(false);
//        }
//        else{
//            checkBox.setVisibility(View.GONE);
//        }

        TextView nameTextField = (TextView) rowView.findViewById(R.id.deviceName);
        TextView addressTextField = (TextView) rowView.findViewById(R.id.deviceAddress);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(nameArray.get(position).toString());
        addressTextField.setText(addressArray.get(position).toString());
        return rowView;
    };
}
