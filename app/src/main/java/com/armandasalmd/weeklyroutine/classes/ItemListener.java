package com.armandasalmd.weeklyroutine.classes;

public interface ItemListener {
    void onItemLongClick(int position);
    void onDelete(int position);
    void onEdit(int position);
}
