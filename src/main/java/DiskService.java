import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;

public class DiskService {
    private RandomAccessFile dataFile;
    private HashSet<Integer> loaders = new HashSet<>();
    private String userUrl;

    public DiskService(String userUrl) throws FileNotFoundException {
        this.userUrl = userUrl;
        String[] urlChunks = userUrl.split("/");
        String fileName = urlChunks[urlChunks.length - 1];
        String tempFileName = fileName;// + ".tmp";
        this.dataFile = new RandomAccessFile(tempFileName, "rw");
    }

    public ArrayList<Chunk> createChunks() {
        // TODO: implement searching for a suspended loading process
        return null;
    }

    public void registerLoader(int id) {
        loaders.add(id);
    }

    public void unregisterLoader(int id) {
        loaders.remove(id);
        if (loaders.isEmpty()) {
            try {
                dataFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(Chunk chunk, byte[] data, int dataLength) {
        int id = chunk.getId();
        long start = chunk.getStart();
        long end = chunk.getEnd();
        int offset = chunk.getOffset();

        System.out.println(
                String.format(
                        "id: %s; from: %s; to: %s; starting: %s; ending: %s",
                        id,
                        start,
                        end,
                        start + offset,
                        start + offset + dataLength
                )
        );
        try {
            dataFile.seek(start + offset);
            dataFile.write(data, offset, dataLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
