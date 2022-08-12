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

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultScriptTrace implements EnhanceableScriptTrace, ScriptTrace {

    protected long startTimeMillis;
    protected long endTimeMillis;
    protected String uuid;
    protected ScriptEngineRequest request;
    protected Throwable exception;
    protected Set<TraceTag> traceTags = new LinkedHashSet<>();

    public DefaultScriptTrace(long startTimeMillis, long endTimeMillis, String uuid, ScriptEngineRequest request, Throwable caughtException) {
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
        this.uuid = uuid;
        this.request = request;
        this.exception = caughtException;
    }

    public static DefaultScriptTrace successTrace(long startTimeMillis, long endTimeMillis, ScriptEngineRequest request) {
        return new DefaultScriptTrace(startTimeMillis, endTimeMillis, null, request, null);
    }

    public static DefaultScriptTrace errorTrace(long startTimeMillis, long endTimeMillis, String uuid, ScriptEngineRequest request, Throwable caughtException) {
        return new DefaultScriptTrace(startTimeMillis, endTimeMillis, uuid, request, caughtException);
    }

    @Override
    public EnhanceableScriptTrace addTraceTag(TraceTag tag) {
        this.traceTags.add(tag);
        return this;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public ScriptEngineRequest getRequest() {
        return request;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public Set<TraceTag> getTraceTags() {
        return traceTags;
    }

    @Override
    public long geStartTimeMillis() {
        return startTimeMillis;
    }

    @Override
    public Duration getDuration() {
        return Duration.ofMillis(endTimeMillis - startTimeMillis);
    }
}
