package reductor;

import static reductor.Files.*;


public class Main {


    public static void main(String[] args) {

        Piece piece = new Piece(BEETHOVEN_5_IV);
        piece.scaleTempo(2);
        piece.play();

    }


}