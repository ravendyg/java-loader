import com.beust.jcommander.Parameter;

public class Args {
    public String getUrl() {
        return url;
    }

    @Parameter(names = { "--url", "-U" }, description = "Resource URL")
    private String url;
}
