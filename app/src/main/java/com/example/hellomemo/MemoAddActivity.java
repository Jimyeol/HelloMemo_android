package com.example.hellomemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class MemoAddActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addmemo);
    }

    public static class LineEditText extends AppCompatEditText {
        // we need this constructor for LayoutInflater
        public LineEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }
}
