package me.ykrank.s1next.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;

import java.util.List;

import me.ykrank.s1next.R;
import me.ykrank.s1next.databinding.ToolbarSpinnerBinding;
import me.ykrank.s1next.view.fragment.ForumFragment;
import me.ykrank.s1next.view.internal.ToolbarDropDownInterface;
import me.ykrank.s1next.viewmodel.DropDownItemListViewModel;

/**
 * An Activity shows the forum groups.
 * <p>
 * This Activity has Spinner in Toolbar to switch between different forum groups.
 */
public final class ForumActivity extends BaseActivity
        implements ToolbarDropDownInterface.Callback, AdapterView.OnItemSelectedListener {

    /**
     * The serialization (saved instance state) Bundle key representing
     * the position of the selected spinner item.
     */
    private static final String STATE_SPINNER_SELECTED_POSITION = "spinner_selected_position";

    private ToolbarSpinnerBinding mToolbarSpinnerBinding;

    /**
     * Stores selected Spinner position.
     */
    private int mSelectedPosition = 0;

    private ToolbarDropDownInterface.OnItemSelectedListener onItemSelectedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base);

        Fragment fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fragment = new ForumFragment();
            fragmentManager.beginTransaction().add(R.id.frame_layout, fragment, ForumFragment.TAG)
                    .commit();
        } else {
            mSelectedPosition = savedInstanceState.getInt(STATE_SPINNER_SELECTED_POSITION);
            fragment = fragmentManager.findFragmentByTag(ForumFragment.TAG);
        }

        onItemSelectedListener = (ToolbarDropDownInterface.OnItemSelectedListener) fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_SPINNER_SELECTED_POSITION, mSelectedPosition);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mSelectedPosition = position;
        onItemSelectedListener.onToolbarDropDownItemSelected(mSelectedPosition);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void setupToolbarDropDown(List<? extends CharSequence> dropDownItemList) {
        if (mToolbarSpinnerBinding == null) {
            setTitle(null);

            // add Spinner to Toolbar
            mToolbarSpinnerBinding = DataBindingUtil.inflate(getLayoutInflater(),
                    R.layout.toolbar_spinner, getToolbar().get(), true);
            mToolbarSpinnerBinding.spinner.setOnItemSelectedListener(this);
            // let spinner's parent to handle clicking event in order
            // to increase spinner's clicking area.
            mToolbarSpinnerBinding.spinnerContainer.setOnClickListener(v ->
                    mToolbarSpinnerBinding.spinner.performClick());
            mToolbarSpinnerBinding.setDropDownItemListViewModel(new DropDownItemListViewModel());
        }

        DropDownItemListViewModel viewModel = mToolbarSpinnerBinding.getDropDownItemListViewModel();
        viewModel.setSelectedItemPosition(mSelectedPosition);
        viewModel.dropDownItemList.clear();
        viewModel.dropDownItemList.addAll(dropDownItemList);
    }
}
