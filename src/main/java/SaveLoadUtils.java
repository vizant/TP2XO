import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SaveLoadUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String schemeFileName = "scheme.json";

    public static Game loadGameByPlayersNames(Player firstPlayer, Player secondPlayer) throws GameIsNotExistException {
        String gameName = Game.generateGameFileName(firstPlayer, secondPlayer);
        if(!Files.exists(Paths.get(gameName)))
            throw new GameIsNotExistException();
        Game game = null;
        try (
                FileReader readerValidationScheme = new FileReader(schemeFileName);
                FileReader readerJsonObject = new FileReader(gameName)
        ) {
            JSONObject jsonSchema = new JSONObject(
                    new JSONTokener(readerValidationScheme));
            JSONObject jsonSubject = new JSONObject(
                    new JSONTokener(readerJsonObject));
            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonSubject);
            game = gson.fromJson(jsonSubject.toString(), Game.class);
            game.setPlayerByName(firstPlayer);
            game.setPlayerByName(secondPlayer);
        } catch (IOException e) {
            throw new GameIsNotExistException();
        }catch (ValidationException e){
            System.out.println("Validation error!");
            try {
                Files.delete(Paths.get(gameName));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            throw new GameIsNotExistException();
        }
        return game;
    }

    public static void save(Game game){
        try (
                PrintWriter printWriter = new PrintWriter(game.getFileName())
        ) {
            String gameJson = gson.toJson(game);
            printWriter.write(gameJson);
        } catch (FileNotFoundException e) {
            System.out.println("Failed to write data!");
        }
    }

    public static void deleteExistingFile(Game game) throws IOException {
        Files.deleteIfExists(Paths.get(game.getFileName()));
    }
}
