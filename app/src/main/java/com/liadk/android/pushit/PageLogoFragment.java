package com.liadk.android.pushit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.util.UUID;

public class PageLogoFragment extends Fragment {

    private Page mPage;

    private ImageView mImageView;
    private ImageButton mImageButton;
    private TextView mNoLogoTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.set_page_logo);

        UUID id = (UUID) getArguments().getSerializable(ItemFragment.EXTRA_ID);
        mPage = PageCollection.get(getActivity()).getPage(id);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page_logo, container, false);

        // Referencing Widgets
        mImageView = (ImageView) v.findViewById(R.id.pageLogoImageView);
        mImageButton = (ImageButton) v.findViewById(R.id.pageLogoButton);
        mNoLogoTextView = (TextView) v.findViewById(R.id.noLogoTextView);

        // Configuring
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();

                PickImageDialog.build(new PickSetup()
                        .setTitle(getString(R.string.set_push_image))
                        .setSystemDialog(true))
                        .setOnPickResult(new IPickResult() {
                            @Override
                            public void onPickResult(PickResult r) {
                                if(r.getError() == null) {
                                    mPage.setLogoImage(r.getBitmap());
                                    onImageUpdated();
                                }
                            }
                        }).show(fm);
            }
        });

        onImageUpdated();

        return v;
    }

    private void onImageUpdated() {
        if(mPage.getLogoImage() == null) {
            mImageView.setVisibility(View.GONE);
            mNoLogoTextView.setVisibility(View.VISIBLE);
        }
        else {
            mNoLogoTextView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(mPage.getLogoImage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                Intent intent = NavUtils.getParentActivityIntent(getActivity());
                intent.putExtra(PageFragment.EXTRA_ID, mPage.getId());

                NavUtils.navigateUpTo(getActivity(), intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Fragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(PageFragment.EXTRA_ID, id);

        PageLogoFragment fragment = new PageLogoFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
