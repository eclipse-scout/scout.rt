/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.tagfield;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallResult;
import org.eclipse.scout.rt.client.services.lookup.IQueryParam;
import org.eclipse.scout.rt.client.services.lookup.LookupCallResult;
import org.eclipse.scout.rt.client.services.lookup.QueryParam;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRowFetchedCallback;

@ClassId("c3e4f668-d55b-4748-b21d-8c539c25501a")
public abstract class AbstractTagField extends AbstractValueField<Set<String>> implements ITagField {

  private ITagFieldUIFacade m_uiFacade;
  private ILookupCall<String> m_lookupCall;
  private IFuture<Void> m_runningLookup;

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());

    setMaxLength(getConfiguredMaxLength());
    Class<? extends ILookupCall<String>> lookupCallClass = getConfiguredLookupCall();
    if (lookupCallClass != null) {
      setLookupCall(BEANS.get(lookupCallClass));
    }
  }

  /**
   * Configures the initial value of {@link AbstractTagField#getMaxLength()
   * <p>
   * Subclasses can override this method
   * <p>
   * Default is 500
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(330)
  protected int getConfiguredMaxLength() {
    return 500;
  }

  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(250)
  protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
    return null;
  }

  @Override
  protected String formatValueInternal(Set<String> value) {
    // Info: value and displayText are not related in the TagField
    return "";
  }

  @Override
  public void addTag(String tag) {
    if (StringUtility.isNullOrEmpty(tag)) {
      return;
    }
    Set<String> tags = getOrCreateValue();
    if (tags.contains(tag)) {
      return;
    }

    Set<String> newTags = newSet(tags);
    newTags.add(tag);
    setValue(newTags);
  }

  protected Set<String> newSet(Collection<String> tags) {
    return new LinkedHashSet<>(tags);
  }

  protected Set<String> getOrCreateValue() {
    Set<String> value = getValue();
    if (value == null) {
      value = newSet(Collections.emptySet());
    }
    return value;
  }

  @Override
  public void setTags(Collection<String> tags) {
    if (tags == null) {
      tags = Collections.emptySet();
    }
    Set<String> tagSet = newSet(tags);
    if (tagSet.equals(getOrCreateValue())) {
      return;
    }
    setValue(tagSet);
  }

  @Override
  public void removeTag(String tag) {
    if (StringUtility.isNullOrEmpty(tag)) {
      return;
    }
    Set<String> tags = getOrCreateValue();
    if (!tags.contains(tag)) {
      return;
    }

    Set<String> newTags = newSet(tags);
    newTags.remove(tag);
    setValue(newTags);
  }

  @Override
  public void removeAllTags() {
    setTags(null);
  }

  @Override
  public void setMaxLength(int maxLength) {
    boolean changed = propertySupport.setPropertyInt(PROP_MAX_LENGTH, Math.max(0, maxLength));
    if (changed && isInitConfigDone()) {
      setValue(getValue());
    }
  }

  @Override
  public int getMaxLength() {
    return propertySupport.getPropertyInt(PROP_MAX_LENGTH);
  }

  @Override
  public void lookupByText(String text) {
    if (m_lookupCall == null) {
      return;
    }
    if (m_runningLookup != null) {
      m_runningLookup.cancel(false);
    }
    ILookupRowFetchedCallback<String> callback = new P_LookupByTextCallback(text);
    m_lookupCall.setText(text);
    m_runningLookup = m_lookupCall.getDataByTextInBackground(ClientRunContexts.copyCurrent(), callback);
  }

  public ILookupCall<String> getLookupCall() {
    return m_lookupCall;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ILookupCallResult<String> getResult() {
    return (ILookupCallResult<String>) propertySupport.getProperty(PROP_RESULT);
  }

  public void setResult(ILookupCallResult<String> result) {
    propertySupport.setProperty(PROP_RESULT, result);
  }

  protected void setLookupCall(ILookupCall<String> lookupCall) {
    m_lookupCall = lookupCall;
  }

  @Override
  public ITagFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements ITagFieldUIFacade {

    @Override
    public void setValueFromUI(Set<String> value) {
      if (!isEnabledIncludingParents() || !isVisibleIncludingParents()) {
        return;
      }
      Set<String> ensuredValue = value.stream()
          .map(this::ensureMaxLength)
          .collect(Collectors.toSet());
      setValue(ensuredValue);
    }

    @Override
    public void lookupByTextFromUI(String text) {
      if (!isEnabledIncludingParents() || !isVisibleIncludingParents()) {
        return;
      }
      lookupByText(ensureMaxLength(text));
    }

    protected String ensureMaxLength(String value) {
      return StringUtility.substring(value, 0, getMaxLength());
    }
  }

  protected class P_LookupByTextCallback implements ILookupRowFetchedCallback<String> {

    private final IQueryParam<String> m_queryParam;

    public P_LookupByTextCallback(String text) {
      m_queryParam = QueryParam.createByText(text);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSuccess(List<? extends ILookupRow<String>> lookupRows) {
      try {
        ClientRunContext runContext = ClientRunContexts.copyCurrent();
        if (runContext.getRunMonitor().isCancelled()) {
          return;
        }
        ModelJobs.schedule(() -> setResult(new LookupCallResult(lookupRows, m_queryParam, null)), ModelJobs.newInput(runContext)
                .withName("Updating {}", AbstractTagField.this.getClass().getName()))
            .awaitDone(); // block the current thread until completed
      }
      catch (ThreadInterruptedError e) { // NOSONAR
        // NOP
      }
    }

    @Override
    public void onFailure(RuntimeException exception) {
      // NOP
    }
  }
}
