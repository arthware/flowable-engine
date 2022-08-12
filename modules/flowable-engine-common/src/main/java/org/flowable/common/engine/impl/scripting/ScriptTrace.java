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
import java.util.Objects;
import java.util.Set;

/**
 * Captures meta information about a script invocatioon, like the start time,
 * the duration of the script execution, tags, whether it ended with an exception, etc.
 */
public interface ScriptTrace {

    /**
     * @return optional UUID for the script trace for correlation purposes.
     */
    String getUuid();

    ScriptEngineRequest getRequest();

    Throwable getException();

    Set<TraceTag> getTraceTags();

    long geStartTimeMillis();

    Duration getDuration();

    default boolean hasException() {
        return getException() != null;
    }

    static class TraceTag {
        private final boolean idTag;
        private final String key;
        private final String value;

        protected TraceTag(boolean uniqueId, String key, String value) {
            this.idTag = uniqueId;
            this.key = key;
            this.value = value;
        }

        /**
         * Id tags should be used when this tag
         * holds a unique identifier like a UUID or a database sequence value.
         *
         * Those tags won't be used for metrics.
         * @param key the tagKey
         * @param value the tag value
         * @return the unique id tag
         */
        public static TraceTag idTag(String key, String value) {
            return new TraceTag(true, key, value);
        }

        /**
         * Tags should be used, when this TraceTag can be used as
         * classifier e.g. when the value does not hold any unique entity
         * id but allows to classify invocations in a finite set of tuples.
         * <p>
         * Those tags can be used to tag metrics.
         * </p>
         * @param key the tagKey
         * @param value the tag value
         * @return the classifier tag
         */
        public static TraceTag tag(String key, String value) {
            return new TraceTag(false, key, value);
        }

        public boolean isIdTag() {
            return idTag;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("%s=%s", key, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TraceTag traceTag = (TraceTag) o;
            return idTag == traceTag.idTag && key.equals(traceTag.key) && value.equals(traceTag.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idTag, key, value);
        }
    }

}
