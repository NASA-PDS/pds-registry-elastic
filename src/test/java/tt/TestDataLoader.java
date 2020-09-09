package tt;


import java.io.File;
import java.net.HttpURLConnection;

import gov.nasa.pds.registry.mgr.util.es.DataLoader;


public class TestDataLoader
{

    public static void main(String[] args) throws Exception
    {
        HttpURLConnection.setFollowRedirects(true);
        
        DataLoader dl = new DataLoader("localhost", "t1");
        //dl.setBatchSize(10);
        
        try
        {
            File file = new File("/tmp/harvest/out/es-docs.json");
            dl.loadFile(file);
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
    
}