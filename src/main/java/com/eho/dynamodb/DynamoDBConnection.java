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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.util.TableUtils;



import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;


/**
 *
 * @author borna.jafarpour
 */
public class DynamoDBConnection {
    public static FhirContext fCtx = FhirContext.forDstu2();
    public static final String PATIENT_TABLE = "eConsult";
    public static final String PRIMARY_KEY = "dynamodb-id";
    //public static final String PRIMARY_KEY_URL = "www.ehealthontario.on.ca/fhir/eConsult/primaryKey";
    public static final String JSON_TEXT_URL = "www.ehealthontario.on.ca/fhir/eConsult/jsontext";

    public static final String PROXY_IP = "10.61.128.178"; //former proxy. not used
    public static final int PROXY_PORT = 8080;
    public static final boolean USE_PROXY = false;   
    public static final boolean LOCAL_DYNAMO_DB = true;   
    
    
    private static final AmazonDynamoDBClient dynamoDBClient;
    public static String create_search_exp(String criteria_values,String criteria_name ,Map<String, AttributeValue> expressionAttributeValues)
    {
        String filter_expression  = "";
        boolean previous_expression = false;
        if (!expressionAttributeValues.isEmpty())
            previous_expression = true;

        if (criteria_values!=null) 
        {
            if (criteria_values.contains(","))
            {
                String [] identifiers = criteria_values.split(",");
                for (int i = 0 ; i < identifiers.length;i++)
                {
                    expressionAttributeValues.put(":this"+criteria_name+i, new AttributeValue().withS(identifiers[i].toLowerCase()));
                    if (i > 0)
                        filter_expression+= " OR ";                        
                    filter_expression+= "contains(" +criteria_name+  ", :this"+criteria_name+i+ ")";
                }
            }else       
            {
                    expressionAttributeValues.put(":this"+criteria_name, new AttributeValue().withS(criteria_values.toLowerCase()));
                    filter_expression += "contains("+criteria_name+" , :this"+criteria_name+")";
            }
            filter_expression = "(" + filter_expression + ")";
            if (previous_expression)
                filter_expression = " AND " + filter_expression;
        }
        return filter_expression;
    }

    static{
        
        ClientConfiguration cc = new ClientConfiguration();
        if (USE_PROXY)
        {
            cc.setProxyHost(DynamoDBConnection.PROXY_IP);
            cc.setProxyPort(DynamoDBConnection.PROXY_PORT);
        }
        
        dynamoDBClient = new AmazonDynamoDBClient(cc);
        
        if (LOCAL_DYNAMO_DB)//we will try to crea the table needed for this task
        {
            dynamoDBClient.setEndpoint("http://localhost:8000"); 
            CreateTableRequest ctr = new CreateTableRequest()
                    .withTableName("eConsult")
                    .withProvisionedThroughput(new ProvisionedThroughput(new Long(10), new Long(10)))
                    .withKeySchema(new KeySchemaElement().withAttributeName("dynamodb-id").withKeyType(KeyType.HASH))
                     .withAttributeDefinitions(new AttributeDefinition().withAttributeName("dynamodb-id").withAttributeType(ScalarAttributeType.S));
            TableUtils.createTableIfNotExists(dynamoDBClient, ctr);            
        }


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
   
    private static String generate_uuid()
    {
        return UUID.randomUUID().toString();
    }

    
    public static String upload_resource( BaseResource  resource ) throws Exception
    {
        String primary_key = generate_uuid();
        resource.setId(primary_key);
        upload_resource(resource,primary_key);
        return primary_key;
    }
    public static void upload_resource( BaseResource  resource, String id /* if no primary key in case of post, send null*/ ) throws Exception
    {
        //String id = add_primary_as_extension(primary_key);
        String resource_string = DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);;
        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
        Table table = dynamoDB.getTable(PATIENT_TABLE);
        
        //lets retreive based on the key. if key invalid (not assigned yet) null is returned.
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
        
        if (resource.getResourceName().equals("Patient"))
        {
            HashSet<String> ids  = get_patient_identifiers(resource_string);
            item_to_upload.withStringSet("identifiers", ids);
            HashSet<String> givens  = get_patient_name(resource_string,"given");
            item_to_upload.withStringSet("givens", givens);
            HashSet<String> families  = get_patient_name(resource_string,"family");
            item_to_upload.withStringSet("families", families);
        }
        
        PutItemSpec putItemSpec = new PutItemSpec()
                .withItem(item_to_upload);
        table.putItem(putItemSpec);
    }
    
   private static HashSet<String> get_patient_name(String patient,String nameType)
    {
        org.json.JSONObject p= new org.json.JSONObject(patient);
        JSONArray names = p.optJSONArray("name");
        HashSet<String> arr = new HashSet<>();
        if (names!=null)
        {
            for (int k = 0 ; k < names.length() ; k++)
            {
                JSONArray givens = names.getJSONObject(k).optJSONArray(nameType);
                if (givens!=null)
                    for (int i = 0 ; i < givens.length();i++)
                        arr.add(givens.getString(i).toLowerCase());
            }
        }
        return arr;
            
    }        
    public static HashSet<String> get_patient_identifiers(String patient){
        org.json.JSONObject p= new org.json.JSONObject(patient);
        JSONArray ids = p.optJSONArray("identifier");
        HashSet<String> idsarr = new HashSet<>();
        
        if (ids!=null)
        {
            for (int i = 0 ; i < ids.length() ; i++)
            {
                org.json.JSONObject identifier = ids.getJSONObject(i);
                String value = identifier.optString("value");
                String system = identifier.optString("system");
                idsarr.add(system.toLowerCase() + "|" + value);
            }
        }
        return idsarr;
    }    
    
    
    public static ItemCollection<QueryOutcome> query_dynamodb(QuerySpec spec )
    {
        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
        Table table = dynamoDB.getTable(PATIENT_TABLE);
        ItemCollection<QueryOutcome> items = table.query(spec);
        return items;
    }

    
//    public static UpdateItemOutcome update_resource(String resource) throws Exception
//    {
//        String id ;
//        JSONObject json_resource = new JSONObject(resource);
//        //does the resource have a primary key?
//        if (json_resource.has(PRIMARY_KEY))//if it does not have a primary key, create one using uuid
//            id = json_resource.getString(PRIMARY_KEY);
//        else
//            id = UUID.randomUUID().toString();
//        
//        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
//        Table table = dynamoDB.getTable(PATIENT_TABLE);
//        
//        //lets retreive based on the key. if key invalid (not assigned yet) nullis returned.
//        Item retreived_item = table.getItem(PRIMARY_KEY,id);
//        if (retreived_item == null)//if null instantiate it
//        {
//            retreived_item = new Item();
//            retreived_item.withPrimaryKey(PRIMARY_KEY, id);
//        }
//        
//        Integer new_version = retreived_item.getInt("version") + 1;        
//        retreived_item.withInt("version", new_version);
//        String new_version_str= new_version.toString();
//        
//        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
//                .withPrimaryKey(PRIMARY_KEY,id)
//                .withUpdateExpression("SET " + new_version_str+ "= :newval")
//            .withValueMap(new ValueMap()
//                .withString(":newval", resource))
//            .withReturnValues(ReturnValue.ALL_NEW);
//        
//        return table.updateItem(updateItemSpec);
//    }
   
    
}
