
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Main {
    private final static String URL = "http://localhost:4000/users_data/images/1.exe";
    private final static int MAX_CHUNK_NUMBER = 10;
    private final static int MIN_CHUNK_SIZE = 50000;

    public static void main(String[] args) {
        try {
            URL url = new URL(URL);
            DiskService diskService = new DiskService(URL);
            ArrayList<Chunk> chunks = diskService.getChunks();

            if (chunks == null) {
                HttpURLConnection connection = null;
                try {
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
                    }
                    diskService.loadChunks(chunks);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    connection.disconnect();
                }
            }

            for (Chunk chunk : chunks) {
                Loader loader = new Loader(url, chunk, diskService);
                loader.start();
            }
        } catch (MalformedURLException err) {
            System.out.println("Incorrect url");
        }

    }
}
