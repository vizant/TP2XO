import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Setter
@Getter
public class Game {
    private static final int[][] winCoordinates =
            {
                    {0, 1, 2},
                    {3, 4, 5},
                    {6, 7, 8},
                    {0, 3, 6},
                    {1, 4, 7},
                    {2, 5, 8},
                    {0, 4, 8},
                    {2, 4, 6}
            };

    private final char[] gameBoard = new char[9];
    private final String fileName;
    private Player player_x;
    private Player player_o;
    private Player winner;
    private Player lastMovedPlayer;
    private transient GameFinishStatus finishStatus;

    enum GameFinishStatus{
        DRAW, WINNER_EXISTS
    }

    {
        for (int i = 0; i <= 8; i++) {
            gameBoard[i] = Character.forDigit(i, 10);
        }
    }

    public Game(Player player_x, Player player_o) {
        this.player_x = player_x;
        this.player_x.setPlayerSymbol(Player.PlayerSymbol.PLAYER_X);
        this.player_o = player_o;
        this.player_o.setPlayerSymbol(Player.PlayerSymbol.PLAYER_O);
        this.fileName = generateGameFileName(player_x, player_o);
    }

    public void setSymbolOnBoard(int position, char symbol) {
        gameBoard[position] = symbol;
    }

    public char getSymbolOnBoard(int position) {
        return gameBoard[position];
    }

    public void setPlayerByName(Player player){
        if(getPlayer_x().getName().equals(player.getName())){
            player_x = player;
            player_x.setPlayerSymbol(Player.PlayerSymbol.PLAYER_X);
        } else {
            player_o = player;
            player_o.setPlayerSymbol(Player.PlayerSymbol.PLAYER_O);
        }
    }

    public boolean isFinished() {

        for (int[] winCd : winCoordinates) {
            if (gameBoard[winCd[0]] == gameBoard[winCd[1]] &&
                    gameBoard[winCd[1]] == gameBoard[winCd[2]] &&
                    gameBoard[winCd[0]] == gameBoard[winCd[2]]) {
                char winnerSymbol = gameBoard[winCd[0]];
                if (winnerSymbol == player_x.getPlayerSymbol().getSymbol()) {
                    winner = player_x;
                } else {
                    winner = player_o;
                }
                finishStatus = GameFinishStatus.WINNER_EXISTS;
                return true;
            }
        }

        boolean isDraw = true;
        for (int i = 0; i < gameBoard.length; i++) {
            if(gameBoard[i] != 'X' && gameBoard[i] != 'O'){
                isDraw = false;
                break;
            }
        }
        if(isDraw) {
            finishStatus = GameFinishStatus.DRAW;
            return true;
        }

        return false;
    }

    public static String generateGameFileName(Player first, Player second) {
        String firstPlayerName = first.getName();
        String secondPlayerName = second.getName();
        final String gameTittle = "game";
        final String fileFormat = ".json";

        StringBuilder gameNameBuilder = new StringBuilder();
        if (firstPlayerName.compareTo(secondPlayerName) < 0)
            gameNameBuilder.append(firstPlayerName).append(secondPlayerName);
        else
            gameNameBuilder.append(secondPlayerName).append(firstPlayerName);
        gameNameBuilder.append(gameTittle).append(fileFormat);

        return gameNameBuilder.toString();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("-------------\n");
        for (int i = 0; i < 3; i++) {
            stringBuilder.append(String.format("| %c | %c | %c |\n", gameBoard[i * 3],
                    gameBoard[1 + i * 3], gameBoard[2 + i * 3]));
        }
        stringBuilder.append("-------------");
        return stringBuilder.toString();
    }
}
