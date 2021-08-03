/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.services.lookup;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRestrictionDo;
import org.eclipse.scout.rt.dataobject.lookup.AbstractLookupRowDo;
import org.eclipse.scout.rt.dataobject.lookup.LookupResponse;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * IMPORTANT: If you use subclasses of this lookup call in a smart column, make sure to use an
 * {@code org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractRestLookupSmartColumn}, not
 * {@code org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractSmartColumn}. Otherwise, batch lookups are
 * performed via service tunnel instead of calling a REST service.
 */
public abstract class AbstractRestLookupCall<RESTRICTION extends AbstractLookupRestrictionDo<?, ID>, ID> implements ILookupCall<ID> {
  private static final long serialVersionUID = 1L;

  protected RESTRICTION m_restrictionDo;

  protected AbstractRestLookupCall() {
    m_restrictionDo = BEANS.get(resolveRestrictionClass());
  }

  /**
   * Overwrite to resolve to a specific restriction class.
   * <p>
   * By default, the super hierarchy is looked for the restriction class type in the generic type declaration.
   */
  @SuppressWarnings("unchecked")
  protected Class<RESTRICTION> resolveRestrictionClass() {
    return TypeCastUtility.getGenericsParameterClass(getClass(), AbstractRestLookupCall.class, 0);
  }

  /**
   * @return the live restriction object (not {@code null})
   */
  public RESTRICTION getRestriction() {
    return m_restrictionDo;
  }

  @Override
  public ID getKey() {
    return CollectionUtility.firstElement(m_restrictionDo.getIds());
  }

  @Override
  public void setKey(ID key) {
    m_restrictionDo.getIds().clear();
    if (key != null) {
      m_restrictionDo.getIds().add(key);
    }
  }

  public List<ID> getKeys() {
    return m_restrictionDo.getIds();
  }

  public void setKeys(Collection<? extends ID> keys) {
    m_restrictionDo.withIds(keys);
  }

  @SafeVarargs
  public final void setKeys(@SuppressWarnings("unchecked") ID... keys) {
    m_restrictionDo.withIds(keys);
  }

  @Override
  public void setText(String text) {
    m_restrictionDo.withText(text);
  }

  @Override
  public void setAll(String s) {
    // NOP - unsupported
  }

  @Override
  public String getAll() {
    return null;
  }

  @Override
  public void setRec(ID parent) {
    // NOP - unsupported
  }

  @Override
  public ID getRec() {
    return null;
  }

  @Override
  public void setMaster(Object master) {
    // NOP - unsupported
  }

  @Override
  public Object getMaster() {
    return null;
  }

  @Override
  public void setActive(TriState activeState) {
    m_restrictionDo.withActive(activeState == null ? null : activeState.getBooleanValue());
  }

  @Override
  public TriState getActive() {
    return TriState.parse(m_restrictionDo.getActive());
  }

  @Override
  public String getText() {
    return m_restrictionDo.getText();
  }

  @Override
  public List<? extends ILookupRow<ID>> getDataByKey() {
    if (!m_restrictionDo.ids().exists() || m_restrictionDo.ids().isEmpty()) {
      return Collections.emptyList();
    }
    return getData();
  }

  @Override
  public IFuture<Void> getDataByKeyInBackground(RunContext runContext, ILookupRowFetchedCallback<ID> callback) {
    return loadDataInBackground(this::getDataByKey, runContext, callback);
  }

  @Override
  public List<? extends ILookupRow<ID>> getDataByText() {
    return getData();
  }

  @Override
  public IFuture<Void> getDataByTextInBackground(RunContext runContext, ILookupRowFetchedCallback<ID> callback) {
    return loadDataInBackground(this::getDataByText, runContext, callback);
  }

  @Override
  public List<? extends ILookupRow<ID>> getDataByAll() {
    return getData();
  }

  @Override
  public IFuture<Void> getDataByAllInBackground(RunContext runContext, ILookupRowFetchedCallback<ID> callback) {
    return loadDataInBackground(this::getDataByAll, runContext, callback);
  }

  @Override
  public List<? extends ILookupRow<ID>> getDataByRec() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public IFuture<Void> getDataByRecInBackground(RunContext runContext, ILookupRowFetchedCallback<ID> callback) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public int getMaxRowCount() {
    return NumberUtility.nvl(m_restrictionDo.getMaxRowCount(), 0);
  }

  @Override
  public void setMaxRowCount(int n) {
    m_restrictionDo.withMaxRowCount(n);
  }

  @Override
  public String getWildcard() {
    return null;
  }

  @Override
  public void setWildcard(String wildcard) {
    // NOP - unsupported
  }

  @Override
  public void setMultilineText(boolean b) {
    // NOP - unsupported
  }

  @Override
  public boolean isMultilineText() {
    return false;
  }

  protected List<? extends ILookupRow<ID>> getData() {
    execPrepareRestriction(m_restrictionDo);
    LookupResponse<? extends AbstractLookupRowDo<?, ID>> response = remoteCall().apply(m_restrictionDo);
    return transformLookupResponse(response);
  }

  /**
   * Called before remote call to lookup service resource
   */
  protected void execPrepareRestriction(RESTRICTION restriction) {
  }

  /**
   * Return function to fetch {@link LookupResponse} from remote
   */
  protected abstract Function<RESTRICTION, LookupResponse<? extends AbstractLookupRowDo<?, ID>>> remoteCall();

  /**
   * Loads data asynchronously, and calls the specified callback once completed.
   */
  protected IFuture<Void> loadDataInBackground(final Supplier<List<? extends ILookupRow<ID>>> supplier, final RunContext runContext, final ILookupRowFetchedCallback<ID> callback) {
    return Jobs.schedule(() -> loadData(supplier, callback), Jobs.newInput()
        .withRunContext(runContext)
        .withName("Fetching lookup data [lookupCall={}]", getClass().getName()));
  }

  /**
   * Loads data synchronously, and calls the specified callback once completed.
   */
  protected void loadData(final Supplier<List<? extends ILookupRow<ID>>> supplier, final ILookupRowFetchedCallback<ID> callback) {
    try {
      callback.onSuccess(supplier.get());
    }
    catch (RuntimeException e) {
      callback.onFailure(e);
    }
  }

  /**
   * Transforms {@link LookupResponse} with a list of {@link AbstractLookupRowDo} into a list of {@link ILookupRow}.
   */
  protected List<? extends ILookupRow<ID>> transformLookupResponse(LookupResponse<? extends AbstractLookupRowDo<?, ID>> response) {
    return response.getRows().stream()
        .map(this::transformLookupRow)
        .collect(Collectors.toList());
  }

  /**
   * Transforms one {@link AbstractLookupRowDo} into a {@link ILookupRow}.
   */
  protected ILookupRow<ID> transformLookupRow(AbstractLookupRowDo<?, ID> row) {
    return new LookupRow<>(row.getId(), row.getText())
        .withActive(row.isActive())
        .withEnabled(row.isEnabled())
        .withParentKey(row.getParentId());
  }

  @Override
  public AbstractRestLookupCall<RESTRICTION, ID> copy() {
    try {
      @SuppressWarnings("unchecked")
      AbstractRestLookupCall<RESTRICTION, ID> clone = (AbstractRestLookupCall<RESTRICTION, ID>) super.clone();
      clone.m_restrictionDo = BEANS.get(DataObjectHelper.class).clone(m_restrictionDo);
      return clone;
    }
    catch (CloneNotSupportedException e) {
      throw new PlatformException("could not clone rest lookup call", e);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[ m_restrictionDo=" + m_restrictionDo + "]";
  }
}
