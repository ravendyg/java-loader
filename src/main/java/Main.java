import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Main {
    private final static String url = "http://localhost:4000/users_data/images/2.png";
    private final static int MAX_CHUNK_NUMBER = 10;
    private final static int MIN_CHUNK_SIZE = 50000;

    private static class Loader extends Thread {
        private int id;
        private int start;
        private int end;
        private RandomAccessFile fileHandler;

        public Loader(int id, int start, int end, RandomAccessFile fileHandler) {
            this.id = id;
            this.start = start;
            this.end = end;
            this.fileHandler = fileHandler;
        }

        @Override
        public void run() {
            HttpURLConnection connection = null;
            try {
                URL urL = new URL(url);
                connection = (HttpURLConnection) urL.openConnection();
                connection.setRequestProperty("Range", String.format("bytes=%s-%s", start, end));
                int len = end - start;
                InputStream is = new BufferedInputStream(connection.getInputStream());
                byte[] data = new byte[len];
                int offset = 0;

                while (offset < len) {
                    int read = is.read(data, offset, len - offset);
                    if (read < 0) {
                        break;
                    }
                    synchronized (fileHandler) {
                        System.out.println(
                                String.format(
                                        "id: %s; from: %s; to: %s; starting: %s; ending: %s",
                                        id,
                                        start,
                                        end,
                                        start + offset,
                                        start + offset + read
                                )
                        );
                        fileHandler.seek(start + offset);
                        fileHandler.write(data, offset, read);
                    }
                    offset += read;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
        }
    }

    public static void main(String[] args) {
        HttpURLConnection connection = null;
        RandomAccessFile file = null;

        try {
            String[] urlChunks = url.split("/");
            String fileName = urlChunks[urlChunks.length - 1];
            String tempFileName = fileName;// + ".tmp";
            file = new RandomAccessFile(tempFileName, "rw");

            URL urL = new URL(url);
            connection = (HttpURLConnection) urL.openConnection();
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

            for (int chunkId = 0; chunkId < chunksNumber; chunkId++) {
                int lastByte;
                lastByte = (chunkId + 1) * chunkSize;
                if (lastByte > len) {
                    lastByte = len;
                }
                Loader loader = new Loader(chunkId, chunkId * chunkSize, lastByte, file);
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
