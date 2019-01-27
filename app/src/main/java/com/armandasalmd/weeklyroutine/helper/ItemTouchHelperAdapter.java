package com.armandasalmd.weeklyroutine.helper;

/**
 * Created by Armandas on 2017-09-20.
 */

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
