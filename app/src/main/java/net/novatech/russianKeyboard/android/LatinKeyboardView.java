package net.novatech.russianKeyboard.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import net.novatech.russianKeyboard.inputMethod.Keyboard;
import net.novatech.russianKeyboard.inputMethod.Keyboard.Key;
import net.novatech.russianKeyboard.inputMethod.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.InputMethodSubtype;

import net.novatech.russianKeyboard.R;

import java.util.List;
import java.util.Objects;


public class LatinKeyboardView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;
    static final int KEYCODE_LANGUAGE_SWITCH = -101;

    public LatinKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LatinKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected boolean onLongPress(Key key) {

        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        } else if (key.codes[0] == '0') {
            getOnKeyboardActionListener().onKey('+', null);
            return true;
        } else if (key.codes[0] == 'q' || key.codes[0] == 'Q' || key.codes[0] == '۱') {
            getOnKeyboardActionListener().onKey('1', null);
            return true;
        } else if (((key.codes[0] == 'w' || key.codes[0] == 'W') && SoftKeyboard.mActiveKeyboard == "en_US") || key.codes[0] == '۲') {
            getOnKeyboardActionListener().onKey('2', null);
            return true;
        } else if (key.codes[0] == 'e' || key.codes[0] == 'E' || key.codes[0] == '۳') {
            getOnKeyboardActionListener().onKey('3', null);
            return true;
        } else if (((key.codes[0] == 'r' || key.codes[0] == 'R') && SoftKeyboard.mActiveKeyboard == "en_US") || key.codes[0] == '۴') {
            getOnKeyboardActionListener().onKey('4', null);
            return true;
        } else if (((key.codes[0] == 't' || key.codes[0] == 'T') && SoftKeyboard.mActiveKeyboard == "en_US") || key.codes[0] == '۵') {
            getOnKeyboardActionListener().onKey('5', null);
            return true;
        } else if (key.codes[0] == 'y' || key.codes[0] == 'Y' || key.codes[0] == '۶') {
            getOnKeyboardActionListener().onKey('6', null);
            return true;
        } else if (key.codes[0] == 'u' || key.codes[0] == 'U' || key.codes[0] == '۷') {
            getOnKeyboardActionListener().onKey('7', null);
            return true;
        } else if (key.codes[0] == 'i' || key.codes[0] == 'I' || key.codes[0] == '۸') {
            getOnKeyboardActionListener().onKey('8', null);
            return true;
        } else if (key.codes[0] == 'o' || key.codes[0] == 'O' || key.codes[0] == '۹') {
            getOnKeyboardActionListener().onKey('9', null);
            return true;
        } else if (key.codes[0] == 'p' || key.codes[0] == 'P' || key.codes[0] == '۰') {
            getOnKeyboardActionListener().onKey('0', null);
            return true;
        } else if ((key.codes[0] == 'S' || key.codes[0] == 's') && SoftKeyboard.mActiveKeyboard == "en_US") {
            getOnKeyboardActionListener().onKey('ϐ', null);
            return true;

            // For Latin Keys
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && (key.codes[0] == 'w' || key.codes[0] == 'W')) {
            getOnKeyboardActionListener().onKey('1', null);
            return true;
        } else if (key.codes[0] == 'ə' || key.codes[0] == 'Ə') {
            getOnKeyboardActionListener().onKey('2', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 's') {
            getOnKeyboardActionListener().onKey('š', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'd') {
            getOnKeyboardActionListener().onKey('ḍ', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'z') {
            getOnKeyboardActionListener().onKey('ž', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'c') {
            getOnKeyboardActionListener().onKey('č', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'n') {
            getOnKeyboardActionListener().onKey('ṇ', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'g') {
            getOnKeyboardActionListener().onKey('ǧ', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'l') {
            getOnKeyboardActionListener().onKey('ɣ', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'j') {
            getOnKeyboardActionListener().onKey('ǰ', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'S') {
            getOnKeyboardActionListener().onKey('Š', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'D') {
            getOnKeyboardActionListener().onKey('Ḍ', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'Z') {
            getOnKeyboardActionListener().onKey('Ž', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'C') {
            getOnKeyboardActionListener().onKey('Č', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'N') {
            getOnKeyboardActionListener().onKey('Ṇ', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'G') {
            getOnKeyboardActionListener().onKey('Ǧ', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'L') {
            getOnKeyboardActionListener().onKey('Ɣ', null);
            return true;
        } else if (SoftKeyboard.mActiveKeyboard != "en_US" && key.codes[0] == 'J') {
            getOnKeyboardActionListener().onKey('ǰ', null);
            return true;
        } else {
            return super.onLongPress(key);
        }
    }

    void setSubtypeOnSpaceKey(final InputMethodSubtype subtype) {
        final LatinKeyboard keyboard = (LatinKeyboard) getKeyboard();
        invalidateAllKeys();
    }
    Paint paint = new Paint();


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(getResources().getDimension(R.dimen.canvasTextSize));
        int keyYLocation = getResources().getDimensionPixelSize(R.dimen.canvasKeyY);
        paint.setColor(getResources().getColor(R.color.white));
        List<Key> keys = getKeyboard().getKeys();
        for (Key key : keys) {
            if (key.label != null) {

                if (key.label.toString().equals("q") || key.label.toString().equals("Q") || key.label.toString().equals("۱"))
                    canvas.drawText(String.valueOf(1), key.x + (key.width / 2) + 10, key.y + keyYLocation, paint);

                else if (((key.codes[0] == 'w' || key.codes[0] == 'W') && SoftKeyboard.mActiveKeyboard == "en_US") || key.label.toString().equals("۲"))
                    canvas.drawText(String.valueOf(2), key.x + (key.width / 2) + 10, key.y + keyYLocation, paint);

                else if (key.label.toString().equals("e") || key.label.toString().equals("E") || key.label.toString().equals("۳"))
                    canvas.drawText(String.valueOf(3), key.x + (key.width / 2) + 10, key.y + keyYLocation, paint);

                else if (key.label.toString().equals("r") || key.label.toString().equals("R") || key.label.toString().equals("۴"))
                    canvas.drawText(String.valueOf(4), key.x + (key.width / 2) + 10, key.y + keyYLocation, paint);

                else if (key.label.toString().equals("t") || key.label.toString().equals("T") || key.label.toString().equals("۵"))
                    canvas.drawText(String.valueOf(5), key.x + (key.width / 2) + 10, key.y + keyYLocation, paint);

                else if (key.label.toString().equals("y") || key.label.toString().equals("Y") || key.label.toString().equals("۶"))
                    canvas.drawText(String.valueOf(6), key.x + (key.width / 2) + 10, key.y + keyYLocation, paint);

                else if (key.label.toString().equals("u") || key.label.toString().equals("U") || key.label.toString().equals("۷"))
                    canvas.drawText(String.valueOf(7), key.x + (key.width / 2) + 10, key.y + keyYLocation, paint);

                else if (key.label.toString().equals("i") || key.label.toString().equals("I") || key.label.toString().equals("۸"))
                    canvas.drawText(String.valueOf(8), key.x + (key.width / 2) + 10, key.y + keyYLocation, paint);

                else if (key.label.toString().equals("o") || key.label.toString().equals("o") || key.label.toString().equals("۹"))
                    canvas.drawText(String.valueOf(9), key.x + (key.width / 2) + 10, key.y + keyYLocation, paint);

                else if (key.label.toString().equals("p") || key.label.toString().equals("P") || key.label.toString().equals("۰"))
                    canvas.drawText(String.valueOf(0), key.x + (key.width / 2) + 10, key.y + keyYLocation, paint);


            }

        }
    }
}