/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eho.fhir.pixm.RestControllers;

import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.base.resource.ResourceMetadataMap;
import ca.uhn.fhir.model.dstu2.composite.AgeDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.Parameters;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.IdDt;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.eho.dynamodb.DynamoDBConnection;
import com.eho.validation.PIXmValidator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sun.awt.image.ImageCache;

/**
 *
 * @author borna.jafarpour
 */
@RestController
@RequestMapping(value="/Patient/$ihe-pix")
public class IHEpixmController {
//    
//    @RequestMapping(method=RequestMethod.GET, produces = "application/json;charset=UTF-8")
//    public ResponseEntity<String> patient_search(@RequestParam(required = false) String given, @RequestParam(required = false) String family, @RequestParam(required = false) String identifier) throws Exception
//    {    
//        return new ResponseEntity("test ihe-pixm goooz",HttpStatus.OK);
//    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> ihe_pixm_post( @RequestBody final  String parameters) throws Exception{ 
            //Parameters posted_parameters = DynamoDBConnection.fCtx.newJsonParser().parseResource(Parameters.class, parameters);
       JSONObject params = new JSONObject(parameters);
        try {
            JSONArray parameter = params.optJSONArray("parameter");
            if (parameter == null)
                throw new Exception("No parameter was provided");
            
            if (parameter.length() > 1)
                throw new Exception("Too many paramaters");
            
            if (!"sourceIdentifier".equals(parameter.getJSONObject(0).optString("name")))
                throw new Exception("Incorrect parameter type. SourceIdentifier is expected");

            
            String system = parameter.optJSONObject(0).optJSONObject("valueIdentifier").optString("system").toLowerCase();
            String value = parameter.optJSONObject(0).optJSONObject("valueIdentifier").optString("value");
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            String filter_expression = DynamoDBConnection.create_search_exp(system+"|"+value,"identifiers",expressionAttributeValues);
            
            AmazonDynamoDB client = DynamoDBConnection.getDynamoDBClient();
            ScanRequest scanRequest = new ScanRequest()
                .withTableName(DynamoDBConnection.PATIENT_TABLE)
                .withFilterExpression(filter_expression)
                //.withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues);

            ScanResult result = client.scan(scanRequest);            
        

            Parameters pm = new Parameters();
            ResourceMetadataMap rmm  = new ResourceMetadataMap();//.put(ResourceMetadataKeyEnum.PROFILES,"salaam" );
            rmm.put(ResourceMetadataKeyEnum.PROFILES, new IdDt("http://ehealthontario.ca/API/FHIR/StructureDefinition/pcr-parameters-pixm-out|1.0"));
            pm.setResourceMetadata(rmm);            
            
            HashSet<String> identifiers_so_far = new HashSet<>();

            for (Map<String, AttributeValue> item : result.getItems()) {//add all the items to a bundle to be returned
                int latest_version = Integer.valueOf(item.get("version").getN());
                String item_id = item.get("dynamodb-id").getS();
                pm.addParameter(new Parameters.Parameter().setName("targetId").setValue(new IdDt(item_id)));
                
                String latest_text = item.get("text"+latest_version).getS();
                HashSet<String> identifiers = DynamoDBConnection.get_patient_identifiers(latest_text);
                for (String thisid : identifiers)
                {
                    system =thisid.substring(0, thisid.indexOf("|"));
                    value = thisid.substring(thisid.indexOf("|")+1);
                    if (!identifiers_so_far.contains(system + "|" + value))//avoiding duplicate identifiers
                    {
                        identifiers_so_far.add(system + "|" + value);
                        pm.addParameter(new Parameters.Parameter().setName("targetIdentifier").setValue( new IdentifierDt(system, value)));
                    }
                }
            }        
            return new ResponseEntity(DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(pm),HttpStatus.OK);
        } catch (Exception e) {
            OperationOutcome oo = new OperationOutcome().addIssue(new OperationOutcome.Issue().setSeverity(IssueSeverityEnum.ERROR).setDiagnostics(e.getMessage()));
            PIXmValidator.validate_json(parameters, "Parameters", oo);
            return new ResponseEntity(DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(oo),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }    
    
}
