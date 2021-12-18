import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

@Getter
@Setter
public class Client {
    private String name;
    private char symbol;
    private PrintWriter serverWriter;
    private BufferedReader serverReader;

    public static void main(String[] args) {
        System.out.printf("Host: %s\nPort: %s\n", args[0], args[1]);
        try (
                Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
                InputStream input = socket.getInputStream();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(input));
                OutputStream output = socket.getOutputStream();
                PrintWriter writer =
                        new PrintWriter(output, true)
        ) {
            Gson gson = new Gson();

            System.out.println("Connected to the server!");
            Client client = new Client();
            client.setServerReader(reader);
            client.setServerWriter(writer);

            sendName(client);

            String savedStatus = SendRecvUtils.receive(client.getServerReader());
            GameFileStatus gameFileStatus = gson.fromJson(savedStatus, GameFileStatus.class);
            if(gameFileStatus == GameFileStatus.SAVED){
                String savedGame = SendRecvUtils.receive(client.getServerReader());
                Game game = gson.fromJson(savedGame, Game.class);
                System.out.println("--SAVED GAME--");
                System.out.println(game);
                System.out.println("-------------");
            }

            String symbolJson = SendRecvUtils.receive(client.getServerReader());
            char symbol = gson.fromJson(symbolJson, char.class);
            System.out.println("Your symbol is> " + symbol);
            client.setSymbol(symbol);

            while (true){
                String gameStatusJson = SendRecvUtils.receive(client.getServerReader());
                GameStatus status = gson.fromJson(gameStatusJson, GameStatus.class);
                if(status == GameStatus.GAME_ENDED) {
                    String gameJsonFromServer = SendRecvUtils.receive(client.getServerReader());
                    Game game = gson.fromJson(gameJsonFromServer, Game.class);
                    System.out.println(game);
                    if(game.getWinner() != null) {
                        System.out.printf("\nWinner - %s!", game.getWinner().getName());
                    }else {
                        System.out.println("DRAW!");
                    }
                    break;
                }

                String gameJsonFromServer = SendRecvUtils.receive(reader);
                Game game = gson.fromJson(gameJsonFromServer, Game.class);

                setSymbolOnBoard(game, client);

                String gameJsonToServer = gson.toJson(game);
                SendRecvUtils.send(gameJsonToServer, client.getServerWriter());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendName(Client client){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name> ");
        String name = scanner.nextLine();
        client.setName(name);
        client.getServerWriter().println(name);
    }

    private static void setSymbolOnBoard(Game game, Client client){
        System.out.println(game);
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.print("Choose number> ");
            try {
                String strNumber = scanner.nextLine();
                int number = Integer.parseInt(strNumber);
                validateGameBoard(number, game);
                game.setSymbolOnBoard(number, client.getSymbol());
                break;
            }catch (OutOfGameBoardRangeException e){
                System.out.println("Enter number from 0 to 8!");
            }catch (PlaceAlreadyTakenException e){
                System.out.println("This place is already taken!");
            } catch (NumberFormatException e){
                System.out.println("Enter a number!");
            }
        }
    }

    private static void validateGameBoard(int number, Game game) throws OutOfGameBoardRangeException,
            PlaceAlreadyTakenException {

        if(number < 0 || number > 8) {
            throw new OutOfGameBoardRangeException();
        }
        if(game.getSymbolOnBoard(number) == 'X' || game.getSymbolOnBoard(number) == 'O'){
            throw new PlaceAlreadyTakenException();
        }
    }
}
