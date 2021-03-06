package me.ykrank.s1next.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Browser;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.view.View;

import javax.inject.Inject;

import me.ykrank.s1next.App;
import me.ykrank.s1next.R;
import me.ykrank.s1next.data.api.model.Thread;
import me.ykrank.s1next.data.api.model.ThreadLink;
import me.ykrank.s1next.data.db.ReadProgressDbWrapper;
import me.ykrank.s1next.data.db.dbmodel.ReadProgress;
import me.ykrank.s1next.data.pref.ReadProgressPreferencesManager;
import me.ykrank.s1next.util.OnceClickUtil;
import me.ykrank.s1next.view.fragment.PostListFragment;
import me.ykrank.s1next.widget.WifiBroadcastReceiver;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * An Activity which includes {@link android.support.v4.view.ViewPager}
 * to represent each page of post lists.
 */
public final class PostListActivity extends BaseActivity
        implements WifiBroadcastReceiver.NeedMonitorWifi {
    public static final int RESULT_BLACKLIST = 11;

    private static final String ARG_THREAD = "thread";
    private static final String ARG_SHOULD_GO_TO_LAST_PAGE = "should_go_to_last_page";

    private static final String ARG_THREAD_LINK = "thread_link";
    private static final String ARG_COME_FROM_OTHER_APP = "come_from_other_app";

    private static final String ARG_READ_PROGRESS = "read_progress";

    @Inject
    ReadProgressPreferencesManager mReadProgressPrefManager;

    public static void startPostListActivity(Context context, Thread thread, boolean shouldGoToLastPage) {
        Intent intent = new Intent(context, PostListActivity.class);
        intent.putExtra(ARG_THREAD, thread);
        intent.putExtra(ARG_SHOULD_GO_TO_LAST_PAGE, shouldGoToLastPage);

        if (context instanceof Activity)
            ((Activity)context).startActivityForResult(intent, RESULT_BLACKLIST);
        else context.startActivity(intent);
    }

    public static void startPostListActivity(Activity activity, ThreadLink threadLink) {
        startPostListActivity(activity, threadLink, !activity.getPackageName().equals(
                // see android.text.style.URLSpan#onClick(View)
                activity.getIntent().getStringExtra(Browser.EXTRA_APPLICATION_ID)));
    }

    public static void startPostListActivity(Context context, ThreadLink threadLink, boolean comeFromOtherApp) {
        Intent intent = new Intent(context, PostListActivity.class);
        intent.putExtra(ARG_THREAD_LINK, threadLink);
        intent.putExtra(ARG_COME_FROM_OTHER_APP, comeFromOtherApp);

        context.startActivity(intent);
    }

    /**
     * 点击打开有读取进度的帖子
     *
     * @param view     点击焦点
     * @param thread   帖子信息
     * @return
     */
    public static Subscription clickStartPostListActivity(@NonNull View view, @NonNull Thread thread) {
        ReadProgressPreferencesManager preferencesManager = App.getAppComponent(view.getContext()).getReadProgressPreferencesManager();
        if (preferencesManager.isLoadAuto()){
            return OnceClickUtil.onceClickObservable(view, 1000)
                    .observeOn(Schedulers.io())
                    .map(vo -> ReadProgressDbWrapper.getInstance().getWithThreadId(thread.getId()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(progress -> {
                        Context context = view.getContext();
                        Intent intent = new Intent(context, PostListActivity.class);
                        intent.putExtra(ARG_THREAD, thread);
                        intent.putExtra(ARG_READ_PROGRESS, progress);
                        if (context instanceof Activity)
                            ((Activity)context).startActivityForResult(intent, RESULT_BLACKLIST);
                        else context.startActivity(intent);
                    }, e -> e.printStackTrace());
        }else{
            return OnceClickUtil.setOnceClickLister(view, v->{
                PostListActivity.startPostListActivity(v.getContext(), thread, false);
            });
        }
        
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        disableDrawerIndicator();

        if (savedInstanceState == null) {
            Fragment fragment;
            Intent intent = getIntent();
            Thread thread = intent.getParcelableExtra(ARG_THREAD);
            ReadProgress progress = intent.getParcelableExtra(ARG_READ_PROGRESS);
            if (thread == null) {//通过链接打开
                fragment = PostListFragment.newInstance(intent.getParcelableExtra(ARG_THREAD_LINK));
            } else if (progress != null){//有进度信息
                fragment = PostListFragment.newInstance(thread, progress);
            } else {//没有进度信息
                fragment = PostListFragment.newInstance(thread, intent.getBooleanExtra(
                        ARG_SHOULD_GO_TO_LAST_PAGE, false));
            }
            getSupportFragmentManager().beginTransaction().add(R.id.frame_layout, fragment,
                    PostListFragment.TAG).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getIntent().getBooleanExtra(ARG_COME_FROM_OTHER_APP, false)) {
                    // this activity is not part of this app's task
                    // so create a new task when navigating up
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(new Intent(this, ForumActivity.class))
                            .startActivities();
                    finish();

                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
