package karsai.laszlo.bringcloser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.futuremind.recyclerviewfastscroll.FastScroller;

/**
 * FastScroller is under the following copyright:
 * Copyright 2015 Future Mind

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 * This class has been created to handle recyclerview when it is null!
 * No other changes were made compared to the product of the above copyright.
 */
public class CustomFastScroller extends FastScroller {

    private RecyclerView mRecyclerView;

    public CustomFastScroller(Context context) {
        super(context);
    }

    public CustomFastScroller(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mRecyclerView != null) {
            super.onLayout(changed, l, t, r, b);
        }
    }

    @Override
    public void setRecyclerView(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        super.setRecyclerView(recyclerView);
        requestLayout();
    }
}
