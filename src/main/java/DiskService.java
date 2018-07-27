import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class DiskService {
    private RandomAccessFile dataFile;
    private HashSet<Integer> loaders;
    private String fileName;
    private ObjectOutputStream dout;

    public DiskService(String userUrl) {
        try {
            String[] urlChunks = userUrl.split("/");
            fileName = urlChunks[urlChunks.length - 1];
            String tempFileName = fileName + ".tmp";
            this.dataFile = new RandomAccessFile(tempFileName, "rw");
            loaders = new HashSet<>();
            dout = new ObjectOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(fileName + ".meta")
                    )
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                dout.close();

                File fileMeta = new File(fileName + ".meta");
                if (fileMeta.exists()) {
                    fileMeta.delete();
                }
                File fileIn = new File(fileName + ".tmp");
                File fileOut = new File(fileName);
                if (fileOut.exists()) {
                    fileOut.delete();
                }
                fileIn.renameTo(fileOut);
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
