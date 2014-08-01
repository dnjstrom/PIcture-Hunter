package se.nielstrom.picture_hunter.albums;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import java.io.File;

import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.photos.FileAdapter;
import se.nielstrom.picture_hunter.util.InteractionBehavior;


public class AlbumBehavior extends InteractionBehavior {

    public AlbumBehavior(Fragment fragment) {
        super(fragment);
    }

    @Override
    public void onClick(View view) {}

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {}

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        fragment.getActivity().getMenuInflater().inflate(R.menu.album_actions, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        GridView grid = (GridView) fragment.getView().findViewById(R.id.grid);
        final FileAdapter adapter = (FileAdapter) grid.getAdapter();
        final SparseBooleanArray checked = grid.getCheckedItemPositions();

        final File[] files = new File[grid.getCheckedItemCount()];

        for (int i = 0, j = 0; i < adapter.getCount() && j < files.length; i++) {
            if (checked.get(i)) {
                files[j] = adapter.getItem(i);
                j++;
            }
        }

        switch (item.getItemId()) {
            case R.id.rename:
                final EditText text = new EditText(fragment.getActivity());
                text.setSingleLine();
                text.setText(files[0].getName());
                text.setSelectAllOnFocus(true);

                AlertDialog dialog = new AlertDialog.Builder(fragment.getActivity())
                        .setIcon(R.drawable.ic_action_edit)
                        .setTitle("Rename picture")
                        .setView(text)
                        .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = String.valueOf(text.getText()).trim();
                                files[0].renameTo(new File(files[0].getParentFile(), name));
                                mode.finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
                break;
            case R.id.delete:
                new AlertDialog.Builder(fragment.getActivity())
                        .setIcon(R.drawable.ic_action_discard)
                        .setTitle("Confirm deletion")
                        .setMessage("Are you sure you want to delete these pictures?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storage.delete(files);
                                mode.finish();
                            }

                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                break;
        }

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {

    }
}
