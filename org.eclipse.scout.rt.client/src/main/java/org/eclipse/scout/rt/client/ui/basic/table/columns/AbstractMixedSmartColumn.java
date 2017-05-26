/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IColumnExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IMixedSmartColumnExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.MixedSmartColumnChains.MixedSmartColumnConvertKeyToValueChain;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractMixedSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IMixedSmartField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;

@ClassId("e9fef73d-71e4-4078-b34b-e3f5b8602a4c")
public abstract class AbstractMixedSmartColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> extends AbstractContentAssistColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> implements IMixedSmartColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE>, IContributionOwner {
  private boolean m_sortCodesByDisplayText;
  private IContributionOwner m_contributionHolder;

  public AbstractMixedSmartColumn() {
    this(true);
  }

  public AbstractMixedSmartColumn(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <T> List<T> getContributionsByClass(Class<T> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <T> T getContribution(Class<T> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  /**
   * Configures whether the values are sorted by display text or by sort code in case of a code type class. This
   * configuration only is useful if a code type class is set (see {@link #getConfiguredCodeType()}). In case of a
   * lookup call, the values are sorted by display text.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if values are sorted by display text, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(160)
  protected boolean getConfiguredSortCodesByDisplayText() {
    return false;
  }

  /**
   * When the smart proposal finds no matching records and this property is not null, then it displays a link or menu
   * with this label.<br>
   * When clicked the method {@link AbstractSmartField#execBrowseNew(String)} is invoked, which in most cases is
   * implemented as opening a "New XY..." dialog
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(315)
  protected String getConfiguredBrowseNewText() {
    return null;
  }

  /**
   * the default implementation simply casts one to the other type
   *
   * @param key
   * @return
   */
  @SuppressWarnings("unchecked")
  @ConfigOperation
  @Order(400)
  protected VALUE_TYPE execConvertKeyToValue(LOOKUP_CALL_KEY_TYPE key) {
    return (VALUE_TYPE) key;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_contributionHolder = new ContributionComposite(this);
    setSortCodesByDisplayText(getConfiguredSortCodesByDisplayText());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSortCodesByDisplayText() {
    return m_sortCodesByDisplayText;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setSortCodesByDisplayText(boolean b) {
    m_sortCodesByDisplayText = b;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected VALUE_TYPE parseValueInternal(ITableRow row, Object rawValue) {
    VALUE_TYPE validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (getDataType().isAssignableFrom(rawValue.getClass())) {
      validValue = (VALUE_TYPE) rawValue;
    }
    else {
      try {
        validValue = TypeCastUtility.castValue(rawValue, getDataType());
      }
      catch (Exception e) {
        throw new ProcessingException("invalid " + getDataType().getSimpleName() + " value in column '" + getClass().getName() + "': " + rawValue + " class=" + rawValue.getClass(), e);
      }
    }
    return validValue;
  }

  @Override
  protected IFormField prepareEditInternal(final ITableRow row) {
    MixedSmartEditorField f = (MixedSmartEditorField) getDefaultEditor();
    f.setRow(row);
    mapEditorFieldProperties(f);
    return f;
  }

  protected void mapEditorFieldProperties(IMixedSmartField<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> f) {
    super.mapEditorFieldProperties(f);
    f.setBrowseNewText(getConfiguredBrowseNewText());
  }

  @Override
  protected MixedSmartEditorField createDefaultEditor() {
    return new MixedSmartEditorField();
  }

  @Override
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    ICodeType<?, LOOKUP_CALL_KEY_TYPE> codeType = getCodeTypeClass() != null ? BEANS.opt(getCodeTypeClass()) : null;
    ILookupCall<LOOKUP_CALL_KEY_TYPE> call = getLookupCall() != null ? getLookupCall() : null;
    if (codeType != null) {
      if (isSortCodesByDisplayText()) {
        String s1 = getDisplayText(r1);
        String s2 = getDisplayText(r2);
        return StringUtility.compareIgnoreCase(s1, s2);
      }
      else {
        LOOKUP_CALL_KEY_TYPE t1 = interceptConvertValueToKey(getValue(r1));
        LOOKUP_CALL_KEY_TYPE t2 = interceptConvertValueToKey(getValue(r2));
        Integer sort1 = (t1 != null ? codeType.getCodeIndex(t1) : -1);
        Integer sort2 = (t2 != null ? codeType.getCodeIndex(t2) : -1);
        int c = sort1.compareTo(sort2);
        return c;
      }
    }
    else if (call != null) {
      String s1 = getDisplayText(r1);
      String s2 = getDisplayText(r2);
      return StringUtility.compareIgnoreCase(s1, s2);
    }
    else {
      return super.compareTableRows(r1, r2);
    }
  }

  protected final VALUE_TYPE interceptConvertKeyToValue(LOOKUP_CALL_KEY_TYPE key) {
    List<? extends IColumnExtension<VALUE_TYPE, ? extends AbstractColumn<VALUE_TYPE>>> extensions = getAllExtensions();
    MixedSmartColumnConvertKeyToValueChain<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> chain = new MixedSmartColumnConvertKeyToValueChain<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE>(extensions);
    return chain.execConvertKeyToValue(key);
  }

  protected static class LocalMixedSmartColumnExtension<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE, OWNER extends AbstractMixedSmartColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE>>
      extends LocalContentAssistColumnExtension<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE, OWNER> implements IMixedSmartColumnExtension<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE, OWNER> {

    public LocalMixedSmartColumnExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public VALUE_TYPE execConvertKeyToValue(MixedSmartColumnConvertKeyToValueChain<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> chain, LOOKUP_CALL_KEY_TYPE key) {
      return getOwner().execConvertKeyToValue(key);
    }
  }

  @Override
  protected IMixedSmartColumnExtension<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE, ? extends AbstractMixedSmartColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE>> createLocalExtension() {
    return new LocalMixedSmartColumnExtension<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE, AbstractMixedSmartColumn<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE>>(this);
  }

  /**
   * Internal editor field
   */
  @ClassId("eb0895b4-c31e-4376-b97b-35aea0063c79")
  @SuppressWarnings("bsiRulesDefinition:orderMissing")
  protected class MixedSmartEditorField extends AbstractMixedSmartField<VALUE_TYPE, LOOKUP_CALL_KEY_TYPE> {
    private ITableRow m_row;

    protected ITableRow getRow() {
      return m_row;
    }

    protected void setRow(ITableRow row) {
      m_row = row;
    }

    @Override
    protected void initConfig() {
      super.initConfig();
      propertySupport.putPropertiesMap(AbstractMixedSmartColumn.this.propertySupport.getPropertiesMap());
    }

    @Override
    public Class<VALUE_TYPE> getHolderType() {
      return AbstractMixedSmartColumn.this.getDataType();
    }

    @Override
    protected void execPrepareLookup(ILookupCall<LOOKUP_CALL_KEY_TYPE> call) {
      AbstractMixedSmartColumn.this.interceptPrepareLookup(call, getRow());
    }

    @Override
    protected LOOKUP_CALL_KEY_TYPE execConvertValueToKey(VALUE_TYPE value) {
      return AbstractMixedSmartColumn.this.interceptConvertValueToKey(value);
    }

    @Override
    protected VALUE_TYPE execConvertKeyToValue(LOOKUP_CALL_KEY_TYPE key) {
      return AbstractMixedSmartColumn.this.interceptConvertKeyToValue(key);
    }

    @Override
    protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
      Class[] menuCandidates = ConfigurationUtility.getDeclaredPublicClasses(AbstractMixedSmartColumn.this.getClass());
      List<Class<IMenu>> menuClazzes = ConfigurationUtility.filterClasses(menuCandidates, IMenu.class);
      for (Class<? extends IMenu> menuClazz : menuClazzes) {
        menus.addOrdered(ConfigurationUtility.newInnerInstance(AbstractMixedSmartColumn.this, menuClazz));
      }

      List<IMenu> contributedMenus = AbstractMixedSmartColumn.this.m_contributionHolder.getContributionsByClass(IMenu.class);
      menus.addAllOrdered(contributedMenus);
    }
  }
}
