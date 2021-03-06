package me.ykrank.s1next.view.adapter;

import android.app.Activity;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.google.common.base.Preconditions;
import com.hannesdorfmann.adapterdelegates.AdapterDelegate;
import com.hannesdorfmann.adapterdelegates.AdapterDelegatesManager;

import java.util.ArrayList;
import java.util.List;

import me.ykrank.s1next.view.adapter.delegate.ProgressAdapterDelegate;
import me.ykrank.s1next.view.adapter.item.ProgressItem;

public abstract class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_PROGRESS = 0;

    private List<Object> mList;
    private final AdapterDelegatesManager<List<Object>> mAdapterDelegatesManager;

    BaseRecyclerViewAdapter(Activity activity) {
        mList = new ArrayList<>();
        mAdapterDelegatesManager = new AdapterDelegatesManager<>();
        mAdapterDelegatesManager.addDelegate(new ProgressAdapterDelegate(activity,
                VIEW_TYPE_PROGRESS));
    }

    @Override
    @CallSuper
    public int getItemViewType(int position) {
        return mAdapterDelegatesManager.getItemViewType(mList, position);
    }

    @Override
    @CallSuper
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mAdapterDelegatesManager.onCreateViewHolder(parent, viewType);
    }

    @Override
    @CallSuper
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mAdapterDelegatesManager.onBindViewHolder(mList, position, holder);
    }

    @Override
    @CallSuper
    public int getItemCount() {
        return mList.size();
    }

    @Override
    @CallSuper
    public long getItemId(int position) {
        Preconditions.checkArgument(mAdapterDelegatesManager.getItemViewType(mList, position)
                == VIEW_TYPE_PROGRESS);
        return Integer.MIN_VALUE;
    }

    final void addAdapterDelegate(AdapterDelegate<List<Object>> adapterDelegate) {
        Preconditions.checkArgument(adapterDelegate.getItemViewType() != VIEW_TYPE_PROGRESS);
        mAdapterDelegatesManager.addDelegate(adapterDelegate);
    }

    final int getItemViewTypeFromDelegatesManager(int position) {
        return mAdapterDelegatesManager.getItemViewType(mList, position);
    }

    public final void setHasProgress(boolean hasProgress) {
        if (hasProgress) {
            Preconditions.checkState(mList.size() == 0);
            mList.add(new ProgressItem());
        } else {
            // we do not need to clear list if we have already changed
            // data set or we have no ProgressItem to been cleared
            if (mList.size() == 1 && mList.get(0) instanceof ProgressItem) {
                mList.clear();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public final void setDataSet(List<?> list) {
        mList = (List<Object>) list;
    }

    public final List<Object> getDataSet() {
        return mList;
    }

    final Object getItem(int position) {
        return mList.get(position);
    }

    final void addItem(Object object) {
        mList.add(object);
    }

    final void removeItem(int position) {
        mList.remove(position);
    }
}
