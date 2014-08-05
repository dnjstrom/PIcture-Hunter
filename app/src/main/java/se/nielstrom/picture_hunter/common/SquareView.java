package se.nielstrom.picture_hunter.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import se.nielstrom.picture_hunter.R;

/**
 * A forced square view. One can change whether the size is determined by width or height.
 */
public class SquareView extends LinearLayout {
    private boolean sizeByHeight = false;

    public SquareView(Context context) {
        super(context);
    }

    public SquareView(Context context, AttributeSet attrs) {
        super(context, attrs);
        handleAttrs(attrs);
    }

    public SquareView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        handleAttrs(attrs);
    }

    /**
     * Look for xml attributes and set members accordingly.
     *
     * @param attrs
     */
    private void handleAttrs(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SquareView, 0, 0);

        try {
            sizeByHeight = a.getBoolean(R.styleable.SquareView_size_by_height, false);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (sizeByHeight) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        if (sizeByHeight) {
            super.onSizeChanged(h, h, oldw, oldh);
        } else {
            super.onSizeChanged(w, w, oldw, oldh);
        }
    }
}
