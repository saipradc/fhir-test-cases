/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eho.dynamodb;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hl7.fhir.instance.validation.InstanceValidator;

/**
 *
 * @author borna.jafarpour
 */
public class DynamoDBConnection {
    public static FhirContext fCtx = FhirContext.forDstu2Hl7Org();
    public static final String PATIENT_TABLE = "eConsult";
    public static final String PRIMARY_KEY = "dynamodb-id";
    public static final String PRIMARY_KEY_URL = "www.ehealthontario.on.ca/fhir/eConsult/primaryKey";
    public static final String JSON_TEXT_URL = "www.ehealthontario.on.ca/fhir/eConsult/jsontext";

    public static final String PROXY_IP = "10.61.128.178";
    public static final int PROXY_PORT = 8080;
    
    private static final AmazonDynamoDBClient dynamoDBClient;
    static{
        ClientConfiguration cc = new ClientConfiguration();
        cc.setProxyHost(DynamoDBConnection.PROXY_IP);
        cc.setProxyPort(DynamoDBConnection.PROXY_PORT);
         dynamoDBClient = new AmazonDynamoDBClient(cc);
    }
    public static AmazonDynamoDBClient getDynamoDBClient()
    {
        return dynamoDBClient;
    }
    
   public static Item get_item_by_ID(String id)
    {
        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
        Table table = dynamoDB.getTable(PATIENT_TABLE);
        Item retreived_item = table.getItem(PRIMARY_KEY,id);
        return retreived_item;
    }
    public static PutItemOutcome upload_resource_old(String resource) throws Exception
    {
        String id ;
        JSONObject json_resource = new JSONObject(resource);
        //does the resource have a primary key?
        if (json_resource.has(PRIMARY_KEY))//if it does not have a primary key, create one using uuid
            id = json_resource.getString(PRIMARY_KEY);
        else
            id = UUID.randomUUID().toString();
        
        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
        Table table = dynamoDB.getTable(PATIENT_TABLE);
        
        //lets retreive based on the key. if key invalid (not assigned yet) nullis returned.
        Item retreived_item = table.getItem(PRIMARY_KEY,id);
        if (retreived_item == null)//if null instantiate it
        {
            retreived_item = new Item();
            retreived_item.withPrimaryKey(PRIMARY_KEY, id);
        }
        
        Integer new_version = retreived_item.getInt("version") + 1;
        retreived_item.withInt("version", new_version);
        
        Item item_to_upload = Item.fromJSON(retreived_item.toJSONPretty()).withJSON("Document", resource);
        PutItemSpec putItemSpec = new PutItemSpec()
                .withItem(item_to_upload)
                .withReturnValues(ReturnValue.NONE);
        return table.putItem(putItemSpec);
    }
    private static String add_uuid(BaseResource resource)//returns existing primary key is it already exists
    {
        return add_primary_as_extension(resource, UUID.randomUUID().toString());
    }
    private static String add_primary_as_extension(BaseResource resource, String uuid)//returns existing primary key is it already exists
    {
        ExtensionDt ext = new ExtensionDt();
        ext.setModifier(false);
        ext.setUrl(DynamoDBConnection.PRIMARY_KEY_URL);
        ext.setValue(new StringDt(uuid));
        resource.addUndeclaredExtension(ext);            
        return uuid;
    }
    private static void add_string_as_extension(BaseResource resource, String text)//returns existing primary key is it already exists
    {
        ExtensionDt ext = new ExtensionDt();
        ext.setModifier(false);
        ext.setUrl(JSON_TEXT_URL);
        ext.setValue(new IdDt(text));
        resource.addUndeclaredExtension(ext);            
    }

    
    public static String upload_resource( BaseResource  resource ) throws Exception
    {
        String primary_key = add_uuid(resource);
        return upload_resource(resource,primary_key);
    }
    public static String upload_resource( BaseResource  resource, String primary_key /* if no primary key in case of post, send null*/ ) throws Exception
    {
        String id = add_primary_as_extension(resource,primary_key);
        String resource_string = DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);;
        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
        Table table = dynamoDB.getTable(PATIENT_TABLE);
        
        //lets retreive based on the key. if key invalid (not assigned yet) nullis returned.
        Item retreived_item = table.getItem(PRIMARY_KEY,id);
        if (retreived_item == null)//if null instantiate it
        {
            retreived_item = new Item();
            retreived_item.withPrimaryKey(PRIMARY_KEY, id);
            retreived_item.withInt("version", -1);
        }
        
        Integer new_version = retreived_item.getInt("version") + 1;        
        retreived_item.withInt("version", new_version);
        
        
                
        Item item_to_upload = retreived_item//Item.fromJSON(retreived_item.toJSONPretty())
                .withString("text" +new_version.toString() , resource_string)
                .withMap("json-document", new ObjectMapper().readValue(resource_string, LinkedHashMap.class));
        PutItemSpec putItemSpec = new PutItemSpec()
                .withItem(item_to_upload);
        table.putItem(putItemSpec);
        return id;
    }
    
    
    public static ItemCollection<QueryOutcome> query_dynamodb(QuerySpec spec )
    {
        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
        Table table = dynamoDB.getTable(PATIENT_TABLE);
        ItemCollection<QueryOutcome> items = table.query(spec);
        return items;
    }
    
    public static ScanResult scan_dynamodb(Map<String,String> strings, Map<String, String> numbers)
    {
        Map<String, AttributeValue> expression_values = new HashMap<>();
        Map<String, String> expression_names = new HashMap<>();
        
        
        if (strings.keySet().contains("#jsondocument.#name[0].given[0]") | strings.keySet().contains("#jsondocument.#name[0].#family[0]"))
            expression_names.put("#name","name"); 
        
        if (strings.keySet().contains("#jsondocument.#name[0].#family[0]"))
            expression_names.put("#family","family"); 
        
        expression_names.put("#jsondocument", "json-document");

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(DynamoDBConnection.PATIENT_TABLE);
        
        int i = 0 ;
        String filter_expression = "";
        for (String path : strings.keySet())
        {
            String thisVal = strings.get(path);
            expression_values.put(":stringval"+i, new AttributeValue().withS(thisVal));
            if (filter_expression.equals(""))
                filter_expression += "(" + path + " = :stringval"+i++ +")";
            else
                filter_expression += " AND (" + path + " = :stringval"+i++ +")";
        }
         scanRequest.withExpressionAttributeNames(expression_names)
                    .withExpressionAttributeValues(expression_values)
                    .withFilterExpression(filter_expression);
        return dynamoDBClient.scan(scanRequest);
                
    }

    
    public static UpdateItemOutcome update_resource(String resource) throws Exception
    {
        String id ;
        JSONObject json_resource = new JSONObject(resource);
        //does the resource have a primary key?
        if (json_resource.has(PRIMARY_KEY))//if it does not have a primary key, create one using uuid
            id = json_resource.getString(PRIMARY_KEY);
        else
            id = UUID.randomUUID().toString();
        
        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
        Table table = dynamoDB.getTable(PATIENT_TABLE);
        
        //lets retreive based on the key. if key invalid (not assigned yet) nullis returned.
        Item retreived_item = table.getItem(PRIMARY_KEY,id);
        if (retreived_item == null)//if null instantiate it
        {
            retreived_item = new Item();
            retreived_item.withPrimaryKey(PRIMARY_KEY, id);
        }
        
        Integer new_version = retreived_item.getInt("version") + 1;        
        retreived_item.withInt("version", new_version);
        String new_version_str= new_version.toString();
        
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey(PRIMARY_KEY,id)
                .withUpdateExpression("SET " + new_version_str+ "= :newval")
            .withValueMap(new ValueMap()
                .withString(":newval", resource))
            .withReturnValues(ReturnValue.ALL_NEW);
        
        return table.updateItem(updateItemSpec);
    }
    

    private static int create_new_version_numbe_old(Item item) throws Exception
    {
        JSONObject json_resource = new JSONObject(item.toJSONPretty());
        Iterator<String> itr= json_resource.keys();
        int max_version = Integer.MIN_VALUE;
        while (itr.hasNext())
        {
            String nextString = itr.next();
            
            if (nextString.matches("[0-9]+"))//if a ltter does not exist in the key.this means it is a version of the rseource
            {
                int thisValue = Integer.valueOf(nextString);
                if (thisValue > max_version)
                    max_version= thisValue;
            }
        }
        
        if (max_version == Integer.MIN_VALUE)
            return 0;
        else
            return ++max_version;
    }    
    public static String get_extension(BaseResource br, String url)
    {
        List<ExtensionDt> resourceExts = br.getUndeclaredExtensionsByUrl(url);
        if (!resourceExts.isEmpty())
            return resourceExts.get(0).getValue().toString();
        else
            return null;
    }
    
    
    
}