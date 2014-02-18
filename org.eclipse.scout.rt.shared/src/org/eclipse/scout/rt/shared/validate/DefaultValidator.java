/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.validate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.validate.annotations.CodeValue;
import org.eclipse.scout.rt.shared.validate.annotations.LookupValue;
import org.eclipse.scout.rt.shared.validate.annotations.Mandatory;
import org.eclipse.scout.rt.shared.validate.annotations.MaxLength;
import org.eclipse.scout.rt.shared.validate.annotations.MaxValue;
import org.eclipse.scout.rt.shared.validate.annotations.MinLength;
import org.eclipse.scout.rt.shared.validate.annotations.MinValue;
import org.eclipse.scout.rt.shared.validate.annotations.RegexMatch;
import org.eclipse.scout.rt.shared.validate.annotations.Treat0AsNull;
import org.eclipse.scout.rt.shared.validate.annotations.ValidateAnnotationMarker;
import org.eclipse.scout.rt.shared.validate.checks.CodeValueCheck;
import org.eclipse.scout.rt.shared.validate.checks.LookupValueCheck;
import org.eclipse.scout.rt.shared.validate.checks.MandatoryCheck;
import org.eclipse.scout.rt.shared.validate.checks.MasterValueRequiredCheck;
import org.eclipse.scout.rt.shared.validate.checks.MaxLengthCheck;
import org.eclipse.scout.rt.shared.validate.checks.MaxLengthGenericCheck;
import org.eclipse.scout.rt.shared.validate.checks.MaxValueCheck;
import org.eclipse.scout.rt.shared.validate.checks.MinLengthCheck;
import org.eclipse.scout.rt.shared.validate.checks.MinValueCheck;
import org.eclipse.scout.rt.shared.validate.checks.RegexMatchCheck;

/**
 * Does input/output validation of arbitrary serializable data.
 * <p>
 * This default traverses all objects of the arguments map in the complete data structure by writing the object to a
 * void stream.
 * <p>
 * This default delegates {@link AbstractFormData} to a {@link DefaultFormDataValidator} and does nothing otherwise.
 * <p>
 * Default maxLength is checked in {@link #checkMaxLenghtDefault(Object)}
 */
public class DefaultValidator extends ValidationUtility.ValidateTreeVisitor implements IValidator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultValidator.class);
  private static final Map<String, Object> NO_RULE_MAP = new HashMap<String, Object>();

  private IValidationStrategy m_validationStrategy;
  private ValidateCheckSet m_defaultCheckSet;
  private HashSet<String/*id*/> m_consumedChecks = new HashSet<String/*id*/>();

  public DefaultValidator(IValidationStrategy validationStrategy) {
    m_validationStrategy = validationStrategy;
    m_defaultCheckSet = new ValidateCheckSet();
  }

  @Override
  public void validateMethodCall(Method m, Object[] parameters) throws Exception {
    if (parameters == null || parameters.length == 0) {
      return;
    }
    initDefaultChecks(m_defaultCheckSet);
    if (m_defaultCheckSet.isEmpty()) {
      m_defaultCheckSet = null;
    }
    Collection<Annotation>[] parameterAnnotations = collectParameterAnnotations(m);
    for (int i = 0; i < parameters.length; i++) {
      start(parameters[i], parameterAnnotations[i]);
    }
  }

  @Override
  public void validateParameter(Object parameter, Collection<Annotation> parameterAnnotations) throws Exception {
    initDefaultChecks(m_defaultCheckSet);
    if (m_defaultCheckSet.isEmpty()) {
      m_defaultCheckSet = null;
    }
    start(parameter, parameterAnnotations);
  }

  protected void initDefaultChecks(ValidateCheckSet checkSet) {
    checkSet.addCheck(new MaxLengthGenericCheck(250, 64000000, 64000000, 10000));
  }

  /**
   * Collect list of annotations for each parameter based on the method declaration and all its super class method
   * declarations.
   * <p>
   * See {@link ValidationUtility#getParameterAnnotations(Method)} for more details
   */
  protected Collection<Annotation>[] collectParameterAnnotations(Method m) {
    return ValidationUtility.getParameterAnnotations(m);
  }

  /*
   * Annotation based validation
   */

  protected ValidateCheckSet validateObjectByAnnotations(Collection<Annotation> annotationList, Object obj) throws Exception {
    if (isTreat0AsNullFromAnnotations(annotationList)) {
      obj = ValidationUtility.treat0AsNull(obj);
    }
    //default node: retrieve checks by annotations
    ValidateCheckSet localSet = new ValidateCheckSet();
    ValidateCheckSet subtreeSet = new ValidateCheckSet();
    addChecksFromAnnotations(localSet, subtreeSet, annotationList, obj);
    //do checks
    m_consumedChecks.clear();
    if (localSet != null) {
      localSet.applyChecks(m_validationStrategy, obj, m_consumedChecks);
    }
    if (subtreeSet != null) {
      subtreeSet.applyChecks(m_validationStrategy, obj, m_consumedChecks);
    }
    if (m_defaultCheckSet != null) {
      m_defaultCheckSet.applyChecks(m_validationStrategy, obj, m_consumedChecks);
    }
    return subtreeSet;
  }

  protected boolean isTreat0AsNullFromAnnotations(Collection<Annotation> annotationList) {
    if (annotationList.size() == 0) {
      return false;
    }
    for (Annotation a : annotationList) {
      if (a.annotationType() == Treat0AsNull.class) {
        return ((Treat0AsNull) a).value();
      }
    }
    return false;
  }

  /**
   * @param annotations
   *          are all annotations with a {@link ValidateAnnotationMarker}
   */
  protected void addChecksFromAnnotations(ValidateCheckSet localSet, ValidateCheckSet subtreeSet, Collection<Annotation> annotationList, Object value) {
    if (annotationList.size() == 0) {
      return;
    }
    for (Annotation a : annotationList) {
      try {
        addCheckFromAnnotation(localSet, subtreeSet, a, value);
      }
      catch (Throwable t) {
        LOG.error("failed creating check implementation for " + a, t);
      }
    }
  }

  /**
   * override this method to support for more check annotations (annotations marked with
   * {@link ValidateAnnotationMarker}
   */
  protected void addCheckFromAnnotation(ValidateCheckSet localSet, ValidateCheckSet subtreeSet, Annotation a, Object value) throws Exception {
    if (a.annotationType() == CodeValue.class) {
      CodeValue x = (CodeValue) a;
      if (value == null || (value.getClass().isArray() && Array.getLength(value) == 0)) {
        return;
      }
      (x.subtree() ? subtreeSet : localSet).addCheck(new CodeValueCheck(CODES.getCodeType(x.value())));
    }
    else if (a.annotationType() == LookupValue.class) {
      LookupValue x = (LookupValue) a;
      if (value == null || (value.getClass().isArray() && Array.getLength(value) == 0)) {
        return;
      }
      (x.subtree() ? subtreeSet : localSet).addCheck(new LookupValueCheck(x.value().newInstance()));
    }
    else if (a.annotationType() == Mandatory.class) {
      Mandatory x = (Mandatory) a;
      (x.subtree() ? subtreeSet : localSet).addCheck(new MandatoryCheck(x.value()));
    }
    else if (a.annotationType() == MinLength.class) {
      MinLength x = (MinLength) a;
      (x.subtree() ? subtreeSet : localSet).addCheck(new MinLengthCheck(x.value()));
    }
    else if (a.annotationType() == MaxLength.class) {
      MaxLength x = (MaxLength) a;
      (x.subtree() ? subtreeSet : localSet).addCheck(new MaxLengthCheck(x.value()));
    }
    else if (a.annotationType() == MinValue.class) {
      if (value == null) {
        return;
      }
      MinValue x = (MinValue) a;
      (x.subtree() ? subtreeSet : localSet).addCheck(new MinValueCheck(x.value()));
    }
    else if (a.annotationType() == MaxValue.class) {
      if (value == null) {
        return;
      }
      MaxValue x = (MaxValue) a;
      (x.subtree() ? subtreeSet : localSet).addCheck(new MaxValueCheck(x.value()));
    }
    else if (a.annotationType() == RegexMatch.class) {
      if (value == null) {
        return;
      }
      RegexMatch x = (RegexMatch) a;
      (x.subtree() ? subtreeSet : localSet).addCheck(new RegexMatchCheck(x.value()));
    }
  }

  /*
   * FormData validation rule based validation
   */
  protected void validateFormDataFields(AbstractFormData formData) throws Exception {
    for (Map<String, AbstractFormFieldData> map : formData.getAllFieldsRec().values()) {
      for (AbstractFormFieldData field : map.values()) {
        if (!field.isValueSet()) {
          continue;
        }
        Map<String, Object> ruleMap = field.getValidationRules();
        if (ruleMap.size() == 0) {
          continue;
        }
        //
        Object fieldValue = null;
        if (field instanceof AbstractValueFieldData<?>) {
          fieldValue = ((AbstractValueFieldData<?>) field).getValue();
        }
        //pre-mark the fieldValue object so the validation tree traversal will skip it when it continues
        markObject(fieldValue);
        validateFormDataFieldByValidationRules(formData, ruleMap, field.getClass().getSimpleName(), fieldValue);
      }
    }
  }

  protected void validateFormDataFieldByValidationRules(AbstractFormData formData, Map<String, Object> ruleMap, String fieldName, Object obj) throws Exception {
    FormDataCheckContext ctx = new FormDataCheckContext();
    ctx.formData = formData;
    ctx.ruleMap = ruleMap;
    ctx.fieldName = fieldName;
    if (isTreat0AsNullFromValidationRules(ctx)) {
      obj = ValidationUtility.treat0AsNull(obj);
    }
    //default node: retrieve checks by annotations
    ValidateCheckSet localCheckSet = new ValidateCheckSet();
    addChecksFromValidationRules(localCheckSet, ctx, obj);
    //do checks
    m_consumedChecks.clear();
    if (localCheckSet != null) {
      localCheckSet.applyChecks(m_validationStrategy, obj, m_consumedChecks);
    }
    if (m_defaultCheckSet != null) {
      m_defaultCheckSet.applyChecks(m_validationStrategy, obj, m_consumedChecks);
    }
  }

  protected boolean isTreat0AsNullFromValidationRules(FormDataCheckContext ctx) {
    return Boolean.TRUE.equals(ctx.ruleMap.get(ValidationRule.ZERO_NULL_EQUALITY));
  }

  protected void addChecksFromValidationRules(ValidateCheckSet set, FormDataCheckContext ctx, Object value) {
    if (ctx.ruleMap == null || ctx.ruleMap.size() == 0) {
      return;
    }
    for (Map.Entry<String, Object> e : ctx.ruleMap.entrySet()) {
      if (e.getKey() == null) {
        continue;
      }
      ctx.ruleName = e.getKey();
      ctx.ruleValue = e.getValue();
      try {
        addCheckFromValidationRule(set, ctx, value);
      }
      catch (Throwable t) {
        LOG.error("failed creating check implementation for " + ctx.ruleName + "=" + ctx.ruleValue, t);
      }
    }
    return;
  }

  /**
   * override this method to support for more checks
   */
  protected void addCheckFromValidationRule(ValidateCheckSet set, FormDataCheckContext ctx, Object value) throws Exception {//NO_CHECKSTYLE
    String key = ctx.ruleName;
    if (key.equals(ValidationRule.CODE_TYPE)) {
      if (value == null || (value.getClass().isArray() && Array.getLength(value) == 0)) {
        return;
      }
      set.addCheck(new CodeValueCheck(createCodeTypeByValidationRules(ctx)));
    }
    else if (key.equals(ValidationRule.LOOKUP_CALL)) {
      if (value == null || (value.getClass().isArray() && Array.getLength(value) == 0)) {
        return;
      }
      set.addCheck(new LookupValueCheck(createLookupCallByValidationRules(ctx)));
    }
    else if (key.equals(ValidationRule.MANDATORY)) {
      if (!Boolean.TRUE.equals(ctx.ruleValue)) {
        return;
      }
      set.addCheck(new MandatoryCheck(true));
    }
    else if (key.equals(ValidationRule.MIN_LENGTH)) {
      set.addCheck(new MinLengthCheck(((Number) ctx.ruleValue).intValue()));
    }
    else if (key.equals(ValidationRule.MAX_LENGTH)) {
      set.addCheck(new MaxLengthCheck(((Number) ctx.ruleValue).intValue()));
    }
    else if (key.equals(ValidationRule.MIN_VALUE)) {
      if (value == null) {
        return;
      }
      set.addCheck(new MinValueCheck(ctx.ruleValue));
    }
    else if (key.equals(ValidationRule.MAX_VALUE)) {
      if (value == null) {
        return;
      }
      set.addCheck(new MaxValueCheck(ctx.ruleValue));
    }
    else if (key.equals(ValidationRule.REGEX)) {
      if (value == null) {
        return;
      }
      set.addCheck(new RegexMatchCheck((String) ctx.ruleValue));
    }
    else if (key.equals(ValidationRule.MASTER_VALUE_REQUIRED)) {
      //if slave value is null, no rule applies
      if (value == null || (value.getClass().isArray() && Array.getLength(value) == 0)) {
        return;
      }
      //slave is set and not null
      //is a master value necessary?
      if (!Boolean.TRUE.equals(ctx.ruleMap.get(ValidationRule.MASTER_VALUE_REQUIRED))) {
        return;
      }
      set.addCheck(new MasterValueRequiredCheck(ctx));
    }
  }

  protected ICodeType<?, ?> createCodeTypeByValidationRules(FormDataCheckContext ctx) throws Exception {
    @SuppressWarnings("unchecked")
    Class<? extends ICodeType<?, ?>> cls = (Class<? extends ICodeType<?, ?>>) ctx.ruleValue;
    ICodeType<?, ?> codeType = CODES.getCodeType(cls);
    if (codeType == null) {
      throw new ProcessingException(ctx.fieldName + " codeType " + cls.getSimpleName() + " does not exist");
    }
    return codeType;
  }

  @SuppressWarnings("unchecked")
  protected LookupCall createLookupCallByValidationRules(FormDataCheckContext ctx) throws Exception {
    Class<? extends LookupCall> cls = (Class<? extends LookupCall>) ctx.ruleValue;
    LookupCall call;
    try {
      call = cls.newInstance();
    }
    catch (Throwable t) {
      throw new ProcessingException(ctx.fieldName + " can not verify " + cls.getSimpleName());
    }
    //does a master value exist?
    if (ctx.ruleMap.containsKey(ValidationRule.MASTER_VALUE_FIELD)) {
      Class<? extends AbstractValueFieldData<?>> masterFieldClass = (Class<? extends AbstractValueFieldData<?>>) ctx.ruleMap.get(ValidationRule.MASTER_VALUE_FIELD);
      if (masterFieldClass == null) {
        throw new ProcessingException(ctx.fieldName + " missing master field");
      }
      AbstractValueFieldData<?> masterField = ctx.formData.getFieldByClass(masterFieldClass);
      if (masterField == null) {
        throw new ProcessingException(ctx.fieldName + " missing master field " + masterFieldClass.getSimpleName());
      }
      call.setMaster(masterField.getValue());
    }
    return call;
  }

  /*
   * tree walker
   */

  @Override
  protected void visitObject(Object obj, Collection<Annotation> annotationList) throws Exception {
    ValidateCheckSet additionalSubtreeSet = null;
    if (obj instanceof AbstractFormData) {
      //special node: form data fields
      validateFormDataFields((AbstractFormData) obj);
    }
    else {
      //default node
      additionalSubtreeSet = validateObjectByAnnotations(annotationList, obj);
      if (additionalSubtreeSet.isEmpty()) {
        additionalSubtreeSet = null;
      }
    }
    //subtree
    if (additionalSubtreeSet != null) {
      ValidateCheckSet oldSet = m_defaultCheckSet;
      try {
        m_defaultCheckSet = additionalSubtreeSet;
        additionalSubtreeSet.setParent(oldSet);
        //
        visitSubTree(obj);
      }
      finally {
        m_defaultCheckSet = oldSet;
      }
    }
    else {
      visitSubTree(obj);
    }
  }

  public static class FormDataCheckContext {
    public AbstractFormData formData;
    public String fieldName;
    public Map<String, Object> ruleMap;
    public String ruleName;
    public Object ruleValue;
  }
}
