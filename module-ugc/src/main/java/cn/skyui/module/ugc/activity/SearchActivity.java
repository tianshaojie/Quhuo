package cn.skyui.module.ugc.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chenenyu.router.annotation.Route;
import cn.skyui.library.base.activity.BaseSwipeBackActivity;
import cn.skyui.library.data.constant.Constants;
import cn.skyui.library.data.constant.ImageConstants;
import cn.skyui.library.data.model.User;
import cn.skyui.library.data.model.UserVO;
import cn.skyui.library.glide.GlideApp;
import cn.skyui.library.glide.GlideCircleTransform;
import cn.skyui.library.http.HttpObserver;
import cn.skyui.library.http.RetrofitFactory;
import cn.skyui.library.http.RxSchedulers;
import cn.skyui.library.http.exception.ApiException;
import cn.skyui.module.ugc.R;
import cn.skyui.module.ugc.data.ApiService;
import cn.skyui.module.ugc.helper.UiHelper;

import java.util.List;

/**
 * Created by tiansj on 2018/5/5.
 */
@Route("search")
public class SearchActivity extends BaseSwipeBackActivity {

    private RecyclerView mRecyclerView;
    private BaseQuickAdapter mAdapter;
    private TextView mTextEmptyTips;

    private Integer pageNum;
    private String keyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initData();
        initView();
        loadData();
    }

    private void initData() {
        pageNum = 0;
        keyword = "";
    }

    private void initView() {
        mTextEmptyTips = findViewById(R.id.textEmptyTips);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        mAdapter = new BaseQuickAdapter<UserVO, BaseViewHolder>(R.layout.search_list_item, null) {
            @Override
            protected void convert(BaseViewHolder helper, UserVO item) {
                ImageView imgAvatar = helper.getView(R.id.imgAvatar);
                GlideApp.with(mContext).load(ImageConstants.getSmallUrl(item.getAvatar()))
                        .apply(new RequestOptions().transform(new GlideCircleTransform(getResources().getColor(R.color.gray_light), 2)))
                        .into(imgAvatar);
                helper.setText(R.id.textNickname, item.getNickname());
                helper.setText(R.id.textId, "ID: " + item.getId());
            }
        };
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            UserVO user = (UserVO) adapter.getItem(position);
            if(user != null) {
                UiHelper.showUserActivity(mActivity, user.getId(), user.getNickname(), user.getAvatar());
            }
        });
        mAdapter.setOnLoadMoreListener(this::loadData, mRecyclerView);
        mAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
//        mAdapter.setEmptyView(View.inflate(mActivity,R.layout.layout_search_empty, null));

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem mSearch = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) mSearch.getActionView();
        searchView.setQueryHint("输入用户昵称或ID");
        searchView.onActionViewExpanded();

        LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
        LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
        LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
        autoComplete.setTextSize(16);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query != null && query.length() > 0) {
                    keyword = query;
                    pageNum = 1;
                    mAdapter.getData().clear();
                    loadData();
                }
                mRecyclerView.setVisibility(View.VISIBLE);
                mTextEmptyTips.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText == null || newText.length() == 0) {
                    pageNum = 1;
                    mAdapter.getData().clear();
                    mAdapter.notifyDataSetChanged();
                    mTextEmptyTips.setVisibility(View.GONE);
                }
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void loadData() {
        RetrofitFactory.createService(ApiService.class)
                .search(pageNum, Constants.DEFAULT_PAGE_SIZE, User.getInstance().userId, keyword)
                .compose(RxSchedulers.io2main())
                .subscribe(new HttpObserver<List<UserVO>>() {
                    @Override
                    protected void onSuccess(List<UserVO> list) {
                        if(pageNum == 1 && list.isEmpty()) {
                            mTextEmptyTips.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.GONE);
                        } else {
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mTextEmptyTips.setVisibility(View.GONE);
                        }

                        if(list.size() < Constants.DEFAULT_PAGE_SIZE) {
                            mAdapter.loadMoreEnd(true);
                        }  else {
                            mAdapter.loadMoreComplete();
                        }
                        if (pageNum == 1) {
                            mAdapter.setNewData(list);
                        } else {
                            mAdapter.addData(list);
                        }
                        pageNum++;
                    }

                    @Override
                    protected void onFailure(ApiException e) {
                        super.onFailure(e);
                        mAdapter.loadMoreFail();
                    }
                });
    }
}
