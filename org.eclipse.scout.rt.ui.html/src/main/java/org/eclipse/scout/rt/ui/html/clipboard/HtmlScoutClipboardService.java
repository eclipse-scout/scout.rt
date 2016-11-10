/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.clipboard;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardService;
import org.eclipse.scout.rt.client.ui.dnd.TextTransferObject;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.clipboard.ClipboardForm;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.resource.MimeType;
import org.eclipse.scout.rt.platform.util.StringUtility;

public class HtmlScoutClipboardService implements IClipboardService {

  @Override
  public Collection<BinaryResource> getClipboardContents(MimeType... mimeTypes) {
    ClipboardForm form = new ClipboardForm();
    form.setMimeTypes(mimeTypes);
    execInitClipboardForm(form);

    form.startPaste();
    form.waitFor();
    if (form.isFormStored()) {
      return form.getClipboardField().getValue();
    }
    return Collections.emptyList();
  }

  /**
   * Callback to modify the clipboard-form before it is started.
   *
   * @param form
   */
  protected void execInitClipboardForm(ClipboardForm form) {
    // default empty
  }

  @Override
  public void setContents(final TransferObject transferObject) {
    if (transferObject instanceof TextTransferObject) {
      setTextContents(((TextTransferObject) transferObject).getPlainText());
      return;
    }
    throw new ProcessingException("Not implemented");
  }

  @Override
  public void setTextContents(String textContents) {
    ClipboardForm form = new ClipboardForm();
    form.setMimeTypes(MimeType.TXT);
    // anonymous text paste, no filename
    BinaryResource binaryResource = BinaryResources.create()
        .withContentType(MimeType.TXT.getType())
        .withContent(StringUtility.nvl(textContents, "").getBytes(StandardCharsets.UTF_8))
        .build();

    form.getClipboardField().setValue(Collections.singleton(binaryResource));
    execInitClipboardForm(form);
    form.startCopy();
  }
}
