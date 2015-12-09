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
/**
 * Pendant for org.eclipse.scout.rt.platform.resource.MimeType enumeration.
 */
scout.mimeTypes = {

  TEXT_PLAIN: 'text/plain',

  IMAGE_PNG: 'image/png',
  IMAGE_JPG: 'image/jpg',
  IMAGE_JPEG: 'image/jpeg',
  IMAGE_GIF: 'image/gif',

  getDefaultFilename: function(type, suffix, prefix) {
    // TODO [5.2] mot, bsh: Use translated texts, Check with com.bsiag.crm.client.core.document.AbstractDocumentTableBox.execHandleClipboardPaste(Collection<BinaryResource>)
    var filename = 'Data';
    var extension = '';
    if (type === this.TEXT_PLAIN) {
      filename = 'Text';
      extension = '.txt';
    } else if (type === this.IMAGE_PNG) {
      filename = 'Picture';
      extension = '.png';
    } else if (type === this.IMAGE_JPG) {
      filename = 'Picture';
      extension = '.jpg';
    } else if (type === this.IMAGE_JPEG) {
      filename = 'Picture';
      extension = '.jpeg';
    } else if (type === this.IMAGE_GIF) {
      filename = 'Picture';
      extension = '.gif';
    }
    return scout.strings.box(prefix, filename, suffix) + extension;
  }

};
