/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;

public class SearchState extends AbstractPropertyObserver implements ISearchState {

  private final ISearchStateUiFacade m_uiFacade;

  public SearchState() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(createUIFacade(), ModelContext.copyCurrent());
    setPending(true);
  }

  protected ISearchStateUiFacade createUIFacade() {
    return new P_UIFacade();
  }

  @Override
  public ISearchStateUiFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public int getResultCount() {
    return propertySupport.getPropertyInt(PROP_RESULT_COUNT);
  }

  @Override
  public void setResultCount(int resultCount) {
    propertySupport.setProperty(PROP_RESULT_COUNT, resultCount);
  }

  @Override
  public boolean isLimited() {
    return propertySupport.getPropertyBool(PROP_LIMITED);
  }

  @Override
  public void setLimited(boolean limited) {
    propertySupport.setProperty(PROP_LIMITED, limited);
  }

  @Override
  public boolean isPending() {
    return propertySupport.getPropertyBool(PROP_PENDING);
  }

  @Override
  public void setPending(boolean pending) {
    propertySupport.setProperty(PROP_PENDING, pending);
  }

  protected class P_UIFacade implements ISearchStateUiFacade {

    @Override
    public void setResultCountFromUI(int resultCount) {
      setResultCount(resultCount);
    }

    @Override
    public void setLimitedFromUI(boolean limited) {
      setLimited(limited);
    }

    @Override
    public void setPendingFromUI(boolean pending) {
      setPending(pending);
    }
  }
}
