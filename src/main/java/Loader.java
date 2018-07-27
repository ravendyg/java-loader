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
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) this.url.openConnection();
            connection.setRequestProperty(
                    "Range",
                    String.format(
                            "bytes=%s-%s",
                            // offset != 0 if Chunk has been restored from disk
                            chunk.getStart() + chunk.getOffset(),
                            chunk.getEnd()
                    )
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
            diskService.unregisterLoader(chunk.getId());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
    }
}