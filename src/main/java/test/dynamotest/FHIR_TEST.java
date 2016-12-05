/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.dynamotest;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.valueset.IdentifierUseEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.OidDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.util.json.JSONObject;
import com.eho.dynamodb.DynamoDBConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.JsonObject;
import org.hl7.fhir.instance.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseDatatype;

/**
 *
 * @author borna.jafarpour
 */
public class FHIR_TEST {
    public static void main(String[] args) throws Exception
    {
        
        
        ExtensionDt ext = new ExtensionDt();
        ext.setModifier(false);
        ext.setUrl(":* ;+");
        //ext.setValue(new IdDt("123456789"));
        ext.setValue(new StringDt(":* ;+"));
        Patient p  = new Patient();
        UriDt u = new IdDt(":* ;+");
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
    
 public static String pat1 = "{\n" +
"  \"resourceType\": \"Patient\",\n" +
"  \"id\": \"example\",\n" +
"  \"text\": {\n" +
"    \"status\": \"generated\",\n" +
"    \"div\": \"<div>\\n      \\n      <table>\\n        \\n        <tbody>\\n          \\n          <tr>\\n            \\n            <td>Name</td>\\n            \\n            <td>Peter James \\n              <b>Chalmers</b> (&quot;Jim&quot;)\\n            </td>\\n          \\n          </tr>\\n          \\n          <tr>\\n            \\n            <td>Address</td>\\n            \\n            <td>534 Erewhon, Pleasantville, Vic, 3999</td>\\n          \\n          </tr>\\n          \\n          <tr>\\n            \\n            <td>Contacts</td>\\n            \\n            <td>Home: unknown. Work: (03) 5555 6473</td>\\n          \\n          </tr>\\n          \\n          <tr>\\n            \\n            <td>Id</td>\\n            \\n            <td>MRN: 12345 (Acme Healthcare)</td>\\n          \\n          </tr>\\n        \\n        </tbody>\\n      \\n      </table>    \\n    \\n    </div>\"\n" +
"  },\n" +
"  \"identifier\": [\n" +
"    {\n" +
"      \"fhir_comments\": [\n" +
"        \"   MRN assigned by ACME healthcare on 6-May 2001   \"\n" +
"      ],\n" +
"      \"use\": \"usual\",\n" +
"      \"type\": {\n" +
"        \"coding\": [\n" +
"          {\n" +
"            \"system\": \"http://hl7.org/fhir/v2/0203\",\n" +
"            \"code\": \"MR\"\n" +
"          }\n" +
"        ]\n" +
"      },\n" +
"      \"system\": \"urn:oid:1.2.36.146.595.217.0.1\",\n" +
"      \"value\": \"12345\",\n" +
"      \"period\": {\n" +
"        \"start\": \"2001-05-06\"\n" +
"      },\n" +
"      \"assigner\": {\n" +
"        \"display\": \"Acme Healthcare\"\n" +
"      }\n" +
"    }\n" +
"  ],\n" +
"  \"active\": true,\n" +
"  \"name\": [\n" +
"    {\n" +
"      \"fhir_comments\": [\n" +
"        \"   Peter James Chalmers, but called \\\"Jim\\\"   \"\n" +
"      ],\n" +
"      \"use\": \"official\",\n" +
"      \"family\": [\n" +
"        \"Chalmers\"\n" +
"      ],\n" +
"      \"given\": [\n" +
"        \"Peter\",\n" +
"        \"James\"\n" +
"      ]\n" +
"    },\n" +
"    {\n" +
"      \"use\": \"usual\",\n" +
"      \"given\": [\n" +
"        \"Jim\"\n" +
"      ]\n" +
"    }\n" +
"  ],\n" +
"  \"telecom\": [\n" +
"    {\n" +
"      \"fhir_comments\": [\n" +
"        \"   home communication details aren't known   \"\n" +
"      ],\n" +
"      \"use\": \"home\"\n" +
"    },\n" +
"    {\n" +
"      \"system\": \"phone\",\n" +
"      \"value\": \"(03) 5555 6473\",\n" +
"      \"use\": \"work\"\n" +
"    }\n" +
"  ],\n" +
"  \"gender\": \"male\",\n" +
"  \"_gender\": {\n" +
"    \"fhir_comments\": [\n" +
"      \"   use FHIR code system for male / female   \"\n" +
"    ]\n" +
"  },\n" +
"  \"birthDate\": \"1974-12-25\",\n" +
"  \"_birthDate\": {\n" +
"    \"extension\": [\n" +
"      {\n" +
"        \"url\": \"http://hl7.org/fhir/StructureDefinition/patient-birthTime\",\n" +
"        \"valueDateTime\": \"1974-12-25T14:35:45-05:00\"\n" +
"      }\n" +
"    ]\n" +
"  },\n" +
"  \"deceasedBoolean\": false,\n" +
"  \"address\": [\n" +
"    {\n" +
"      \"use\": \"home\",\n" +
"      \"type\": \"both\",\n" +
"      \"line\": [\n" +
"        \"534 Erewhon St\"\n" +
"      ],\n" +
"      \"city\": \"PleasantVille\",\n" +
"      \"district\": \"Rainbow\",\n" +
"      \"state\": \"Vic\",\n" +
"      \"postalCode\": \"3999\",\n" +
"      \"period\": {\n" +
"        \"start\": \"1974-12-25\"\n" +
"      }\n" +
"    }\n" +
"  ],\n" +
"  \"contact\": [\n" +
"    {\n" +
"      \"relationship\": [\n" +
"        {\n" +
"          \"coding\": [\n" +
"            {\n" +
"              \"system\": \"http://hl7.org/fhir/patient-contact-relationship\",\n" +
"              \"code\": \"partner\"\n" +
"            }\n" +
"          ]\n" +
"        }\n" +
"      ],\n" +
"      \"name\": {\n" +
"        \"family\": [\n" +
"          \"du\",\n" +
"          \"Marché\"\n" +
"        ],\n" +
"        \"_family\": [\n" +
"          {\n" +
"            \"extension\": [\n" +
"              {\n" +
"                \"fhir_comments\": [\n" +
"                  \"   the \\\"du\\\" part is a family name prefix (VV in iso 21090)   \"\n" +
"                ],\n" +
"                \"url\": \"http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier\",\n" +
"                \"valueCode\": \"VV\"\n" +
"              }\n" +
"            ]\n" +
"          },\n" +
"          null\n" +
"        ],\n" +
"        \"given\": [\n" +
"          \"Bénédicte\"\n" +
"        ]\n" +
"      },\n" +
"      \"telecom\": [\n" +
"        {\n" +
"          \"system\": \"phone\",\n" +
"          \"value\": \"+33 (237) 998327\"\n" +
"        }\n" +
"      ],\n" +
"      \"gender\": \"female\",\n" +
"      \"period\": {\n" +
"        \"start\": \"2012\",\n" +
"        \"_start\": {\n" +
"          \"fhir_comments\": [\n" +
"            \"   The contact relationship started in 2012   \"\n" +
"          ]\n" +
"        }\n" +
"      }\n" +
"    }\n" +
"  ],\n" +
"  \"managingOrganization\": {\n" +
"    \"reference\": \"Organization/1\"\n" +
"  }\n" +
"}";
}
