package gov.nasa.pds.registry.mgr.es.client;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;

import gov.nasa.pds.registry.mgr.Constants;
import gov.nasa.pds.registry.mgr.util.PropUtils;
import gov.nasa.pds.registry.mgr.util.es.EsUtils;

public class HttpConnectionFactory
{
    private int timeout = 5000;
    private URL url;
    private HttpHost host;
    private String authHeader;

    
    public HttpConnectionFactory(String esUrl, String indexName, String api) throws Exception
    {
        HttpHost host = EsUtils.parseEsUrl(esUrl);
        this.url = new URL(host.toURI() + "/" + indexName + "/" + api);
    }
    
    
    public HttpURLConnection createConnection() throws Exception
    {
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setConnectTimeout(timeout);
        con.setReadTimeout(timeout);
        con.setAllowUserInteraction(false);
        
        if(authHeader != null)
        {
            con.setRequestProperty("Authorization", authHeader);
        }
        
        return con;
    }

    
    public void setTimeoutSec(int timeoutSec)
    {
        if(timeoutSec <= 0) throw new IllegalArgumentException("Timeout should be > 0");
        this.timeout = timeoutSec * 1000;
    }

    
    public String getHostName()
    {
        return host.getHostName();
    }
    
    
    public void setBasicAuthentication(String user, String pass)
    {
        String auth = user + ":" + pass;
        String b64auth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        this.authHeader = "Basic " + b64auth;
    }
    
    
    public void initAuth(String authConfigFile) throws Exception
    {
        if(authConfigFile == null) return;
        
        Properties props = PropUtils.loadProps(authConfigFile);
        if(props == null) return;
        
        // Trust self-signed certificates
        if(Boolean.TRUE.equals(PropUtils.getBoolean(props, Constants.AUTH_TRUST_SELF_SIGNED)))
        {
            SSLContext sslCtx = SSLUtils.createTrustAllContext();
            HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());
        }
        
        // Basic authentication
        String user = props.getProperty("user");
        String pass = props.getProperty("password");
        if(user != null && pass != null)
        {
            setBasicAuthentication(user, pass);
        }
    }

}
