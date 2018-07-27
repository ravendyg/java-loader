import java.io.Serializable;

public class Chunk implements Serializable {
    private int id;
    private long start;
    private long end;
    private int offset;

    public Chunk(int id, int start, int end) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.offset = 0;
    }

    public void incrementOffset(int read) {
        this.offset += read;
    }

    public int getLength() {
        return (int) (end - start - offset);
    }

    public int getId() {
        return id;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public int getOffset() {
        return offset;
    }
}
