package se.nielstrom.picture_hunter.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import java.io.File;


public class ThumbnailZoomer {

    private final ValueAnimator animation;

    public ThumbnailZoomer(final View thumbView, final View fullImageView, int duration) {
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();

        thumbView.getGlobalVisibleRect(startBounds);
        fullImageView.getGlobalVisibleRect(finalBounds);

        fullImageView.setVisibility(View.VISIBLE);

        final int finalWidth = fullImageView.getWidth();
        final int finalHeight = fullImageView.getHeight();


        animation = new ValueAnimator().ofFloat(0, 1);
        animation.setDuration(duration);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float value = (Float) valueAnimator.getAnimatedValue();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fullImageView.getLayoutParams();
                params.leftMargin = (int) ((startBounds.left - finalBounds.left) * (1 - value));
                params.topMargin = (int) ((startBounds.top - finalBounds.top) * (1 - value));
                params.width = (int) (thumbView.getWidth() + ((finalWidth-thumbView.getWidth()) * value));
                params.height = (int) (thumbView.getHeight() + ((finalHeight-thumbView.getHeight()) * value));
                fullImageView.setLayoutParams(params);
            }
        });

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }
        });
    }

    public void zoom() {
        animation.start();
    }
}
