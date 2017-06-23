package com.abhi.bottomslidingdialog;

import android.graphics.drawable.Drawable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Abhishek on 6/22/2017.
 */

public class BottomSlidingDialog {

    private final LinearLayout mSheetView;
    AppCompatActivity mActivity;
    BottomSheetDialog mBottomSheetDialog;
    ActionListener mActionListener;

    /**
     * Constructor for @{@link BottomSlidingDialog}
     *
     * @param activity Activity Context
     */
    public BottomSlidingDialog(AppCompatActivity activity) {
        mBottomSheetDialog = new BottomSheetDialog(activity);
        mActivity = activity;

        mSheetView
                = (LinearLayout) mActivity
                .getLayoutInflater()
                .inflate(R.layout.botton_sheet_dialog, null);

        mBottomSheetDialog.setContentView(mSheetView);
    }

    public BottomSlidingDialog setDialogTitle(int titleRes) {
        setDialogTitle(mActivity.getString(titleRes));
        return this;
    }

    public BottomSlidingDialog setDialogTitle(String title) {
        TextView titleTextView = ((TextView) mSheetView.findViewById(R.id.dialog_title));
        titleTextView.setText(title);
        titleTextView.setVisibility(View.VISIBLE);
        return this;
    }

    public BottomSlidingDialog setActionListener(ActionListener listener) {
        mActionListener = listener;
        return this;
    }

    public BottomSlidingDialog addAction(int actionTitleStringRes,
                                         int actionIconDrawableRes,
                                         int actionId) {
        addAction(mActivity.getString(actionTitleStringRes),
                mActivity.getDrawable(actionIconDrawableRes),
                actionId);

        return this;
    }

    /**
     * Adds action to the dialog,
     * returns the dialog to implement builder pattern
     *
     * @param actionTitle
     * @param actionIcon
     * @param actionId    this ID is sent when an action is selected
     * @return the same dialog is returned, to implement builder like pattern
     */
    public BottomSlidingDialog addAction(String actionTitle,
                                         Drawable actionIcon,
                                         final int actionId) {

        View actionView = mActivity
                .getLayoutInflater()
                .inflate(R.layout.action_item_layout, mSheetView, false);

        ImageView iv = (ImageView) actionView
                .findViewWithTag(mActivity.getString(R.string.tagActionImage));
        TextView tv = (TextView) actionView
                .findViewWithTag(mActivity.getString(R.string.tagActionText));

        iv.setImageDrawable(actionIcon);
        tv.setText(actionTitle);

        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActionListener != null) mActionListener.onActionSelected(actionId);
                mBottomSheetDialog.dismiss();
            }
        });

        mSheetView
                .addView(actionView);

        return this;
    }

    public void show() {
        mBottomSheetDialog.show();
    }

    public interface ActionListener {
        public void onActionSelected(int actionId);
    }
}
