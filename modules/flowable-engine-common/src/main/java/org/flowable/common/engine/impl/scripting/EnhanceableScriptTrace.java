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

/**
 * Allows enhancing of  {@link ScriptTrace ScriptTraces} with additional meta information.
 */
public interface EnhanceableScriptTrace {

    /**
     * Adds a tracing id tag to this script trace.
     * Tags are used to identify the origin of a script invocation.
     * <p>
     * ID tags are tags, which uniquely identify an entity in a system and could grow
     * infinitely through the lifetime of a system.
     * Those tags won't be used for use cases, where classification of script invocations
     * is required. Only for logging.
     * </p>
     */
    default EnhanceableScriptTrace addTraceIdTag(String key, String value) {
       return addTraceTag(ScriptTrace.TraceTag.idTag(key, value));
    }

    /**
     * Adds a tracing tag to this script trace.
     * Tags are used to identify the origin of a script invocation and can also
     * be used to classify script invocations e.g. to distinguish different use-cases
     * etc. Those key/value pairs must have a finite number of
     * combinations through the lifetime of a system.
     */
    default  EnhanceableScriptTrace addTraceTag(String key, String value){
        return addTraceTag(ScriptTrace.TraceTag.tag(key, value));
    }

    EnhanceableScriptTrace addTraceTag(ScriptTrace.TraceTag tag);

    ScriptEngineRequest getRequest();

    Throwable getException();
}
