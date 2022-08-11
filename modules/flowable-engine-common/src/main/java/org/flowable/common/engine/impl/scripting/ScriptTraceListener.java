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

public interface ScriptTraceListener {

    /**
     * Filter method to allow implementing custom logic for specific requests to be traced.
     * <p>
     * Allows to implement, for example, sampling for a fraction of script requests only (let every 10th request pass)
     * or filter for specific requests. By default, only errors are accepted.
     * </p>
     * <p>
     * Note, that the {@link ScriptTrace} object is created <i>only</i>, when this
     * method returns true, to minimize the runtime impact.
     * </p>
     */
    default boolean shouldBeTraced(ScriptEngineRequest request, boolean error) {
        return error;
    }

    /**
     * Callback method, when {@link #shouldBeTraced(ScriptEngineRequest, boolean)} for a request returned true.
     * The populated request in the trace object is the same which has been passed to the accept method before.
     *
     * @param scriptTrace
     */
    void onScriptTrace(ScriptTrace scriptTrace);
}
