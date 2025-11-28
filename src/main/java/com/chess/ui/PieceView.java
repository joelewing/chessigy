package com.chess.ui;

import com.chess.core.Piece;
import com.chess.core.PieceColor;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PieceView extends StackPane {
    private static final double TILE_SIZE = 80;

    public PieceView(Piece piece) {
        String imagePath = getImagePath(piece);
        if (imagePath != null) {
            try {
                var resource = getClass().getResource(imagePath);
                if (resource != null) {
                    Image image = new Image(resource.toExternalForm());
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(TILE_SIZE);
                    imageView.setFitHeight(TILE_SIZE);
                    imageView.setPreserveRatio(true);
                    getChildren().add(imageView);
                } else {
                    System.err.println("Could not find image resource: " + imagePath);
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + imagePath);
                e.printStackTrace();
            }
        }
    }

    private String getImagePath(Piece piece) {
        String colorCode = piece.getColor() == PieceColor.WHITE ? "w" : "b";
        String typeCode = "";
        switch (piece.getType()) {
            case PAWN:
                typeCode = "p";
                break;
            case ROOK:
                typeCode = "r";
                break;
            case KNIGHT:
                typeCode = "n";
                break;
            case BISHOP:
                typeCode = "b";
                break;
            case QUEEN:
                typeCode = "q";
                break;
            case KING:
                typeCode = "k";
                break;
        }
        return "/com/chess/chess_pieces/" + colorCode + typeCode + ".png";
    }
}
