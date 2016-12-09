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
  TEXT_HTML: 'text/html',
  TEXT_CSS: 'text/css',

  IMAGE_PNG: 'image/png',
  IMAGE_JPG: 'image/jpg',
  IMAGE_JPEG: 'image/jpeg',
  IMAGE_GIF: 'image/gif',

  JSON: 'application/json',
  JAVASCRIPT: 'text/javascript',
  ZIP: 'application/zip',

  isTextMimeType: function(mimeType) {
    return scout.isOneOf(mimeType,
      this.TEXT_PLAIN,
      this.TEXT_HTML,
      this.TEXT_CSS,
      this.JAVASCRIPT,
      this.JSON);
  }

};
