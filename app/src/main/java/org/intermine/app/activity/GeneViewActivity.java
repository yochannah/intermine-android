package org.intermine.app.activity;

/*
 * Copyright (C) 2015 InterMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import org.intermine.app.R;
import org.intermine.app.core.Gene;
import org.intermine.app.fragment.GeneViewFragment;
import org.intermine.app.listener.GetListsListener;
import org.intermine.app.net.request.get.GetListsRequest;
import org.intermine.app.util.Collections;
import org.intermine.app.util.Sharing;
import org.intermine.app.util.Strs;
import org.intermine.app.util.Uris;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneViewActivity extends MainActivity implements GeneViewFragment.GeneActionCallbacks {
    private static final String CLASS_PARAM = "class";
    private static final String CLASS_DEFAULT_VALUE = "Gene";
    private static final String EXTERNALIDS = "externalids";
    private Gene mGene;

    private String mGeneFavoritesListName;

    // --------------------------------------------------------------------------------------------
    // Static Methods
    // --------------------------------------------------------------------------------------------

    /**
     * Conventional method for starting activity
     *
     * @param ctx  Context
     * @param gene Gene to show basic info about
     */
    public static void start(Context ctx, Gene gene) {
        Intent intent = new Intent(ctx, GeneViewActivity.class);
        intent.putExtra(GeneViewFragment.GENE_EXTRA, gene);
        ctx.startActivity(intent);
    }

    // --------------------------------------------------------------------------------------------
    // Activity Lifecycle
    // --------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (null != bundle) {
            mGene = bundle.getParcelable(GeneViewFragment.GENE_EXTRA);
        }

        populateContentFragment(GeneViewFragment.newInstance(mGene));
        mGeneFavoritesListName = getString(R.string.gene_favorites_list_name);
    }

    // --------------------------------------------------------------------------------------------
    //  Event Listeners
    // --------------------------------------------------------------------------------------------

    @Override
    public void onGeneAddedToFavorites(Gene gene) {
        String token = getStorage().getUserToken(gene.getMine());

        if (Strs.isNullOrEmpty(token)) {
            Toast.makeText(this, R.string.unauthorized_gene_to_favorites_error_message,
                    Toast.LENGTH_LONG).show();
        } else {
            GetListsRequest request = new GetListsRequest(this, gene.getMine(),
                    mGeneFavoritesListName);
            List<Gene> genes = Collections.newArrayList();
            genes.add(gene);
            execute(request, new GetListsListener(this, gene.getMine(), genes));
            Toast.makeText(this, R.string.gene_added_to_favorites, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGeneSelectedToBeShared(Gene gene) {
        shareText();
    }

    @Override
    public void onShowGeneReport(Gene gene) {
        String geneReportUrl = generateGeneReportUrl(gene);

        if (!Strs.isNullOrEmpty(geneReportUrl)) {
            String title = "Gene Report";

            if (!Strs.isNullOrEmpty(gene.getSymbol())) {
                title = "Gene " + gene.getSymbol() + " Report";
            }
            WebActivity.start(this, title, geneReportUrl);
        }
    }

    // --------------------------------------------------------------------------------------------
    // Helper Methods
    // --------------------------------------------------------------------------------------------

    protected void shareText() {
        Intent intent = Sharing.generateIntentToSendText(mGene);

        PackageManager manager = getPackageManager();
        List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);

        if (infos.size() > 0) {
            startActivity(Intent.createChooser(intent,
                    getResources().getString(R.string.share_message)));
        } else {
            Toast.makeText(this, "No messenger installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateGeneReportUrl(Gene gene) {
        if (!Strs.isNullOrEmpty(gene.getMine())) {
            String webAppUrl = mStorage.getMineWebAppUrl(gene.getMine());

            if (!Strs.isNullOrEmpty(webAppUrl)) {
                Map<String, String> params = new HashMap<>();
                params.put(CLASS_PARAM, CLASS_DEFAULT_VALUE);
                params.put(EXTERNALIDS, gene.getPrimaryDBId());
                return Uris.expandQuery(webAppUrl + getString(R.string.report_path), params);
            }
        }
        return Strs.EMPTY_STRING;
    }
}