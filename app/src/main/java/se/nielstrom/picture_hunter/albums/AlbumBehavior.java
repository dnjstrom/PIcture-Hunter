package se.nielstrom.picture_hunter.albums;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import se.nielstrom.picture_hunter.common.FileAdapter;
import se.nielstrom.picture_hunter.photos.PhotoListActivity;
import se.nielstrom.picture_hunter.util.Storage;


/**
 * Convenience class that implements the interaction possibilities of the AlbumListFragment including
 * the context menu.
 */
public class AlbumBehavior  implements AdapterView.OnItemClickListener,
                                        View.OnClickListener,
                                        GridView.MultiChoiceModeListener {

    private final Fragment fragment;
    private final Storage storage;

    public AlbumBehavior(Fragment fragment) {
        this.fragment = fragment;
        this.storage = Storage.getInstance(fragment.getActivity());
    }

    /**
     * Create a new album in the fragments directory.
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        GridView grid = (GridView) fragment.getView().findViewById(R.id.grid);
        FileAdapter adapter = (FileAdapter) grid.getAdapter();
        storage.createAlbumAt(adapter.getLocation());
    }

    /**
     * Launch a new PhotoListActivity for the clicked album.
     *
     * @param adapterView
     * @param view
     * @param i
     * @param l
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        File file = (File) adapterView.getAdapter().getItem(i);
        Intent intent = new Intent(fragment.getActivity(), PhotoListActivity.class);
        intent.putExtra(PhotoListActivity.KEY_PATH, file.getAbsolutePath());
        fragment.startActivity(intent);
    }

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
    public void onDestroyActionMode(ActionMode actionMode) {}

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {}

    /**
     * Takes an appropriate action according to what action bar button has been pressed.
     *
     * @param mode
     * @param item
     * @return
     */
    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        GridView grid = (GridView) fragment.getView().findViewById(R.id.grid);
        final FileAdapter adapter = (FileAdapter) grid.getAdapter();
        final SparseBooleanArray checked = grid.getCheckedItemPositions();

        final File[] files = new File[grid.getCheckedItemCount()];

        // Extract the selected files for easier manipulation
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

                // Create a temporary dialog where the user can rename the album
                AlertDialog dialog = new AlertDialog.Builder(fragment.getActivity())
                        .setIcon(R.drawable.ic_action_edit)
                        .setTitle(R.string.rename_album)
                        .setView(text)
                        .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = String.valueOf(text.getText()).trim(); // remove surrounding white space
                                files[0].renameTo(new File(files[0].getParentFile(), name));
                                mode.finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();

                // Make sure that the soft keyboard is opened together with the dialog so the user
                // can start typing immediately.
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
                break;
            case R.id.delete:
                // Create a confirmation dialog.
                new AlertDialog.Builder(fragment.getActivity())
                        .setIcon(R.drawable.ic_action_discard)
                        .setTitle(R.string.confirm_delete)
                        .setMessage(R.string.confirm_delete_album_message)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storage.delete(files);
                                mode.finish();
                            }

                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                break;
        }

        return true;
    }
}
