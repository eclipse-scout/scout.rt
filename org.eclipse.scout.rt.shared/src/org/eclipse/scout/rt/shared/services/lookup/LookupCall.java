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
package org.eclipse.scout.rt.shared.services.lookup;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.shared.services.common.session.IJobRunnable;
import org.eclipse.scout.rt.shared.services.common.session.ISessionService;
import org.eclipse.scout.rt.shared.validate.annotations.MaxLength;
import org.eclipse.scout.service.SERVICES;

/**
 * There are 2 variants to use lookup values <br>
 * a) with a service that yields data (external data, large amount, on database) <br>
 * b) with data directly generated in the call itself (local data, small amount,
 * in memory)
 * <p>
 * When using (a) the configured property getConfiguredService is non-null <br>
 * When using (b) the method execCreateLookupRows() is non-empty. The subclass CodeLookupCall is implementing this
 * method by using data from a code type (enum).
 * <p>
 * The following three examples illustrate these cases
 * <p>
 * 1. External data on backend <br>
 * Service interface: ICompanyLookupService <br>
 * Service implementation: CompanyLookupService <br>
 * LookupCall: subclass of LookupCall, for example CompanyLookupCall using ICompanyLookupService
 * <p>
 * 2. CodeType (enum) <br>
 * Service interface: --- <br>
 * Service implementation: --- <br>
 * LookupCall: CodeLookupCall(ICodeType enumType), for example new CodeLookupCall(projectStatusCodeType)
 * <p>
 * 3. Local data <br>
 * Service interface: --- <br>
 * Service implementation: --- <br>
 * LookupCall: subclass of LocalLookupCall() with implementation of method
 * {@link LocalLookupCall#execCreateLookupRows()}
 */
public class LookupCall<KEY_TYPE> implements ILookupCall<KEY_TYPE>, Cloneable, Serializable, ITypeWithClassId {
  private static final long serialVersionUID = 0L;

  private KEY_TYPE m_key;
  @MaxLength(2000)
  private String m_text;
  private String m_all;
  private KEY_TYPE m_rec;
  private Object m_master;
  private TriState m_active;
  private int m_maxRowCount;
  private transient ILookupService<KEY_TYPE> m_serviceCached;

  public LookupCall() {
    m_serviceCached = createLookupService();
    m_active = TriState.UNDEFINED;
  }

  /**
   * Configurator will add get/set for properties
   */

  /**
   * Configurator is implementing this method
   */
  @ConfigProperty(ConfigProperty.LOOKUP_SERVICE)
  @Order(10)
  protected Class<? extends ILookupService<KEY_TYPE>> getConfiguredService() {
    return null;
  }

  /**
   * @return true if a master value is required for lookups by {@link #getDataByText()} and {@link #getDataByAll()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredMasterRequired() {
    return false;
  }

  @ConfigProperty(ConfigProperty.DOC)
  @Order(30)
  protected String getConfiguredDoc() {
    return null;
  }

  @Override
  public String classId() {
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
  }

  public ILookupService<KEY_TYPE> getLookupService() {
    if (m_serviceCached == null) {
      m_serviceCached = createLookupService();
    }
    return m_serviceCached;
  }

  private ILookupService<KEY_TYPE> createLookupService() {
    ILookupService<KEY_TYPE> s = null;
    if (getConfiguredService() != null) {
      s = SERVICES.getService(getConfiguredService());
      if (s == null) {
        throw new IllegalArgumentException("service " + getConfiguredService().getName() + " is either not registered in the clientProxy extension in the plugin.xml or this constructor is called outside the model thread");
      }
    }
    return s;
  }

  @Override
  public KEY_TYPE getKey() {
    return m_key;
  }

  public long getKeyAsLong() {
    Object o = getKey();
    if (o instanceof Number) {
      return ((Number) o).longValue();
    }
    else {
      return 0;
    }
  }

  @Override
  public void setKey(KEY_TYPE key) {
    m_key = key;
  }

  @Override
  public String getText() {
    return m_text;
  }

  @Override
  public void setText(String s) {
    m_text = s;
  }

  @Override
  public String getAll() {
    return m_all;
  }

  @Override
  public void setAll(String s) {
    m_all = s;
  }

  @Override
  public KEY_TYPE getRec() {
    return m_rec;
  }

  public long getRecAsLong() {
    Object o = getRec();
    if (o instanceof Number) {
      return ((Number) o).longValue();
    }
    else {
      return 0;
    }
  }

  @Override
  public void setRec(KEY_TYPE parent) {
    m_rec = parent;
  }

  /**
   * @return {@link TriState#TRUE} if only active rows should be fetched, {@link TriState#FALSE} if only incative rows
   *         should be fetched and {@link TriState#UNDEFINED} if active and inactive rows should be
   *         fetched
   */
  @Override
  public TriState getActive() {
    return m_active;
  }

  /**
   * see {@link #getActive()}
   */
  @Override
  public void setActive(TriState s) {
    if (s == null) {
      s = TriState.UNDEFINED;
    }
    m_active = s;
  }

  @Override
  public Object getMaster() {
    return m_master;
  }

  public long getMasterAsLong() {
    Object o = getMaster();
    if (o instanceof Number) {
      return ((Number) o).longValue();
    }
    else {
      return 0;
    }
  }

  @Override
  public void setMaster(Object master) {
    m_master = master;
  }

  @Override
  public int getMaxRowCount() {
    return m_maxRowCount;
  }

  @Override
  public void setMaxRowCount(int n) {
    m_maxRowCount = n;
  }

  @Override
  public Object clone() {
    LookupCall c = null;
    try {
      c = (LookupCall) super.clone();
    }
    catch (CloneNotSupportedException e) {
    }
    return c;
  }

  @Override
  public int hashCode() {
    return (m_key != null ? m_key.hashCode() : 0) ^ (m_text != null ? m_text.hashCode() : 0) ^ (m_all != null ? m_all.hashCode() : 0);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() != this.getClass()) {
      return false;
    }
    LookupCall other = (LookupCall) obj;
    if (this.m_serviceCached != null && other.m_serviceCached != null && this.m_serviceCached != other.m_serviceCached) {
      return false;
    }
    if (!(this.m_key == other.m_key || (this.m_key != null && this.m_key.equals(other.m_key)))) {
      return false;
    }
    if (!(this.m_text == other.m_text || (this.m_text != null && this.m_text.equals(other.m_text)))) {
      return false;
    }
    if (!(this.m_all == other.m_all || (this.m_all != null && this.m_all.equals(other.m_all)))) {
      return false;
    }
    if (!(this.m_rec == other.m_rec || (this.m_rec != null && this.m_rec.equals(other.m_rec)))) {
      return false;
    }
    if (!(this.m_master == other.m_master || (this.m_master != null && this.m_master.equals(other.m_master)))) {
      return false;
    }
    if (!(this.m_active == other.m_active || (this.m_active != null && this.m_active.equals(other.m_active)))) {
      return false;
    }
    if (this.m_maxRowCount != other.m_maxRowCount) {
      return false;
    }
    return true;
  }

  /**
   * Lookup by performing a "key" filter and activating the <key> tags
   * <p>
   * When getConfiguredService is set then delegate call to service, otherwise returns an empty array
   */
  @Override
  public List<? extends ILookupRow<KEY_TYPE>> getDataByKey() throws ProcessingException {
    if (getKey() == null) {
      return Collections.emptyList();
    }
    if (getLookupService() != null) {
      return getLookupService().getDataByKey(this);
    }
    return Collections.emptyList();
  }

  /**
   * Same as {@link #getDataByKey()} but in background.<br>
   * see {@link ILookupCallFetcher}<br>
   * {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is
   * called in the background thread
   * <p>
   * Note: background call is only done when lookup call is not a {@link LocalLookupCall}
   * 
   * @return the created async job if applicable or null
   */
  @Override
  public JobEx getDataByKeyInBackground(final ILookupCallFetcher<KEY_TYPE> caller) {
    if (!(LookupCall.this instanceof LocalLookupCall)) {
      JobEx job = createAsyncJob(getClass().getSimpleName() + ".getDataByKeyInBackground",
          new IJobRunnable() {
            @Override
            public IStatus run(IProgressMonitor monitor) {
              try {
                List<? extends ILookupRow<KEY_TYPE>> rows = getDataByKey();
                if (!JobEx.isCurrentJobCanceled()) {
                  caller.dataFetched(rows, null);
                }
              }
              catch (ProcessingException e) {
                if (!e.isInterruption() && !JobEx.isCurrentJobCanceled()) {
                  caller.dataFetched(null, e);
                }
              }
              return Status.OK_STATUS;
            }
          });
      job.setSystem(true);
      job.schedule();
      return job;
    }
    else {
      try {
        caller.dataFetched(getDataByKey(), null);
      }
      catch (ProcessingException e) {
        caller.dataFetched(null, e);
      }
      return null;
    }
  }

  /**
   * Lookup by performing a "text" filter and activating the <text> tags
   * <p>
   * When getConfiguredService is set then delegate call to service, otherwise return null
   */
  @Override
  public List<? extends ILookupRow<KEY_TYPE>> getDataByText() throws ProcessingException {
    boolean masterValid = ((!getConfiguredMasterRequired()) || getMaster() != null);
    if (masterValid && getLookupService() != null) {
      return getLookupService().getDataByText(this);
    }
    else {
      return Collections.emptyList();
    }
  }

  /**
   * Same as {@link #getDataByText()} but in background.<br>
   * see {@link ILookupCallFetcher}<br>
   * {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is
   * called in the background thread
   * <p>
   * Note: background call is only done when lookup call is not a {@link LocalLookupCall}
   * 
   * @return the created async job if applicable or null
   */
  @Override
  public JobEx getDataByTextInBackground(final ILookupCallFetcher<KEY_TYPE> caller) {
    if (!(LookupCall.this instanceof LocalLookupCall)) {
      JobEx job = createAsyncJob(getClass().getSimpleName() + ".getDataByTextInBackground", new IJobRunnable() {
        @Override
        public IStatus run(IProgressMonitor monitor) {
          try {
            List<? extends ILookupRow<KEY_TYPE>> rows = getDataByText();
            if (!JobEx.isCurrentJobCanceled()) {
              caller.dataFetched(rows, null);
            }
          }
          catch (ProcessingException e) {
            if (!e.isInterruption() && !JobEx.isCurrentJobCanceled()) {
              caller.dataFetched(null, e);
            }
          }
          return Status.OK_STATUS;
        }
      });
      job.setSystem(true);
      job.schedule();
      return job;
    }
    else {
      try {
        caller.dataFetched(getDataByText(), null);
      }
      catch (ProcessingException e) {
        caller.dataFetched(null, e);
      }
      return null;
    }
  }

  /**
   * Lookup by performing a "all" filter and activating the <all> tags
   * <p>
   * When getConfiguredService is set then delegate call to service, otherwise return null
   */
  @Override
  public List<? extends ILookupRow<KEY_TYPE>> getDataByAll() throws ProcessingException {
    boolean masterValid = ((!getConfiguredMasterRequired()) || getMaster() != null);
    if (masterValid && getLookupService() != null) {
      return getLookupService().getDataByAll(this);
    }
    else {
      return Collections.emptyList();
    }
  }

  /**
   * Same as {@link #getDataByAll()} but in background.<br>
   * see {@link ILookupCallFetcher}<br>
   * {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is
   * called in the background thread
   * <p>
   * Note: background call is only done when lookup call is not a {@link LocalLookupCall}
   * 
   * @return the created async job if applicable or null
   */
  @Override
  public JobEx getDataByAllInBackground(final ILookupCallFetcher<KEY_TYPE> caller) {
    if (!(LookupCall.this instanceof LocalLookupCall)) {
      JobEx job = createAsyncJob(getClass().getSimpleName() + ".getDataByAllInBackground",
          new IJobRunnable() {
            @Override
            public IStatus run(IProgressMonitor monitor) {
              try {
                List<? extends ILookupRow<KEY_TYPE>> rows = getDataByAll();
                if (!JobEx.isCurrentJobCanceled()) {
                  caller.dataFetched(rows, null);
                }
              }
              catch (ProcessingException e) {
                if (!e.isInterruption() && !JobEx.isCurrentJobCanceled()) {
                  caller.dataFetched(null, e);
                }
              }
              return Status.OK_STATUS;
            }
          });
      job.setSystem(true);
      job.schedule();
      return job;
    }
    else {
      try {
        caller.dataFetched(getDataByAll(), null);
      }
      catch (ProcessingException e) {
        caller.dataFetched(null, e);
      }
      return null;
    }
  }

  /**
   * Lookup by performing a "recursion" filter and activating the <rec> tags
   * <p>
   * When getConfiguredService is set then delegate call to service, otherwise return null
   */
  @Override
  public List<? extends ILookupRow<KEY_TYPE>> getDataByRec() throws ProcessingException {
    if (getLookupService() != null) {
      return getLookupService().getDataByRec(this);
    }
    else {
      return Collections.emptyList();
    }
  }

  /**
   * Same as {@link #getDataByRec()} but in background.<br>
   * see {@link ILookupCallFetcher}<br>
   * {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is
   * called in the background thread
   * <p>
   * Note: background call is only done when lookup call is not a {@link LocalLookupCall}
   * 
   * @return the created async job if applicable or null
   */
  @Override
  public JobEx getDataByRecInBackground(final ILookupCallFetcher<KEY_TYPE> caller) {
    if (!(LookupCall.this instanceof LocalLookupCall)) {
      JobEx job = createAsyncJob(getClass().getSimpleName() + ".getDataByRecInBackground",
          new IJobRunnable() {
            @Override
            public IStatus run(IProgressMonitor monitor) {
              try {
                List<? extends ILookupRow<KEY_TYPE>> rows = getDataByRec();
                if (!JobEx.isCurrentJobCanceled()) {
                  caller.dataFetched(rows, null);
                }
              }
              catch (ProcessingException e) {
                if (!e.isInterruption() && !JobEx.isCurrentJobCanceled()) {
                  caller.dataFetched(null, e);
                }
              }
              return Status.OK_STATUS;
            }
          });
      job.setSystem(true);
      job.schedule();
      return job;
    }
    else {
      try {
        caller.dataFetched(getDataByRec(), null);
      }
      catch (ProcessingException e) {
        caller.dataFetched(null, e);
      }
      return null;
    }
  }

  private JobEx createAsyncJob(String name, IJobRunnable runnable) {
    ISessionService service = SERVICES.getService(ISessionService.class);
    if (service == null) {
      return null;
    }
    return service.createAsyncJob(name, runnable);
  }

  @Override
  public String toString() {
    StringBuffer b = new StringBuffer(getClass().getSimpleName() + "[");
    if (m_key != null) {
      b.append("key=" + m_key + " ");
    }
    if (m_text != null) {
      b.append("text=" + m_text + " ");
    }
    if (m_all != null) {
      b.append("all=" + m_all + " ");
    }
    if (m_rec != null) {
      b.append("rec=" + m_rec + " ");
    }
    if (m_master != null) {
      b.append("master=" + m_master + " ");
    }
    if (m_maxRowCount > 0) {
      b.append("maxRowCount=" + m_maxRowCount + " ");
    }
    b.append("]");
    return b.toString();
  }
}
