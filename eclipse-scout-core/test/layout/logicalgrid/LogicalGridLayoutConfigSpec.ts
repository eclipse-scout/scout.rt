/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HtmlEnvironment, InitModelOf, LogicalGridLayoutConfig, ObjectOrModel, scout, Widget} from '../../../src';

describe('LogicalGridLayoutConfig', () => {
  let session: SandboxSession;

  class LogicalGridWidget extends Widget {
    layoutConfig: LogicalGridLayoutConfig;

    protected override _init(model: InitModelOf<this>) {
      super._init(model);
      this._setLayoutConfig(this.layoutConfig);
    }

    setLayoutConfig(config: ObjectOrModel<LogicalGridLayoutConfig>) {
      this.setProperty('layoutConfig', config);
    }

    protected _setLayoutConfig(config: ObjectOrModel<LogicalGridLayoutConfig>) {
      this._setProperty('layoutConfig', LogicalGridLayoutConfig.ensure(config));
      LogicalGridLayoutConfig.initHtmlEnvChangeHandler(this, () => this.layoutConfig, layoutConfig => this.setLayoutConfig(layoutConfig));
    }
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  it('is initialized with defaults from html env', () => {
    let config = new LogicalGridLayoutConfig();
    expect(config.columnWidth).toBe(HtmlEnvironment.get().formColumnWidth);
    expect(config.rowHeight).toBe(HtmlEnvironment.get().formRowHeight);
    expect(config.hgap).toBe(HtmlEnvironment.get().formColumnGap);
    expect(config.vgap).toBe(HtmlEnvironment.get().formRowGap);
  });

  it('updates itself on the widget when html env changes', () => {
    let htmlEnv = HtmlEnvironment.get();
    let config = new LogicalGridLayoutConfig();
    let widget = scout.create(LogicalGridWidget, {
      parent: session.desktop,
      layoutConfig: config
    });
    expect(widget.layoutConfig.columnWidth).toBe(htmlEnv.formColumnWidth);
    expect(widget.layoutConfig.rowHeight).toBe(htmlEnv.formRowHeight);

    htmlEnv.formColumnWidth = 99;
    htmlEnv.formRowHeight = 98;
    htmlEnv.init(); // Triggers property change event
    expect(widget.layoutConfig).not.toBe(config); // Different instance
    expect(widget.layoutConfig.columnWidth).toBe(99);
    expect(widget.layoutConfig.rowHeight).toBe(98);
  });

  it('removes env listener if widget is destroyed', () => {
    let htmlEnv = HtmlEnvironment.get();
    let listenerCount = htmlEnv.events.count();
    let config = new LogicalGridLayoutConfig();
    let widget = scout.create(LogicalGridWidget, {
      parent: session.desktop,
      layoutConfig: config
    });
    expect(htmlEnv.events.count()).toBe(listenerCount + 1);

    widget.setLayoutConfig(new LogicalGridLayoutConfig({hgap: 3}));
    expect(htmlEnv.events.count()).toBe(listenerCount + 1); // No additional listener registered

    widget.destroy();
    expect(htmlEnv.events.count()).toBe(listenerCount);
  });

  it('retains custom properties when html env changes', () => {
    let htmlEnv = HtmlEnvironment.get();
    let widget = scout.create(LogicalGridWidget, {
      parent: session.desktop,
      layoutConfig: {
        rowHeight: 887,
        hgap: 886,
        vgap: 885
      }
    });
    expect(widget.layoutConfig.columnWidth).toBe(htmlEnv.formColumnWidth); // not customized
    expect(widget.layoutConfig.rowHeight).toBe(887);
    expect(widget.layoutConfig.hgap).toBe(886);
    expect(widget.layoutConfig.vgap).toBe(885);

    htmlEnv.formColumnWidth = 99;
    htmlEnv.formRowHeight = 98;
    htmlEnv.formColumnGap = 97;
    htmlEnv.formRowGap = 96;
    htmlEnv.init(); // Triggers property change event
    expect(widget.layoutConfig.columnWidth).toBe(99);
    expect(widget.layoutConfig.rowHeight).toBe(887);
    expect(widget.layoutConfig.hgap).toBe(886);
    expect(widget.layoutConfig.vgap).toBe(885);
  });
});
