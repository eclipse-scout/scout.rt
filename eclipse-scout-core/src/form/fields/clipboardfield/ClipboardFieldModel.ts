/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ValueFieldModel} from '../../../index';

export default interface ClipboardFieldModel extends ValueFieldModel<string> {
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
