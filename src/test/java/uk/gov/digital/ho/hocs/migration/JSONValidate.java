package uk.gov.digital.ho.hocs.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JSONValidate {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

    @Test
    public void testCaseValidMessage() throws Exception {
        try (
                InputStream schemaStream = inputStreamFromClasspath("hocs-migration-schema.json");
                InputStream jsonStream = inputStreamFromClasspath("jsonMigrationExamples/valid-migration-message.json")
        ) {
            testSchemaValid(schemaStream, jsonStream);
        }
    }

    @Test
    public void testInvalidCaseData() throws Exception {
        try (
                InputStream schemaStream = inputStreamFromClasspath("hocs-migration-schema.json");
                InputStream jsonStream = inputStreamFromClasspath("jsonMigrationExamples/invalid-case-data-migration-message.json")
        ) {
            Set<ValidationMessage> validationMessages = testSchemaInvalid(schemaStream, jsonStream);
            System.out.println(validationMessages.toString());
            Set<String> expectedMessages = new HashSet<>();
            expectedMessages.add("$.caseData[0].value: integer found, string expected");
            expectedMessages.add("$.caseData[1].third: is not defined in the schema and the schema does not allow additional properties");
            expectedMessages.add("$.caseData[2].forth: is not defined in the schema and the schema does not allow additional properties");
            expectedMessages.add("$.additionalField: is not defined in the schema and the schema does not allow additional properties");

            assertTrue(checkForValidationMessage(validationMessages,expectedMessages));
        }
    }

    private void testSchemaValid(InputStream schemaStream, InputStream jsonStream) throws IOException {
        byte[] jsonBytes = jsonStream.readAllBytes();
        JsonNode json = objectMapper.readTree(jsonBytes);
        JsonSchema schema = schemaFactory.getSchema(schemaStream);
        Set<ValidationMessage> validationResult = schema.validate(json);
        if (validationResult.isEmpty()) {
            assertTrue(jsonBytes.length < 256000);
        } else {
            for (ValidationMessage validationMessage : validationResult) {
                System.out.println(validationMessage.getMessage());
            }
            fail();
        }
    }

    private boolean checkForValidationMessage (Set<ValidationMessage> validationMessages, Set<String> expectedMessages){
        if (validationMessages.size() != expectedMessages.size()) {
            return false;
        }
        for (String expectedMessage : expectedMessages){
            if (validationMessages.stream().noneMatch(o -> o.getMessage().equals(expectedMessage))){
                return false;
            }
        }
        return true;
    }

    private Set<ValidationMessage> testSchemaInvalid(InputStream schemaStream, InputStream jsonStream) throws IOException {
        JsonNode json = objectMapper.readTree(jsonStream);
        JsonSchema schema = schemaFactory.getSchema(schemaStream);
        return schema.validate(json);

    }
    private static InputStream inputStreamFromClasspath(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
}
