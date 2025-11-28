package com.chess.core;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Board board;
    private final MoveValidator validator;
    private PieceColor currentTurn;
    private final List<Move> moveHistory;
    private int currentMoveIndex; // To support traversing history

    public Game() {
        this.board = new Board();
        this.validator = new MoveValidator(board);
        this.currentTurn = PieceColor.WHITE;
        this.moveHistory = new ArrayList<>();
        this.currentMoveIndex = -1;
    }

    public Board getBoard() {
        return board;
    }

    public PieceColor getCurrentTurn() {
        return currentTurn;
    }

    public boolean makeMove(Move move) {
        // Validate move
        List<Move> legalMoves = validator.getLegalMoves(currentTurn);
        boolean isLegal = false;
        for (Move m : legalMoves) {
            if (m.getStartX() == move.getStartX() && m.getStartY() == move.getStartY() &&
                    m.getEndX() == move.getEndX() && m.getEndY() == move.getEndY()) {
                isLegal = true;
                // Use the legal move object as it might have flags set (though our validator
                // currently doesn't set flags much)
                // Actually, we should probably use the move passed in if it's valid,
                // but the validator generates moves with captured pieces.
                // Let's match and use the validator's move to ensure captured piece is correct.
                move = m;
                break;
            }
        }

        if (!isLegal)
            return false;

        // Execute move
        board.movePiece(move);

        // Update history
        // If we are in the middle of history, truncate future
        if (currentMoveIndex < moveHistory.size() - 1) {
            moveHistory.subList(currentMoveIndex + 1, moveHistory.size()).clear();
        }
        moveHistory.add(move);
        currentMoveIndex++;

        // Switch turn
        currentTurn = currentTurn.opposite();

        return true;
    }

    public List<Move> getLegalMoves(int x, int y) {
        Piece p = board.getPiece(x, y);
        if (p == null || p.getColor() != currentTurn)
            return new ArrayList<>();

        List<Move> allLegal = validator.getLegalMoves(currentTurn);
        List<Move> pieceMoves = new ArrayList<>();
        for (Move m : allLegal) {
            if (m.getStartX() == x && m.getStartY() == y) {
                pieceMoves.add(m);
            }
        }
        return pieceMoves;
    }

    public void reset() {
        board.resetBoard();
        currentTurn = PieceColor.WHITE;
        moveHistory.clear();
        currentMoveIndex = -1;
    }

    // Navigation
    public void previousMove() {
        if (currentMoveIndex >= 0) {
            Move move = moveHistory.get(currentMoveIndex);
            board.undoMove(move);
            currentMoveIndex--;
            currentTurn = currentTurn.opposite();
        }
    }

    public void nextMove() {
        if (currentMoveIndex < moveHistory.size() - 1) {
            currentMoveIndex++;
            Move move = moveHistory.get(currentMoveIndex);
            board.movePiece(move);
            currentTurn = currentTurn.opposite();
        }
    }

    public void goToFirstMove() {
        while (currentMoveIndex >= 0) {
            previousMove();
        }
    }

    public void goToLastMove() {
        while (currentMoveIndex < moveHistory.size() - 1) {
            nextMove();
        }
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public int getCurrentMoveIndex() {
        return currentMoveIndex;
    }

    /**
     * Loads and replays a game from a list of algebraic notation moves.
     * 
     * @param moves List of moves in algebraic notation (e.g., "e4", "Nf3")
     * @return true if all moves were successfully loaded, false otherwise
     */
    public boolean loadFromPGN(List<String> moves) {
        // Reset the game to starting position
        reset();

        // Replay each move
        for (String moveStr : moves) {
            Move move = AlgebraicNotationParser.parseMove(moveStr, this);
            if (move == null) {
                System.err.println("Failed to parse move: " + moveStr);
                return false;
            }

            if (!makeMove(move)) {
                System.err.println("Failed to make move: " + moveStr);
                return false;
            }
        }

        return true;
    }

    public String getFen() {
        StringBuilder sb = new StringBuilder();

        // 1. Piece placement
        sb.append(board.getFenPiecePlacement());
        sb.append(" ");

        // 2. Active color
        sb.append(currentTurn == PieceColor.WHITE ? "w" : "b");
        sb.append(" ");

        // 3. Castling availability
        String castling = getCastlingRights();
        sb.append(castling);
        sb.append(" ");

        // 4. En passant target square
        String enPassant = getEnPassantTarget();
        sb.append(enPassant);
        sb.append(" ");

        // 5. Halfmove clock
        int halfMoveClock = getHalfMoveClock();
        sb.append(halfMoveClock);
        sb.append(" ");

        // 6. Fullmove number
        int fullMoveNumber = (moveHistory.size() / 2) + 1;
        sb.append(fullMoveNumber);

        return sb.toString();
    }

    private String getCastlingRights() {
        StringBuilder sb = new StringBuilder();
        appendCastlingRightsForColor(PieceColor.WHITE, 'K', 'Q', sb);
        appendCastlingRightsForColor(PieceColor.BLACK, 'k', 'q', sb);
        return sb.length() > 0 ? sb.toString() : "-";
    }

    private void appendCastlingRightsForColor(PieceColor color, char kingSymbol, char queenSymbol, StringBuilder sb) {
        int rank = color == PieceColor.WHITE ? 7 : 0;
        Piece king = board.getPiece(4, rank);
        if (isEligibleKing(king, color)) {
            appendRookRight(color, rank, 7, kingSymbol, sb);
            appendRookRight(color, rank, 0, queenSymbol, sb);
        }
    }

    private boolean isEligibleKing(Piece piece, PieceColor color) {
        return piece != null && piece.getType() == PieceType.KING && piece.getColor() == color && !piece.hasMoved();
    }

    private void appendRookRight(PieceColor color, int rank, int file, char symbol, StringBuilder sb) {
        Piece rook = board.getPiece(file, rank);
        if (rook != null && rook.getType() == PieceType.ROOK && rook.getColor() == color && !rook.hasMoved()) {
            sb.append(symbol);
        }
    }

    private String getEnPassantTarget() {
        Move lastMove = board.getLastMove();
        if (lastMove != null) {
            Piece p = lastMove.getMovedPiece();
            if (p.getType() == PieceType.PAWN) {
                int startY = lastMove.getStartY();
                int endY = lastMove.getEndY();
                if (Math.abs(startY - endY) == 2) {
                    // It was a double push
                    int targetY = (startY + endY) / 2;
                    int targetX = lastMove.getStartX(); // x is same
                    return BoardCoordinates.toAlgebraic(targetX, targetY);
                }
            }
        }
        return "-";
    }

    private int getHalfMoveClock() {
        int halfMoves = 0;
        for (int i = moveHistory.size() - 1; i >= 0; i--) {
            Move m = moveHistory.get(i);
            if (m.getMovedPiece().getType() == PieceType.PAWN || m.getCapturedPiece() != null) {
                return halfMoves;
            }
            halfMoves++;
        }
        return halfMoves;
    }

    public boolean makeMoveFromUCI(String uci) {
        if (uci == null || uci.length() < 4)
            return false;

        int startX = BoardCoordinates.xFromFile(uci.charAt(0));
        int startY = BoardCoordinates.yFromRankChar(uci.charAt(1));
        int endX = BoardCoordinates.xFromFile(uci.charAt(2));
        int endY = BoardCoordinates.yFromRankChar(uci.charAt(3));

        PieceType promotion = null;
        if (uci.length() == 5) {
            switch (uci.charAt(4)) {
                case 'q':
                    promotion = PieceType.QUEEN;
                    break;
                case 'r':
                    promotion = PieceType.ROOK;
                    break;
                case 'b':
                    promotion = PieceType.BISHOP;
                    break;
                case 'n':
                    promotion = PieceType.KNIGHT;
                    break;
            }
        }

        List<Move> legalMoves = validator.getLegalMoves(currentTurn);
        for (Move m : legalMoves) {
            if (m.getStartX() == startX && m.getStartY() == startY &&
                    m.getEndX() == endX && m.getEndY() == endY) {

                if (promotion != null) {
                    if (m.isPromotion() && m.getPromotionType() == promotion) {
                        return makeMove(m);
                    }
                } else {
                    if (!m.isPromotion()) {
                        return makeMove(m);
                    }
                }
            }
        }
        return false;
    }

    public GameState getGameState() {
        List<Move> legalMoves = validator.getLegalMoves(currentTurn);
        if (legalMoves.isEmpty()) {
            if (validator.isKingInCheck(currentTurn)) {
                return GameState.CHECKMATE;
            } else {
                return GameState.STALEMATE;
            }
        }
        return GameState.IN_PROGRESS;
    }

}
