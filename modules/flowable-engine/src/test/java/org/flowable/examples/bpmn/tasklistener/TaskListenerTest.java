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
package org.flowable.examples.bpmn.tasklistener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.common.engine.api.FlowableIllegalStateException;
import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.common.engine.impl.scripting.FlowableScriptEvaluationException;
import org.flowable.engine.impl.test.HistoryTestHelper;
import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceBuilder;
import org.flowable.engine.test.Deployment;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.delegate.TaskListener;
import org.junit.jupiter.api.Test;

/**
 * @author Joram Barrez
 */
public class TaskListenerTest extends PluggableFlowableTestCase {

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
    public void testTaskCreateListener() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
        org.flowable.task.api.Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getName()).isEqualTo("Schedule meeting");
        assertThat(task.getDescription()).isEqualTo("TaskCreateListener is listening!");

        // Manually cleanup the process instance. If we don't do this, the
        // following actions will occur:
        // 1. The cleanup rule will delete the process
        // 2. The process deletion will fire a DELETE event to the TaskAllEventsListener
        // 3. The TaskAllEventsListener will set a variable on the org.flowable.task.service.Task
        // 4. Setting that variable will result in an entry in the ACT_HI_DETAIL table
        // 5. The AbstractActivitiTestCase will fail the test because the DB is not clean
        // By triggering the DELETE event from within the test, we ensure that
        // all of the records are written before the test cleanup begins
        runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");
    }

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerInSubProcessTest.bpmn20.xml" })
    public void testTaskCreateListenerInSubProcess() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerInSubProcess");
        org.flowable.task.api.Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getName()).isEqualTo("Schedule meeting");
        assertThat(task.getDescription()).isEqualTo("TaskCreateListener is listening!");

        runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");
    }

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
    public void testTaskAssignmentListener() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
        org.flowable.task.api.Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDescription()).isEqualTo("TaskCreateListener is listening!");

        // Set assignee and check if event is received
        taskService.setAssignee(task.getId(), "kermit");
        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDescription()).isEqualTo("TaskAssignmentListener is listening: kermit");

        runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");
        
        waitForHistoryJobExecutorToProcessAllJobs(7000, 100);
    }

    /**
     * Validate fix for ACT-1627: Not throwing assignment event on every update
     */
    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
    public void testTaskAssignmentListenerNotCalledWhenAssigneeNotUpdated() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
        org.flowable.task.api.Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDescription()).isEqualTo("TaskCreateListener is listening!");

        // Set assignee and check if event is received
        taskService.setAssignee(task.getId(), "kermit");
        task = taskService.createTaskQuery().singleResult();

        assertThat(task.getDescription()).isEqualTo("TaskAssignmentListener is listening: kermit");

        // Reset description and assign to same person. This should NOT trigger an assignment
        task.setDescription("Clear");
        taskService.saveTask(task);
        taskService.setAssignee(task.getId(), "kermit");
        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDescription()).isEqualTo("Clear");

        // Set assignee through task-update
        task.setAssignee("kermit");
        taskService.saveTask(task);

        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDescription()).isEqualTo("Clear");

        // Update another property should not trigger assignment
        task.setName("test");
        taskService.saveTask(task);

        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDescription()).isEqualTo("Clear");

        // Update to different
        task.setAssignee("john");
        taskService.saveTask(task);

        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDescription()).isEqualTo("TaskAssignmentListener is listening: john");

        // Manually cleanup the process instance.
        runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");
        
        waitForHistoryJobExecutorToProcessAllJobs(7000, 100);
    }
    
    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
    public void testTaskUnassignListener() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
        org.flowable.task.api.Task task = taskService.createTaskQuery().singleResult();

        // Set assignee and check if event is received
        taskService.claim(task.getId(), "kermit");
        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDescription()).isEqualTo("TaskAssignmentListener is listening: kermit");

        taskService.unclaim(task.getId());
        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDescription()).isEqualTo("TaskAssignmentListener is listening: null");

        taskService.setAssignee(task.getId(), "kermit");
        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDescription()).isEqualTo("TaskAssignmentListener is listening: kermit");

        taskService.setAssignee(task.getId(), null);
        task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDescription()).isEqualTo("TaskAssignmentListener is listening: null");

        runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");

        waitForHistoryJobExecutorToProcessAllJobs(7000, 100);
    }

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
    public void testTaskCompleteListener() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
        assertThat(runtimeService.getVariable(processInstance.getId(), "greeting")).isNull();
        assertThat(runtimeService.getVariable(processInstance.getId(), "expressionValue")).isNull();

        // Completing first task will change the description
        org.flowable.task.api.Task task = taskService.createTaskQuery().singleResult();
        taskService.complete(task.getId());

        assertThat(runtimeService.getVariable(processInstance.getId(), "greeting")).isEqualTo("Hello from The Process");
        assertThat(runtimeService.getVariable(processInstance.getId(), "shortName")).isEqualTo("Act");
    }

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
    public void testTaskListenerWithExpression() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerProcess");
        assertThat(runtimeService.getVariable(processInstance.getId(), "greeting2")).isNull();

        // Completing first task will change the description
        org.flowable.task.api.Task task = taskService.createTaskQuery().singleResult();
        taskService.complete(task.getId());

        assertThat(runtimeService.getVariable(processInstance.getId(), "greeting2")).isEqualTo("Write meeting notes");
    }

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTest.bpmn20.xml" })
    public void testAllEventsTaskListener() {
        runtimeService.startProcessInstanceByKey("taskListenerProcess");
        org.flowable.task.api.Task task = taskService.createTaskQuery().singleResult();

        // Set assignee and complete task
        taskService.setAssignee(task.getId(), "kermit");
        taskService.complete(task.getId());

        // Verify the all-listener has received all events
        String eventsReceived = (String) runtimeService.getVariable(task.getProcessInstanceId(), "events");
        assertThat(eventsReceived).isEqualTo("create - assignment - complete - delete");
        waitForHistoryJobExecutorToProcessAllJobs(7000, 100);
    }

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTest.testTaskListenersOnDelete.bpmn20.xml" })
    public void testTaskListenersOnDeleteByComplete() {
        TaskDeleteListener.clear();
        TaskSimpleCompleteListener.clear();
        runtimeService.startProcessInstanceByKey("executionListenersOnDelete");

        List<org.flowable.task.api.Task> tasks = taskService.createTaskQuery().list();
        assertThat(tasks).hasSize(1);

        org.flowable.task.api.Task task = taskService.createTaskQuery().taskName("User Task 1").singleResult();
        assertThat(task).isNotNull();

        assertThat(TaskDeleteListener.getCurrentMessages()).isEmpty();
        assertThat(TaskSimpleCompleteListener.getCurrentMessages()).isEmpty();

        taskService.complete(task.getId());

        tasks = taskService.createTaskQuery().list();

        assertThat(tasks).isEmpty();

        assertThat(TaskDeleteListener.getCurrentMessages())
                .containsOnly("Delete Task Listener executed.");

        assertThat(TaskSimpleCompleteListener.getCurrentMessages())
                .containsOnly("Complete Task Listener executed.");
    }

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTest.testTaskListenersOnDelete.bpmn20.xml" })
    public void testTaskListenersOnDeleteByDeleteProcessInstance() {
        TaskDeleteListener.clear();
        TaskSimpleCompleteListener.clear();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersOnDelete");

        List<org.flowable.task.api.Task> tasks = taskService.createTaskQuery().list();
        assertThat(tasks).hasSize(1);

        org.flowable.task.api.Task task = taskService.createTaskQuery().taskName("User Task 1").singleResult();
        assertThat(task).isNotNull();

        assertThat(TaskDeleteListener.getCurrentMessages()).isEmpty();
        assertThat(TaskSimpleCompleteListener.getCurrentMessages()).isEmpty();

        runtimeService.deleteProcessInstance(processInstance.getProcessInstanceId(), "");

        tasks = taskService.createTaskQuery().list();

        assertThat(tasks).isEmpty();

        assertThat(TaskDeleteListener.getCurrentMessages())
                .containsOnly("Delete Task Listener executed.");

        assertThat(TaskSimpleCompleteListener.getCurrentMessages()).isEmpty();
    }

    @Test
    @Deployment
    public void testTaskServiceTaskListeners() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
            .processDefinitionKey("taskServiceListeners")
            .transientVariable("taskServiceTaskDelegateTaskListener",
                (TaskListener) delegateTask -> delegateTask.setVariable("variableFromDelegateExpression", "From delegate expression"))
            .start();

        assertThat(processInstance.getProcessVariables())
            .containsOnly(
                entry("variableFromClassDelegate", "From class delegate"),
                entry("variableFromDelegateExpression", "From delegate expression")
            );
    }

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerDelegateExpressionThrowsException.bpmn20.xml" })
    public void testTaskListenerWithDelegateExpressionThrowsFlowableException() {
        ProcessInstanceBuilder builder = runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionKey("taskListenerProcess")
                .transientVariable("bean", (TaskListener) delegateTask -> {
                    throw new FlowableIllegalArgumentException("Message from listener");
                });
        assertThatThrownBy(builder::start)
                .isInstanceOf(FlowableIllegalArgumentException.class)
                .hasNoCause()
                .hasMessage("Message from listener");
    }

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerDelegateExpressionThrowsException.bpmn20.xml" })
    public void testTaskListenerWithDelegateExpressionThrowsNonFlowableException() {
        ProcessInstanceBuilder builder = runtimeService
                .createProcessInstanceBuilder()
                .processDefinitionKey("taskListenerProcess")
                .transientVariable("bean", (TaskListener) delegateTask -> {
                    throw new RuntimeException("Message from listener");
                });
        assertThatThrownBy(builder::start)
                .isExactlyInstanceOf(RuntimeException.class)
                .hasNoCause()
                .hasMessage("Message from listener");
    }

    @Test
    @Deployment(resources = "org/flowable/examples/bpmn/tasklistener/TaskListenerNoEventDefined.bpmn20.xml", validateBpmn = false)
    public void testTaskWithMissingEventAttribute() {
        assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("testProcess1"))
                .isInstanceOf(FlowableIllegalStateException.class)
                .hasMessageContaining("No event defined for taskListener in UserTask 'Task with Listener missing event attribute'");
    }

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTypeScript.bpmn20.xml" })
    public void testInvalidTypeEventListener() {
        assertThatThrownBy(() -> runtimeService.startProcessInstanceByKey("testProcess3"))
                .isInstanceOf(FlowableIllegalStateException.class)
                .hasMessageContaining("Script content is null or evaluated to null for listener of type 'script'");
    }

    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTypeScript.bpmn20.xml" })
    public void testTaskListenerTypeScript() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("scriptLanguageAsExpression", "groovy");
        vars.put("scriptPayloadAsExpression", "def foo = \"usertask2ReturnVal\"; return foo");
        vars.put("resultVarAsExpression", "task2ScriptListenerResult");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerTypeScriptProcess", vars);
        org.flowable.task.api.Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getName()).as("Name does not match").isEqualTo("User Task with Script Task Listener");
        assertThat(task.getOwner()).isEqualTo("kermit");

        taskService.complete(task.getId());

        if (HistoryTestHelper.isHistoryLevelAtLeast(HistoryLevel.AUDIT, processEngineConfiguration)) {
            /* The script calls taskService.save(task) after setting the owner.
             * Without calling taskService.save() the historic instance has owner set to null, but the actual instance above
             * has the owner set. */
            HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
            assertThat(historicTask.getOwner()).isEqualTo("kermit");

            task = taskService.createTaskQuery().singleResult();
            assertThat(task.getName()).as("Task name not set with 'scriptResultVariable' variable").isEqualTo("User Task 2 name defined in script");
        }

        Object localVariable = runtimeService.getVariable(processInstance.getId(), "localVariable");
        assertThat(localVariable).as("Expected 'localVariable' variable to be local to script").isNull();

        Object scriptVar = runtimeService.getVariable(processInstance.getId(), "scriptVar");
        assertThat(scriptVar).as("Could not find the 'scriptVar' variable in variable scope").isEqualTo("scriptVarValue");

        // Expect evaluation of script supports expressions for language, payload and resultVariable
        Object task2ScriptListenerResult = runtimeService.getVariable(processInstance.getId(), "task2ScriptListenerResult");
        assertThat(task2ScriptListenerResult).as("Expected 'task2ScriptListenerResult' variable in variable scope").isEqualTo("usertask2ReturnVal");
    }

    /**
     * Tests error trace enhacement by {@link org.flowable.engine.impl.scripting.ProcessEngineScriptTraceEnhancer}.
     */
    @Test
    @Deployment(resources = { "org/flowable/examples/bpmn/tasklistener/TaskListenerTypeScript.bpmn20.xml" })
    public void testTaskListenerTypeScriptSyntaxErrorInScript() {
        Map<String, Object> vars = new HashMap<>();
        assertThatThrownBy(()->runtimeService.startProcessInstanceByKey("testProcessErrorInScript", vars))
                .isInstanceOf(FlowableScriptEvaluationException.class)
                .hasMessageContaining("processDefinitionKey=testProcessErrorInScript")
                .hasMessageContaining("taskDefinitionKey=p4usertask1");
        ;
    }
}
