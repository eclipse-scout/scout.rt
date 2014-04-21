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
package org.eclipse.scout.rt.server.services.common.jdbc.builder;

import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.ClassIdentifier;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;

/**
 * Definition of a property-to-sql and valueField-to-sql mapping for {@link AbstractPropertyData} and
 * {@link AbstractValueFieldData}
 * 
 * @deprecated use the more general {@link BasicPartDefinition} instead. Will be removed in the M-Release
 */
@Deprecated
@SuppressWarnings("deprecation")
public class ValuePartDefinition extends BasicPartDefinition {

  /**
   * @param valueType
   *          {@link AbstractPropertyData} or {@link AbstractValueFieldData}
   * @param sqlAttribute
   *          contains bind names :a, :b, :c for the values of the values correcpsonding to valueTypes
   *          <p>
   *          If different bind names are used,
   *          {@link #createNewInstance(FormDataStatementBuilder, List, List, List, Map)} shoult be overridden
   * @param operator
   *          any of the {@link DataModelConstants#OPERATOR_*} values
   */
  public ValuePartDefinition(Class<?> valueType, String sqlAttribute, int operator) {
    this(new Class[]{valueType}, sqlAttribute, operator, false);
  }

  /**
   * @param fieldTypeClassIdentifier
   * @param sqlAttribute
   * @param operator
   */
  public ValuePartDefinition(ClassIdentifier fieldTypeClassIdentifier, String sqlAttribute, int operator) {
    this(new ClassIdentifier[]{fieldTypeClassIdentifier}, sqlAttribute, operator, false);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public ValuePartDefinition(Class<?> valueType, String sqlAttribute) {
    this(new Class[]{valueType}, sqlAttribute, DataModelConstants.OPERATOR_NONE, false);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public ValuePartDefinition(Class<?>[] valueTypes, String sqlAttribute) {
    this(valueTypes, sqlAttribute, DataModelConstants.OPERATOR_NONE, false);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public ValuePartDefinition(Class<?> valueType, String sqlAttribute, int operator, boolean plainBind) {
    this(new Class[]{valueType}, sqlAttribute, operator, plainBind);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public ValuePartDefinition(ClassIdentifier valueTypeIdentifier, String sqlAttribute, int operator, boolean plainBind) {
    this(new ClassIdentifier[]{valueTypeIdentifier}, sqlAttribute, operator, plainBind);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public ValuePartDefinition(Class<?>[] valueTypes, String sqlAttribute, int operator) {
    this(valueTypes, sqlAttribute, operator, false);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public ValuePartDefinition(Class<?>[] valueTypes, String sqlAttribute, int operator, boolean plainBind) {
    this(ClassIdentifier.convertClassArrayToClassIdentifierArray(valueTypes), sqlAttribute, operator, plainBind);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public ValuePartDefinition(ClassIdentifier[] valueTypeClassIdentifiers, String sqlAttribute, int operator, boolean plainBind) {
    super(valueTypeClassIdentifiers, sqlAttribute, operator, plainBind);
  }

  @Override
  public boolean accept(AbstractFormData formData) throws ProcessingException {
    Map<Integer, Map<String, AbstractFormFieldData>> fieldsBreathFirstMap = formData.getAllFieldsRec();
    Map<Integer, Map<String, AbstractPropertyData<?>>> propertiesBreathFirstMap = formData.getAllPropertiesRec();
    return accept(formData, fieldsBreathFirstMap, propertiesBreathFirstMap);
  }

  /**
   * Computes whether this {@link ValuePartDefinition} takes part in building the filter criterion.
   * 
   * @param formData
   *          the form data to be checked.
   * @param fieldsBreathFirstMap
   *          The breath-first map of all form data fields as returned by {@link AbstractFormData#getAllFieldsRec()}.
   * @param propertiesBreathFirstMap
   *          The breath-first map of all property data as returned by {@link AbstractFormData#getAllPropertiesRec()}
   * @return <code>true</code> if the properties in the form data are sufficient in order to append this part to the
   *         result statement This will result in a call to
   *         {@link #createNewInstance(FormDataStatementBuilder, List, List, List, Map)} building that part.
   *         <p>
   *         Default accepts when any of the value of the valueType set is set (isValueSet) and has a non-null value in
   *         the form data
   * @throws ProcessingException
   */
  protected boolean accept(AbstractFormData formData, Map<Integer, Map<String, AbstractFormFieldData>> fieldsBreathFirstMap, Map<Integer, Map<String, AbstractPropertyData<?>>> propertiesBreathFirstMap) throws ProcessingException {
    if (getValueTypeClassIdentifiers().length > 0) {
      for (ClassIdentifier valueType : getValueTypeClassIdentifiers()) {
        Object dataObject = formData.findFieldByClass(fieldsBreathFirstMap, valueType);
        if (dataObject instanceof AbstractValueFieldData<?>) {
          AbstractValueFieldData<?> v = (AbstractValueFieldData<?>) dataObject;
          if (v.isValueSet() && v.getValue() != null) {
            return true;
          }
        }
        dataObject = formData.findPropertyByClass(propertiesBreathFirstMap, valueType);
        if (dataObject instanceof AbstractPropertyData<?>) {
          AbstractPropertyData<?> p = (AbstractPropertyData<?>) dataObject;
          if (p.isValueSet() && p.getValue() != null) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  protected String createInstanceImpl(FormDataStatementBuilder builder, List<Object> valueDatas, List<String> bindNames, List<Object> bindValues, Map<String, String> parentAliasMap) throws ProcessingException {
    return createInstance(builder, valueDatas, bindNames, bindValues, parentAliasMap);
  }

  /**
   * @deprecated override
   *             {@link BasicPartDefinition#createInstanceImpl(FormDataStatementBuilder, List, List, List, Map)}
   *             instead. Will be removed in the 5.0 Release.
   */
  @Deprecated
  protected String createInstance(FormDataStatementBuilder builder, List<Object> valueDatas, List<String> bindNames, List<Object> bindValues, Map<String, String> parentAliasMap) throws ProcessingException {
    return createNewInstance(builder, valueDatas, bindNames, bindValues, parentAliasMap);
  }

  /**
   * @deprecated use {@link #createInstance(FormDataStatementBuilder, List, List, List, Map)} instead. Will be removed
   *             in the 5.0 Release.
   */
  @Deprecated
  protected String createNewInstance(FormDataStatementBuilder builder, List<Object> valueDatas, List<String> bindNames, List<Object> bindValues, Map<String, String> parentAliasMap) throws ProcessingException {
    return builder.createSqlPart(DataModelConstants.AGGREGATION_NONE, getSqlAttribute(), getOperation(), bindNames, bindValues, isPlainBind(), parentAliasMap);
  }
}
