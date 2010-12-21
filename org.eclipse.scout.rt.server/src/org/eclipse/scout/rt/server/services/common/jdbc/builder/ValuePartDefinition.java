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
 */
public class ValuePartDefinition {
  private final ClassIdentifier[] m_valueTypeClassIdentifiers;
  private final String m_sqlAttribute;
  private final int m_operation;
  private final boolean m_plainBind;

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
  public ValuePartDefinition(Class valueType, String sqlAttribute, int operator) {
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
  public ValuePartDefinition(Class valueType, String sqlAttribute) {
    this(new Class[]{valueType}, sqlAttribute, DataModelConstants.OPERATOR_NONE, false);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public ValuePartDefinition(Class[] valueTypes, String sqlAttribute) {
    this(valueTypes, sqlAttribute, DataModelConstants.OPERATOR_NONE, false);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public ValuePartDefinition(Class valueType, String sqlAttribute, int operator, boolean plainBind) {
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
  public ValuePartDefinition(Class[] valueTypes, String sqlAttribute, int operator) {
    this(valueTypes, sqlAttribute, operator, false);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public ValuePartDefinition(Class[] valueTypes, String sqlAttribute, int operator, boolean plainBind) {
    this(ClassIdentifier.convertClassArrayToClassIdentifierArray(valueTypes), sqlAttribute, operator, plainBind);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public ValuePartDefinition(ClassIdentifier[] valueTypeClassIdentifiers, String sqlAttribute, int operator, boolean plainBind) {
    m_valueTypeClassIdentifiers = valueTypeClassIdentifiers != null ? valueTypeClassIdentifiers : new ClassIdentifier[0];
    m_sqlAttribute = sqlAttribute;
    m_operation = operator;
    m_plainBind = plainBind;
    //check types
    for (ClassIdentifier classIdentifier : m_valueTypeClassIdentifiers) {
      Class<?> c = classIdentifier.getLastSegment();
      if (AbstractPropertyData.class.isAssignableFrom(c)) {
        //ok
      }
      else if (AbstractValueFieldData.class.isAssignableFrom(c)) {
        //ok
      }
      else {
        throw new IllegalArgumentException("" + c + " is not of type AbstractPropertyData or AbstractValueFieldData");
      }
      for (int i = 0; i < classIdentifier.getClasses().length - 1; i++) {
        Class<?> containerClass = classIdentifier.getClasses()[i];
        if (AbstractFormFieldData.class.isAssignableFrom(containerClass)) {
          // ok
        }
        else {
          throw new IllegalArgumentException("" + containerClass + " is not of type AbstractFormFieldData (segment " + i + " in " + classIdentifier + ")");
        }
      }
    }
  }

  public String getSqlAttribute() {
    return m_sqlAttribute;
  }

  public int getOperation() {
    return m_operation;
  }

  /**
   * @return true for a plain bind (without jdbc ?) and false for jdbc ? binds.
   */
  public boolean isPlainBind() {
    return m_plainBind;
  }

  /**
   * @return array of {@link AbstractValueData} class identifiers
   */
  public ClassIdentifier[] getValueTypeClassIdentifiers() {
    return m_valueTypeClassIdentifiers;
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
  public boolean accept(AbstractFormData formData, Map<Integer, Map<String, AbstractFormFieldData>> fieldsBreathFirstMap, Map<Integer, Map<String, AbstractPropertyData<?>>> propertiesBreathFirstMap) throws ProcessingException {
    if (getValueTypeClassIdentifiers().length > 0) {
      for (ClassIdentifier valueType : getValueTypeClassIdentifiers()) {
        Object dataObject = formData.findFieldByClass(fieldsBreathFirstMap, valueType);
        if (dataObject instanceof AbstractValueFieldData) {
          AbstractValueFieldData v = (AbstractValueFieldData) dataObject;
          if (v.isValueSet() && v.getValue() != null) {
            return true;
          }
        }
        dataObject = formData.findPropertyByClass(propertiesBreathFirstMap, valueType);
        if (dataObject instanceof AbstractPropertyData) {
          AbstractPropertyData p = (AbstractPropertyData) dataObject;
          if (p.isValueSet() && p.getValue() != null) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Override this method to intercept and change part instance properties such as values, operation type, etc.<br>
   * Sometimes it is convenient to set the operation to {@link DataModelConstants#OPERATOR_NONE} which uses the
   * attribute
   * itself as the complete statement part.
   * 
   * @param builder
   * @param valueDatas
   *          the form data objects containing the runtime values {@link AbstractValueFieldData} and
   *          {@link AbstractPropertyData}
   * @param bindNames
   *          by default the names "a", "b", "c", ... representing then value bindValues in the same order as the
   *          values
   * @param bindValues
   *          the values of the {@link AbstractValueFieldData}s and {@link AbstractPropertyData}s in the same order
   * @return the result sql text; null if that part is to be ignored
   *         <p>
   *         normally calls
   *         {@link FormDataStatementBuilder#createStatementPart(Integer, String, int, List, List, boolean, Map)}
   *         <p>
   *         Can make use of alias markers such as @Person@.LAST_NAME, these are resolved in the
   *         {@link FormDataStatementBuilder}
   *         <p>
   *         Only additional bind values - other than the bindValues passed to createStatementPart - must be added using
   *         {@link FormDataStatementBuilder#addBind(String, Object)}
   * @throws ProcessingException
   */
  public String createNewInstance(FormDataStatementBuilder builder, List<Object> valueDatas, List<String> bindNames, List<Object> bindValues, Map<String, String> parentAliasMap) throws ProcessingException {
    return builder.createStatementPart(DataModelConstants.AGGREGATION_NONE, getSqlAttribute(), getOperation(), bindNames, bindValues, isPlainBind(), parentAliasMap);
  }
}
