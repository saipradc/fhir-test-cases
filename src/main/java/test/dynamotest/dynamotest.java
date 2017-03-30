/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.dynamotest;

import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.parser.IParser;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.util.TableUtils;


import com.eho.dynamodb.DynamoDBConnection;
import static com.eho.dynamodb.DynamoDBConnection.PATIENT_TABLE;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;


/**
 *
 * @author borna.jafarpour
 */
public class dynamotest {
    public static void main(String[] args) 
    {
         /*String file="patient-test.json";
         JSONObject patient =   new JSONObject(readFile(file));
         IParser json_parser =  DynamoDBConenction.fCtx.newJsonParser();
         Patient p = json_parser.parseResource(Patient.class, patient.toString());
         //System.out.println(json_parser.encodeResourceToString(p));
         patient.put(DynamoDBConenction.PRIMARY_KEY, /*"8bb46c10-74c4-4700-9386-067827468e6f"*/ ///"123123" );
         //System.out.println("----> " + DynamoDBConenction.upload_resource(patient.toString()).getPutItemResult());
//         System.out.println("----> " + DynamoDBConenction.update_resource(patient.toString()).getUpdateItemResult());
         //DynamoDBConenction.upload_resource(patient.toString());
         

        /*JSONObject jo = new JSONObject(patient3);
        Map<String,Object> result = new ObjectMapper().readValue(jo.toString(), LinkedHashMap.class);
        Item i = new Item();
        i.withJSON("1", patient3);
        System.out.println(patient3);
        System.out.println("=========================================================");
        System.out.println(i.toJSONPretty());*/
        
        //test_dynamo_db_search();
        //test_dynamo_db_query2();
//        AmazonDynamoDB dynamoDBClient = DynamoDBConnection.getDynamoDBClient();
//        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
//        Table table = dynamoDB.getTable("gooooz10");
//        
//        
//        System.out.println("TABLEEEE ->" + table);
        
            AmazonDynamoDB client = DynamoDBConnection.getDynamoDBClient();
            System.out.println("Creating table");            
            client.setEndpoint("http://localhost:8000"); 

            CreateTableRequest ctr = new CreateTableRequest()
                    .withTableName("angoozpiss")
                    .withProvisionedThroughput(new ProvisionedThroughput(new Long(10), new Long(10)))
                    .withKeySchema(new KeySchemaElement().withAttributeName("dynamodb-id").withKeyType(KeyType.HASH))
                     .withAttributeDefinitions(new AttributeDefinition().withAttributeName("dynamodb-id").withAttributeType(ScalarAttributeType.S));
            TableUtils.createTableIfNotExists(client, ctr);
        
    }
    
    public static void test_dynamo_db_query2()
    {
        AmazonDynamoDB client = DynamoDBConnection.getDynamoDBClient();

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        Map<String, String> expressionAttributeNames = new HashMap<>();

        expressionAttributeValues.put(":thisid", new AttributeValue().withS("urn:oid:2.16.840.1.113883.19.5|12345"));

        ScanRequest scanRequest = new ScanRequest()
            .withTableName(DynamoDBConnection.PATIENT_TABLE)
            .withFilterExpression("(contains(identifiers , :thisid))")
            //.withExpressionAttributeNames(expressionAttributeNames)
            .withExpressionAttributeValues(expressionAttributeValues);
        
        ScanResult result = client.scan(scanRequest);
        System.out.println("Count --> "  +result.getCount());
        for (Map<String, AttributeValue> item : result.getItems()) {
            System.out.println(item.toString());
        }

    }   
    

    
    public static void test_dynamo_db_query3()
    {
        AmazonDynamoDB client = DynamoDBConnection.getDynamoDBClient();

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        Map<String, String> expressionAttributeNames = new HashMap<>();

        expressionAttributeValues.put(":given", new AttributeValue().withS("Henry"));
        expressionAttributeNames.put("#name","name")  ; 
        expressionAttributeNames.put("#jsondocument", "json-document");

        ScanRequest scanRequest = new ScanRequest()
            .withTableName(DynamoDBConnection.PATIENT_TABLE)
            .withFilterExpression("(#jsondocument.#name[0].given[0] = :given) ")
                .withExpressionAttributeNames(expressionAttributeNames)
            .withExpressionAttributeValues(expressionAttributeValues);
        
        ScanResult result = client.scan(scanRequest);
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        for (Map<String, AttributeValue> item : result.getItems()) {
            System.out.println(item.toString());
        }
           System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");

    }
    
private static String readFile( String file ) throws IOException {
    /*BufferedReader reader = new BufferedReader( new FileReader (file));
    String         line = null;
    StringBuilder  stringBuilder = new StringBuilder();
    String         ls = System.getProperty("line.separator");

    while( ( line = reader.readLine() ) != null ) {
        stringBuilder.append( line );
        stringBuilder.append( ls );
    }

    return stringBuilder.toString();*/
    String s = "";
    
    try {
            s =  new Scanner(new File(file)).useDelimiter("\\Z").next();

    } catch (Exception e) {
        
        System.err.println(e.getMessage());
    }
    
    return s;

}    
private static String patient3 = "{ \"resourceType\": \"Patient\", \"id\": \"example\", \"text\": { \"status\": \"generated\", \"div\": \"<div>\\n \\n <table>\\n \\n <tbody>\\n \\n <tr>\\n \\n <td>Name</td>\\n \\n <td>Peter James \\n <b>Chalmers</b> (&quot;Jim&quot;)\\n </td>\\n \\n </tr>\\n \\n <tr>\\n \\n <td>Address</td>\\n \\n <td>534 Erewhon, Pleasantville, Vic, 3999</td>\\n \\n </tr>\\n \\n <tr>\\n \\n <td>Contacts</td>\\n \\n <td>Home: unknown. Work: (03) 5555 6473</td>\\n \\n </tr>\\n \\n <tr>\\n \\n <td>Id</td>\\n \\n <td>MRN: 12345 (Acme Healthcare)</td>\\n \\n </tr>\\n \\n </tbody>\\n \\n </table> \\n \\n </div>\" }, \"identifier\": [ { \"fhir_comments\": [ \" MRN assigned by ACME healthcare on 6-May 2001 \" ], \"use\": \"usual\", \"type\": { \"coding\": [ { \"system\": \"http://hl7.org/fhir/v2/0203\", \"code\": \"MR\" } ] }, \"system\": \"urn:oid:1.2.36.146.595.217.0.1\", \"value\": \"12345\", \"period\": { \"start\": \"2001-05-06\" }, \"assigner\": { \"display\": \"Acme Healthcare\" } } ], \"active\": true, \"name\": [ { \"fhir_comments\": [ \" Peter James Chalmers, but called \\\"Jim\\\" \" ], \"use\": \"official\", \"family\": [ \"Chalmers\" ], \"given\": [ \"Peter\", \"James\" ] }, { \"use\": \"usual\", \"given\": [ \"Jim\" ] } ], \"telecom\": [ { \"fhir_comments\": [ \" home communication details aren't known \" ], \"use\": \"home\" }, { \"system\": \"phone\", \"value\": \"(03) 5555 6473\", \"use\": \"work\" } ], \"gender\": \"male\", \"_gender\": { \"fhir_comments\": [ \" use FHIR code system for male / female \" ] }, \"birthDate\": \"1974-12-25\", \"_birthDate\": { \"extension\": [ { \"url\": \"http://hl7.org/fhir/StructureDefinition/patient-birthTime\", \"valueDateTime\": \"1974-12-25T14:35:45-05:00\" } ] }, \"deceasedBoolean\": false, \"address\": [ { \"use\": \"home\", \"type\": \"both\", \"line\": [ \"534 Erewhon St\" ], \"city\": \"PleasantVille\", \"district\": \"Rainbow\", \"state\": \"Vic\", \"postalCode\": \"3999\", \"period\": { \"start\": \"1974-12-25\" } } ], \"contact\": [ { \"relationship\": [ { \"coding\": [ { \"system\": \"http://hl7.org/fhir/patient-contact-relationship\", \"code\": \"partner\" } ] } ], \"name\": { \"family\": [ \"du\", \"Marché\" ], \"_family\": [ { \"extension\": [ { \"fhir_comments\": [ \" the \\\"du\\\" part is a family name prefix (VV in iso 21090) \" ], \"url\": \"http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier\", \"valueCode\": \"VV\" } ] }, null ], \"given\": [ \"Bénédicte\" ] }, \"telecom\": [ { \"system\": \"phone\", \"value\": \"+33 (237) 998327\" } ], \"gender\": \"female\", \"period\": { \"start\": \"2012\", \"_start\": { \"fhir_comments\": [ \" The contact relationship started in 2012 \" ] } } } ], \"managingOrganization\": { \"reference\": \"Organization/1\" } }";
}
