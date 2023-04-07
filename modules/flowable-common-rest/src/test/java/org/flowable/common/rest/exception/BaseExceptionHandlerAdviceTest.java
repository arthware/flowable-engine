/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.common.rest.exception;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.flowable.common.engine.api.FlowableForbiddenException;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.common.engine.api.FlowableIllegalStateException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.api.FlowableTaskAlreadyClaimedException;
import org.flowable.common.engine.api.error.ClientErrorEnhancer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Filip Hrisafov
 */
class BaseExceptionHandlerAdviceTest {

    protected BaseExceptionHandlerAdvice handlerAdvice = new BaseExceptionHandlerAdvice();
    protected TestController testController = new TestController();

    protected MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(testController)
                .setControllerAdvice(handlerAdvice)
                .build();
    }

    @AfterEach
    void tearDown() {
        handlerAdvice.setSendFullErrorException(true);
    }

    @Test
    void handleFlowableContentNotSupportedException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableContentNotSupportedException("test content not supported");

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isUnsupportedMediaType())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Content is not supported',"
                        + "  exception: 'test content not supported'"
                        + "}");

    }

    @Test
    void handleFlowableContentNotSupportedExceptionWithoutSendFullErrorException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableContentNotSupportedException("other test content not supported");
        handlerAdvice.setSendFullErrorException(false);

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isUnsupportedMediaType())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Content is not supported',"
                        + "  exception: 'other test content not supported'"
                        + "}");

    }

    @Test
    void handleFlowableConflictException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableConflictException("process already exists");

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Conflict',"
                        + "  exception: 'process already exists'"
                        + "}");

    }

    @Test
    void handleFlowableConflictExceptionWithoutSendFullErrorException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableConflictException("task already exists");
        handlerAdvice.setSendFullErrorException(false);

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Conflict',"
                        + "  exception: 'task already exists'"
                        + "}");

    }

    @Test
    void handleFlowableObjectNotException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableObjectNotFoundException("process not found");

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Not found',"
                        + "  exception: 'process not found'"
                        + "}");

    }

    @Test
    void handleFlowableObjectNotExceptionWithoutSendFullErrorException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableObjectNotFoundException("task not found");
        handlerAdvice.setSendFullErrorException(false);

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Not found',"
                        + "  exception: 'task not found'"
                        + "}");

    }

    @Test
    void handleFlowableForbiddenException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableForbiddenException("no access to process");

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Forbidden',"
                        + "  exception: 'no access to process'"
                        + "}");

    }

    @Test
    void handleFlowableForbiddenExceptionWithoutSendFullErrorException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableForbiddenException("no access to task");
        handlerAdvice.setSendFullErrorException(false);

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Forbidden',"
                        + "  exception: 'no access to task'"
                        + "}");

    }

    @Test
    void handleFlowableIllegalArgumentException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableIllegalArgumentException("process name is mandatory");

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Bad request',"
                        + "  exception: 'process name is mandatory'"
                        + "}");

    }

    @Test
    void handleFlowableIllegalArgumentExceptionWithoutSendFullErrorException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableIllegalArgumentException("task name is mandatory");
        handlerAdvice.setSendFullErrorException(false);

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Bad request',"
                        + "  exception: 'task name is mandatory'"
                        + "}");

    }

    @Test
    void handleFlowableIllegalStateException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableIllegalStateException("process not active");

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Bad request',"
                        + "  exception: 'process not active'"
                        + "}");

    }

    @Test
    void handleFlowableIllegalStateExceptionWithoutSendFullErrorException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableIllegalStateException("task not active");
        handlerAdvice.setSendFullErrorException(false);

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Bad request',"
                        + "  exception: 'task not active'"
                        + "}");

    }

    @Test
    void handleFlowableTaskAlreadyClaimedException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableTaskAlreadyClaimedException("task-1", "tester");

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Task was already claimed',"
                        + "  exception: \"Task 'task-1' is already claimed by someone else.\""
                        + "}");

    }

    @Test
    void handleFlowableTaskAlreadyClaimedExceptionWithoutSendFullErrorException() throws Exception {
        testController.exceptionSupplier = () -> new FlowableTaskAlreadyClaimedException("task-2", "tester");
        handlerAdvice.setSendFullErrorException(false);

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Task was already claimed',"
                        + "  exception: \"Task 'task-2' is already claimed by someone else.\""
                        + "}");

    }

    @Test
    void handleUnknownException() throws Exception {
        testController.exceptionSupplier = () -> new RuntimeException("Some unknown message");

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Internal server error',"
                        + "  exception: 'Some unknown message'"
                        + "}");

    }

    @Test
    void handleUnknownExceptionWithoutSendFullErrorException() throws Exception {
        testController.exceptionSupplier = () -> new RuntimeException("Some unknown message");
        handlerAdvice.setSendFullErrorException(false);

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isInternalServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Internal server error',"
                        + "  exception: '${json-unit.any-string}'"
                        + "}");
        assertThatJson(body)
                .inPath("exception").asString().startsWith("Error with ID: ");
    }

    @Test
    void handleErrorPropertiesProvidingExceptionWithJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode errorDetailsNode = mapper.createObjectNode();
        ArrayNode arrayNode = errorDetailsNode.putArray("invalidFields");
        arrayNode.add(mapper.createObjectNode().put("field", "firstName").put("reason", "min length must be 2"));
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("simpleString", "invalid");
        properties.put("failedValidation", errorDetailsNode);

        testController.exceptionSupplier = () -> new ErrorPropertyProvidingException("Validation failed", properties);
        handlerAdvice.setSendFullErrorException(false);

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Bad request',"
                        + "  exception: 'Validation failed',"
                        + "  simpleString: 'invalid',"
                        + "  failedValidation: {"
                        + "    invalidFields: [{field: 'firstName', reason: 'min length must be 2'}]"
                        + "  }"
                        + "}");

        ErrorInfo errorInfo = new ObjectMapper().readValue(body, ErrorInfo.class);
        assertThat(errorInfo.getException()).isEqualTo("Validation failed");
        assertThat(errorInfo.getMessage()).isEqualTo("Bad request");
        assertThat(errorInfo.getProperties()).containsKey("failedValidation");
    }

    @Test
    void handleErrorPropertiesProvidingExceptionWithList() throws Exception {
        List<String> strings = Arrays.asList("msg1", "msg2");
        testController.exceptionSupplier = () -> new ErrorPropertyProvidingException("Validation failed", strings);
        handlerAdvice.setSendFullErrorException(false);

        String body = mockMvc.perform(get("/"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(body)
                .isEqualTo("{"
                        + "  message: 'Bad request',"
                        + "  exception: 'Validation failed',"
                        + "  errorProperty: ['msg1', 'msg2']"
                        + "}");

        ErrorInfo errorInfo = new ObjectMapper().readValue(body, ErrorInfo.class);
        assertThat(errorInfo.getException()).isEqualTo("Validation failed");
        assertThat(errorInfo.getMessage()).isEqualTo("Bad request");
        assertThat(errorInfo.getProperties()).containsEntry("errorProperty", strings);
    }

    @RestController
    static class TestController {

        protected Supplier<RuntimeException> exceptionSupplier = () -> new RuntimeException("default exception");

        @GetMapping("/")
        public void execute() {
            throw exceptionSupplier.get();
        }

    }

    static class ErrorPropertyProvidingException extends FlowableIllegalArgumentException implements ClientErrorEnhancer {

        Object properties;

        public ErrorPropertyProvidingException(String message, Object properties) {
            super(message);
            this.properties = properties;
        }

        @Override
        public void enhanceErrorPayload(BiConsumer<String, Object> errorProperties) {
            if (properties instanceof Map) {
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) properties).entrySet()) {
                    errorProperties.accept(entry.getKey(), entry.getValue());
                }
            } else {
                errorProperties.accept("errorProperty", properties);
            }
        }
    }
}