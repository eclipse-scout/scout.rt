/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ValueFieldModel} from '../../../index';

export interface ClipboardFieldModel extends ValueFieldModel<string> {
  /**
   * Configures the allowed mime types for the clipboard paste event.
   * Default is null which does not restrict the allowed types.
   * @see https://developer.mozilla.org/en-US/docs/Glossary/MIME_type
   */
  allowedMimeTypes?: string[];
  /**
   * Configures the maximum size for a clipboard paste event. Default is no limit.
   */
  maximumSize?: number;
  readOnly?: boolean;
}
