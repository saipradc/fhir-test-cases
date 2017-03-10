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
import ca.uhn.fhir.model.dstu2.valueset.IssueTypeEnum;
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
//@RequestMapping(value="/Patient")
public class PatientController {

    @RequestMapping(method=RequestMethod.GET, produces = "application/json;charset=UTF-8",value="/Patient")
    public ResponseEntity<String> patient_search(@RequestParam(required = false) String given, @RequestParam(required = false) String family, @RequestParam(required = false) String identifier, @RequestParam(required = false) String _format) throws Exception
    {
        if (_format == null)
            _format="json";
        if (!(_format.toLowerCase().equals("json") | _format.toLowerCase().equals("xml")))
            throw new Exception(_format + " format is not supported");
            
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
        String bundle_resource_string ;
        if (_format.toLowerCase().equals("xml"))
            bundle_resource_string  = DynamoDBConnection.fCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(bundle_resource);
        else
            bundle_resource_string  = DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle_resource);
        
        return new ResponseEntity(bundle_resource_string,HttpStatus.OK);
    }    
    @RequestMapping(value="/Patient/{id}", method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> patientGET(@PathVariable String id,@RequestParam(required = false) String _format) throws Exception{
        try {
            if (_format == null)
                _format =  "json";
            if (!_format.equals("json") && !_format.equals("xml") )
                throw new Exception(_format + "format is not supported");
            
            Item item = DynamoDBConnection.get_item_by_ID(id);
            if (item == null)
            {
                OperationOutcome oo = new OperationOutcome();
                oo.addIssue(new OperationOutcome.Issue().setSeverity(IssueSeverityEnum.ERROR)
                        .setCode(IssueTypeEnum.PROCESSING_FAILURE).setDiagnostics("Patient/"+id+" does not exist"));
                String ret ="";
                
                if (_format .equals( "json"))
                    ret = DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
                else if  (_format.equals("xml"))
                    ret = DynamoDBConnection.fCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(oo);  
                
                return new ResponseEntity(ret,HttpStatus.NOT_FOUND);
            }
            Integer last_version = item.getInt("version");

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
            OperationOutcome oo = new OperationOutcome();
                oo.addIssue(new OperationOutcome.Issue().setSeverity(IssueSeverityEnum.ERROR)
                        .setCode(IssueTypeEnum.PROCESSING_FAILURE).setDiagnostics(e.getMessage()));
                String ret ="";
                
                if (_format .equals( "xml"))
                    ret = DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
                else if  (_format.equals("json"))
                    ret = DynamoDBConnection.fCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(oo);                 
            return new ResponseEntity(ret,HttpStatus.BAD_REQUEST);
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
    
    @RequestMapping(value="/Patient/{id}",method = RequestMethod.PUT, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> patient_put( @RequestBody final  String patient_resource_string,@PathVariable String id) throws Exception
    { 
        
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
    
@RequestMapping(value="/**", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> handle_error() throws Exception
    {
            OperationOutcome.Issue is = new OperationOutcome.Issue();
            is.setSeverity(IssueSeverityEnum.ERROR);
            is.setCode(IssueTypeEnum.NOT_FOUND);
            is.setDiagnostics("You shouldn't be here! Only /Patient and /Patient/$ihe-pix are supported" );
            OperationOutcome oo = new OperationOutcome();
            oo.addIssue(is); 
            return new ResponseEntity(DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo),HttpStatus.BAD_REQUEST);
    }    
    
    
}
