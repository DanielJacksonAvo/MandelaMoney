package com.example.mandelamoney.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.example.mandelamoney.R;

import java.util.WeakHashMap;

public final class ErrorBorder {
    private static final WeakHashMap<TextView, Drawable> ORIGINALS = new WeakHashMap<>();

    private ErrorBorder() {}

    public static void applyMandelaYellowBorder(TextView view) {
        if (!ORIGINALS.containsKey(view)) {
            ORIGINALS.put(view, view.getBackground());
        }

        Drawable base = ORIGINALS.get(view);
        Drawable stroke = makeStroke(
                view.getContext(),
                R.color.mandelaYellow,
                2,
                20f
        );

        LayerDrawable layered = new LayerDrawable(new Drawable[]{ base, stroke });
        layered.setPaddingMode(LayerDrawable.PADDING_MODE_NEST);
        view.setBackground(layered);
    }

    public static void removeStroke(TextView view) {
        Drawable original = ORIGINALS.get(view);
        if (original != null) {
            view.setBackground(original);
        }
    }

    private static Drawable makeStroke(Context ctx, @ColorRes int colorRes, int strokeDp, float cornerRadiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(Color.TRANSPARENT);
        d.setStroke(dpToPx(ctx, strokeDp), ContextCompat.getColor(ctx, colorRes));
        d.setCornerRadius(dpToPxF(ctx, cornerRadiusDp));
        return d;
    }

    private static int dpToPx(Context ctx, int dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density);
    }

    private static float dpToPxF(Context ctx, float dp) {
        return dp * ctx.getResources().getDisplayMetrics().density;
    }
}
