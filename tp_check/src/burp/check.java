package burp;
import java.io.PrintWriter;

public class check extends Thread {
    private String url;
    private String requests;
    private PrintWriter stdout;
    check(String url,String requests,PrintWriter stdout)
    {
        this.url = url;
        this.requests = requests;
        this.stdout = stdout;
    }
    @Override
    public void run()
    {
        http http = new http();
        http.doHttp(url,requests,stdout);
    }
}
