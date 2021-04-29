package gov.nasa.pds.registry.mgr.dd.parser;

import java.io.File;
import java.io.FileReader;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import gov.nasa.pds.registry.mgr.util.CloseUtils;

/**
 * Base parser of PDS LDD JSON files (Data dictionary files).
 * This implementation is using Google "GSON" streaming parser to reduce memory footprint.
 * (We only need a subset of values from a JSON file).
 *  
 * @author karpenko
 */
public class BaseLddParser
{
    protected File ddFile;
    protected JsonReader jsonReader;
    
    protected String ddVersion;
    protected String ddDate;
    
    
    /**
     * Constructor
     * @param file PDS LDD JSON file to parse
     * @throws Exception
     */
    public BaseLddParser(File file) throws Exception
    {
        this.ddFile = file;
        jsonReader = new JsonReader(new FileReader(file));
    }

    /**
     * Returns LDD version
     * @return
     */
    public String getLddVersion()
    {
        return ddVersion;
    }
    
    /**
     * Returns LDD (creation) date
     * @return
     */
    public String getLddDate()
    {
        return ddDate;
    }
    
    /**
     * Parse PDS LDD JSON file
     * @throws Exception
     */
    public void parse() throws Exception
    {
        try
        {
            parseRoot();
        }
        finally
        {
            CloseUtils.close(jsonReader);
        }
    }
    
    
    /**
     * Parse root element
     * @throws Exception
     */
    private void parseRoot() throws Exception
    {
        jsonReader.beginArray();
        
        while(jsonReader.hasNext() && jsonReader.peek() != JsonToken.END_ARRAY)
        {
            jsonReader.beginObject();

            while(jsonReader.hasNext() && jsonReader.peek() != JsonToken.END_OBJECT)
            {
                String name = jsonReader.nextName();
                if("dataDictionary".equals(name))
                {
                    parseDataDic();
                }
                else
                {
                    jsonReader.skipValue();
                }
            }
            
            jsonReader.endObject();
        }
        
        jsonReader.endArray();
    }
    
    
    /**
     * Parse "dataDictionary" -> "classDictionary" subtree
     * @throws Exception
     */
    protected void parseClassDictionary() throws Exception
    {
        jsonReader.skipValue();
    }
    

    /**
     * Parse "dataDictionary" -> "attributeDictionary" subtree
     * @throws Exception
     */
    protected void parseAttributeDictionary() throws Exception
    {
        jsonReader.skipValue();
    }

    
    /**
     * Parse "dataDictionary" subtree
     * @throws Exception
     */
    private void parseDataDic() throws Exception
    {
        jsonReader.beginObject();

        while(jsonReader.hasNext() && jsonReader.peek() != JsonToken.END_OBJECT)
        {
            String name = jsonReader.nextName();
            
            if("Version".equals(name))
            {
                ddVersion = jsonReader.nextString();
            }
            else if("Date".equals(name))
            {
                ddDate = jsonReader.nextString();
            }
            else if("classDictionary".equals(name))
            {
                parseClassDictionary();
            }
            else if("attributeDictionary".equals(name))
            {
                parseAttributeDictionary();
            }
            else
            {
                jsonReader.skipValue();
            }
        }
        
        jsonReader.endObject();
    }

}