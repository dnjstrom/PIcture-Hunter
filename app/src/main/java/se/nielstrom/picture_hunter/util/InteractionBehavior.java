package se.nielstrom.picture_hunter.util;

import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;


public class InteractionBehavior implements AdapterView.OnItemClickListener,
                                            View.OnClickListener,
                                            GridView.MultiChoiceModeListener{

    protected final Fragment fragment;
    protected final Storage storage;

    public InteractionBehavior(Fragment fragment) {
        this.fragment = fragment;
        storage = Storage.getInstance(fragment.getActivity());
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {}

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {}

    @Override
    public void onClick(View view) {}

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {}
}
