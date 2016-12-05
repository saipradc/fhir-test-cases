/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eho.fhir.econsult.RestControllers;


import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.valueset.BundleTypeEnum;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.eho.dynamodb.DynamoDBConnection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author borna.jafarpour
 */
@RestController
@RequestMapping(value="/practitioner")

public class PractitionerController {
    
    
@RequestMapping(method=RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> practitioner_search(@RequestParam(required = false) String given,@RequestParam(required = false) String family) throws Exception{
        //first set of parameters with the letter changed to upper-case--dynamodb is case sensitive
        int total  = 0;
        
        Bundle bundle_resource = new Bundle();
        bundle_resource.setType(BundleTypeEnum.SEARCH_RESULTS);
        
        Map<String,String> lower_params = new HashMap<>();
        Map<String,String> upper_params = new HashMap<>();        
        
        if (given!=null)
        {
            String lower_given = given.substring(0, 1).toLowerCase() + given.substring(1);
            String upper_given = given.substring(0, 1).toUpperCase() + given.substring(1);
            lower_params.put("#jsondocument.#name[0].given[0]", lower_given);
            upper_params.put("#jsondocument.#name[0].given[0]", upper_given);
        }
        
        if (family!=null)
        {
            String lower_family = family.substring(0, 1).toLowerCase() + family.substring(1);
            String upper_family = family.substring(0, 1).toUpperCase() + family.substring(1);
            lower_params.put("#jsondocument.#name[0].#family[0]", lower_family);
            upper_params.put("#jsondocument.#name[0].#family[0]", upper_family);
        }
        
        //first set of parameters with the letter changed to lower-case
        ScanResult lower_results = DynamoDBConnection.scan_dynamodb(lower_params, null);
        for (Map<String, AttributeValue> item : lower_results.getItems()) {//add all the items to a bundle to be returned
            total++;
            int latest_version = Integer.valueOf(item.get("version").getN());
            String latest_text = item.get("text"+latest_version).getS();
            Practitioner this_resource = DynamoDBConnection.fCtx.newJsonParser().parseResource(Practitioner.class, latest_text);
            bundle_resource.addEntry(new Bundle.Entry().setResource(this_resource));
        }        
        ScanResult upper_results = DynamoDBConnection.scan_dynamodb(upper_params, null);
        for (Map<String, AttributeValue> item : upper_results.getItems()) {//add all the items to a bundle to be returned
            total++;
            int latest_version = Integer.valueOf(item.get("version").getN());
            String latest_text = item.get("text"+latest_version).getS();
            Practitioner this_resource = DynamoDBConnection.fCtx.newJsonParser().parseResource(Practitioner.class, latest_text);
            bundle_resource.addEntry(new Bundle.Entry().setResource(this_resource));
        }     
        bundle_resource.setTotal(total);
        String bundle_resource_string  = DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle_resource);
        return new ResponseEntity(bundle_resource_string,HttpStatus.OK);
    }    
      @RequestMapping(value="/{id}", method=RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> practitionerGET(@PathVariable String id) throws Exception{
        Item item = DynamoDBConnection.get_item_by_ID(id);
        if (item == null)
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        Integer last_version = item.getInt("version");
        return new ResponseEntity(item.get("text" + last_version.toString()),HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> practitioner_post( @RequestBody final  String practitioner_resource_string) throws Exception{ 
        String to_return ;
        try {
            Practitioner new_practitioner = DynamoDBConnection.fCtx.newJsonParser().parseResource(Practitioner.class, practitioner_resource_string);
            to_return = DynamoDBConnection.upload_resource(new_practitioner);    
            return new ResponseEntity(to_return,HttpStatus.OK);
        } catch (Exception e) {
            to_return = e.getMessage();
            return new ResponseEntity(to_return,HttpStatus.BAD_REQUEST);
        }
    }
      
    @RequestMapping(value="/{id}",method = RequestMethod.PUT, produces = "text/plain;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> practitioner_put( @RequestBody final  String practitioner_resource_string,@PathVariable String id) throws Exception
    { 
        String to_return;
        try {
            Practitioner newPatient = DynamoDBConnection.fCtx.newJsonParser().parseResource(Practitioner.class, practitioner_resource_string);//check if it is properly
            to_return = DynamoDBConnection.upload_resource(newPatient,id);    
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(to_return,HttpStatus.OK);
    } 
    
}
