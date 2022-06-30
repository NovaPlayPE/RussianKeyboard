package net.novatech.russianKeyboard.window;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.novatech.russianKeyboard.R;

public class AboutWindow extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView aboutTextView = findViewById(R.id.about_textView);
        aboutTextView.setMovementMethod(new ScrollingMovementMethod());
    }
}