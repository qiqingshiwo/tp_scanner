package burp;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

public class BurpExtender implements IBurpExtender,IScannerCheck,IProxyListener,ITab{
    private PrintWriter stdout;
    private IExtensionHelpers helpers;
    private IBurpExtenderCallbacks callbacks;
    private String[] abandon = {
      ".jpeg",".jpg",".heliyong",".js",".css",".png",".swf",".js_",".gif",".txt"
    };
    private JPanel jPanel;
    private JLabel jLabel;
    private JTextField jTextField;
    private JLabel jLabel1;
    @Override
    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks)
    {
        this.callbacks = callbacks;
        callbacks.setExtensionName("TP_scaner");
        helpers = callbacks.getHelpers();
        this.stdout = new PrintWriter(callbacks.getStdout(),true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jPanel = new JPanel();
                jLabel = new JLabel("延时：");
                jTextField = new JTextField("1",20);
                jLabel1 = new JLabel("秒");
                jPanel.add(jLabel);
                jPanel.add(jTextField);
                jPanel.add(jLabel1);
                callbacks.customizeUiComponent(jPanel);
                callbacks.addSuiteTab(BurpExtender.this);
            }
        });
        callbacks.registerScannerCheck(this);
        callbacks.registerProxyListener(this);
        stdout.println("hack by QiQing");
    }

    private boolean in_array(String qian,String[] hou)
    {
        for(int x = 0;x<=hou.length-1;x++)
        {
            if(qian.indexOf(hou[x]) != -1)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<IScanIssue> doPassiveScan(IHttpRequestResponse baseRequestResponse)
    {
        if(!jTextField.getText().equals("0"))
        {
            http.setSleep(jTextField.getText());
        }
        byte[] requests = baseRequestResponse.getRequest();
        String request =  new String(requests);
        IRequestInfo req = helpers.analyzeRequest(baseRequestResponse);
        String body = request.substring(req.getBodyOffset());
        String url = req.getUrl().toString();
        List<String> headers  = req.getHeaders();
        request = "";
        for (String header: headers) {
            request = request + header + "\r\n";
        }
        request = request + "\r\n" + body;
        if(!in_array(url,abandon))
        {
            stdout.println(url);
            check c1 = new check(url,request,stdout);
            c1.start();
        }
        return null;
    }
    @Override
    public List<IScanIssue> doActiveScan(IHttpRequestResponse requestResponse,IScannerInsertionPoint insertionPoint)
    {
        return null;
    }
    @Override
    public int consolidateDuplicateIssues(IScanIssue existingIssue,IScanIssue newIssue)
    {
        if(existingIssue.getIssueName().equals(newIssue.getIssueName()))
        {
            return -1;
        }
        else return 0;
    }
    @Override
    public void processProxyMessage(boolean messageIsRequest,IInterceptedProxyMessage message)
    {
        return;
    }
    @Override
    public String getTabCaption()
    {
        return "TP_scanner";
    }
    @Override
    public Component getUiComponent()
    {
        return jPanel;
    }
}

class CustomScanIssue implements IScanIssue
{
    private IHttpService httpService;
    private URL url;
    private IHttpRequestResponse[] httpMessages;
    private String name;
    private String detail;
    private String serverity;

    public CustomScanIssue(
            String name,
            String detail,
            String serverity)
    {
        this.name = name;
        this.detail = detail;
        this.serverity = serverity;
    }

    @Override
    public URL getUrl()
    {
        return url;
    }
    @Override
    public String getIssueName()
    {
        return name;
    }
    @Override
    public int getIssueType()
    {
        return 0;
    }
    @Override
    public String getSeverity()
    {
        return serverity;
    }

    @Override
    public String getConfidence()
    {
        return "Certain";
    }

    @Override
    public String getIssueBackground()
    {
        return null;
    }

    @Override
    public String getRemediationBackground()
    {
        return null;
    }

    @Override
    public String getIssueDetail()
    {
        return detail;
    }

    @Override
    public String getRemediationDetail()
    {
        return null;
    }

    @Override
    public IHttpRequestResponse[] getHttpMessages()
    {
        return httpMessages;
    }

    @Override
    public IHttpService getHttpService()
    {
        return httpService;
    }
}
