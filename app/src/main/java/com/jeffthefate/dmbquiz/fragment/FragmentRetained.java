package com.jeffthefate.dmbquiz.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.jeffthefate.dmbquiz.SavedInstance;

public class FragmentRetained extends Fragment {

    private SavedInstance savedInstance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setSavedInstance(SavedInstance savedInstance) {
        this.savedInstance = savedInstance;
    }

    public SavedInstance getSavedInstance() {
        return savedInstance;
    }
}
