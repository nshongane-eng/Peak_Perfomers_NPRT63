package com.peakperformers.smartloadshedding.utils;

import android.view.View;
import android.widget.TextView;

import com.peakperformers.smartloadshedding.R;

public class EmptyStateHelper {

    /**
     * Configures the <include>d empty-state layout's text, then shows or
     * hides it (and the RecyclerView it sits on top of) based on isEmpty.
     */
    public static void apply(View emptyStateRoot, View recyclerView, boolean isEmpty,
                              String icon, String title, String subtitle) {
        if (isEmpty) {
            TextView tvIcon = emptyStateRoot.findViewById(R.id.tvEmptyIcon);
            TextView tvTitle = emptyStateRoot.findViewById(R.id.tvEmptyTitle);
            TextView tvSubtitle = emptyStateRoot.findViewById(R.id.tvEmptySubtitle);
            tvIcon.setText(icon);
            tvTitle.setText(title);
            tvSubtitle.setText(subtitle);

            emptyStateRoot.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateRoot.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
