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
package org.eclipse.scout.rt.server.jdbc.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.shared.data.model.DataModelConstants;

/**
 * Definition of a properties-to-sql, fields-to-sql mapping for {@link AbstractPropertyData}s and
 * {@link AbstractFormFieldData}s
 * <p>
 * The part definition uses 1..n fields and properties of a form-data and constructs a statement contribution for the
 * query.
 * <p>
 * The {@link FormDataStatementBuilder} first calls {@link #accept(AbstractFormData, Map, Map)} to decide if this part
 * is used or not. Then it calls {@link #getValueTypeClassIdentifiers()} to collect the filled out values of the form
 * data, to finally call {@link #createInstance(FormDataStatementBuilder, List, List, List, Map)} to retrieve the
 * statement contribution. In its simplest form, the result is just a whereClause.
 */
public class BasicPartDefinition implements DataModelConstants {
  private final ClassIdentifier[] m_valueTypeClassIdentifiers;
  private final String m_sqlAttribute;
  private final int m_operation;
  private final boolean m_plainBind;

  /**
   * @param valueType
   *          {@link AbstractPropertyData} or {@link AbstractFormFieldData}
   * @param sqlAttribute
   *          contains bind names :a, :b, :c for the values of the values correcpsonding to valueTypes
   *          <p>
   *          If different bind names are used,
   *          {@link #createNewInstance(FormDataStatementBuilder, List, List, List, Map)} shoult be overridden
   * @param operator
   *          any of the {@link DataModelConstants#OPERATOR_*} values
   */
  public BasicPartDefinition(Class<?> valueType, String sqlAttribute, int operator) {
    this(new Class[]{valueType}, sqlAttribute, operator, false);
  }

  /**
   * @param fieldTypeClassIdentifier
   * @param sqlAttribute
   * @param operator
   */
  public BasicPartDefinition(ClassIdentifier fieldTypeClassIdentifier, String sqlAttribute, int operator) {
    this(new ClassIdentifier[]{fieldTypeClassIdentifier}, sqlAttribute, operator, false);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public BasicPartDefinition(Class<?> valueType, String sqlAttribute) {
    this(new Class[]{valueType}, sqlAttribute, DataModelConstants.OPERATOR_NONE, false);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public BasicPartDefinition(Class<?>[] valueTypes, String sqlAttribute) {
    this(valueTypes, sqlAttribute, DataModelConstants.OPERATOR_NONE, false);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public BasicPartDefinition(Class<?> valueType, String sqlAttribute, int operator, boolean plainBind) {
    this(new Class[]{valueType}, sqlAttribute, operator, plainBind);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public BasicPartDefinition(ClassIdentifier valueTypeIdentifier, String sqlAttribute, int operator, boolean plainBind) {
    this(new ClassIdentifier[]{valueTypeIdentifier}, sqlAttribute, operator, plainBind);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public BasicPartDefinition(Class<?>[] valueTypes, String sqlAttribute, int operator) {
    this(valueTypes, sqlAttribute, operator, false);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public BasicPartDefinition(Class<?>[] valueTypes, String sqlAttribute, int operator, boolean plainBind) {
    this(ClassIdentifier.convertClassArrayToClassIdentifierArray(valueTypes), sqlAttribute, operator, plainBind);
  }

  /**
   * see {@link #ValuePartDefinition(Class, String, int)}
   */
  public BasicPartDefinition(ClassIdentifier[] valueTypeClassIdentifiers, String sqlAttribute, int operator, boolean plainBind) {
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
      else if (AbstractFormFieldData.class.isAssignableFrom(c)) {
        //ok
      }
      else {
        throw new IllegalArgumentException("" + c + " is not of type AbstractPropertyData or AbstractFormFieldData");
      }
      Class<?>[] classes = classIdentifier.getClasses();
      for (int i = 0; i < classes.length - 1; i++) {
        Class<?> containerClass = classes[i];
        if (AbstractFormFieldData.class.isAssignableFrom(containerClass)) {
          // ok
        }
        else {
          throw new IllegalArgumentException("" + containerClass + " is not of type AbstractFormFieldData (segment " + i + " in " + classIdentifier + ")");
        }
      }
    }
  }

  protected String getSqlAttribute() {
    return m_sqlAttribute;
  }

  protected int getOperation() {
    return m_operation;
  }

  /**
   * @return true for a plain bind (without jdbc ?) and false for jdbc ? binds.
   */
  protected boolean isPlainBind() {
    return m_plainBind;
  }

  /**
   * @return array of {@link AbstractValueData} class identifiers (fields, properties) that are accepted by this part
   *         definition. This is used by {@link #accept(AbstractFormData, Map, Map)} and
   *         {@link #createInstance(FormDataStatementBuilder, List, List, List, Map)}
   */
  protected ClassIdentifier[] getValueTypeClassIdentifiers() {
    return m_valueTypeClassIdentifiers;
  }

  /**
   * Computes whether this {@link BasicPartDefinition} takes part in building the filter criterion.
   *
   * @param formData
   *          the form data to be checked.
   * @return <code>true</code> if the properties in the form data are sufficient in order to append this part to the
   *         result statement This will result in a call to
   *         {@link #createNewInstance(FormDataStatementBuilder, List, List, List, Map)} building that part.
   *         <p>
   *         Default accepts when any of the value of the valueType set is set (isValueSet) and has a non-null value in
   *         the form data
   */
  public boolean accept(AbstractFormData formData) {
    Map<Integer, Map<String, AbstractFormFieldData>> fieldsBreathFirstMap = formData.getAllFieldsRec();
    Map<Integer, Map<String, AbstractPropertyData<?>>> propertiesBreathFirstMap = formData.getAllPropertiesRec();
    if (getValueTypeClassIdentifiers().length > 0) {
      for (ClassIdentifier valueType : getValueTypeClassIdentifiers()) {
        Object dataObject = formData.findFieldByClass(fieldsBreathFirstMap, valueType);
        if (dataObject instanceof AbstractFormFieldData) {
          AbstractValueFieldData<?> v = (dataObject instanceof AbstractValueFieldData<?> ? (AbstractValueFieldData<?>) dataObject : null);
          AbstractFormFieldData f = (AbstractFormFieldData) dataObject;
          if (f.isValueSet() && (v == null || v.getValue() != null)) {
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

  /**
   * Override this method to intercept and change part instance properties such as values, operation type, etc.<br>
   * Sometimes it is convenient to set the operation to {@link DataModelConstants#OPERATOR_NONE} which uses the
   * attribute itself as the complete statement part.
   *
   * @param builder
   * @param formData
   *          the form data to be checked.
   * @return the result EntityContribution. null if that part is to be ignored
   *         <p>
   *         normally calls
   *         {@link FormDataStatementBuilder#createStatementPart(Integer, String, int, List, List, boolean, Map)}
   *         <p>
   *         Can make use of alias markers such as @Person@.LAST_NAME, these are resolved in the
   *         {@link FormDataStatementBuilder}
   *         <p>
   *         Only additional bind values - other than the bindValues passed to createStatementPart - must be added using
   *         {@link FormDataStatementBuilder#addBind(String, Object)}
   */
  public EntityContribution createInstance(FormDataStatementBuilder builder, AbstractFormData formData, Map<String, String> parentAliasMap) {
    Map<Integer, Map<String, AbstractFormFieldData>> fieldsBreathFirstMap = formData.getAllFieldsRec();
    Map<Integer, Map<String, AbstractPropertyData<?>>> propertiesBreathFirstMap = formData.getAllPropertiesRec();
    ClassIdentifier[] valueTypes = getValueTypeClassIdentifiers();
    /**
     * the form data objects containing the runtime values {@link AbstractFormFieldData} and
     * {@link AbstractPropertyData}
     */
    ArrayList<Object> valueDatas = new ArrayList<Object>(valueTypes.length);
    /**
     * by default the names "a", "b", "c", ... represent the bindValues in the same order as the values
     */
    ArrayList<String> bindNames = new ArrayList<String>(valueTypes.length);
    /**
     * the values of the {@link AbstractFormFieldData}s and {@link AbstractPropertyData}s in the same order
     */
    ArrayList<Object> bindValues = new ArrayList<Object>(valueTypes.length);
    for (int i = 0; i < valueTypes.length; i++) {
      if (AbstractFormFieldData.class.isAssignableFrom(valueTypes[i].getLastSegment())) {
        AbstractFormFieldData field = formData.findFieldByClass(fieldsBreathFirstMap, valueTypes[i]);
        valueDatas.add(field);
        bindNames.add("" + (char) (('a') + i));
        if (field instanceof AbstractValueFieldData<?>) {
          bindValues.add(((AbstractValueFieldData<?>) field).getValue());
        }
        else {
          bindValues.add(null);
        }
      }
      else if (AbstractPropertyData.class.isAssignableFrom(valueTypes[i].getLastSegment())) {
        AbstractPropertyData<?> property = formData.findPropertyByClass(propertiesBreathFirstMap, valueTypes[i]);
        valueDatas.add(property);
        bindNames.add("" + (char) (('a') + i));
        bindValues.add(property.getValue());
      }
      else {
        valueDatas.add(null);
        bindNames.add("" + (char) (('a') + i));
        bindValues.add(null);
      }
    }
    //
    String wherePart = createInstanceImpl(builder, valueDatas, bindNames, bindValues, parentAliasMap);
    return EntityContribution.create(wherePart);
  }

  /**
   * This method is called when {@link #createInstance(FormDataStatementBuilder, AbstractFormData, Map)} is not
   * overridden and can be used to only override the core of create instance.
   * <p>
   * valueDatas, bindNames, bindValues are all pre-processed.
   */
  protected String createInstanceImpl(FormDataStatementBuilder builder, List<Object> valueDatas, List<String> bindNames, List<Object> bindValues, Map<String, String> parentAliasMap) {
    return builder.createSqlPart(DataModelConstants.AGGREGATION_NONE, getSqlAttribute(), getOperation(), bindNames, bindValues, isPlainBind(), parentAliasMap);
  }

}
