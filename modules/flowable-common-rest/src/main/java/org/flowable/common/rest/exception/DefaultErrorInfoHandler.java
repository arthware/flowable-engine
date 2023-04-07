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

import org.flowable.common.engine.api.error.ClientErrorEnhancer;

/**
 * The default error info handler adds additional properties
 * to the returned {@link ErrorInfo} in case the exception implements the {@link ClientErrorEnhancer} interface.
 *
 * @author Arthur Hupka-Merle
 */
public class DefaultErrorInfoHandler implements ErrorInfoHandler {

    @Override
    public ErrorInfo handleErrorInfo(Exception handledException, ErrorInfo errorInfo) {
        if (handledException instanceof ClientErrorEnhancer) {
            ClientErrorEnhancer enhancer = (ClientErrorEnhancer) handledException;
            enhancer.enhanceErrorPayload(errorInfo::setProperty);
            String enhancedErrorMessage = enhancer.enhanceErrorMessage(errorInfo.getException());
            if (enhancedErrorMessage != null) {
                errorInfo.setException(enhancedErrorMessage);
            }
        }
        return errorInfo;
    }
}
