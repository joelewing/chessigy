package com.chess.core;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses standard algebraic notation (SAN) into Move objects.
 * Supports: piece moves, pawn moves, captures, castling, promotion, and
 * disambiguation.
 */
public class AlgebraicNotationParser {

    // Regex pattern for algebraic notation
    // Group 1: piece type (K,Q,R,B,N) or empty for pawn
    // Group 2: file disambiguation (a-h)
    // Group 3: rank disambiguation (1-8)
    // Group 4: capture indicator (x)
    // Group 5: destination file (a-h)
    // Group 6: destination rank (1-8)
    // Group 7: promotion (=Q, =R, =B, =N)
    // Group 8: check/mate (+, #)
    private static final Pattern MOVE_PATTERN = Pattern.compile(
            "^([KQRBN])?([a-h])?([1-8])?(x)?([a-h])([1-8])(=[QRBN])?[+#]?$");

    private static final Pattern CASTLING_PATTERN = Pattern.compile("^O-O(-O)?[+#]?$");

    /**
     * Parses an algebraic notation move string and returns the corresponding Move
     * object.
     * 
     * @param algebraic The algebraic notation string (e.g., "e4", "Nf3", "O-O")
     * @param game      The current game state
     * @return The parsed Move object, or null if parsing fails
     */
    public static Move parseMove(String algebraic, Game game) {
        if (algebraic == null || algebraic.isEmpty()) {
            return null;
        }

        // Clean up the notation (remove spaces, annotations)
        algebraic = algebraic.trim();

        // Check for castling
        Matcher castlingMatcher = CASTLING_PATTERN.matcher(algebraic);
        if (castlingMatcher.matches()) {
            return parseCastling(algebraic, game);
        }

        // Try to parse standard move
        Matcher moveMatcher = MOVE_PATTERN.matcher(algebraic);
        if (moveMatcher.matches()) {
            return parseStandardMove(moveMatcher, game);
        }

        return null;
    }

    private static Move parseCastling(String algebraic, Game game) {
        Board board = game.getBoard();
        PieceColor color = game.getCurrentTurn();
        int rank = (color == PieceColor.WHITE) ? 7 : 0;
        boolean kingside = !algebraic.contains("O-O-O");

        // King starts at e-file (column 4)
        int kingStartX = 4;
        int kingEndX = kingside ? 6 : 2;

        // Find the castling move in legal moves
        List<Move> legalMoves = game.getLegalMoves(kingStartX, rank);
        for (Move move : legalMoves) {
            if (move.isCastling() && move.getEndX() == kingEndX) {
                return move;
            }
        }

        return null;
    }

    private static Move parseStandardMove(Matcher matcher, Game game) {
        String pieceStr = matcher.group(1);
        String fileDisambig = matcher.group(2);
        String rankDisambig = matcher.group(3);
        String destFileStr = matcher.group(5);
        String destRankStr = matcher.group(6);
        String promotionStr = matcher.group(7);

        // Determine piece type
        PieceType pieceType;
        boolean isPawn = (pieceStr == null || pieceStr.isEmpty());
        if (isPawn) {
            pieceType = PieceType.PAWN;

            // For pawns: if fileDisambig exists but no explicit dest file in group 5,
            // then fileDisambig IS the destination file (e.g., "e4" not "exd5")
            // In captures like "exd5": fileDisambig='e', destFile='d'
            // In simple moves like "e4": fileDisambig='e', but should be treated as
            // destFile
            if (fileDisambig != null && destFileStr != null) {
                // This is a capture like "exd5" - fileDisambig is source, destFileStr is dest
                // Keep as is
            } else if (fileDisambig != null && destFileStr == null && rankDisambig != null) {
                // This is a simple pawn move like "e4"
                // fileDisambig is actually the destination file
                // rankDisambig is actually the destination rank
                destFileStr = fileDisambig;
                destRankStr = rankDisambig;
                fileDisambig = null;
                rankDisambig = null;
            }
        } else {
            pieceType = PieceRepresentation.fromNotationSymbol(pieceStr.charAt(0));
        }

        // Parse destination coordinates
        int destX = BoardCoordinates.xFromFile(destFileStr.charAt(0));
        int destY = BoardCoordinates.yFromRankChar(destRankStr.charAt(0));

        // Parse disambiguation
        Integer disambigFile = fileDisambig != null ? BoardCoordinates.xFromFile(fileDisambig.charAt(0)) : null;
        Integer disambigRank = rankDisambig != null ? BoardCoordinates.yFromRankChar(rankDisambig.charAt(0)) : null;

        // Parse promotion
        PieceType promotion = null;
        if (promotionStr != null) {
            promotion = PieceRepresentation.fromNotationSymbol(promotionStr.charAt(1)); // Skip '='
        }

        // Find matching legal move
        Board board = game.getBoard();
        PieceColor color = game.getCurrentTurn();

        // Get all legal moves for the current player
        MoveValidator validator = new MoveValidator(board);
        List<Move> allLegalMoves = validator.getLegalMoves(color);

        // Filter by destination and piece type
        for (Move move : allLegalMoves) {
            if (move.getEndX() != destX || move.getEndY() != destY) {
                continue;
            }

            Piece piece = move.getMovedPiece();
            if (piece == null || piece.getType() != pieceType) {
                continue;
            }

            // Check disambiguation
            if (disambigFile != null && move.getStartX() != disambigFile) {
                continue;
            }
            if (disambigRank != null && move.getStartY() != disambigRank) {
                continue;
            }

            // Check promotion
            if (promotion != null) {
                move.setPromotion(true);
                move.setPromotionType(promotion);
            }

            return move;
        }

        return null;
    }

    /**
     * Converts a move to standard algebraic notation.
     * 
     * @param move  The move to convert
     * @param board The current board state
     * @param color The color of the player making the move
     * @return The algebraic notation string
     */
    public static String toAlgebraicNotation(Move move, Board board, PieceColor color) {
        if (move == null) {
            return "";
        }

        StringBuilder notation = new StringBuilder();

        // Handle castling
        if (move.isCastling()) {
            boolean kingside = move.getEndX() > move.getStartX();
            return kingside ? "O-O" : "O-O-O";
        }

        Piece piece = move.getMovedPiece();
        if (piece == null) {
            return "";
        }

        // Piece prefix (except for pawns)
        if (piece.getType() != PieceType.PAWN) {
            notation.append(PieceRepresentation.toNotationSymbol(piece.getType()));

            // Add disambiguation if needed
            String disambig = getDisambiguation(move, board, color);
            notation.append(disambig);
        } else if (move.getCapturedPiece() != null || move.isEnPassant()) {
            // For pawn captures, include the starting file
            notation.append(BoardCoordinates.fileFromX(move.getStartX()));
        }

        // Capture indicator
        if (move.getCapturedPiece() != null || move.isEnPassant()) {
            notation.append('x');
        }

        // Destination square
        notation.append(BoardCoordinates.fileFromX(move.getEndX()));
        notation.append(BoardCoordinates.rankFromY(move.getEndY()));

        // Promotion
        if (move.isPromotion() && move.getPromotionType() != null) {
            notation.append('=');
            notation.append(PieceRepresentation.toNotationSymbol(move.getPromotionType()));
        }

        return notation.toString();
    }

    private static String getDisambiguation(Move move, Board board, PieceColor color) {
        // Check if other pieces of the same type can move to the same square
        MoveValidator validator = new MoveValidator(board);
        List<Move> allLegalMoves = validator.getLegalMoves(color);

        boolean needFileDisambig = false;
        boolean needRankDisambig = false;
        boolean needFullDisambig = false;

        for (Move other : allLegalMoves) {
            if (other == move)
                continue;

            Piece otherPiece = other.getMovedPiece();
            if (otherPiece == null || otherPiece.getType() != move.getMovedPiece().getType()) {
                continue;
            }

            if (other.getEndX() == move.getEndX() && other.getEndY() == move.getEndY()) {
                // Another piece can move to the same destination
                if (other.getStartX() != move.getStartX()) {
                    needFileDisambig = true;
                }
                if (other.getStartY() != move.getStartY()) {
                    needRankDisambig = true;
                }
                if (other.getStartX() == move.getStartX() && other.getStartY() != move.getStartY()) {
                    needRankDisambig = true;
                    needFileDisambig = false;
                }
            }
        }

        StringBuilder result = new StringBuilder();
        if (needFileDisambig || needFullDisambig) {
            result.append(BoardCoordinates.fileFromX(move.getStartX()));
        }
        if (needRankDisambig || needFullDisambig) {
            result.append(BoardCoordinates.rankFromY(move.getStartY()));
        }

        return result.toString();
    }

}
