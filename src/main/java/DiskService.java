import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class DiskService {
    private RandomAccessFile dataFile;
    private HashSet<Integer> loaders;
    private String fileName;
    private ArrayList<Chunk> chunks;

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public DiskService(String userUrl) {
        try {
            String[] urlChunks = userUrl.split("/");
            fileName = urlChunks[urlChunks.length - 1];
            String tempFileName = fileName + ".tmp";
            dataFile = new RandomAccessFile(tempFileName, "rw");
            loaders = new HashSet<>();
            readChunksFromDisk();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadChunks(ArrayList<Chunk> chunks) {
        this.chunks = chunks;
        for (Chunk chunk : chunks) {
            registerLoader(chunk.getId());
        }
    }

    public void unregisterLoader(Chunk chunk) {
        loaders.remove(chunk.getId());
        if (loaders.isEmpty()) {
            finish();
        }
    }

    public void write(Chunk chunk, byte[] data, int dataLength) {
        int id = chunk.getId();
        long start = chunk.getStart();
        long end = chunk.getEnd();
        int offset = chunk.getOffset();

        // logging info, remove later
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
            // write data
            dataFile.seek(start + offset);
            dataFile.write(data, offset, dataLength);
            // update meta info
            try (ObjectOutputStream dout = new ObjectOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(fileName + ".meta")
                    )
            )) {
                dout.writeObject(chunks);
                dout.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readChunksFromDisk() {
        try (ObjectInputStream din = new ObjectInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName + ".meta")
                )
        )) {
            chunks = (ArrayList<Chunk>) din.readObject();
            for(Chunk chunk : chunks) {
                registerLoader(chunk.getId());
            }
        } catch (FileNotFoundException e) {
            // that is absolutely OK
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerLoader(int id) {
        loaders.add(id);
    }

    private void finish() {
        try {
            dataFile.close();
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
