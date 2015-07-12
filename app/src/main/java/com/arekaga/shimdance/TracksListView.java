package com.arekaga.shimdance;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Devices List View
 */
public class TracksListView extends ListView {

    //region Item class

    /**
     * Container for one list item data
     */
    private class Item {
        public String name;
        public String subscription;
        public Boolean selected;

        public Item(String name, String subscription) {
            this.name = name;
            this.subscription = subscription;
            this.selected = false;
        }
    }
    //endregion

    //region DevicesArrayAdapter

    /**
     * Devices Array Adapter for this List View
     */
    private class TracksArrayAdapter extends ArrayAdapter<Item> implements CompoundButton.OnCheckedChangeListener {

        /**
         * List of all items
         */
        private ArrayList<Item> mItems = new ArrayList<Item>();

        /**
         * Selected item
         */
        private Item mSelectedItem = null;

        /**
         * Default public constructor
         * @param context Context view
         */
        public TracksArrayAdapter(Context context) {
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
                convertView = inflater.inflate(R.layout.row_tracks, parent, false);
            }

            // get item
            Item item = getItem(position);

            // get row GUI elements
            TextView trackName = (TextView) convertView.findViewById(R.id.row_track_name);
            TextView trackSubscription = (TextView) convertView.findViewById(R.id.row_track_subscription);
            RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.row_track_radio_button);

            // rewrite data from item to GUI elements
            trackName.setText(item.name);
            trackSubscription.setText(item.subscription);
            radioButton.setChecked(item.selected);

            // save item position in check box
            radioButton.setTag(position);

            // add check box listener
            radioButton.setOnCheckedChangeListener(this);

            // return view
            return convertView;
        }

        /**
         * Implementatino of OnCheckedChangeListener.
         * Saves new radio button state in related item.
         * @param b new radio button state
         */
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            // get item position from tag
            int position = (Integer) compoundButton.getTag();
            // get item
            Item item = getItem(position);

            // if state is checked set this item as selected one
            if (b) {
                if (mSelectedItem != null) {
                    mSelectedItem.selected = false;
                }

                item.selected = true;
                mSelectedItem = item;
            }

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
        public Item getSelected() {
            return mSelectedItem;
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
    private TracksArrayAdapter mAdapter;

    /**
     * Calls default constructor and creates adapter
     * @param context View context
     */
    public TracksListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, R.layout.row_tracks);

        // create and add DevicesArrayAdatper
        mAdapter = new TracksArrayAdapter(context);
        this.setAdapter(mAdapter);
    }

    /**
     * Adds given list of Tracks to List View
     * @param tracks list of tracks, each track is a string array
     *               where first element is track name, and the
     *               second is track subscription
     */
    public void addTracks(String[][] tracks) {
        // clear devices list in adapter
        mAdapter.clearItemsList();
        // for each device create and add Item object
        for (String[] data: tracks) {
            mAdapter.addItem(new Item(data[0], data[1]));
        }
        // refresh view
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Returns selected track
     * @return selected track
     */
    public String[] getSelected() {
        Item item = mAdapter.getSelected();
        return new String[] {item.name, item.subscription};
    }
}
