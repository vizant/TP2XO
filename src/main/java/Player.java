import lombok.Data;

import java.io.BufferedReader;
import java.io.PrintWriter;

@Data
public class Player {
    private transient BufferedReader reader;
    private transient PrintWriter writer;
    private String name;
    private PlayerSymbol playerSymbol;

    enum PlayerSymbol {
        PLAYER_X('X'), PLAYER_O('O');
        private char symbol;

        PlayerSymbol(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol(){
            return symbol;
        }
    }
}
