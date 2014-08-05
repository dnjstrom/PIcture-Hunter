package se.nielstrom.picture_hunter.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;

import se.nielstrom.picture_hunter.R;

/**
 * A custom layout that implements the checkable interface so that it can be used as the base
 * for a listview with selection. Optionally shows or hides an overlay when becoming checked.
 */
public class CheckableRowLayout extends FrameLayout implements Checkable {
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    private boolean checked = false;
    private View overlay;

    public CheckableRowLayout(Context context) {
        super(context);
    }

    public CheckableRowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableRowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;

        overlay = findViewById(R.id.overlay);

        if (overlay != null) {
            // Show the overlay if checked
            overlay.setVisibility( checked ? VISIBLE : INVISIBLE );
        }

        refreshDrawableState();
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        // Make room for a checked state in the drawable state
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }

        return drawableState;
    }
}
