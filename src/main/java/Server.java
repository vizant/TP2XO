import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        System.out.println("Server is listening on the port " + args[0]);

        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
             Socket firstSocket = serverSocket.accept();
             Socket secondSocket = serverSocket.accept();
             PrintWriter firstWriter =
                     new PrintWriter(firstSocket.getOutputStream(), true);
             BufferedReader firstReader =
                     new BufferedReader(new InputStreamReader(firstSocket.getInputStream()));
             PrintWriter secondWriter =
                     new PrintWriter(secondSocket.getOutputStream(), true);
             BufferedReader secondReader =
                     new BufferedReader(new InputStreamReader(secondSocket.getInputStream()))
        ) {
            System.out.println("Users connected!");

            Player firstPlayer = new Player();
            firstPlayer.setReader(firstReader);
            firstPlayer.setWriter(firstWriter);
//            firstPlayer.setPlayerSymbol(Player.PlayerSymbol.PLAYER_X);
            Thread firstThread = new Thread(new NameReader(firstPlayer));
            firstThread.start();

            Player secondPlayer = new Player();
            secondPlayer.setReader(secondReader);
            secondPlayer.setWriter(secondWriter);
//            secondPlayer.setPlayerSymbol(Player.PlayerSymbol.PLAYER_O);
            Thread secondThread = new Thread(new NameReader(secondPlayer));
            secondThread.start();

            firstThread.join();
            secondThread.join();

            Gson gson = new Gson();

            Game game = null;
            Player firstPlayer1;
            Player secondPlayer1;
            try {
                game = SaveLoadUtils.loadGameByPlayersNames(firstPlayer, secondPlayer);
                SendRecvUtils.send(gson.toJson(GameFileStatus.SAVED), firstPlayer.getWriter(), secondPlayer.getWriter());
                SendRecvUtils.send(gson.toJson(game), firstPlayer.getWriter(), secondPlayer.getWriter());

                if(game.getLastMovedPlayer().getName().equals(game.getPlayer_x().getName())){
                    firstPlayer1 = game.getPlayer_o();
                    secondPlayer1 = game.getPlayer_x();
                }else{
                    firstPlayer1 = game.getPlayer_x();
                    secondPlayer1 = game.getPlayer_o();
                }

            } catch (GameIsNotExistException e) {
                game = new Game(firstPlayer, secondPlayer);
                SendRecvUtils.send(gson.toJson(GameFileStatus.NOT_SAVED), firstPlayer.getWriter(), secondPlayer.getWriter());
                firstPlayer1 = firstPlayer;
                secondPlayer1 = secondPlayer;
            }

            char player_x_symbol = firstPlayer.getPlayerSymbol().getSymbol();
            String player_x_symbol_Json = gson.toJson(player_x_symbol);
            SendRecvUtils.send(player_x_symbol_Json, firstPlayer.getWriter());

            char player_o_symbol = secondPlayer.getPlayerSymbol().getSymbol();
            String player_o_symbol_Json = gson.toJson(player_o_symbol);
            SendRecvUtils.send(player_o_symbol_Json, secondPlayer.getWriter());


            while (true) {
                String gameStatusToFP = gson.toJson(GameStatus.GAME_RUNNING);
                SendRecvUtils.send(gameStatusToFP, firstPlayer1.getWriter());

                String gameJsonToFP = gson.toJson(game);
                SendRecvUtils.send(gameJsonToFP, firstPlayer1.getWriter());

                String gameJsonFromFP = SendRecvUtils.receive(firstPlayer1.getReader());
                game = gson.fromJson(gameJsonFromFP, Game.class);
                game.setLastMovedPlayer(firstPlayer1);
                SaveLoadUtils.save(game);

                System.out.println(game);

                if (game.isFinished())
                    break;

                String gameStatusToSP = gson.toJson(GameStatus.GAME_RUNNING);
                SendRecvUtils.send(gameStatusToSP, secondPlayer1.getWriter());

                String gameJsonToSP = gson.toJson(game);
                SendRecvUtils.send(gameJsonToSP, secondPlayer1.getWriter());

                String gameJsonFromSP = SendRecvUtils.receive(secondPlayer1.getReader());
                game = gson.fromJson(gameJsonFromSP, Game.class);
                game.setLastMovedPlayer(secondPlayer1);
                SaveLoadUtils.save(game);

                System.out.println(game);

                if (game.isFinished())
                    break;

            }
            Player winner = game.getWinner();

            if(winner == null){
                System.out.println("DRAW!");
            }else {
                if (winner.getPlayerSymbol().getSymbol() == firstPlayer1.getPlayerSymbol().getSymbol()) {
                    System.out.println("Winner - " + firstPlayer1.getName());
                } else {
                    System.out.println("Winner - " + secondPlayer1.getName());
                }
            }
            String gameStatusTOAll = gson.toJson(GameStatus.GAME_ENDED);
            SendRecvUtils.send(gameStatusTOAll, firstPlayer1.getWriter(), secondPlayer1.getWriter());

            String gameJsonToAll = gson.toJson(game);
            SendRecvUtils.send(gameJsonToAll, firstPlayer1.getWriter(), secondPlayer1.getWriter());

            SaveLoadUtils.deleteExistingFile(game);

        } catch (IOException | InterruptedException exception) {
            System.out.println(exception.getMessage());
        }
    }

    static class NameReader implements Runnable {
        private final Player player;

        public NameReader(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            try {
                String playerName = player.getReader().readLine();
                player.setName(playerName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
