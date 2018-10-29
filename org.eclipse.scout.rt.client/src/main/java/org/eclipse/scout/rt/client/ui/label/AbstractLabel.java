/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.label;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.label.ILabelExtension;
import org.eclipse.scout.rt.client.extension.ui.label.LabelChains.LabelAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;

public class AbstractLabel extends AbstractWidget implements ILabel {
  private final ObjectExtensions<AbstractLabel, ILabelExtension<? extends AbstractLabel>> m_objectExtensions;
  private ILabelUIFacade m_uiFacade;
  private Map<String, BinaryResource> m_attachments;

  public AbstractLabel() {
    this(true);
  }

  public AbstractLabel(boolean callInitializer) {
    super(false);
    m_objectExtensions = new ObjectExtensions<>(this, false);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    m_attachments = new HashMap<>();
    super.initConfig();
    setHtmlEnabled(getConfiguredHtmlEnabled());
    setValue(getConfiguredValue());
  }

  @Override
  protected void initConfigInternal() {
    m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), this::initConfig);
  }

  @Override
  public String getValue() {
    return propertySupport.getPropertyString(PROP_VALUE);
  }

  @Override
  public void setValue(String value) {
    propertySupport.setPropertyString(PROP_VALUE, value);
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(10)
  protected String getConfiguredValue() {
    return null;
  }

  @Override
  public boolean isHtmlEnabled() {
    return propertySupport.getPropertyBool(PROP_HTML_ENABLED);
  }

  @Override
  public void setHtmlEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_HTML_ENABLED, enabled);
  }

  /**
   * Configures, if HTML rendering is enabled.
   * <p>
   * Subclasses can override this method. Default is {@code false}. Make sure that any user input (or other insecure
   * input) is encoded (security), if this property is enabled.
   *
   * @return {@code true}, if HTML rendering is enabled, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredHtmlEnabled() {
    return false;
  }

  /**
   * local images and local resources bound to the html text
   */
  @Override
  public Set<BinaryResource> getAttachments() {
    return CollectionUtility.hashSet(m_attachments.values());
  }

  @Override
  public BinaryResource getAttachment(String filename) {
    return m_attachments.get(filename);
  }

  @Override
  public void setAttachments(Collection<? extends BinaryResource> attachments) {
    if (attachments == null) {
      m_attachments = new HashMap<>(0);
      return;
    }
    Map<String, BinaryResource> newMap = new HashMap<>(attachments.size());
    for (BinaryResource attachment : attachments) {
      if (attachment != null) {
        newMap.put(attachment.getFilename(), attachment);
      }
    }
    m_attachments = newMap;
  }

  @Override
  public void doAppLinkAction(String ref) {
    interceptAppLinkAction(ref);
  }

  /**
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(230)
  protected void execAppLinkAction(String ref) {
  }

  protected final void interceptAppLinkAction(String ref) {
    List<? extends ILabelExtension<? extends AbstractLabel>> extensions = getAllExtensions();
    LabelAppLinkActionChain chain = new LabelAppLinkActionChain(extensions);
    chain.execAppLinkAction(ref);
  }

  protected ILabelExtension<? extends AbstractLabel> createLocalExtension() {
    return new LocalLabelExtension<>(this);
  }

  @Override
  public final List<? extends ILabelExtension<? extends AbstractLabel>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  protected static class LocalLabelExtension<OWNER extends AbstractLabel> extends AbstractExtension<OWNER> implements ILabelExtension<OWNER> {

    public LocalLabelExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execAppLinkAction(LabelAppLinkActionChain chain, String ref) {
      getOwner().execAppLinkAction(ref);
    }
  }

  @Override
  public ILabelUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected ILabelUIFacade createUIFacade() {
    return new P_UIFacade();
  }

  protected class P_UIFacade implements ILabelUIFacade {

    @Override
    public void fireAppLinkActionFromUI(String ref) {
      doAppLinkAction(ref);
    }
  }

}
