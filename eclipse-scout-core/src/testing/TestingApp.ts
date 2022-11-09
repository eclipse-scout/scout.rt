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
import {InitModelOf, RemoteApp, Session} from '../index';

export class TestingApp extends RemoteApp {

  protected override _doBootstrapDefaultValues(): JQuery.Promise<void> {
    // nop for testing
    return null;
  }

  override _createSession(options: InitModelOf<Session>): Session {
    return super._createSession(options);
  }
}
