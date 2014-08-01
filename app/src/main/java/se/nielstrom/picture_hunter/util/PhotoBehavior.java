package se.nielstrom.picture_hunter.util;

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

public abstract class PhotoBehavior extends InteractionBehavior {

    public PhotoBehavior(Fragment fragment) {
        super(fragment);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }


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
            case R.id.copy:
                try {
                    storage.copy(files);
                    Toast.makeText(fragment.getActivity(), "Files have been copied", Toast.LENGTH_SHORT).show();
                    mode.finish();
                } catch (IOException e) {
                    Toast.makeText(fragment.getActivity(), "Couldn't copy the files", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.cut:
                try {
                    storage.cut(files);
                    Toast.makeText(fragment.getActivity(), "Files have been cut", Toast.LENGTH_SHORT).show();
                    mode.finish();
                } catch (IOException e) {
                    Toast.makeText(fragment.getActivity(), "Couldn't cut the files", Toast.LENGTH_LONG).show();
                }
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
    public void onDestroyActionMode(ActionMode mode) {
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        GridView grid = (GridView) fragment.getView().findViewById(R.id.grid);

        if (grid.getCheckedItemCount() == 1) {
            mode.getMenu().findItem(R.id.rename).setVisible(true);
        } else {
            mode.getMenu().findItem(R.id.rename).setVisible(false);
        }

        return;
    }
}
