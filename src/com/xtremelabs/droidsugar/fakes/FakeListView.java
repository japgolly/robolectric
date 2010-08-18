package com.xtremelabs.droidsugar.fakes;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ListView.class)
public class FakeListView extends FakeAdapterView {
    public boolean itemsCanFocus;
    private ListView realListView;

    public FakeListView(ListView listView) {
        super(listView);
        this.realListView = listView;
    }

    public void setItemsCanFocus(boolean itemsCanFocus) {
        this.itemsCanFocus = itemsCanFocus;
    }

    public boolean performItemClick(View view, int position, long id) {
        AdapterView.OnItemClickListener onItemClickListener = getOnItemClickListener();
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(realListView, view, position, id);
            return true;
        }
        return false;
    }

    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
    }

    public boolean performItemClick(int position) {
        return realListView.performItemClick(realListView.getChildAt(position), position, realListView.getItemIdAtPosition(position));
    }
}
