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
package org.flowable.engine.impl.scripting;

import org.flowable.common.engine.impl.scripting.ScriptTraceEnhancer;
import org.flowable.common.engine.impl.scripting.EnhanceableScriptTrace;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.service.delegate.DelegateTask;

public class ProcessEngineScriptTraceEnhancer implements ScriptTraceEnhancer {

    @Override
    public void enhanceScriptTrace(EnhanceableScriptTrace scriptTrace) {
        if (scriptTrace.getRequest().getVariableContainer() instanceof DelegateExecution) {
            DelegateExecution execution = (DelegateExecution) scriptTrace.getRequest().getVariableContainer();
            addProcessDefinition(execution.getProcessDefinitionId(), scriptTrace);
            scriptTrace.addTraceTag("activityId", execution.getCurrentActivityId());
            addTenantId(scriptTrace, execution.getTenantId());
        }
        if (scriptTrace.getRequest().getVariableContainer() instanceof DelegateTask) {
            DelegateTask task = (DelegateTask) scriptTrace.getRequest().getVariableContainer();
            addProcessDefinition(task.getProcessDefinitionId(), scriptTrace);
            scriptTrace.addTraceTag("taskDefinitionKey", task.getTaskDefinitionKey());
            addTenantId(scriptTrace, task.getTenantId());
        }
    }

    protected void addProcessDefinition(String processDefinitionId, EnhanceableScriptTrace scriptTrace) {
        ProcessDefinition processDefinition = getProcessDefinition(processDefinitionId);
        if (processDefinition != null) {
            scriptTrace.addTraceTag("processDefinitionKey", processDefinition.getKey());
        }
    }

    protected void addTenantId(EnhanceableScriptTrace scriptTrace, String tenantId) {
        if (tenantId != null && !tenantId.isEmpty()) {
            scriptTrace.addTraceTag("tenantId", tenantId);
        }
    }

    protected ProcessDefinition getProcessDefinition(String processDefinitionId) {
        ProcessDefinition processDefinition = CommandContextUtil.getProcessEngineConfiguration().getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        return processDefinition;
    }
}