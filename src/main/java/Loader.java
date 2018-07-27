import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class Loader extends Thread {
    private Chunk chunk;
    private URL url;
    private DiskService diskService;

    public Loader(URL url, Chunk chunk, DiskService diskService) {
        this.url = url;
        this.chunk = chunk;
        this.diskService = diskService;
    }

    @Override
    public void run() {
        long rangeStart = chunk.getStart() + chunk.getOffset();
        long rangeEnd = chunk.getEnd();
        // can happen when the chunk is loaded from the storage
        if (rangeStart < rangeEnd) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) this.url.openConnection();
                connection.setRequestProperty(
                        "Range",
                        // offset != 0 if the chunk has been restored from disk
                        String.format("bytes=%s-%s", rangeStart, rangeEnd)
                );
                int len = chunk.getLength();
                InputStream is = new BufferedInputStream(connection.getInputStream());
                byte[] data = new byte[len];

                while (chunk.getOffset() < len) {
                    int offset = chunk.getOffset();
                    int read = is.read(data, offset, len - offset);
                    if (read < 0) {
                        break;
                    }
                    synchronized (diskService) {
                        diskService.write(chunk, data, read);
                    }
                    chunk.incrementOffset(read);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
        }
        diskService.unregisterLoader(chunk);
    }
}