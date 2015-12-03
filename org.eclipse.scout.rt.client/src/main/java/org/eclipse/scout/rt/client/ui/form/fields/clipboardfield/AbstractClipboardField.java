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
package org.eclipse.scout.rt.client.ui.form.fields.clipboardfield;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.clipboardfield.IClipboardFieldExtension;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.ResourceListTransferObject;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.MimeType;
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

    // DND
    setDropType(getConfiguredDropType());
    setDragType(getConfiguredDragType());
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
   * Configures the drop support of this string field.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drop support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(30)
  protected int getConfiguredDropType() {
    return IDNDSupport.TYPE_FILE_TRANSFER;
  }

  /**
   * Configures the drag support of this string field.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drag support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(40)
  protected int getConfiguredDragType() {
    return 0;
  }

  /**
   * Configures the allowed mime types for the clipboard paste event.
   * <p>
   * Subclasses can override this method. Default is <code>null</code> which does not restrict the allowed types.
   *
   * @return allowed mime types.
   */
  @ConfigProperty(ConfigProperty.MIME_TYPES)
  @Order(50)
  protected List<String> getConfiguredAllowedMimeTypes() {
    return null;
  }

  @ConfigOperation
  @Order(60)
  protected TransferObject execDragRequest() {
    return null;
  }

  @ConfigOperation
  @Order(70)
  protected void execDropRequest(TransferObject transferObject) {
    if (transferObject instanceof ResourceListTransferObject) {
      ResourceListTransferObject resourceListTransferObject = (ResourceListTransferObject) transferObject;
      setValue(resourceListTransferObject.getResources());
    }
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
    return formatValueAsText(value);
  }

  protected String formatValueAsText(Collection<BinaryResource> value) {
    if (value != null && !value.isEmpty()) {
      for (BinaryResource res : value) {
        if (MimeType.TEXT_PLAIN.getType().equals(res.getContentType())) {
          return res.getContentAsString();
        }
      }
      return TEXTS.get("ElementsInserted");
    }
    else {
      return "";
    }
  }

  // DND
  @Override
  public void setDragType(int dragType) {
    propertySupport.setPropertyInt(PROP_DRAG_TYPE, dragType);
  }

  @Override
  public int getDragType() {
    return propertySupport.getPropertyInt(PROP_DRAG_TYPE);
  }

  @Override
  public void setDropType(int dropType) {
    propertySupport.setPropertyInt(PROP_DROP_TYPE, dropType);
  }

  @Override
  public int getDropType() {
    return propertySupport.getPropertyInt(PROP_DROP_TYPE);
  }

  @Override
  public void setDropMaximumSize(long dropMaximumSize) {
    setMaximumSize(dropMaximumSize);
  }

  @Override
  public long getDropMaximumSize() {
    return getMaximumSize();
  }
}
