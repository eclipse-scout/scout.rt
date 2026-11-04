/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

/**
 * Wrapper for binary content with metadata.
 *
 * @see "org.eclipse.scout.rt.platform.resource.BinaryResource"
 */
export interface BinaryResource {
  /**
   * Base64 encoded BLOB
   */
  content?: string;
  /**
   * Byte length of {@link content} when decoded or -1 if there is no content.
   */
  contentLength?: number;
  /**
   * The number of milliseconds since January 1, 1970, 00:00:00 GMT (epoch).
   */
  lastModified?: number;
  contentType?: string;
  filename?: string;
  /**
   * e.g. "UTF-8"
   */
  charset?: string;
  /**
   * A checksum-style fingerprint of the {@link content}. This fingerprint was calculated by applying the Adler32 algorithm to the content. If there is no content, the fingerprint is -1.
   */
  fingerprint?: number;
  cachingAllowed?: boolean;
  cacheMaxAge?: number;
}
