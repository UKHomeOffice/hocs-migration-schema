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
    
    private static InputStream inputStreamFromClasspath(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
}
