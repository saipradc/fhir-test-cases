/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.dynamotest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.base.resource.ResourceMetadataMap;
import ca.uhn.fhir.model.dstu2.composite.AddressDt;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Parameters;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.valueset.IdentifierUseEnum;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.OidDt;
import ca.uhn.fhir.model.primitive.PositiveIntDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.eho.dynamodb.DynamoDBConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.json.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharSet;
import org.hl7.fhir.instance.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.instance.model.BooleanType;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author borna.jafarpour
 */
public class FHIR_TEST {
   private static HashSet<String> get_patient_name(String patient,String nameType)
    {
        org.json.JSONObject p= new org.json.JSONObject(patient);
        JSONArray names = p.optJSONArray("name");
        HashSet<String> arr = new HashSet<String>();
        if (names!=null)
        {
            for (int k = 0 ; k < names.length() ; k++)
            {
                JSONArray givens = names.getJSONObject(k).optJSONArray(nameType);
                if (givens!=null)
                    for (int i = 0 ; i < givens.length();i++)
                    {
                        arr.add(givens.getString(i));
                    }
            }
        }
        return arr;
            
    }    
    public static void main(String[] args) throws Exception
    {
        String pat = new String(Files.readAllBytes(Paths.get("C:\\Users\\borna.jafarpour\\Documents\\NetBeansProjects\\eConsult-FHIR\\eho-fhir-econsult\\src\\main\\java\\test\\dynamotest\\patient.json")));
        Patient newPatient = DynamoDBConnection.fCtx.newJsonParser().parseResource(Patient.class, pat);
        System.out.println(DynamoDBConnection.fCtx.newXmlParser().encodeResourceToString(newPatient));
        
        
        
//
//        JSONObject ret = new JSONObject();
//        ret.put("meta", new JSONObject().put("profile", new JSONArray(new String[]{"http://ehealthontario.ca/API/FHIR/StructureDefinition/pcr-parameters-pixm-out|1.0"})));
//
//        System.out.println("----------->"+ret.toString());
        
//        Parameters pm = new Parameters();
//        ResourceMetadataMap rmm  = new ResourceMetadataMap();//.put(ResourceMetadataKeyEnum.PROFILES,"salaam" );
//        rmm.put(ResourceMetadataKeyEnum.PROFILES, new IdDt("http://ehealthontario.ca/API/FHIR/StructureDefinition/pcr-parameters-pixm-out|1.0"));
//        pm.setResourceMetadata(rmm);
//        System.out.println(DynamoDBConnection.fCtx.newJsonParser().encodeResourceToString(pm));
//        
        

        
//        String pat = new String(Files.readAllBytes(Paths.get("C:\\Users\\borna.jafarpour\\Documents\\NetBeansProjects\\eConsult-FHIR\\eho-fhir-econsult\\src\\main\\java\\test\\dynamotest\\patient.json")));
//        HashSet<String> givens = get_patient_name(pat,"given");
//        for (String g:givens)
//            System.out.println(g);
//        
//        System.out.println("---------------------------");
//        givens = get_patient_name(pat,"family");
//        for (String g:givens)
//            System.out.println(g);
        
        
        
//        ExtensionDt ext = new ExtensionDt();
//        ext.setModifier(false);
//        ext.setUrl(":* ;+");
//        //ext.setValue(new IdDt("123456789"));
//        ext.setValue(new StringDt(":* ;+"));
//        Patient p  = new Patient();
//        UriDt u = new IdDt(":* ;+");
//        
//
//        String pat = new String(Files.readAllBytes(Paths.get("C:\\Users\\borna.jafarpour\\Documents\\NetBeansProjects\\eConsult-FHIR\\eho-fhir-econsult\\src\\main\\java\\test\\dynamotest\\patient.json")));
//        Patient p2  = DynamoDBConnection.fCtx.newJsonParser().parseResource(Patient.class,pat);                
//        System.out.println("-------------->"  + p2.getId().getIdPart());
//        System.out.println("name"+p2.getResourceName());
//        ArrayList<String> ids = get_identifiers(pat);
//        for (String s:ids)
//            System.out.println(s);
        
//        JSONObject p= new JSONObject(pat);
//        JSONArray ids = p.optJSONArray("identifier");
//        
//        if (ids!=null)
//        {
//            String[] idsarr = new String[ids.length()];
//            for (int i = 0 ; i < ids.length() ; i++)
//            {
//                JSONObject identifier = ids.getJSONObject(i);
//                String value = identifier.getString("value");
//                String system = identifier.getString("system");
//                System.out.println(system + "|" + value);
//
//            }
//
//        }
        
        
        
        
    /*    Patient p  = new Patient();
          ExtensionDt ext = new ExtensionDt();
        ext.setModifier(false);
        ext.setUrl(DynamoDBConnection.PRIMARY_KEY_URL);
        //ext.setValue(new IdDt("123456789"));
        ext.setValue(new StringDt("12345"));
        p.addUndeclaredExtension(ext);            
        String ps = DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(p);
        System.out.println(ps);*/
       //Patient p2  = DynamoDBConnection.fCtx.newJsonParser().parseResource(Patient.class,pat1);
        //DynamoDBConnection.fCtx.newJsonParser().parseResource(Practitioner.class,prac1);;
        /*Map<String,String> map = new LinkedHashMap<>();
        map.put("resourceType", "Practitioner");
        System.out.println(map.toString());*/
        
        //Practitioner p = new Practitioner();
       
       /* Practitioner p = new Practitioner();
        System.out.println(DynamoDBConnection.fCtx.newJsonParser().encodeResourceToString(p));
        
        Patient pp = new Patient();
        System.out.println(DynamoDBConnection.fCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(pp));*/
        
       /* JSONObject jo = new JSONObject();
        jo.put("resourceType","Practitioner");
        
        JSONObject name_jo = new JSONObject();
        name_jo.put("given", "borna");
        name_jo.put("family", "jafarpour");
        jo.put("name",name_jo);
        
        System.out.println(jo.toString());
        
        HashMap<String, Object> lhm = new HashMap<>();
        lhm.put("resourceType", "practitioner");
        
        Map<String, String> name_lhm = new HashMap<>();
        name_lhm.put("given", "borna");
        name_lhm.put("family", "jafarpour");
        lhm.put("name",name_lhm);
        JSONObject jo2 = new JSONObject(lhm);
        System.out.println(jo2);*/
        
        
        /*FhirContext ctx = FhirContext.forDstu2();
 
// Create a FhirInstanceValidator and register it to a validator
        FhirValidator validator = ctx.newValidator();
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator();
        validator.registerValidatorModule(instanceValidator);
 

        Observation obs = new Observation();
    obs.getCode().addCoding().setSystem("http://loinc.org").setCode("12345-6");
    obs.setValue(new StringDt("This is a value"));
 
    // Validate
    ValidationResult result = validator.validateWithResult(obs);
 
    // Do we have any errors or fatal errors?
    System.out.println(result.isSuccessful()); // false

    // Show the issues
    for (SingleValidationMessage next : result.getMessages()) {
       System.out.println(" Next issue " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage());
    }    */    
        
        
    }
    
    private static ArrayList<String> get_identifiers(String patient){
        JSONObject p= new JSONObject(patient);
        JSONArray ids = p.optJSONArray("identifier");
        ArrayList<String> idsarr = new ArrayList<>();
        
        if (ids!=null)
        {
            for (int i = 0 ; i < ids.length() ; i++)
            {
                JSONObject identifier = ids.getJSONObject(i);
                String value = identifier.getString("value");
                String system = identifier.getString("system");
                idsarr.add(system + "|" + value);
            }
        }
        return idsarr;
    }    
 
public static String prac1 = "{\n" +
"  \"resourceType\": \"Practitioner\",\n" +
"  \"id\": \"example\",\n" +
"  \"text\": {\n" +
"    \"status\": \"generated\",\n" +
"    \"div\": \"<div>\\n      <p>Dr Adam Careful is a Referring Practitioner for Acme Hospital from 1-Jan 2012 to 31-Mar\\n        2012</p>\\n    </div>\"\n" +
"  },\n" +
"  \"identifier\": [\n" +
"    {\n" +
"      \"system\": \"http://www.acme.org/practitioners\",\n" +
"      \"value\": \"23\"\n" +
"    }\n" +
"  ],\n" +
"  \"active\": true,\n" +
"  \"name\": {\n" +
"    \"family\": [\n" +
"      \"Careful\"\n" +
"    ],\n" +
"    \"given\": [\n" +
"      \"Adam\"\n" +
"    ],\n" +
"    \"prefix\": [\n" +
"      \"Dr\"\n" +
"    ]\n" +
"  },\n" +
"  \"practitionerRole\": [\n" +
"    {\n" +
"      \"managingOrganization\": {\n" +
"        \"reference\": \"Organization/f001\"\n" +
"      },\n" +
"      \"role\": {\n" +
"        \"fhir_comments\": [\n" +
"          \"  Referring Practitioner for the first 3 months of 2012  \"\n" +
"        ],\n" +
"        \"coding\": [\n" +
"          {\n" +
"            \"system\": \"http://hl7.org/fhir/v2/0286\",\n" +
"            \"code\": \"RP\"\n" +
"          }\n" +
"        ]\n" +
"      },\n" +
"      \"period\": {\n" +
"        \"start\": \"2012-01-01\",\n" +
"        \"end\": \"2012-03-31\"\n" +
"      },\n" +
"      \"location\": [\n" +
"        {\n" +
"          \"reference\": \"Location/1\",\n" +
"          \"display\": \"South Wing, second floor\"\n" +
"        }\n" +
"      ],\n" +
"      \"healthcareService\": [\n" +
"        {\n" +
"          \"reference\": \"HealthcareService/example\"\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  ],\n" +
"  \"qualification\": [\n" +
"    {\n" +
"      \"identifier\": [\n" +
"        {\n" +
"          \"system\": \"http://example.org/UniversityIdentifier\",\n" +
"          \"value\": \"12345\"\n" +
"        }\n" +
"      ],\n" +
"      \"code\": {\n" +
"        \"text\": \"Bachelor of Science\"\n" +
"      },\n" +
"      \"period\": {\n" +
"        \"start\": \"1995\"\n" +
"      },\n" +
"      \"issuer\": {\n" +
"        \"display\": \"Example University\"\n" +
"      }\n" +
"    }\n" +
"  ]\n" +
"}";
    
}
