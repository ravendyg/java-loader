
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Main {
    private final static String URL = "http://localhost:4000/users_data/images/2.png";
    private final static int MAX_CHUNK_NUMBER = 10;
    private final static int MIN_CHUNK_SIZE = 50000;

    public static void main(String[] args) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(URL);
            DiskService diskService = new DiskService(URL);
            ArrayList<Chunk> chunks = diskService.createChunks();

            if (chunks == null) {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                int len = connection.getContentLength();
                int chunkSize = len / MAX_CHUNK_NUMBER;
                int chunksNumber = MAX_CHUNK_NUMBER;
                if (chunkSize < MIN_CHUNK_SIZE) {
                    chunkSize = MIN_CHUNK_SIZE;
                    chunksNumber = (int) Math.floor(len / chunkSize);
                    if (chunksNumber * chunkSize < len) {
                        chunksNumber += 1;
                    }
                }

                chunks = new ArrayList<>();
                for (int chunkId = 0; chunkId < chunksNumber; chunkId++) {
                    int lastByte;
                    lastByte = (chunkId + 1) * chunkSize;
                    if (lastByte > len) {
                        lastByte = len;
                    }
                    Chunk chunk = new Chunk(chunkId, chunkId * chunkSize, lastByte);
                    chunks.add(chunk);
                    diskService.registerLoader(chunkId);
                }
            }

            for (Chunk chunk : chunks) {
                Loader loader = new Loader(url, chunk, diskService);
                loader.start();
            }
        } catch (MalformedURLException err) {
            System.out.println("Incorrect url");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }
}
