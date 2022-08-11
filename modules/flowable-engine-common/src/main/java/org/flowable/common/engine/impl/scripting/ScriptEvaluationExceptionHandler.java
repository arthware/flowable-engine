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
package org.flowable.common.engine.impl.scripting;

public interface ScriptEvaluationExceptionHandler {

    /**
     * Handles exception during script evaluation.
     * The handler should rethrow the exception which should bubble up.
     * The handler can also decide to not throw out an exception and return null
     * or an object, which is used as script return value instead.
     *
     * @param errorTrace the produced trace object holding optional meta data for the failed script invocation.
     * @param exception the exception caught by the script engine
     * @return optional object which, when returned, is used as script return value. In case this handler decides to not rethrow an exception
     * and provide an error fallback value instead.
     */
    Object handleException(ScriptTrace errorTrace, Throwable exception);

}
