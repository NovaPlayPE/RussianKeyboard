package net.novatech.russianKeyboard.android;

public class LatinKeyboardContext {

    public LatinKeyboard normalKeyboard;
    public LatinKeyboard shiftKeyboard;

    public LatinKeyboardContext(LatinKeyboard normal, LatinKeyboard shift){
        this.normalKeyboard= normal;
        this.shiftKeyboard = shift;
    }

}