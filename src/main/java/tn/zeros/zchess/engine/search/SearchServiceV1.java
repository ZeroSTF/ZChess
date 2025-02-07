package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.GameStateChecker;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.evaluate.EvalUtils;
import tn.zeros.zchess.engine.evaluate.EvaluationService;

public class SearchServiceV1 implements SearchService {
    private static final int MAX_DEPTH = SearchUtils.MAX_DEPTH;

    private final MoveOrderingService moveOrderingService = new MoveOrderingService();
    private final TranspositionTable transpositionTable = new TranspositionTable(1 << 21);
    private final SearchMetrics metrics;
    private final SearchLogger logger;

    private final long searchTimeMs;
    private long searchEndTime;
    private boolean searchCancelled;

    private int bestMoveThisIteration;
    private int bestEvalThisIteration;
    private boolean hasSearchedAtLeastOneMove;

    public SearchServiceV1(long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
        this.metrics = new SearchMetrics();
        this.logger = new SearchLogger(metrics);
        SearchDebugConfig.getInstance()
                .enableMetrics(true)
                .enableIterationLogging(true)
                .enableFinalSummary(true)
                .enableVerboseLogging(false);
    }

    @Override
    public int startSearch(BoardState boardState) {
        int bestMove = Move.NULL_MOVE;
        int bestEval = SearchUtils.MIN_EVAL;
        searchEndTime = System.currentTimeMillis() + searchTimeMs; // Set search timeout
        searchCancelled = false; // Reset cancellation flag

        bestMoveThisIteration = Move.NULL_MOVE; // Reset best move for this iteration
        bestEvalThisIteration = SearchUtils.MIN_EVAL; // Reset best eval for this iteration

        for (int searchDepth = 1; searchDepth <= MAX_DEPTH; searchDepth++) { // Iterative deepening loop
            metrics.setCurrentDepth(searchDepth); // Update current depth in metrics
            hasSearchedAtLeastOneMove = false; // Reset flag for each depth

            // Perform alpha-beta search for the current depth
            alphaBetaPrune(searchDepth, SearchUtils.MIN_EVAL, SearchUtils.MAX_EVAL, boardState, 0);

            // After search, check if cancelled or if we found at least one move
            if (!isSearchCancelled() || hasSearchedAtLeastOneMove) {
                bestMove = bestMoveThisIteration; // Update best move found so far
                bestEval = bestEvalThisIteration; // Update best eval found so far
                metrics.setBestMove(bestMove); // Update best move in metrics
                metrics.setBestEval(bestEval); // Update best eval in metrics
                logger.logIterationResults();
            }

            if (isSearchCancelled()) { // Check for search cancellation
                break; // Exit iterative deepening loop if cancelled
            }

            // Reset for next iteration (already done before the loop, but good to reiterate in comments)
            bestMoveThisIteration = Move.NULL_MOVE;
            bestEvalThisIteration = SearchUtils.MIN_EVAL;

            // Early exit if mate is found
            if (SearchUtils.isMateScore(bestEval)) {
                break; // Exit if mate is found
            }

            // Check for time timeout after each iteration
            if (checkSearchTimeout()) {
                endSearch(); // Set cancellation flag if timeout
            }
        }
        searchCancelled = false;
        logger.logFinalSummary();
        return bestMove != Move.NULL_MOVE ? bestMove : getFallbackMove(boardState); // Return best move or fallback
    }

    // 7. Alpha-Beta Search Core (alphaBetaPrune)
    @Override
    public int alphaBetaPrune(int depth, int alpha, int beta, BoardState state, int currentPly) {
        // 7.1. Search Termination Conditions and Checks

        // 7.1.1. Check Timeout and Cancellation
        if (checkSearchTimeout() || isSearchCancelled()) {
            return SearchUtils.TIMEOUT_VALUE; // Immediately return timeout value
        }

        // 7.1.2. Increment Node Count (for metrics)
        metrics.incrementNodes();

        // 7.1.3. Check Drawish Positions (50-move rule, repetition, insufficient material)
        if (currentPly > 0 && isDrawishPosition(state)) {
            return 0; // Return draw score (0)
        }

        // 7.1.4. Mate Distance Pruning
        alpha = Math.max(alpha, -SearchUtils.CHECKMATE_EVAL + currentPly); // Lower bound for alpha
        beta = Math.min(beta, SearchUtils.CHECKMATE_EVAL - currentPly);   // Upper bound for beta
        if (alpha >= beta) {
            return alpha; // Beta cutoff due to mate distance pruning
        }

        // 7.2. Transposition Table Lookup
        final TranspositionTable.Entry ttEntry = getTranspositionEntry(state, currentPly); // Retrieve TT entry
        final int ttScore = lookupEntryEval(ttEntry, depth, alpha, beta); // Lookup score in TT
        if (ttScore != SearchUtils.LOOKUP_FAILED) {
            metrics.incrementTTHits(); // Increment TT hit count
            if (currentPly == 0) { // Store best move for root in iterative deepening
                bestMoveThisIteration = ttEntry.bestMove;
                bestEvalThisIteration = ttScore;
                hasSearchedAtLeastOneMove = true;
            }
            return ttScore; // Return TT score if lookup is successful and valid
        }

        // 7.3. Base Case: Depth 0 - Quiescence Search
        if (depth == 0) {
            return quiescenceSearch(alpha, beta, state, currentPly); // Call quiescence search
        }

        // 7.4. Move Generation and Ordering
        MoveGenerator.MoveList moves = generateLegalMoves(state); // Generate all legal moves
        if (moves.isEmpty()) {
            // 7.4.1. Checkmate or Stalemate
            if (LegalMoveFilter.inCheck(state, state.isWhiteToMove())) {
                return -SearchUtils.CHECKMATE_EVAL; // Checkmate score
            }
            return 0; // Stalemate score
        }

        int ttBestMove = (ttEntry != null) ? ttEntry.bestMove : Move.NULL_MOVE; // Get TT best move for ordering

        // Move Ordering (including TT move, killer moves, history heuristic)
        moveOrderingService.orderMoves(moves, state, currentPly, ttBestMove); // Order moves for efficiency

        // 7.5. Move Loop and Alpha-Beta Pruning
        int originalAlpha = alpha; // Store original alpha for TT entry type
        int bestMove = Move.NULL_MOVE;  // Initialize best move for this node
        int bestScore = SearchUtils.MIN_EVAL; // Initialize best score for this node

        for (int i = 0; i < moves.size; i++) { // Iterate through all moves
            int move = moves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move); // Make move
            int eval = -alphaBetaPrune(depth - 1, -beta, -alpha, state, currentPly + 1); // Recursive alpha-beta call
            MoveExecutor.unmakeMove(state, undoInfo); // Unmake move (restore state)

            // 7.5.1. Check Timeout and Cancellation within the loop
            if (checkSearchTimeout() || isSearchCancelled()) {
                return SearchUtils.TIMEOUT_VALUE; // Return timeout value if time runs out during move loop
            }

            // 7.5.2. Update Best Move and Score
            if (eval > bestScore) {
                bestScore = eval;   // Update best score if we found a better one
                bestMove = move;     // Update best move

                if (currentPly == 0) { // Update root move for iterative deepening
                    bestMoveThisIteration = bestMove;
                    bestEvalThisIteration = bestScore;
                    hasSearchedAtLeastOneMove = true;
                }
                alpha = Math.max(alpha, eval); // Update alpha (lower bound)
            }

            // 7.5.3. Beta Cutoff
            if (alpha >= beta) {
                storeTranspositionEntry(state, depth, beta, TTEntryType.LOWER_BOUND, bestMove, currentPly); // Store TT entry (lower bound)
                moveOrderingService.updateKillerMoves(move, currentPly); // Update killer move heuristic
                return beta; // Beta cutoff - return beta value
            }
        }

        // 7.6. Transposition Table Store (after move loop)
        TTEntryType entryType;
        int entryScore;
        if (bestScore <= originalAlpha) {
            entryType = TTEntryType.UPPER_BOUND;   // Fail-low score
            entryScore = originalAlpha;          // Store original alpha as score
        } else if (bestScore >= beta) {
            entryType = TTEntryType.LOWER_BOUND;   // Fail-high score
            entryScore = beta;                 // Store beta as score
        } else {
            entryType = TTEntryType.EXACT;       // Exact score
            entryScore = bestScore;              // Store best score
        }
        storeTranspositionEntry(state, depth, entryScore, entryType, bestMove, currentPly); // Store TT entry

        // 7.7. History Heuristic Update
        if (bestScore > originalAlpha) { // If best move improved alpha (or caused cutoff - good move)
            moveOrderingService.updateHistoryScores(bestMove, depth); // Update history heuristic
        }

        return bestScore; // Return best score from this node
    }

    // 8. Quiescence Search (quiescenceSearch)
    private int quiescenceSearch(int alpha, int beta, BoardState state, int currentPly) {
        // 8.1. Search Termination Checks

        // 8.1.1. Check Timeout and Cancellation
        if (checkSearchTimeout() || isSearchCancelled()) {
            return SearchUtils.TIMEOUT_VALUE; // Immediately return timeout value
        }

        // 8.2. Transposition Table Lookup
        final TranspositionTable.Entry ttEntry = getTranspositionEntry(state, currentPly); // Retrieve TT entry
        final int ttScore = lookupEntryEval(ttEntry, 0, alpha, beta); // Depth 0 for quiescence entries
        if (ttScore != SearchUtils.LOOKUP_FAILED) {
            metrics.incrementTTHits(); // Increment TT hit count
            if (currentPly == 0) { // Store best move for root in iterative deepening (though likely NULL in quiescence)
                bestMoveThisIteration = ttEntry.bestMove;
                bestEvalThisIteration = ttScore;
                hasSearchedAtLeastOneMove = true;
            }
            return ttScore; // Return TT score if lookup is successful and valid
        }

        // 8.3. Stand-Pat Evaluation
        int standPat = evaluateBoard(state); // Evaluate the current static position
        boolean inCheck = LegalMoveFilter.inCheck(state, state.isWhiteToMove()); // Check if side to move is in check

        // 8.4. Stand-Pat Cutoff (if not in check)
        if (!inCheck) {
            if (standPat >= beta) {
                storeTranspositionEntry(state, 0, beta, TTEntryType.LOWER_BOUND, Move.NULL_MOVE, currentPly); // Store TT entry (lower bound)
                return beta; // Beta cutoff based on stand-pat
            }
            alpha = Math.max(alpha, standPat); // Update alpha if stand-pat improves it
        }

        // 8.5. Move Generation (Captures or all moves if in check)
        MoveGenerator.MoveList moves = generateQuiescenceMoves(state, inCheck); // Generate only captures (or all if in check)

        // 8.6. Check for No Legal Quiescence Moves
        if (moves.isEmpty()) {
            int finalScore = inCheck ? -SearchUtils.CHECKMATE_EVAL : standPat; // Checkmate if in check, otherwise stand-pat
            storeTranspositionEntry(state, 0, finalScore, TTEntryType.EXACT, Move.NULL_MOVE, currentPly); // Store TT entry (exact score for terminal node)
            return finalScore; // Return final score for quiescence search leaf node
        }

        int ttBestMove = (ttEntry != null) ? ttEntry.bestMove : Move.NULL_MOVE; // Get TT best move for ordering
        moveOrderingService.orderMoves(moves, state, currentPly, ttBestMove); // Order moves (captures)

        int originalAlpha = alpha; // Store original alpha for TT entry type
        int bestMove = Move.NULL_MOVE; // Initialize best move for quiescence node
        int bestScore = standPat;      // Initialize best score to stand-pat


        // 8.7. Quiescence Move Loop (Iterate through captures)
        // Use a more conservative margin for delta pruning
        int deltaMargin = EvalUtils.getPieceTypeValue(Piece.PAWN) + 200;

        for (int i = 0; i < moves.size; i++) {
            // 8.7.1. Check Timeout and Cancellation within the loop
            if (checkSearchTimeout() || isSearchCancelled()) {
                break; // Exit move loop if timeout or cancelled
            }

            int move = moves.moves[i];
            int capturedPiece = Move.getCapturedPiece(move);
            int capturedValue = EvalUtils.getPieceTypeValue(capturedPiece);

            // 8.7.2. Delta Pruning (more conservative)
            if (!inCheck && capturedValue != EvalUtils.NONE_VALUE) {
                if (capturedValue + deltaMargin + standPat <= alpha) {
                    continue; // Delta pruning cutoff
                }
            }

            // 8.7.3. Recursive Quiescence Search Call
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move); // Make capture move
            int eval = -quiescenceSearch(-beta, -alpha, state, currentPly + 1); // Recursive quiescence search
            MoveExecutor.unmakeMove(state, undoInfo); // Unmake capture move

            // 8.7.4. Check Timeout and Cancellation after recursive call
            if (checkSearchTimeout() || isSearchCancelled()) {
                return SearchUtils.TIMEOUT_VALUE; // Return timeout value if time ran out during recursive call
            }

            // 8.7.5. Update Best Score and Move
            if (eval > bestScore) {
                bestScore = eval;  // Update best score
                bestMove = move;    // Update best move
                alpha = Math.max(alpha, eval); // Update alpha
            }

            // 8.7.6. Beta Cutoff
            if (alpha >= beta) {
                storeTranspositionEntry(state, 0, beta, TTEntryType.LOWER_BOUND, bestMove, currentPly); // Store TT entry (lower bound)
                moveOrderingService.updateKillerMoves(move, currentPly); // Update killer move heuristic
                return beta; // Beta cutoff - return beta value
            }
        }

        // 8.8. Transposition Table Store (after move loop)
        TTEntryType entryType;
        int entryScore;
        if (bestScore <= originalAlpha) {
            entryType = TTEntryType.UPPER_BOUND;   // Fail-low score
            entryScore = originalAlpha;          // Store original alpha as score
        } else if (bestScore >= beta) {
            entryType = TTEntryType.LOWER_BOUND;   // Fail-high score
            entryScore = beta;                 // Store beta as score
        } else {
            entryType = TTEntryType.EXACT;       // Exact score
            entryScore = bestScore;              // Store best score
        }
        storeTranspositionEntry(state, 0, entryScore, entryType, bestMove, currentPly); // Store TT entry

        return bestScore; // Return best score from quiescence node
    }

    // 9. Move Generation and Ordering Helpers
    private MoveGenerator.MoveList generateLegalMoves(BoardState state) { // Renamed for clarity
        return MoveGenerator.generateAllMoves(state, false);
    }

    private MoveGenerator.MoveList generateQuiescenceMoves(BoardState state, boolean inCheck) { // Clarified purpose
        return MoveGenerator.generateAllMoves(state, !inCheck); // Captures only if not in check
    }


    // 10. Transposition Table Helper Methods (TT Access)
    private TranspositionTable.Entry getTranspositionEntry(BoardState state, int currentPly) {
        final long positionKey = state.getZobristKey(); // Get Zobrist key for the current position
        final TranspositionTable.Entry entry = transpositionTable.get(positionKey); // Lookup entry in TT

        if (entry != null) {
            // Always unadjust mate scores when retrieving from TT to get correct ply distance to mate
            entry.score = SearchUtils.unadjustMateScore(entry.score, currentPly);
        }
        return entry; // Return the TT entry (or null if not found)
    }

    private void storeTranspositionEntry(BoardState state, int depth, int score, TTEntryType type, int bestMove, int currentPly) {
        if (SearchDebugConfig.getInstance().isVerboseLogging() && depth > 2) {
            System.out.printf("Storing TT entry - depth: %d, move: %s%n",
                    depth, Move.toAlgebraic(bestMove));
        }

        if (SearchUtils.isTimeout(score)) return;

        final long positionKey = state.getZobristKey();
        final int adjustedScore = SearchUtils.adjustMateScore(score, currentPly);

        // Only store if we have a valid move
        if (bestMove != Move.NULL_MOVE) {
            transpositionTable.put(positionKey, depth, adjustedScore, type, bestMove, currentPly);
        }
    }

    private int lookupEntryEval(TranspositionTable.Entry ttEntry, int depth, int alpha, int beta) {
        if (ttEntry != null && ttEntry.depth >= depth) { // Check if TT entry is valid and deep enough
            final int score = ttEntry.score; // Get score from TT entry

            // 10.1.1. Exact Score: Return immediately
            if (ttEntry.type == TTEntryType.EXACT) {
                return score; // Exact score found, return it
            }

            // 10.1.2. Bound Checks: Check if score provides cutoff
            if (ttEntry.type == TTEntryType.LOWER_BOUND && score >= beta) {
                return score; // Lower bound score is sufficient for beta cutoff, return it
            }
            if (ttEntry.type == TTEntryType.UPPER_BOUND && score <= alpha) {
                return score; // Upper bound score is sufficient for alpha cutoff, return it
            }
        }
        return SearchUtils.LOOKUP_FAILED; // TT lookup failed or score not usable
    }

    @Override
    public void clear() {
        transpositionTable.clear(); // Clear all entries in the transposition table
        moveOrderingService.clearKillerMoves(); // Also clear killer move heuristic when TT is cleared
        moveOrderingService.clearHistoryScores(); // Clear history heuristic as well
    }


    // 11. Search Control Methods (Timeout, Cancellation)
    public void endSearch() {
        searchCancelled = true; // Set the searchCancelled flag to stop the search
    }

    private boolean checkSearchTimeout() {
        return System.currentTimeMillis() >= searchEndTime; // Check if current time exceeds search end time
    }

    private boolean isSearchCancelled() {
        return searchCancelled; // Return the current state of the searchCancelled flag
    }


    // 12. Evaluation and Static Exchange Evaluation (SEE - if you implement it later)
    private int evaluateBoard(BoardState state) { // Renamed for clarity
        return EvaluationService.evaluate(state); // Delegate board evaluation to EvaluationService
    }

    // 14. Fallback Move (for edge cases)
    private int getFallbackMove(BoardState boardState) {
        MoveGenerator.MoveList moves = generateLegalMoves(boardState); // Generate legal moves
        return moves.size > 0 ? moves.moves[0] : Move.NULL_MOVE; // Return the first legal move if available, otherwise NULL_MOVE
    }


    // 15. Game State Checks (Repetition, 50-move rule etc.)
    private boolean isDrawishPosition(BoardState state) {
        return GameStateChecker.isFiftyMoveRule(state) || // Check for 50-move rule draw
                GameStateChecker.isThreefoldRepetition(state) || // Check for threefold repetition draw
                GameStateChecker.isInsufficientMaterial(state); // Check for insufficient material draw
    }

}