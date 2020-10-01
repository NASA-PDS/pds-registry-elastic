package gov.nasa.pds.registry.mgr.cmd;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;

import gov.nasa.pds.registry.mgr.Constants;
import gov.nasa.pds.registry.mgr.es.client.EsClientFactory;
import gov.nasa.pds.registry.mgr.util.CloseUtils;
import gov.nasa.pds.registry.mgr.util.es.EsRequestBuilder;
import gov.nasa.pds.registry.mgr.util.es.EsUtils;


public class CreateRegistryCmd implements CliCommand
{
    private static enum SchemaType { Registry, DataDictionary };
    
    private RestClient client;

    
    public CreateRegistryCmd()
    {
    }
    
    
    @Override
    public void run(CommandLine cmdLine) throws Exception
    {
        if(cmdLine.hasOption("help"))
        {
            printHelp();
            return;
        }

        String esUrl = cmdLine.getOptionValue("es", "http://localhost:9200");
        String indexName = cmdLine.getOptionValue("index", Constants.DEFAULT_REGISTRY_INDEX);
        String authPath = cmdLine.getOptionValue("auth");
        
        File schemaFile = getSchemaFile(cmdLine.getOptionValue("schema"), SchemaType.Registry);
        int shards = parseShards(cmdLine.getOptionValue("shards", "1"));
        int replicas = parseReplicas(cmdLine.getOptionValue("replicas", "0"));
        
        System.out.println("Elasticsearch URL: " + esUrl);
        System.out.println();
        
        client = EsClientFactory.createRestClient(esUrl, authPath);

        try
        {
            // Registry
            createIndex(schemaFile, indexName, shards, replicas);
            System.out.println();
            
            // Data dictionary
            File ddSchemaFile = getSchemaFile(null, SchemaType.DataDictionary); 
            createIndex(ddSchemaFile, indexName + "-dd", 1, replicas);
        }
        finally
        {
            CloseUtils.close(client);
        }
    }

    
    private void createIndex(File schemaFile, String indexName, int shards, int replicas) throws Exception
    {
        try
        {
            System.out.println("Creating index...");
            System.out.println("   Index: " + indexName);
            System.out.println("  Schema: " + schemaFile.getAbsolutePath());
            System.out.println("  Shards: " + shards);
            System.out.println("Replicas: " + replicas);
            
            // Create request
            Request req = new Request("PUT", "/" + indexName);
            EsRequestBuilder bld = new EsRequestBuilder();
            String jsonReq = bld.createCreateIndexRequest(schemaFile, shards, replicas);
            req.setJsonEntity(jsonReq);

            // Execute request
            Response resp = client.performRequest(req);
            EsUtils.printWarnings(resp);
            System.out.println("Done");
        }
        catch(ResponseException ex)
        {
            throw new Exception(EsUtils.extractErrorMessage(ex));
        }
    }
    

    private int parseShards(String str) throws Exception
    {
        int val = parseInt(str);
        if(val <= 0) throw new Exception("Invalid number of shards: " + str);
        
        return val;
    }
    

    private int parseReplicas(String str) throws Exception
    {
        int val = parseInt(str);
        if(val < 0) throw new Exception("Invalid number of replicas: " + str);
        
        return val;
    }

    
    private int parseInt(String str)
    {
        if(str == null) return 0;
        
        try
        {
            return Integer.parseInt(str);
        }
        catch(Exception ex)
        {
            return -1;
        }
    }
    
    
    private File getSchemaFile(String path, SchemaType type) throws Exception
    {
        File file = null;
        
        if(path == null)
        {
            // Get default
            String home = System.getenv("REGISTRY_MANAGER_HOME");
            if(home == null) 
            {
                throw new Exception("Could not find default configuration directory. REGISTRY_MANAGER_HOME environment variable is not set.");
            }

            switch(type)
            {
            case Registry:
                file = new File(home, "elastic/registry.json");
                break;
            case DataDictionary:
                file = new File(home, "elastic/data-dic.json");
                break;
            }
        }
        else
        {
            file = new File(path);
        }
        
        if(!file.exists()) throw new Exception("Schema file " + file.getAbsolutePath() + " does not exist");
        
        return file;
    }
    
    
    public void printHelp()
    {
        System.out.println("Usage: registry-manager create-registry <options>");

        System.out.println();
        System.out.println("Create registry index");
        System.out.println();
        System.out.println("Optional parameters:");
        System.out.println("  -auth <file>         Authentication config file");
        System.out.println("  -es <url>            Elasticsearch URL. Default is http://localhost:9200");
        System.out.println("  -index <name>        Elasticsearch index name. Default is 'registry'");
        System.out.println("  -schema <path>       Elasticsearch index schema (JSON file)"); 
        System.out.println("                       Default value is $REGISTRY_MANAGER_HOME/elastic/registry.json");
        System.out.println("  -shards <number>     Number of shards (partitions) for registry index. Default is 1");
        System.out.println("  -replicas <number>   Number of replicas (extra copies) of registry index. Default is 0");
        System.out.println();
    }

}
