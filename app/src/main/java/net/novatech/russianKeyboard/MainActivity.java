package net.novatech.russianKeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.novatech.russianKeyboard.android.ImePreferences;
import net.novatech.russianKeyboard.window.AboutWindow;
import net.novatech.russianKeyboard.window.DictionaryWindow;
import net.novatech.russianKeyboard.window.ThemesWindow;

import java.util.List;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout enableSetting = findViewById(R.id.layout_EnableSetting);
        LinearLayout addKeyboards = findViewById(R.id.layout_AddLanguages);
        LinearLayout chooseInputMethod = findViewById(R.id.layout_ChooseInput);
        LinearLayout chooseTheme = findViewById(R.id.layout_ChooseTheme);
        LinearLayout manageDictionaries = findViewById(R.id.layout_ManageDictionary);
        LinearLayout about = findViewById(R.id.layout_about);

        enableSetting.setOnClickListener(this);
        addKeyboards.setOnClickListener(this);
        chooseInputMethod.setOnClickListener(this);
        chooseTheme.setOnClickListener(this);
        manageDictionaries.setOnClickListener(this);
        about.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_EnableSetting:
                startActivityForResult(
                        new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS), 0);
                break;
            case R.id.layout_AddLanguages:
                lunchPreferenceActivity();
                break;
            case R.id.layout_ChooseInput:
                if (isInputEnabled()) {
                    ((InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .showInputMethodPicker();
                } else {
                    Toast.makeText(this, "Please enable keyboard first.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.layout_ChooseTheme:
                startActivity(new Intent(this, ThemesWindow.class));
                break;
            case R.id.layout_ManageDictionary:
                startActivity(new Intent(this, DictionaryWindow.class));
                break;
            case R.id.layout_about:
                startActivity(new Intent(this, AboutWindow.class));
                break;
            default:
                break;
        }
    }


    public boolean isInputEnabled() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();

        final int N = mInputMethodProperties.size();
        boolean isInputEnabled = false;

        for (int i = 0; i < N; i++) {

            InputMethodInfo imi = mInputMethodProperties.get(i);
            Log.d("INPUT ID", String.valueOf(imi.getId()));
            if (imi.getId().contains(getPackageName())) {
                isInputEnabled = true;
            }
        }

        if (isInputEnabled) {
            return true;
        } else {
            return false;
        }
    }

    public void lunchPreferenceActivity() {
        if (isInputEnabled()) {
            Intent intent = new Intent(this, ImePreferences.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Сначала включите клавиатуру.", Toast.LENGTH_SHORT).show();
        }
    }
}