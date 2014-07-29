package se.nielstrom.picture_hunter.util;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;

public abstract class InteractionBehavior implements AdapterView.OnItemClickListener,
                                                     View.OnClickListener,
                                                     AdapterView.OnItemLongClickListener,
                                                     AdapterView.OnItemSelectedListener {

    protected Fragment fragment;

    public InteractionBehavior(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
