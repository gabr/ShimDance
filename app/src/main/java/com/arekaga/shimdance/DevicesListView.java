package com.arekaga.shimdance;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Devices List View
 */
public class DevicesListView extends ListView {

    //region Item class

    /**
     * Container for one list item data
     */
    private class Item {
        public BluetoothDevice bluetoothDevice;
        public String subscription;
        public Boolean selected;

        public Item(BluetoothDevice device) {
            bluetoothDevice = device;
            subscription = "parried device";
            selected = false;
        }
    }
    //endregion

    //region DevicesArrayAdapter

    /**
     * Devices Array Adapter for this List View
     */
    private class DevicesArrayAdapter extends ArrayAdapter<Item> implements CompoundButton.OnCheckedChangeListener {

        /**
         * List of all items
         */
        private ArrayList<Item> mItems = new ArrayList<Item>();

        /**
         * Counts number of selected items
         */
        private int mSelectedItemsCounter = 0;

        /**
         * Maximum number of selected items
         */
        private final int mMaxNumberOfSelectedItems = 2;

        /**
         * Default public constructor
         * @param context Context view
         */
        public DevicesArrayAdapter(Context context) {
            super(context, R.layout.row_device);
        }

        /**
         * Get view method
         * @param position item position
         * @param convertView context view
         * @param parent parent view
         * @return returns view to display
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // use previously created view if exist
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.row_device, parent, false);
            }

            // get item
            Item item = getItem(position);

            // get row GUI elements
            TextView deviceName = (TextView) convertView.findViewById(R.id.row_device_name);
            TextView deviceSubscription = (TextView) convertView.findViewById(R.id.row_device_subscription);
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.row_device_check_box);

            // rewrite data from item to GUI elements
            deviceName.setText(item.bluetoothDevice.getName());
            deviceSubscription.setText(item.subscription);
            checkBox.setChecked(item.selected);

            // save item position in check box
            checkBox.setTag(position);

            // add check box listener
            checkBox.setOnCheckedChangeListener(this);

            // if maximum number of selected items has been reached disable check box if this is not selected one
            if (mSelectedItemsCounter >= mMaxNumberOfSelectedItems && !item.selected) {
                checkBox.setEnabled(false);
            } else {
                checkBox.setEnabled(true);
            }

            // return view
            return convertView;
        }

        /**
         * Implementatino of OnCheckedChangeListener.
         * Saves new check box state in related item.
         * @param b new check box state
         */
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            // get item position from tag
            int position = (Integer) compoundButton.getTag();
            // get item
            Item item = getItem(position);
            // save new state in item
            item.selected = b;

            // update selected items counter
            mSelectedItemsCounter += b ? 1 : -1;

            // redraw all checkboxes
            this.notifyDataSetChanged();
        }

        /**
         * Returns item on given position
         * @param position item position
         * @return item on given position
         */
        @Override
        public Item getItem(int position) {
            return mItems.get(position);
        }

        /**
         * Returns list of selected items
         * @return list of selected items
         */
        public ArrayList<Item> getSelected() {
            ArrayList<Item> result = new ArrayList<Item>();
            for (Item i: mItems) {
                if (i.selected) {
                    result.add(i);
                }
            }
            return result;
        }

        /**
         * Returns number of items
         * @return number of items
         */
        @Override
        public int getCount() {
            return mItems.size();
        }

        /**
         * Adds given item
         * @param item item to add
         */
        public void addItem(Item item) {
            mItems.add(item);
        }

        /**
         * Removes all elements from list
         */
        public void clearItemsList() {
            mItems.clear();
        }
    }

    //endregion

    /**
     * Adapter for this list
     */
    private DevicesArrayAdapter mAdapter;

    /**
     * Calls default constructor and creates adapter
     * @param context View context
     */
    public DevicesListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, R.layout.row_device);

        // create and add DevicesArrayAdatper
        mAdapter = new DevicesArrayAdapter(context);
        this.setAdapter(mAdapter);
    }

    /**
     * Adds given list of Bluetooth Devices to List View
     * @param devices list of Bluetooth Devices
     */
    public void addBluetoothDevices(ArrayList<BluetoothDevice> devices) {
        // clear devices list in adapter
        mAdapter.clearItemsList();
        // for each device create and add Item object
        for (BluetoothDevice bd: devices) {
            mAdapter.addItem(new Item(bd));
        }
        // refresh view
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Returns list of selected Bluetooth Devices
     * @return list of selected Bluetooth Devices
     */
    public ArrayList<BluetoothDevice> getSelectedDevices() {
        // get list of selected items
        ArrayList<Item> selectedDevices = mAdapter.getSelected();
        // create list for Bluetooth Device objects
        ArrayList<BluetoothDevice> result = new ArrayList<BluetoothDevice>();
        // rewrite Bluetooth Device objects from Items list
        for (Item i: selectedDevices) {
            result.add(i.bluetoothDevice);
        }
        // return result
        return result;
    }
}
