package com.inpen.shuffle.playlist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.inpen.shuffle.R;
import com.inpen.shuffle.model.database.MediaContract.PlaylistsEntry;
import com.inpen.shuffle.model.repositories.SongsRepository;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Abhishek on 6/24/2017.
 */

public class AddToPlaylistDialogFragment extends DialogFragment {

    private static final String EXTRA_STRING_SONG_ID = "song_id";
    private String mSongId;
    private AlertDialog mAlertDialog;
    private ArrayList<String> mPlaylists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSongId = getArguments().getString(EXTRA_STRING_SONG_ID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mPlaylists = new ArrayList<>();

        final ArrayAdapter adapter
                = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,
                mPlaylists);

        new AsyncTask<Void, Void, HashSet<String>>() {
            @Override
            protected HashSet<String> doInBackground(Void... voids) {

                Cursor c = getActivity()
                        .getContentResolver()
                        .query(PlaylistsEntry.CONTENT_URI,
                                new String[]{PlaylistsEntry.COLUMN_PLAYLIST_NAME},
                                null, null, null);

                HashSet<String> valueSet = new HashSet<>();

                if (c != null && c.moveToFirst()) {
                    do {
                        valueSet.add(c.getString(0));
                    } while (c.moveToNext());
                }

                if (c != null)
                    c.close();
                return valueSet;
            }

            @Override
            protected void onPostExecute(HashSet<String> valueSet) {
                super.onPostExecute(valueSet);
                mPlaylists.addAll(valueSet);
                adapter.notifyDataSetChanged();
            }
        }.execute();

        int padding = (int) getResources().getDimension(R.dimen.dialog_horizontal_margin);
        int margin = (int) getResources().getDimension(R.dimen.dialog_horizontal_margin);

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setSingleLine();
        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(margin, 0, margin, 0);
        input.setLayoutParams(params);
        input.setTextSize(getResources().getDimension(R.dimen.dialog_text_size));
        container.addView(input);
        input.setHint(getString(R.string.create_new_playlist));
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.add_to_playlist))
                .setView(container)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ArrayList<String> songIdList = new ArrayList<>(1);
                        songIdList.add(mSongId);

                        // Add to chosen playlist
                        new SongsRepository(getActivity())
                                .addSongsToPlaylist(songIdList,
                                        mPlaylists.get(i));
                    }
                })
                .setPositiveButton(getString(R.string.create_new_playlist),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ArrayList<String> songIdList = new ArrayList<>(1);
                                songIdList.add(mSongId);
                                // show dialog box to add new playlist,
                                // add the song to that playlist
                                if (!input.getText().toString().isEmpty())
                                    new SongsRepository(getActivity())
                                            .addSongsToPlaylist(songIdList,
                                                    input.getText().toString());
                            }
                        }
                )
                .create();
        return mAlertDialog;
    }

    public static class Builder {
        private static final String FRAGMENT_TAG = "ATPDFrag_tag";
        AddToPlaylistDialogFragment mFragment;

        /**
         * @param songId the id created by app, not the device id
         */
        public Builder(String songId) {
            mFragment = new AddToPlaylistDialogFragment();
            Bundle args = new Bundle();
            args.putString(EXTRA_STRING_SONG_ID, songId);
            mFragment.setArguments(args);
        }

        public void show(FragmentManager fm) {
            // DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            FragmentTransaction ft = fm.beginTransaction();
            Fragment prev = fm.findFragmentByTag(FRAGMENT_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            // Create and show the dialog.
            mFragment.show(ft, FRAGMENT_TAG);
        }
    }
}
