/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.tagfield;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;

@ClassId("c3e4f668-d55b-4748-b21d-8c539c25501a")
public abstract class AbstractTagField extends AbstractValueField<Set<String>> implements ITagField {

  private ITagFieldUIFacade m_uiFacade;

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
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

    Set<String> newTags = new HashSet<String>(tags);
    newTags.add(tag);
    setValue(new HashSet<String>(newTags));
  }

  protected Set<String> getOrCreateValue() {
    Set<String> value = getValue();
    if (value == null) {
      value = new HashSet<String>(Collections.emptySet());
    }
    return value;
  }

  @Override
  public void setTags(Collection<String> tags0) {
    if (tags0 == null) {
      tags0 = Collections.emptySet();
    }
    Set<String> tags = new HashSet<>(tags0);
    if (tags.equals(getOrCreateValue())) {
      return;
    }

    setValue(new HashSet<String>(tags));
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

    Set<String> newTags = new HashSet<>(tags);
    newTags.remove(tag);
    setValue(new HashSet<String>(newTags));
  }

  @Override
  public void removeAllTags() {
    setTags(null);
  }

  @Override
  public ITagFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  class P_UIFacade implements ITagFieldUIFacade {

    @Override
    public void setValueFromUI(Set<String> value) {
      if (!isEnabled() || !isVisible()) {
        return;
      }
      setValue(value);
    }
  }

}
