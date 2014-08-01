package se.nielstrom.picture_hunter.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;

import se.nielstrom.picture_hunter.R;

/**
 * Created by Daniel on 7/31/2014.
 */
public class CheckableRowLayout extends FrameLayout implements Checkable {
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    private boolean checked = false;
    private Context context;
    private View overlay;

    public CheckableRowLayout(Context context) {
        super(context);
        init(context);
    }

    public CheckableRowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CheckableRowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;

        overlay = findViewById(R.id.overlay);

        if (overlay != null) {
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
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }

        return drawableState;
    }
}
