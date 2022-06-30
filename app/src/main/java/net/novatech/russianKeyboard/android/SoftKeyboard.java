package net.novatech.russianKeyboard.android;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import net.novatech.russianKeyboard.R;
import net.novatech.russianKeyboard.database.DatabaseManager;
import java.util.ArrayList;
import java.util.List;

import static net.novatech.russianKeyboard.window.ThemesWindow.THEME_KEY;

public class SoftKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {


    static final boolean PROCESS_HARD_KEYS = true;
    private InputMethodManager mInputMethodManager;
    private LatinKeyboardView mInputView;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;

    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private boolean mSound;
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;
    private String mWordSeparators;

    // Keyboards (not subtypes)
    private LatinKeyboard englishKeyboard;
    private LatinKeyboard numbersKeyboard;
    private LatinKeyboard symbolsKeyboard;
    private LatinKeyboard symbolsShiftedKeyboard;
    private LatinKeyboard numericKeyboard;

    private LatinKeyboard mCurKeyboard;
    public static String mActiveKeyboard;

    private DatabaseManager db;
    private ArrayList<String> list;
    SharedPreferences sharedPreferences;

    int[] THE_LAYOUTS = {R.layout.input_1, R.layout.input_2, R.layout.input_3,
            R.layout.input_4, R.layout.input_5, R.layout.input_6, R.layout.input_7,
            R.layout.input_8, R.layout.input_9, R.layout.input_10};

    @Override
    public void onCreate() {
        super.onCreate();

        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onInitializeInterface() {
        if (englishKeyboard != null) {
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }

        englishKeyboard = new LatinKeyboard(this, R.xml.keys_en);
        symbolsKeyboard = new LatinKeyboard(this, R.xml.symbols);
        symbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.symbols_shifted);
        numbersKeyboard = new LatinKeyboard(this, R.xml.numbers);
        numericKeyboard = new LatinKeyboard(this, R.xml.numeric);

    }

    @Override
    public View onCreateInputView() {
        mInputView = (LatinKeyboardView) getLayoutInflater().inflate(
                THE_LAYOUTS[sharedPreferences.getInt(THEME_KEY, 0)], null);
        mInputView.setOnKeyboardActionListener(this);
        mInputView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mInputView.closing();
                }
                return false;
            }
        });

        setLatinKeyboard(getSelectedSubtype());

        return mInputView;
    }


    private LatinKeyboard getSelectedSubtype() {
        final InputMethodSubtype subtype = mInputMethodManager.getCurrentInputMethodSubtype();
        String s = subtype.getLocale();
        switch (s) {
            default:
                mActiveKeyboard = "en_US";
                mCurKeyboard = englishKeyboard;
        }

        return mCurKeyboard;
    }

    private void setLatinKeyboard(LatinKeyboard nextKeyboard) {
        nextKeyboard.setLanguageSwitchKeyVisibility(true);
        mInputView.setKeyboard(nextKeyboard);
    }

    @Override
    public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);

        return mCandidateView;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        setInputView(onCreateInputView());


        mComposing.setLength(0);
        updateCandidates();

        if (!restarting) {
            mMetaState = 0;
        }

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;

        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
                mCurKeyboard = numbersKeyboard;
                break;
            case InputType.TYPE_CLASS_DATETIME:
                mCurKeyboard = symbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_PHONE:
                mCurKeyboard = numericKeyboard;
                break;

            case InputType.TYPE_CLASS_TEXT:
                mCurKeyboard = getSelectedSubtype();
                mPredictionOn = sharedPreferences.getBoolean("suggestion", true);

                int variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    mPredictionOn = false;
                }

                if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == InputType.TYPE_TEXT_VARIATION_URI
                        || variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
                    mPredictionOn = false;
                    mActiveKeyboard = "en_US";
                    mCurKeyboard = englishKeyboard;
                }

                if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    mPredictionOn = false;
                    mCompletionOn = isFullscreenMode();
                }

                updateShiftKeyState(attribute);
                break;

            default:
                mCurKeyboard = getSelectedSubtype();
                updateShiftKeyState(attribute);
        }
        // TODO: checks predictions for other keyboards
        if (mPredictionOn) db = new DatabaseManager(this);

        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);

        mSound = sharedPreferences.getBoolean("sound", true);

        setLatinKeyboard(mCurKeyboard);
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();

        clearCandidateView();

        mComposing.setLength(0);
        updateCandidates();

        setCandidatesViewShown(false);

        mCurKeyboard = englishKeyboard;
        if (mInputView != null) {
            mInputView.closing();
        }

        if (db != null) db.close();
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);

        mInputView.closing();
        final InputMethodSubtype subtype = mInputMethodManager.getCurrentInputMethodSubtype();
        mInputView.setSubtypeOnSpaceKey(subtype);
    }

    @Override
    public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype subtype) {
        mInputView.setSubtypeOnSpaceKey(subtype);
        String s = subtype.getLocale();
        switch (s) {
            default:
                mActiveKeyboard = "en_US";
                mCurKeyboard = englishKeyboard;
        }
        setLatinKeyboard(mCurKeyboard);
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }

            List<String> stringList = new ArrayList<>();
            for (CompletionInfo ci : completions) {
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }

    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() - 1);
            int composed = KeyEvent.getDeadChar(accent, c);
            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length() - 1);
            }
        }

        onKey(c, null);

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    }
                }
                break;

            case KeyEvent.KEYCODE_DEL:
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_ENTER:
                return false;
            default:
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState() & KeyEvent.META_ALT_ON) != 0) {
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && mInputView != null && englishKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);

            updateShiftIcon();
        }
    }

    private boolean isAlphabet(int code) {
        return Character.isLetter(code);
    }

    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    public void onKey(int primaryCode, int[] keyCodes) {

        if (isWordSeparator(primaryCode)) {
            // Handle separator
            if (mComposing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            if (primaryCode == 32) {
                if (list != null) {
                    clearCandidateView();
                }

                addUpdateWord();
            }
            sendKey(primaryCode);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            handleClose();
            return;
        } else if (primaryCode == LatinKeyboardView.KEYCODE_LANGUAGE_SWITCH) {
            handleLanguageSwitch();
            return;
        } else if (primaryCode == LatinKeyboardView.KEYCODE_OPTIONS) {
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE
                && mInputView != null) {
            Keyboard current = mInputView.getKeyboard();
            if (current == symbolsKeyboard || current == symbolsShiftedKeyboard) {
                setLatinKeyboard(englishKeyboard);
                updateShiftIcon();
            } else {
                setLatinKeyboard(symbolsKeyboard);
                symbolsKeyboard.setShifted(false);
            }

        } else if (primaryCode == -10001) {
            mComposing.append("\u200C");
            getCurrentInputConnection().setComposingText(mComposing, 1);
        } else if (primaryCode == -10002) {
            mComposing.append("ẋ");
            getCurrentInputConnection().setComposingText(mComposing, 1);
        } else if (primaryCode == -10003) {
            mComposing.append("\u1E8A");
            getCurrentInputConnection().setComposingText(mComposing, 1);
        } else if (primaryCode == 1567) {
            mComposing.append("\u061F");
            getCurrentInputConnection().setComposingText(mComposing, 1);
        } else {
            handleCharacter(primaryCode, keyCodes);
        }

        if (mSound) playClick(primaryCode);
    }

    private void playClick(int keyCode) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am != null) {
            switch (keyCode) {
                case 32:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                    break;
                case Keyboard.KEYCODE_DONE:
                case 10:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                    break;
                case Keyboard.KEYCODE_DELETE:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                    break;
                default:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
            }
        }
    }

    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void updateCandidates() {
        if (!mCompletionOn && mPredictionOn) {
            if (mComposing.length() > 0) {
                SelectDataTask selectDataTask = new SelectDataTask();

                list = new ArrayList<>();
                list.add(mComposing.toString());

                selectDataTask.getSubtype(mCurKeyboard);
                selectDataTask.execute(mComposing.toString());
            } else {
                setSuggestions(null, false, false);
            }
        }
    }

    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }

    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleShift() {
        if (mInputView == null) {
            return;
        }

        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (englishKeyboard == currentKeyboard) {
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        } else if (currentKeyboard == symbolsKeyboard) {
            symbolsKeyboard.setShifted(true);
            setLatinKeyboard(symbolsShiftedKeyboard);
            symbolsShiftedKeyboard.setShifted(true);
        } else if (currentKeyboard == symbolsShiftedKeyboard) {
            symbolsShiftedKeyboard.setShifted(false);
            setLatinKeyboard(symbolsKeyboard);
            symbolsKeyboard.setShifted(false);
        }

        updateShiftIcon();
    }

    private void updateShiftIcon() {
        List<Keyboard.Key> keys = englishKeyboard.getKeys();
        Keyboard.Key currentKey;
        for (int i = 0; i < keys.size() - 1; i++) {
            currentKey = keys.get(i);
            mInputView.invalidateAllKeys();
            if (currentKey.codes[0] == -1) {
                currentKey.label = null;
                if (mInputView.isShifted() || mCapsLock) {
                    currentKey.icon = getResources().getDrawable(R.drawable.ic_keyboard_capslock_on_24dp);
                } else {
                    currentKey.icon = getResources().getDrawable(R.drawable.ic_keyboard_capslock_24dp);
                }
                break;
            }
        }
    }

    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (isAlphabet(primaryCode) && mPredictionOn) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
            updateCandidates();
        } else {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
        }

    }

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }

    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    private void handleLanguageSwitch() {
        mInputMethodManager.switchToNextInputMethod(getToken(), true /* onlyCurrentIme */);
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char) code));
    }

    public void pickDefaultCandidate() {
        pickSuggestionManually(0);
    }

    public void pickSuggestionManually(int index) {
        if (mCompletionOn && mCompletions != null && index >= 0 && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (mComposing.length() > 0) {
            mComposing.setLength(index);
            mComposing = new StringBuilder(list.get(index) + " ");
            commitTyped(getCurrentInputConnection());
        }
    }

    public void swipeRight() {
        if (mCompletionOn) {
            pickDefaultCandidate();
        }
    }

    public void swipeLeft() {
        handleBackspace();
    }

    public void swipeDown() {
        handleClose();
    }

    public void swipeUp() {
    }

    public void onPress(int primaryCode) {
        mInputView.setPreviewEnabled(true);

        // Disable preview key on Shift, Delete, Space, Language, Symbol and Emoticon.
        if (primaryCode == -1 || primaryCode == -5 || primaryCode == -2 || primaryCode == -10000
                || primaryCode == -101 || primaryCode == 32) {
            mInputView.setPreviewEnabled(false);
        }
    }

    public void onRelease(int primaryCode) {
    }

    private class SelectDataTask extends AsyncTask<String, Void, ArrayList<String>> {

        private String subType;

        void getSubtype(LatinKeyboard mCurKeyboard) {
            if (mCurKeyboard == englishKeyboard) {
                subType = "english";
            }
        }

        @Override
        protected ArrayList<String> doInBackground(String... str) {
            list = db.getAllRow(str[0], subType);
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            list = result;
            setSuggestions(result, true, true);
        }
    }

    public void addUpdateWord() {

        if (!getLastWord().isEmpty()) {
            Integer freq = db.getWordFrequency(getLastWord(), mActiveKeyboard);
            if (freq > 0) {
                db.updateRecord(getLastWord(), freq, mActiveKeyboard);
            } else {
                db.insertNewRecord(getLastWord(), mActiveKeyboard);
            }
        }
    }

    public String getLastWord() {
        CharSequence inputChars = getCurrentInputConnection().getTextBeforeCursor(50, 0);
        String inputString = String.valueOf(inputChars);

        return inputString.substring(inputString.lastIndexOf(" ") + 1);
    }

    public void clearCandidateView() {
        if (list != null) list.clear();
    }
}