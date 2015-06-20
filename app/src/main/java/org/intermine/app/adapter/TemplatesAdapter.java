package org.intermine.app.adapter;

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
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.intermine.app.R;
import org.intermine.app.core.templates.Template;
import org.intermine.app.util.Collections;
import org.intermine.app.util.Strs;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Daria Komkova <Daria_Komkova @ hotmail.com>
 */
public class TemplatesAdapter extends BaseAdapter {
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    private ArrayList<Template> mTemplates;

    public TemplatesAdapter(Context ctx) {
        mContext = ctx;
        mLayoutInflater = LayoutInflater.from(ctx);
    }

    public void updateData(Collection<Template> templates) {
        mTemplates = Collections.newArrayList();
        mTemplates.addAll(templates);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (null != mTemplates) {
            return mTemplates.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (null != mTemplates) {
            return mTemplates.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView title, description;

        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.templates_item, parent, false);

            title = (TextView) convertView.findViewById(R.id.template_title);
            description = (TextView) convertView.findViewById(R.id.template_description);

            convertView.setTag(R.id.template_title, title);
            convertView.setTag(R.id.template_description, description);
        } else {
            title = (TextView) convertView.getTag(R.id.template_title);
            description = (TextView) convertView.getTag(R.id.template_description);
        }

        Template template = (Template) getItem(position);

        title.setText(template.getTitle());

        Spanned descriptionText = Html.fromHtml(Strs.nullToEmpty(template.getDescription()));
        description.setText(descriptionText);
        return convertView;
    }
}