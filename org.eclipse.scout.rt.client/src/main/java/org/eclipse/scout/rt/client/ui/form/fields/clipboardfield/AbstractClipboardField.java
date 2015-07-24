/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.clipboardfield;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.extension.ui.form.fields.clipboardfield.IClipboardFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.TEXTS;

/**
 * Clipboard field to receive paste events, e.g. can be used where a paste event must be triggered on a specific field
 * to be caught.
 */
public abstract class AbstractClipboardField extends AbstractValueField<Collection<BinaryResource>> implements IClipboardField {

  public AbstractClipboardField() {
    this(true);
  }

  public AbstractClipboardField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */

  @Override
  protected void initConfig() {
    super.initConfig();

    setAllowedMimeTypes(getConfiguredAllowedMimeTypes());
    setMaximumSize(getConfiguredMaximumSize());
  }

  /**
   * Configures the maximum size for a clipboard paste event.
   * <p>
   * Subclasses can override this method. Default is 1024 * 1024 * 10 bytes.
   *
   * @return maximum size in bytes.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(10)
  protected long getConfiguredMaximumSize() {
    return 1024 * 1024 * 10;
  }

  /**
   * Configures the allowed mime types for the clipboard paste event.
   * <p>
   * Subclasses can override this method. Default is <code>null</code> which does not restrict the allowed types.
   *
   * @return allowed mime types.
   */
  @ConfigProperty(ConfigProperty.MIME_TYPES)
  @Order(20)
  protected List<String> getConfiguredAllowedMimeTypes() {
    return null;
  }

  protected static class LocalClipboardFieldExtension<OWNER extends AbstractClipboardField> extends LocalValueFieldExtension<Collection<BinaryResource>, OWNER> implements IClipboardFieldExtension<OWNER> {

    public LocalClipboardFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IClipboardFieldExtension<? extends AbstractClipboardField> createLocalExtension() {
    return new LocalClipboardFieldExtension<AbstractClipboardField>(this);
  }

  /*
   * Runtime
   */

  @Override
  public List<String> getAllowedMimeTypes() {
    return propertySupport.getPropertyList(PROP_ALLOWED_MIME_TYPES);
  }

  @Override
  public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
    propertySupport.setPropertyList(PROP_ALLOWED_MIME_TYPES, allowedMimeTypes);
  }

  @Override
  public long getMaximumSize() {
    return propertySupport.getPropertyLong(PROP_MAXIMUM_SIZE);
  }

  @Override
  public void setMaximumSize(long maximumSize) {
    propertySupport.setPropertyLong(PROP_MAXIMUM_SIZE, maximumSize);
  }

  @Override
  protected String formatValueInternal(Collection<BinaryResource> value) {
    return (value != null && !value.isEmpty()) ? TEXTS.get("ElementsInserted") : "";
  }
}
