
import com.beust.jcommander.JCommander;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;

public class Main {
    private final static int MAX_CHUNK_NUMBER = 10;
    private final static int MIN_CHUNK_SIZE = 50000;

    public static void main(String[] argv) {
        Args args = new Args();
        new JCommander(args, argv);
        String urlStr = args.getUrl();

        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                int len = connection.getContentLength();
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    System.out.println(
                        String.format("Connection error. Code: %s", responseCode)
                    );
                    return;
                }

                DiskService diskService = new DiskService(urlStr);
                ArrayList<Chunk> chunks = diskService.getChunks();

                if (chunks == null) {
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
                }
                for (Chunk chunk : chunks) {
                    Loader loader = new Loader(url, chunk, diskService);
                    loader.start();
                }
            } catch (SocketException sErr) {
                System.out.println("Server is unavailable. Check the internet connection.");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
        } catch (MalformedURLException err) {
            System.out.println("Incorrect url");
        }

    }
}
