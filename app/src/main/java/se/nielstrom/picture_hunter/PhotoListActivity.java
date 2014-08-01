package se.nielstrom.picture_hunter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
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

import se.nielstrom.picture_hunter.fragments.CameraFragment;
import se.nielstrom.picture_hunter.fragments.PhotoListFragment;
import se.nielstrom.picture_hunter.util.FileAdapter;
import se.nielstrom.picture_hunter.util.FoldersPagerAdapter;
import se.nielstrom.picture_hunter.util.InteractionBehavior;
import se.nielstrom.picture_hunter.util.Storage;

public class PhotoListActivity extends FragmentActivity {

    public static final String KEY_PATH = "KEY_REF_IMAGE_PATH";
    public static final String KEY_POSITION = "KEY_POSITION";

    private String path;
    private File location;
    private ViewPager pager;
    private FoldersPagerAdapter adapter;
    private Storage storage;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);

        storage = Storage.getInstance(this);

        Bundle extras = getIntent().getExtras();
        path = extras.getString(KEY_PATH);
        int startPosition = extras.getInt(KEY_POSITION);
        location = new File(path);

        pager = (ViewPager) findViewById(R.id.pager);

        adapter = new PhotoListAdapter(getSupportFragmentManager(), location);

        pager.setAdapter(adapter);
        pager.setCurrentItem(startPosition);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo_list, menu);
        this.menu = menu;

        menu.findItem(R.id.paste).setVisible(storage.getClipboardCount() > 0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.paste) {
            storage.pasteTo(getCurrentFolder());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private File getCurrentFolder() {
        return adapter.getFolder(pager.getCurrentItem());
    }


    private class PhotoListAdapter extends FoldersPagerAdapter {
        public PhotoListAdapter(FragmentManager fm, File location) {
            super(fm, location);
        }

        @Override
        public Fragment getItem(int position) {
            PhotoListFragment fragment = PhotoListFragment.newInstance(folders.get(position).getAbsolutePath());

            if (storage.isUserFile(location)) {
                return fragment.setBehavior(new UserFileBehavior(fragment));
            } else {
                return fragment.setBehavior(new ForeignFileBehavior(fragment));
            }
        }
    }


    private class UserFileBehavior extends InteractionBehavior {
        public UserFileBehavior(Fragment fragment) {
            super(fragment);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = (File) adapterView.getItemAtPosition(position);
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            startActivity(intent);
        }

        @Override
        public void onClick(View view) {
            File image = storage.createImageFileAt(getCurrentFolder());
            CameraFragment fragment = CameraFragment.newInstance(image.getAbsolutePath());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.grid_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        }


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.context_actions, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            GridView grid = (GridView) fragment.getView().findViewById(R.id.photo_grid);
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
                    final EditText text = new EditText(PhotoListActivity.this);
                    text.setSingleLine();
                    text.setText(files[0].getName());
                    text.setSelectAllOnFocus(true);

                    AlertDialog dialog = new AlertDialog.Builder(PhotoListActivity.this)
                            .setIcon(R.drawable.ic_action_edit)
                            .setTitle("Rename picture")
                            .setView(text)
                            .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String name = String.valueOf(text.getText());
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
                        Toast.makeText(PhotoListActivity.this, "Files have been copied", Toast.LENGTH_SHORT).show();
                        mode.finish();
                        MenuItem paste = menu.findItem(R.id.paste);
                        paste.setVisible(storage.getClipboardCount() > 0);
                    } catch (IOException e) {
                        Toast.makeText(PhotoListActivity.this, "Couldn't copy the files", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.cut:
                    try {
                        storage.cut(files);
                        Toast.makeText(PhotoListActivity.this, "Files have been cut", Toast.LENGTH_SHORT).show();
                        mode.finish();
                        MenuItem paste = menu.findItem(R.id.paste);
                        paste.setVisible(storage.getClipboardCount() > 0);
                    } catch (IOException e) {
                        Toast.makeText(PhotoListActivity.this, "Couldn't cut the files", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.delete:
                    new AlertDialog.Builder(PhotoListActivity.this)
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
            GridView grid = (GridView) fragment.getView().findViewById(R.id.photo_grid);

            if (grid.getCheckedItemCount() == 1) {
                mode.getMenu().findItem(R.id.rename).setVisible(true);
            } else {
                mode.getMenu().findItem(R.id.rename).setVisible(false);
            }

            return;
        }
    }


    private class ForeignFileBehavior extends InteractionBehavior {
        public ForeignFileBehavior(Fragment fragment) {
            super(fragment);
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            File file = (File) adapterView.getItemAtPosition(position);
            Intent intent = new Intent(PhotoListActivity.this, ComparisonActivity.class);
            intent.putExtra(ComparisonActivity.KEY_REF_IMAGE_PATH, file.getAbsolutePath());
            startActivity(intent);
        }
    }
}
