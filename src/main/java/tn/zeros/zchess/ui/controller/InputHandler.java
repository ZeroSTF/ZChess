package tn.zeros.zchess.ui.controller;

public interface InputHandler {
    void handlePress(int square);

    void handleDrag(double x, double y);

    void handleRelease(double x, double y);
}
