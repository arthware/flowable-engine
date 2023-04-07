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
package org.flowable.common.engine.api.error;

import java.util.function.BiConsumer;

/**
 This callback interface is intended for enhancing error messages and/pr properties that are sent back to clients.
 The enhancement method is typically invoked within central exception handlers to enable exception instances
 to augment the client response object with additional data.
 It's particularly useful when there is a need to provide additional structured information as part of the error payload.
 For instance, it can be used in scenarios involving validation errors, where a set of error messages needs to be sent back to the client in a structured format.
 * <p>
 * Example:
 * <pre>
 * class MyException extends FlowableIllegalArgumentException implements ClientErrorEnhancer {
 *
 *   MyException(String message, List failedValidationMessages) {
 *       super(message);
 *       this.failedValidationMessages = failedValidationMessages;
 *   }
 *
 *    public void enhanceErrorPayload(BiConsumer errorProperties) {
 *         // add additional context information to the error object returned to the client
 *         errorProperties.accept("validationMessages", this.failedValidationMessages);
 *    }
 * }
 * </pre>
 * </p>
 *  @author Arthur Hupka-Merle
 */
public interface ClientErrorEnhancer {

    /**
     * @see ClientErrorEnhancer
     * @param errorProperties the consumer of the additional error context information. Typically, the
     * client response object.
     */
    void enhanceErrorPayload(BiConsumer<String, Object> errorProperties);

    default String enhanceErrorMessage(String currentMessage) {
        return currentMessage;
    }
}
