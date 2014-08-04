package se.nielstrom.picture_hunter.photos;

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
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import se.nielstrom.picture_hunter.R;
import se.nielstrom.picture_hunter.common.FileAdapter;
import se.nielstrom.picture_hunter.util.Storage;

/**
 * Takes care of the photo selection and context menu interactions for the photo fragment.
 */
public abstract class PhotoBehavior implements AdapterView.OnItemClickListener,
                                                View.OnClickListener,
                                                GridView.MultiChoiceModeListener {

    private final Fragment fragment;
    private final Storage storage;

    public PhotoBehavior(Fragment fragment) {
        this.fragment = fragment;
        storage = Storage.getInstance(fragment.getActivity());
    }

    @Override
    public void onClick(View view) {}

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {}


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        fragment.getActivity().getMenuInflater().inflate(R.menu.photo_actions, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        GridView grid = (GridView) fragment.getView().findViewById(R.id.grid);

        // Only show the rename action if a single photo is selected.
        if (grid.getCheckedItemCount() == 1) {
            mode.getMenu().findItem(R.id.rename).setVisible(true);
        } else {
            mode.getMenu().findItem(R.id.rename).setVisible(false);
        }

        return;
    }

    /**
     * Handles button presses on the contextual action bar appropriately.
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

        // extract the selected files for easy manipulation
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

                // Create a custom dialog to change the name
                AlertDialog dialog = new AlertDialog.Builder(fragment.getActivity())
                        .setIcon(R.drawable.ic_action_edit)
                        .setTitle(R.string.rename_picture)
                        .setView(text)
                        .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = String.valueOf(text.getText()).trim();
                                files[0].renameTo(new File(files[0].getParentFile(), name));
                                mode.finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();

                // Make sure the keyboard is displayed when the dialog opens
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
                break;
            case R.id.copy:
                try {
                    storage.copy(files);
                    Toast.makeText(fragment.getActivity(), R.string.files_copied, Toast.LENGTH_SHORT).show();
                    mode.finish();
                } catch (IOException e) {
                    Toast.makeText(fragment.getActivity(), R.string.files_not_copied, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.cut:
                try {
                    storage.cut(files);
                    Toast.makeText(fragment.getActivity(), R.string.files_cut, Toast.LENGTH_SHORT).show();
                    mode.finish();
                } catch (IOException e) {
                    Toast.makeText(fragment.getActivity(), R.string.files_not_cut, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.delete:
                //Launch a confirmation dialog.
                new AlertDialog.Builder(fragment.getActivity())
                        .setIcon(R.drawable.ic_action_discard)
                        .setTitle(R.string.confirm_delete)
                        .setMessage(R.string.confirm_delete_message)
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
