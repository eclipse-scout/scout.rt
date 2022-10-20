/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {scout} from '../index';

/**
 * Pendant for org.eclipse.scout.rt.platform.resource.MimeType enumeration.
 */


const TEXT_PLAIN = 'text/plain';
const TEXT_HTML = 'text/html';
const TEXT_CSS = 'text/css';
const TEXT_JAVASCRIPT = 'text/javascript';

const IMAGE_PNG = 'image/png';
const IMAGE_JPG = 'image/jpg';
const IMAGE_JPEG = 'image/jpeg';
const IMAGE_GIF = 'image/gif';

const APPLICATION_JSON = 'application/json';
const APPLICATION_JAVASCRIPT = 'application/javascript';
const APPLICATION_ZIP = 'application/zip';

const JSON = APPLICATION_JSON;
const JAVASCRIPT = APPLICATION_JAVASCRIPT;
const ZIP = APPLICATION_ZIP;

export function isTextMimeType(mimeType) {
  return scout.isOneOf(mimeType,
    TEXT_PLAIN,
    TEXT_HTML,
    TEXT_CSS,
    TEXT_JAVASCRIPT,
    APPLICATION_JAVASCRIPT,
    APPLICATION_JSON);
}

export function isJavaScript(mimeType) {
  return scout.isOneOf(mimeType,
    TEXT_JAVASCRIPT,
    APPLICATION_JAVASCRIPT);
}

export default {
  APPLICATION_JAVASCRIPT,
  APPLICATION_JSON,
  APPLICATION_ZIP,
  IMAGE_GIF,
  IMAGE_JPEG,
  IMAGE_JPG,
  IMAGE_PNG,
  JAVASCRIPT,
  JSON,
  TEXT_CSS,
  TEXT_HTML,
  TEXT_JAVASCRIPT,
  TEXT_PLAIN,
  ZIP,
  isJavaScript,
  isTextMimeType
};
