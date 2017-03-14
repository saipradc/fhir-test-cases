/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.dynamotest;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.OperationOutcome;
import com.eho.validation.PIXmValidator;
import com.phloc.schematron.ISchematronResource;
import com.phloc.schematron.xslt.SchematronResourceSCH;
import java.io.ByteArrayInputStream;
//import com.helger.schematron.ISchematronResource;
//import com.helger.schematron.pure.SchematronResourcePure;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.stream.StreamSource;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
//import com.helger.commons.state.EValidity;


/**
 *
 * @author borna.jafarpour
 */
public class Ph_schematron_test {
    public static void main(String[] args) throws Exception
    {
        
      //PIXmValidator pixmval = new PIXmValidator();
      //String res_add = "C:\\Users\\borna.jafarpour\\Desktop\\pcr-full-ig\\patient1.xml";
       String res_add = "C:\\Users\\borna.jafarpour\\Desktop\\pcr-full-ig\\Patient-pcr-patient-read-example.json";
     // String res_add = "C:\\Users\\borna.jafarpour\\Desktop\\pcr-full-ig\\pixm-in1.xml";
        
      //"C:\\Users\\borna.jafarpour\\Desktop\\pcr-full-ig\\Patient-pcr-patient-read-example.xml"
      String pat = new String(Files.readAllBytes(Paths.get(res_add)));
      OperationOutcome oo  = new OperationOutcome();
      PIXmValidator.validate_json(pat, "patient", oo);
        System.out.println(FhirContext.forDstu2().newJsonParser().setPrettyPrint(true).encodeResourceToString(oo));
    }
    private static void chert() throws Exception
    {
        //        http://stackoverflow.com/questions/10126256/where-can-i-find-a-java-implementation-of-an-iso-schematron-validator
//        http://phax.github.io/ph-schematron/
        String  schema_address =         "C:\\Users\\borna.jafarpour\\Desktop\\pcr-full-ig\\pcr-parameters-pixm-in.sch";
        String file_address = "C:\\Users\\borna.jafarpour\\Desktop\\pcr-full-ig\\Parameters-pcr-parameters-pixm-in-example.json";
          final ISchematronResource aResSCH = SchematronResourceSCH.fromFile (schema_address);
        if (!aResSCH.isValidSchematron ())
          throw new IllegalArgumentException("Invalid Schematron!");
        String exampleString="123";
        InputStream stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));

        SchematronOutputType stot =  aResSCH.applySchematronValidationToSVRL (new StreamSource(file_address));
        int err_num  = stot.getActivePatternAndFiredRuleAndFailedAssertCount();
        
        System.out.println("err_num->" + err_num);
        Pattern ptext = Pattern.compile("(?<=\\stext=)[^;]+(?=;)");
        Pattern plocation = Pattern.compile("(?<=\\slocation=)[^;]+(?=;)");
        for (int i = 0 ; i < err_num;i++)
        {
            System.out.println("----------------------------------");
            String rule = stot.getActivePatternAndFiredRuleAndFailedAssertAtIndex(i).toString();
            Matcher mt = ptext.matcher(rule);
            Matcher ml = plocation.matcher(rule);
            String text=null;
            String location=null;            
            if (mt.find())
                text = mt.group();
            
            if (ml.find())
                location = ml.group();
            
            if (text != null)
            {
                System.out.println(text +"  <>  " + location);
            }
            
           
            
            System.out.println();
        } 
    }
            

    
//public static boolean validateXMLViaPureSchematron (File aSchematronFile, File aXMLFile) throws Exception { 
//  final ISchematronResource aResPure = SchematronResourcePure.fromFile (aSchematronFile);
//  if (!aResPure.isValidSchematron ()) 
//    throw new IllegalArgumentException ("Invalid Schematron!"); 
//  return aResPure.getSchematronValidity(new StreamSource(aXMLFile)).isValid ();
//}    
    //-----------------------------------------------------------------------------------------
//private static void chert()
//{
//    
//        FhirContext ctx = FhirContext.forDstu2();
//        // Create and populate a new patient object
//        Patient p = new Patient();
//        p.addName().addFamily("Smith").addGiven("John").addGiven("Q");
//        p.addIdentifier().setSystem("urn:foo:identifiers").setValue("12345");
//        p.addTelecom().setSystem(ContactPointSystemEnum.PHONE).setValue("416 123-4567");
//
//        // Request a validator and apply it
//        FhirValidator val = ctx.newValidator();
//
//        // Create the Schema/Schematron modules and register them. Note that
//        // you might want to consider keeping these modules around as long-term
//        // objects: they parse and then store schemas, which can be an expensive
//        // operation.
//        IValidatorModule module1 = new SchemaBaseValidator(ctx);
//        IValidatorModule module2 = new SchematronBaseValidator(12);
//        val.registerValidatorModule(module1);
//        val.registerValidatorModule(module2);
//
//        ValidationResult result = val.validateWithResult(p);
//        if (result.isSuccessful()) {
//
//           System.out.println("Validation passed");
//
//        } else {
//           // We failed validation!
//           System.out.println("Validation failed");
//        }
//
//        // The result contains a list of "messages"
//        List<SingleValidationMessage> messages = result.getMessages();
//        for (SingleValidationMessage next : messages) {
//           System.out.println("Message:");
//           System.out.println(" * Location: " + next.getLocationString());
//           System.out.println(" * Severity: " + next.getSeverity());
//           System.out.println(" * Message : " + next.getMessage());
//        }
//
//        // You can also convert the results into an OperationOutcome resource
//        OperationOutcome oo = (OperationOutcome) result.toOperationOutcome();
//        String results = ctx.newXmlParser().setPrettyPrint(true).encodeResourceToString(oo);
//        System.out.println(results);  
// -----------------------------------------------------------------------------------------
//}


//    static JSONObject  SchemaValidation(Source xmlFile,URL schemaFile) throws Exception
//    {
//        JSONObject error = new JSONObject();
//        SchemaFactory schemaFactory = SchemaFactory
//            .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//        Schema schema = schemaFactory.newSchema(schemaFile);
//        Validator validator = schema.newValidator();
//        
//        MyErrorHandler myErrorHandler = new MyErrorHandler();
//        validator.setErrorHandler(myErrorHandler);        
//        //try {
//        validator.validate(xmlFile);
//        //System.out.println(myErrorHandler.getErrors());
//        error.put(xmlFile.getSystemId(), myErrorHandler.getErrors());
//        //error.put(null, (getSender(file).item(0)).getTextContent());
//        
//        /*} catch (SAXParseException e) {
//          System.out.println(xmlFile.getSystemId() + " is NOT valid");
//          System.out.println("Reasons:\n[" + e.getLineNumber()+","+ e.getColumnNumber() + "]--> Message : "+ e.getLocalizedMessage());
//        }*/
//          return error;
//    }    
    
}
