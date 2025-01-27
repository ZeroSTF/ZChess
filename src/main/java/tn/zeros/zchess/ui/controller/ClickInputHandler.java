package tn.zeros.zchess.ui.controller;

public class ClickInputHandler implements InputHandler {
    private final ChessController controller;

    public ClickInputHandler(ChessController controller) {
        this.controller = controller;
    }

    @Override
    public void handlePress(int square) {
        // Replicate the original click logic here
        if (controller.getInteractionState().getSelectedSquare() == -1) {
            controller.handlePieceSelection(square);
        } else {
            controller.handleMoveExecution(square);
        }
    }

    @Override
    public void handleDrag(double x, double y) {
        
    }

    @Override
    public void handleRelease(int square) {
        // Not needed for click-based input (for now)
    }
}