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
package org.flowable.bpmn.model;

/**
 * @author Tijs Rademakers
 */
public class IOParameter extends BaseElement {

    protected String source;
    protected String sourceExpression;
    protected String target;
    protected String targetExpression;
    protected boolean isTransient;

    protected boolean validate;
    protected boolean copyByReference;
    protected String sourceTypeName;
    protected String  sourceTypeModelKey;
    protected String targetTypeName;
    protected String targetTypeModelKey;

    protected boolean required;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSourceExpression() {
        return sourceExpression;
    }

    public void setSourceExpression(String sourceExpression) {
        this.sourceExpression = sourceExpression;
    }

    public String getTargetExpression() {
        return targetExpression;
    }

    public void setTargetExpression(String targetExpression) {
        this.targetExpression = targetExpression;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public boolean isCopyByReference() {
        return copyByReference;
    }

    public void setCopyByReference(boolean copyByReference) {
        this.copyByReference = copyByReference;
    }

    public String getSourceTypeName() {
        return sourceTypeName;
    }

    public void setSourceTypeName(String sourceTypeName) {
        this.sourceTypeName = sourceTypeName;
    }

    public String getSourceTypeModelKey() {
        return sourceTypeModelKey;
    }

    public void setSourceTypeModelKey(String sourceTypeModelKey) {
        this.sourceTypeModelKey = sourceTypeModelKey;
    }

    public String getTargetTypeName() {
        return targetTypeName;
    }

    public void setTargetTypeName(String targetTypeName) {
        this.targetTypeName = targetTypeName;
    }

    public String getTargetTypeModelKey() {
        return targetTypeModelKey;
    }

    public void setTargetTypeModelKey(String targetTypeModelKey) {
        this.targetTypeModelKey = targetTypeModelKey;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public IOParameter clone() {
        IOParameter clone = new IOParameter();
        clone.setValues(this);
        return clone;
    }

    public void setValues(IOParameter otherElement) {
        super.setValues(otherElement);
        setSource(otherElement.getSource());
        setSourceExpression(otherElement.getSourceExpression());
        setTarget(otherElement.getTarget());
        setTargetExpression(otherElement.getTargetExpression());
        setTransient(otherElement.isTransient());
    }
}
