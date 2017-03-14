/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eho.validation;

import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import ca.uhn.fhir.model.dstu2.resource.Parameters;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.IssueSeverityEnum;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.eho.dynamodb.DynamoDBConnection;
import static com.eho.dynamodb.DynamoDBConnection.USE_PROXY;
import com.phloc.schematron.ISchematronResource;
import com.phloc.schematron.xslt.SchematronResourceSCH;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.stream.StreamSource;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;

/**
 *
 * @author borna.jafarpour
 */
public class PIXmValidator {
    
    private static final String patient_schema_address = "C:\\Users\\borna.jafarpour\\Desktop\\pcr-full-ig\\pcr-patient-response.sch";
    private static final String pixm_schema_address = "C:\\Users\\borna.jafarpour\\Desktop\\pcr-full-ig\\pcr-parameters-pixm-in.sch";
    private static final Pattern ptext = Pattern.compile("(?<=\\stext=)[^;]+(?=;)");
    private static final Pattern plocation = Pattern.compile("(?<=\\slocation=)[^;]+(?=;)");
    
    
    private static ISchematronResource pixm_sch;
    private static ISchematronResource patient_sch;
    
    static{
        
            pixm_sch = SchematronResourceSCH.fromFile (pixm_schema_address);
            patient_sch = SchematronResourceSCH.fromFile (patient_schema_address);
            if (!pixm_sch.isValidSchematron() || !patient_sch.isValidSchematron())
                throw new IllegalArgumentException ("Invalid Schematron!");
    }

            
    public static void validate_json(String resource_string, String res_type, OperationOutcome oo) throws Exception
    {
        String str_to_process;

        ArrayList<String> errors = new ArrayList<>();
        ISchematronResource aResSCH;
        
        if (res_type.toLowerCase().contains("patient"))
        {
            Patient p =  DynamoDBConnection.fCtx.newJsonParser().parseResource(Patient.class, resource_string);
            str_to_process = DynamoDBConnection.fCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(p);
            aResSCH = patient_sch;
        }
        else
        {
            Parameters p =  DynamoDBConnection.fCtx.newJsonParser().parseResource(Parameters.class, resource_string);
            str_to_process = DynamoDBConnection.fCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(p);
            aResSCH = pixm_sch;
        }
        System.out.println("=========================================str_to_process==========================================");

        System.out.println(str_to_process);
        InputStream istream = new ByteArrayInputStream(str_to_process.getBytes(StandardCharsets.UTF_8));
        
        SchematronOutputType stot =  aResSCH.applySchematronValidationToSVRL(new StreamSource(istream));
        System.out.println(stot);
        int err_num  = stot.getActivePatternAndFiredRuleAndFailedAssertCount();
        
        System.out.println("err_num->" + err_num);
        for (int i = 0 ; i < err_num;i++)
        {
            String rule = stot.getActivePatternAndFiredRuleAndFailedAssertAtIndex(i).toString();
            Matcher mt = ptext.matcher(rule);
            Matcher ml = plocation.matcher(rule);
            String text=null;
            String location=null;            
            if (mt.find())
                text = mt.group();
            
            if (ml.find())
            {
                location = ml.group();
                location = location.replace("[namespace-uri()='http://hl7.org/fhir']", "");
            }
            

            if (text != null)
            {
                oo.addIssue(new OperationOutcome.Issue().addLocation(location).setDiagnostics(text).setSeverity(IssueSeverityEnum.WARNING));
                errors.add(text);
                errors.add(location);
            }
            
        }           
        //return errors;
    }
    
    
public static void validate_xml(String resource_string, String res_type, OperationOutcome oo) throws Exception
    {

        ArrayList<String> errors = new ArrayList<>();
        ISchematronResource aResSCH;
        
        if (res_type.toLowerCase().contains("patient"))
            aResSCH = patient_sch;
        else
            aResSCH = pixm_sch;
        System.out.println("=========================================str_to_process==========================================");

        InputStream istream = new ByteArrayInputStream(resource_string.getBytes(StandardCharsets.UTF_8));
        
        SchematronOutputType stot =  aResSCH.applySchematronValidationToSVRL(new StreamSource(istream));
        System.out.println(stot);
        int err_num  = stot.getActivePatternAndFiredRuleAndFailedAssertCount();
        
        for (int i = 0 ; i < err_num;i++)
        {
            String rule = stot.getActivePatternAndFiredRuleAndFailedAssertAtIndex(i).toString();
            Matcher mt = ptext.matcher(rule);
            Matcher ml = plocation.matcher(rule);
            String text=null;
            String location=null;            
            if (mt.find())
                text = mt.group();
            
            if (ml.find())
            {
                location = ml.group();
                location = location.replace("[namespace-uri()='http://hl7.org/fhir']", "");

            }
            

            if (text != null)
            {
                oo.addIssue(new OperationOutcome.Issue().addLocation(location).setDiagnostics(text).setSeverity(IssueSeverityEnum.WARNING));
                errors.add(text);
                errors.add(location);
            }
            
        }           
        //return errors;
    }    
    
}

