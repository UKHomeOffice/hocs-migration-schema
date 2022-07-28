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
    public void existing() throws Exception {
        try (
                InputStream schemaStream = inputStreamFromClasspath("hocs-migration-schema.json");
                InputStream jsonStream = inputStreamFromClasspath("jsonMigrationExamples/cms.json")
        ) {
            testSchemaValid(schemaStream, jsonStream);
        }
    }

    @Test
    public void testInvalidCaseData() throws Exception {
        try (
                InputStream schemaStream = inputStreamFromClasspath("hocs-migration-schema.json");
                InputStream jsonStream = inputStreamFromClasspath("jsonMigrationExamples/invalid-case-data.json")
        ) {
            Set<ValidationMessage> validationMessages = testSchemaInvalid(schemaStream, jsonStream);

            Set<String> expectedMessages = new HashSet<>();
            expectedMessages.add("$.case.caseData[1]: there must be a maximum of 2 items in the array");

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
