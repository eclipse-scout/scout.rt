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
package org.eclipse.scout.rt.shared.data.model;

import java.security.Permission;
import java.util.Map;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractDataModelAttribute extends AbstractPropertyObserver implements IDataModelAttribute, DataModelConstants {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDataModelAttribute.class);

  private String m_id;
  private String m_text;
  private int m_type;
  private IDataModelAttributeOp[] m_operators;
  private int[] m_aggregationTypes;
  private String m_iconId;
  private boolean m_allowNullOperator;
  private boolean m_allowNotOperator;
  private boolean m_aggregationEnabled;
  private Class<? extends ICodeType> m_codeTypeClass;
  private LookupCall m_lookupCall;
  private Permission m_visiblePermission;
  private boolean m_visibleGranted;
  private boolean m_visibleProperty;
  private boolean m_visible;
  private IDataModelEntity m_parentEntity;

  public AbstractDataModelAttribute() {
    initConfig();
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(10)
  @ConfigPropertyValue("null")
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  @ConfigPropertyValue("null")
  protected String getConfiguredText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(30)
  @ConfigPropertyValue("null")
  protected Class<? extends LookupCall> getConfiguredLookupCall() {
    return null;
  }

  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(40)
  @ConfigPropertyValue("null")
  protected Class<? extends ICodeType> getConfiguredCodeType() {
    return null;
  }

  @ConfigProperty(ConfigProperty.COMPOSER_ATTRIBUTE_TYPE)
  @Order(70)
  @ConfigPropertyValue("TYPE_STRING")
  protected int getConfiguredType() {
    return TYPE_STRING;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredNullOperatorEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredNotOperatorEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(105)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredAggregationEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(110)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredVisible() {
    return true;
  }

  @ConfigOperation
  @Order(10)
  protected void execInitAttribute() throws ProcessingException {

  }

  @ConfigOperation
  @Order(20)
  protected void execPrepareLookup(LookupCall call) throws ProcessingException {
  }

  public Map<String, String> getMetaDataOfAttribute() {
    return null;
  }

  protected void initConfig() {
    m_visibleGranted = true;
    setNotOperatorEnabled(getConfiguredNotOperatorEnabled());
    setNullOperatorEnabled(getConfiguredNullOperatorEnabled());
    setAggregationEnabled(getConfiguredAggregationEnabled());
    setIconId(getConfiguredIconId());
    setText(getConfiguredText());
    setType(getConfiguredType());
    setVisible(getConfiguredVisible());
    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // lazy lookup decorator
    Class<? extends LookupCall> lsCls = getConfiguredLookupCall();
    if (lsCls != null) {
      LookupCall call;
      try {
        call = lsCls.newInstance();
        setLookupCall(call);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    injectOperators();
    injectAggregationTypes();
  }

  /*
   * Runtime
   */

  protected void injectOperators() {
    new DataModelAttributeInjector().injectOperators(this);
  }

  protected void injectAggregationTypes() {
    new DataModelAttributeInjector().injectAggregationTypes(this);
  }

  public final void initAttribute() throws ProcessingException {
    execInitAttribute();
  }

  public void prepareLookup(LookupCall call) throws ProcessingException {
    execPrepareLookup(call);
  }

  public String getText() {
    return m_text;
  }

  public void setText(String s) {
    m_text = s;
  }

  public int getType() {
    return m_type;
  }

  public void setType(int i) {
    m_type = i;
  }

  public String getIconId() {
    return m_iconId;
  }

  public void setIconId(String s) {
    m_iconId = s;
  }

  public IDataModelAttributeOp[] getOperators() {
    return m_operators;
  }

  public void setOperators(IDataModelAttributeOp[] ops) {
    m_operators = ops;
  }

  public int[] getAggregationTypes() {
    return m_aggregationTypes != null ? m_aggregationTypes : new int[0];
  }

  public void setAggregationTypes(int[] aggregationTypes) {
    m_aggregationTypes = aggregationTypes;
  }

  public boolean isNullOperatorEnabled() {
    return m_allowNullOperator;
  }

  public void setNullOperatorEnabled(boolean b) {
    m_allowNullOperator = b;
  }

  public boolean isAggregationEnabled() {
    return m_aggregationEnabled;
  }

  public void setAggregationEnabled(boolean aggregationEnabled) {
    m_aggregationEnabled = aggregationEnabled;
    if (m_aggregationTypes != null) {
      injectAggregationTypes();
    }
  }

  public boolean isNotOperatorEnabled() {
    return m_allowNotOperator;
  }

  public void setNotOperatorEnabled(boolean b) {
    m_allowNotOperator = b;
  }

  public Class<? extends ICodeType> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  public void setCodeTypeClass(Class<? extends ICodeType> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = new CodeLookupCall(m_codeTypeClass);
    }
  }

  public LookupCall getLookupCall() {
    return m_lookupCall;
  }

  public void setLookupCall(LookupCall call) {
    m_lookupCall = call;
  }

  public Permission getVisiblePermission() {
    return m_visiblePermission;
  }

  public void setVisiblePermission(Permission p) {
    m_visiblePermission = p;
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setVisibleGranted(b);
  }

  public boolean isVisibleGranted() {
    return m_visibleGranted;
  }

  public void setVisibleGranted(boolean b) {
    m_visibleGranted = b;
    calculateVisible();
  }

  public boolean isVisible() {
    return m_visible;
  }

  public void setVisible(boolean b) {
    m_visibleProperty = b;
    calculateVisible();
  }

  public IDataModelEntity getParentEntity() {
    return m_parentEntity;
  }

  public void setParentEntity(IDataModelEntity parent) {
    m_parentEntity = parent;
  }

  /**
   * no access control for system buttons CANCEL and CLOSE
   */
  private void calculateVisible() {
    // access control
    m_visible = m_visibleGranted && m_visibleProperty;
  }

}
