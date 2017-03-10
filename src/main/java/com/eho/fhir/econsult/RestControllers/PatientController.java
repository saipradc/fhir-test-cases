/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eho.fhir.econsult.RestControllers;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.eho.dynamodb.DynamoDBConnection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
///import com.eho.fhir.econsult.resources.Patient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
/**
 *
 * @author borna.jafarpour
 */

@RestController
@RequestMapping(value="/Patient")
public class PatientController {

    @RequestMapping(method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> patient_search(@RequestParam(required = false) String given, @RequestParam(required = false) String family, @RequestParam(required = false) String identifier, @RequestParam(required = false) String _format) throws Exception
    {
        if (_format == null)
            _format="json";
        if (!(_format.toLowerCase().equals("json") | _format.toLowerCase().equals("xml")))
            throw new Exception(_format + " is not supported");

            
        //first set of parameters with the letter changed to upper-case--dynamodb is case sensitive
        AmazonDynamoDB client = DynamoDBConnection.getDynamoDBClient();
        int total  = 0;
      
        Bundle bundle_resource = new Bundle();
        bundle_resource.setType(BundleTypeEnum.SEARCH_RESULTS);
///------------------------------------------------------------------------------------------------------------------        
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        String filter_expression="";
        filter_expression += DynamoDBConnection.create_search_exp(identifier,"identifiers",expressionAttributeValues);
        filter_expression += DynamoDBConnection.create_search_exp(given,"givens",expressionAttributeValues);
        filter_expression += DynamoDBConnection.create_search_exp(family,"families",expressionAttributeValues);

        ScanRequest scanRequest = new ScanRequest()
            .withTableName(DynamoDBConnection.PATIENT_TABLE)
            .withFilterExpression(filter_expression)
            //.withExpressionAttributeNames(expressionAttributeNames)
            .withExpressionAttributeValues(expressionAttributeValues);
        
        ScanResult result = client.scan(scanRequest);

////-----------------------------------------------------------------------------------------------------------------

        for (Map<String, AttributeValue> item : result.getItems()) {//add all the items to a bundle to be returned
            total++;
            int latest_version = Integer.valueOf(item.get("version").getN());
            String latest_text = item.get("text"+latest_version).getS();
            Patient this_resource = DynamoDBConnection.fCtx.newJsonParser().parseResource(Patient.class, latest_text);
            bundle_resource.addEntry(new Bundle.Entry().setResource(this_resource));
        }        
     
        bundle_resource.setTotal(total);
        String bundle_resource_string = "";
        if (_format.toLowerCase().equals("xml"))
            bundle_resource_string  = DynamoDBConnection.fCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle_resource);
        else
            bundle_resource_string  = DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle_resource);
        
        return new ResponseEntity(bundle_resource_string,HttpStatus.OK);
    }    
    @RequestMapping(value="/{id}", method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> patientGET(@PathVariable String id,@RequestParam(required = false) String _format) throws Exception{
        try {
            Item item = DynamoDBConnection.get_item_by_ID(id);
            if (item == null)
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            Integer last_version = item.getInt("version");


            if (_format == null)
                _format="json";
            if (!(_format.toLowerCase().equals("json") | _format.toLowerCase().equals("xml")))
                throw new Exception(_format + " is not supported");
            if (_format.toLowerCase().endsWith("json"))
                return new ResponseEntity(item.get("text" + last_version.toString()),HttpStatus.OK);
            else
            {
                Patient p = DynamoDBConnection.fCtx.newJsonParser().parseResource(Patient.class, last_version.toString());
                return new ResponseEntity(DynamoDBConnection.fCtx.newXmlParser().encodeResourceToString(p),HttpStatus.OK);
            }
            
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> patient_post( @RequestBody final  String patient_resource_string) throws Exception{ 
        String to_return ;
        try {
            Patient newPatient = DynamoDBConnection.fCtx.newJsonParser().parseResource(Patient.class, patient_resource_string);
            
            String thisid = newPatient.getId().getIdPart();
            if (thisid!=null)
                throw new Exception("To crate a resource, it should not contain an id. ids will be assigned by the server");            
            
            to_return = DynamoDBConnection.upload_resource(newPatient);    
            OperationOutcome.Issue is = new OperationOutcome.Issue();
            is.setSeverity(IssueSeverityEnum.INFORMATION);
            is.setDiagnostics("A new resource created by the following id " + to_return);
            OperationOutcome oo = new OperationOutcome();
            oo.addIssue(is);

            
            String is_string = DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
            return new ResponseEntity(is_string,HttpStatus.OK);        
            //return new ResponseEntity(to_return,HttpStatus.OK);
        } catch (Exception e) {
            to_return = e.getMessage();
            
            OperationOutcome.Issue is = new OperationOutcome.Issue();
            is.setSeverity(IssueSeverityEnum.ERROR);
            is.setDiagnostics("An error occured: " + to_return);
            OperationOutcome oo = new OperationOutcome();
            oo.addIssue(is);            
            
            return new ResponseEntity(DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo),HttpStatus.BAD_REQUEST);
        }
    }
    
    @RequestMapping(value="/{id}",method = RequestMethod.PUT, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> patient_put( @RequestBody final  String patient_resource_string,@PathVariable String id) throws Exception{ 
        
        String to_return;
        try {
            Patient newPatient = DynamoDBConnection.fCtx.newJsonParser().parseResource(Patient.class, patient_resource_string);//check if it is properly
            
            String thisid = newPatient.getId().getIdPart();
            
            if (thisid!=null && !thisid.equals(id))
                throw new Exception("the id in the URL ( "+ id+ " ) and the resource do not match the id in the resource (" +thisid+")");
            
            DynamoDBConnection.upload_resource(newPatient,id);    
            
            OperationOutcome.Issue is = new OperationOutcome.Issue();
            is.setSeverity(IssueSeverityEnum.INFORMATION);
            is.setDiagnostics("The following resource was sucessfully updated : " + id);
            OperationOutcome oo = new OperationOutcome();
            oo.addIssue(is);            
            return new ResponseEntity(DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo),HttpStatus.OK);

        } catch (Exception e) {
            to_return = e.getMessage();
            
            OperationOutcome.Issue is = new OperationOutcome.Issue();
            is.setSeverity(IssueSeverityEnum.ERROR);
            is.setDiagnostics("An error occured: " + to_return);
            OperationOutcome oo = new OperationOutcome();
            oo.addIssue(is);            
            return new ResponseEntity(DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo),HttpStatus.BAD_REQUEST);
        }
    }
    
}


//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    @RequestMapping(method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
//    public ResponseEntity<String> patient_search(@RequestParam(required = false) String given, @RequestParam(required = false) String family) throws Exception{
//        //first set of parameters with the letter changed to upper-case--dynamodb is case sensitive
//        int total  = 0;
//        System.out.println("goooooooooooooooooooooooooooooz");
//        
//        Bundle bundle_resource = new Bundle();
//        bundle_resource.setType(BundleTypeEnum.SEARCH_RESULTS);
//        
//        Map<String,String> lower_params = new HashMap<>();
//        Map<String,String> upper_params = new HashMap<>();        
//        Map<String,String> params = new HashMap<>(); 
//                
//        if (given!=null)
//        {
//            String lower_given = given.toLowerCase();
//            String upper_given = given.substring(0, 1).toUpperCase() + given.substring(1);
//            lower_params.put("#jsondocument.#name[0].given[0]", lower_given);
//            upper_params.put("#jsondocument.#name[0].given[0]", upper_given);
//        }
//        
//        if (family!=null)
//        {
//            String lower_family = family.toLowerCase();
//            String upper_family = family.substring(0, 1).toUpperCase() + family.substring(1);
//            lower_params.put("#jsondocument.#name[0].#family[0]", lower_family);
//            upper_params.put("#jsondocument.#name[0].#family[0]", upper_family);
//        }
////        if (identifier!=null)
////        {
////            params.put("#jsondocument.identifier", identifier);
////        }        
////        
////        if (given == null && family == null && identifier==null){
////            throw new Exception("Incorrect search expression");
////        }
//        //first set of parameters with the letter changed to lower-case
//        ScanResult lower_results = DynamoDBConnection.scan_dynamodb(lower_params, null);
//        for (Map<String, AttributeValue> item : lower_results.getItems()) {//add all the items to a bundle to be returned
//            total++;
//            int latest_version = Integer.valueOf(item.get("version").getN());
//            String latest_text = item.get("text"+latest_version).getS();
//            Patient this_resource = DynamoDBConnection.fCtx.newJsonParser().parseResource(Patient.class, latest_text);
//            bundle_resource.addEntry(new Bundle.Entry().setResource(this_resource));
//        }        
//        ScanResult upper_results = DynamoDBConnection.scan_dynamodb(upper_params, null);
//        for (Map<String, AttributeValue> item : upper_results.getItems()) {//add all the items to a bundle to be returned
//            total++;
//            int latest_version = Integer.valueOf(item.get("version").getN());
//            String latest_text = item.get("text"+latest_version).getS();
//            Patient this_resource = DynamoDBConnection.fCtx.newJsonParser().parseResource(Patient.class, latest_text);
//            bundle_resource.addEntry(new Bundle.Entry().setResource(this_resource));
//        }     
//        bundle_resource.setTotal(total);
//        String bundle_resource_string  = DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle_resource);
//        return new ResponseEntity(bundle_resource_string,HttpStatus.OK);
//    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////


//        if (identifier!=null) 
//        {
//            String this_exp="";
//            if (identifier.contains(","))
//            {
//                String [] identifiers = identifier.split(",");
//                for (int i = 0 ; i < identifiers.length;i++)
//                {
//                    expressionAttributeValues.put(":thisid"+i, new AttributeValue().withS(identifiers[i]));
//
//                    if (i > 0)
//                        this_exp+= " OR ";                        
//                    this_exp+= "contains(identifiers , :thisid"+i+ ")";
//                }
//            }else       
//            {
//                    expressionAttributeValues.put(":thisid", new AttributeValue().withS(identifier));
//                    this_exp += "(contains(identifiers , :thisid)) ";
//            }
//            filter_expression = "(" + this_exp + ")";
//        }
//        
//        if (given!=null)
//        {
//            expressionAttributeValues.put(":thisgiven", new AttributeValue().withS(given.toLowerCase()));
//            if (filter_expression != "")
//                filter_expression+= " AND ";
//            filter_expression+= "(contains(givens , :thisgiven))";
//        }
//        
//        if (family!=null)
//        {
//            expressionAttributeValues.put(":thisfamily", new AttributeValue().withS(family.toLowerCase()));
//            if (filter_expression != "")
//                filter_expression+= " AND ";
//            filter_expression+= "(contains(families , :thisfamily))";
//        }