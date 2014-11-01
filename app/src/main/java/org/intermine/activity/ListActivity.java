package org.intermine.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.intermine.R;
import org.intermine.adapter.ListAdapter;
import org.intermine.controller.LoadOnScrollViewController;
import org.intermine.core.Gene;
import org.intermine.core.ListItems;
import org.intermine.fragment.ApiPager;
import org.intermine.net.request.get.GeneSearchRequest;
import org.intermine.net.request.get.GetListResultsRequest;
import org.intermine.net.request.post.PostListResultsRequest;
import org.intermine.util.Collections;
import org.intermine.util.Strs;
import org.intermine.util.Views;
import org.intermine.view.ProgressView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daria Komkova <Daria_Komkova @ hotmail.com>
 */
public class ListActivity extends BaseActivity {
    public static final String LIST_NAME_KEY = "list_name_key";

    protected ListView mListView;
    private ProgressView mProgressView;
    protected View mNotFoundView;

    private ListAdapter mListAdapter;
    private List<List<String>> mListItems;

    protected LoadOnScrollViewController mViewController;
    private LoadOnScrollViewController.LoadOnScrollDataController mDataController;
    private ApiPager mPager;

    private String mListName;

    protected boolean mLoading;

    // --------------------------------------------------------------------------------------------
    // Static Methods
    // --------------------------------------------------------------------------------------------

    public static void start(Context context, String listName) {
        Intent intent = new Intent(context, ListActivity.class);
        intent.putExtra(LIST_NAME_KEY, listName);
        context.startActivity(intent);
    }

    // --------------------------------------------------------------------------------------------
    // Inner Classes
    // --------------------------------------------------------------------------------------------

    public class ListResultsListener implements RequestListener<ListItems> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            setProgress(false);
            Toast.makeText(ListActivity.this, spiceException.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(ListItems result) {
            // first page load
            if (null == mPager) {
                mListItems.clear();
                mPager = new ApiPager(0, 0, GeneSearchRequest.DEFAULT_SIZE);
            }

            if (0 == mPager.getCurrentPage()) {
                mPager = new ApiPager(mPager.getTotal() + result.size(), 0, GeneSearchRequest.DEFAULT_SIZE);
            }

            if (!Collections.isNullOrEmpty(result)) {
                mListAdapter.updateData(result.getFields(), result);
            }

            setProgress(false);
        }
    }
    // --------------------------------------------------------------------------------------------
    // Fragment Lifecycle
    // --------------------------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        mListName = getIntent().getStringExtra(LIST_NAME_KEY);

        mProgressView = (ProgressView) findViewById(R.id.progress_view);
        mNotFoundView = findViewById(R.id.not_found_results_container);

        mListView = (ListView) findViewById(R.id.list);

        mListItems = new ArrayList<List<String>>();
        mListAdapter = new ListAdapter(this);
        mListView.setAdapter(mListAdapter);

        mViewController = new LoadOnScrollViewController(getDataController(), this);
        mListView.setOnScrollListener(mViewController);
        mListView.addFooterView(mViewController.getFooterView());

        if (!Strs.isNullOrEmpty(mListName)) {
            setProgress(true);
            performGetListResultsRequest(mListName);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected List<Gene> getSelectedGenes() {
        List<Gene> genes = new ArrayList<Gene>();

        SparseBooleanArray checkedItemIds = mListView.getCheckedItemPositions();
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            if (checkedItemIds.get(i)) {
                genes.add((Gene) mListAdapter.getItem(i));
            }
        }
        return genes;
    }

    protected LoadOnScrollViewController.LoadOnScrollDataController getDataController() {
        if (null == mDataController) {
            mDataController = generateDataController();
        }
        return mDataController;
    }


    // --------------------------------------------------------------------------------------------
    // Callbacks
    // --------------------------------------------------------------------------------------------


    // --------------------------------------------------------------------------------------------
    // Helper Methods
    // --------------------------------------------------------------------------------------------

    protected LoadOnScrollViewController.LoadOnScrollDataController generateDataController() {
        return new LoadOnScrollViewController.LoadOnScrollDataController() {

            @Override
            public boolean hasMore() {
                return mPager == null || mPager.hasMorePages();
            }

            @Override
            public boolean isLoading() {
                return mLoading;
            }

            @Override
            public void loadMore() {
                if (mPager == null) {
                    performGetListResultsRequest(mListName);
                } else {
                    mPager = mPager.next();
                    performGetListResultsRequest(mListName);
//                    performGetListResultsRequest("bla", GeneSearchRequest.JSON_FORMAT,
//                            mPager.getCurrentPage() * mPager.getPerPage());
                }
                mViewController.onStartLoad();
                mLoading = true;
            }
        };
    }

    protected void performGetListResultsRequest(String listName) {
        GetListResultsRequest request = new GetListResultsRequest(this, listName);
        executeRequest(request, new ListResultsListener());
    }

    protected void setProgress(boolean loading) {
        mLoading = loading;

        if (loading) {
            Views.setVisible(mProgressView);
            Views.setGone(mListView, mNotFoundView);
        } else {
            Views.setVisible(mListView);
            Views.setGone(mProgressView);
        }
    }
}