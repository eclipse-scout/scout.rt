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
package org.eclipse.scout.rt.shared.services.lookup;

import java.io.Serializable;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.concurrent.CancellationException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.eclipse.scout.rt.shared.job.IRunContextProvider;

/**
 * There are 2 variants to use lookup values <br>
 * a) with a service that yields data (external data, large amount, on database) <br>
 * b) with data directly generated in the call itself (local data, small amount, in memory)
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
  private String m_text;
  private String m_all;
  private KEY_TYPE m_rec;
  private Object m_master;
  private TriState m_active;
  private int m_maxRowCount;
  private transient ILookupService<KEY_TYPE> m_serviceCached;

  private String m_wildcard = "*";

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

  protected ILookupService<KEY_TYPE> createLookupService() {
    ILookupService<KEY_TYPE> s = null;
    if (getConfiguredService() != null) {
      s = BEANS.get(getConfiguredService());
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
   *         should be fetched and {@link TriState#UNDEFINED} if active and inactive rows should be fetched
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
  public List<? extends ILookupRow<KEY_TYPE>> getDataByKey() {
    if (getKey() != null && getLookupService() != null) {
      return getLookupService().getDataByKey(this);
    }
    return CollectionUtility.emptyArrayList();
  }

  /**
   * Same as {@link #getDataByKey()} but in background.<br>
   * see {@link ILookupCallFetcher}<br>
   * {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is called in the background thread
   * <p>
   * Note: background call is only done when lookup call is not a {@link LocalLookupCall}
   *
   * @return the created async job if applicable or null
   */
  @Override
  public IFuture<?> getDataByKeyInBackground(final ILookupCallFetcher<KEY_TYPE> caller) {
    if (this instanceof LocalLookupCall) {
      try {
        caller.dataFetched(getDataByKey(), null);
      }
      catch (RuntimeException e) {
        caller.dataFetched(null, e);
      }
      return null;
    }
    else {
      return Jobs.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            caller.dataFetched(getDataByKey(), null);
          }
          catch (InterruptedException | CancellationException e) {
            // NOOP
          }
          catch (RuntimeException e) {
            caller.dataFetched(null, e);
          }
        }
      }, Jobs.newInput()
          .withRunContext(BEANS.get(IRunContextProvider.class).copyCurrent())
          .withName("Fetching lookup data [{}.getDataByKeyInBackground]", getClass().getName()));
    }
  }

  /**
   * Lookup by performing a "text" filter and activating the <text> tags
   * <p>
   * When getConfiguredService is set then delegate call to service, otherwise return null
   */
  @Override
  public List<? extends ILookupRow<KEY_TYPE>> getDataByText() {
    boolean masterValid = ((!getConfiguredMasterRequired()) || getMaster() != null);
    if (masterValid && getLookupService() != null) {
      return getLookupService().getDataByText(this);
    }
    else {
      return CollectionUtility.emptyArrayList();
    }
  }

  /**
   * Same as {@link #getDataByText()} but in background.<br>
   * see {@link ILookupCallFetcher}<br>
   * {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is called in the background thread
   * <p>
   * Note: background call is only done when lookup call is not a {@link LocalLookupCall}
   *
   * @return the created async job if applicable or null
   */
  @Override
  public IFuture<?> getDataByTextInBackground(final ILookupCallFetcher<KEY_TYPE> caller) {
    if (this instanceof LocalLookupCall) {
      try {
        caller.dataFetched(getDataByText(), null);
      }
      catch (RuntimeException e) {
        caller.dataFetched(null, e);
      }
      return null;
    }
    else {
      return Jobs.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            caller.dataFetched(getDataByText(), null);
          }
          catch (InterruptedException | CancellationException e) {
            // NOOP
          }
          catch (RuntimeException e) {
            caller.dataFetched(null, e);
          }
        }
      }, Jobs.newInput()
          .withRunContext(BEANS.get(IRunContextProvider.class).copyCurrent())
          .withName("Fetching lookup data [{}.getDataByTextInBackground]", getClass().getName()));
    }
  }

  /**
   * Lookup by performing a "all" filter and activating the <all> tags
   * <p>
   * When getConfiguredService is set then delegate call to service, otherwise return null
   */
  @Override
  public List<? extends ILookupRow<KEY_TYPE>> getDataByAll() {
    boolean masterValid = ((!getConfiguredMasterRequired()) || getMaster() != null);
    if (masterValid && getLookupService() != null) {
      return getLookupService().getDataByAll(this);
    }
    else {
      return CollectionUtility.emptyArrayList();
    }
  }

  /**
   * Same as {@link #getDataByAll()} but in background.<br>
   * see {@link ILookupCallFetcher}<br>
   * {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is called in the background thread
   * <p>
   * Note: background call is only done when lookup call is not a {@link LocalLookupCall}
   *
   * @return the created async job if applicable or null
   */
  @Override
  public IFuture<?> getDataByAllInBackground(final ILookupCallFetcher<KEY_TYPE> caller) {
    if (this instanceof LocalLookupCall) {
      try {
        caller.dataFetched(getDataByAll(), null);
      }
      catch (RuntimeException e) {
        caller.dataFetched(null, e);
      }
      return null;
    }
    else {
      return Jobs.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            caller.dataFetched(getDataByAll(), null);
          }
          catch (InterruptedException | CancellationException e) {
            // NOOP
          }
          catch (RuntimeException e) {
            caller.dataFetched(null, e);
          }
        }
      }, Jobs.newInput()
          .withRunContext(BEANS.get(IRunContextProvider.class).copyCurrent())
          .withName("Fetching lookup data [{}.getDataByAllInBackground]", getClass().getName()));
    }
  }

  /**
   * Lookup by performing a "recursion" filter and activating the <rec> tags
   * <p>
   * When getConfiguredService is set then delegate call to service, otherwise return null
   */
  @Override
  public List<? extends ILookupRow<KEY_TYPE>> getDataByRec() {
    if (getLookupService() != null) {
      return getLookupService().getDataByRec(this);
    }
    else {
      return CollectionUtility.emptyArrayList();
    }
  }

  /**
   * Same as {@link #getDataByRec()} but in background.<br>
   * see {@link ILookupCallFetcher}<br>
   * {@link ILookupCallFetcher#dataFetched(LookupRow[], ProcessingException)} is called in the background thread
   * <p>
   * Note: background call is only done when lookup call is not a {@link LocalLookupCall}
   *
   * @return the created async job if applicable or null
   */
  @Override
  public IFuture<?> getDataByRecInBackground(final ILookupCallFetcher<KEY_TYPE> caller) {
    if (this instanceof LocalLookupCall) {
      try {
        caller.dataFetched(getDataByRec(), null);
      }
      catch (RuntimeException e) {
        caller.dataFetched(null, e);
      }
      return null;
    }
    else {
      return Jobs.schedule(new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            caller.dataFetched(getDataByRec(), null);
          }
          catch (InterruptedException | CancellationException e) {
            // NOOP
          }
          catch (RuntimeException e) {
            caller.dataFetched(null, e);
          }
        }
      }, Jobs.newInput()
          .withRunContext(BEANS.get(IRunContextProvider.class).copyCurrent())
          .withName("Fetching lookup data [{}.getDataByRecInBackground]", getClass().getName()));
    }
  }

  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("key", m_key, false);
    builder.attr("text", m_text, false);
    builder.attr("all", m_all, false);
    builder.attr("rec", m_rec, false);
    builder.attr("master", m_master, false);
    builder.attr("maxRowCount", m_maxRowCount);
    return builder.toString();
  }

  @Override
  public void setWildcard(String wildcard) {
    if (StringUtility.isNullOrEmpty(wildcard)) {
      throw new IllegalArgumentException("Wildcard must not be null nor empty!");
    }
    m_wildcard = wildcard;
  }

  @Override
  public String getWildcard() {
    return m_wildcard;
  }
}
