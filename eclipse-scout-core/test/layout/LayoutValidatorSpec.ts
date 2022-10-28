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
import {HtmlComponent, scout, StringField} from '../../src/index';

describe('LayoutValidator', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    session.layoutValidator.validate();
    // Remove root component
    session.desktop.$container.removeData('htmlComponent');
  });

  describe('invalidateTree', () => {

    it('keeps track of invalid html components', () => {
      let $comp = $('<div>').appendTo(session.$entryPoint);
      let htmlComp = HtmlComponent.install($comp, session);

      htmlComp.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);
    });

    it('considers only the topmost component', () => {
      let $comp = $('<div>').appendTo(session.$entryPoint);
      let htmlComp = HtmlComponent.install($comp, session);

      let $compChild = $('<div>').appendTo($comp);
      let htmlCompChild = HtmlComponent.install($compChild, session);

      htmlCompChild.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);
    });

    it('and validate roots', () => {
      let $comp = $('<div>').appendTo(session.$entryPoint);
      HtmlComponent.install($comp, session);

      let $compChild = $('<div>').appendTo($comp);
      let htmlCompChild = HtmlComponent.install($compChild, session);
      htmlCompChild.validateRoot = true;

      htmlCompChild.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlCompChild);
    });

    it('makes sure parent components are put in front of child components', () => {
      let $comp = $('<div>').appendTo(session.$entryPoint);
      let htmlComp = HtmlComponent.install($comp, session);
      let $grandchild = $comp.appendDiv().appendDiv();
      let htmlGrandChild = HtmlComponent.install($grandchild, session);

      htmlGrandChild.invalidateLayoutTree(false);
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlGrandChild);

      htmlComp.invalidateLayoutTree(false);
      expect(session.layoutValidator._invalidComponents.length).toBe(2);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);
      expect(session.layoutValidator._invalidComponents[1]).toBe(htmlGrandChild);
    });
  });

  describe('validate', () => {

    it('calls layout for each invalid html component', () => {
      let $comp = $('<div>').appendTo(session.$entryPoint);
      let htmlComp = HtmlComponent.install($comp, session);
      spyOn(htmlComp.layout, 'layout');

      htmlComp.invalidateLayoutTree();
      session.layoutValidator.validate();
      expect(htmlComp.layout.layout).toHaveBeenCalled();
      // @ts-ignore
      expect(htmlComp.layout.layout.calls.count()).toEqual(1);
    });

    it('does not call layout if component has been removed', () => {
      let $comp = $('<div>').appendTo(session.$entryPoint);
      let htmlComp = HtmlComponent.install($comp, session);
      spyOn(htmlComp.layout, 'layout');

      htmlComp.invalidateLayoutTree();
      $comp.remove();
      session.layoutValidator.validate();
      expect(htmlComp.layout.layout).not.toHaveBeenCalled();
    });

    it('does not call layout if component has been detached, but does not remove from invalid components either', () => {
      let $comp = $('<div>').appendTo(session.$entryPoint);
      let htmlComp = HtmlComponent.install($comp, session);
      spyOn(htmlComp.layout, 'layout');

      htmlComp.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);

      $comp.detach();
      session.layoutValidator.validate();
      expect(htmlComp.layout.layout).not.toHaveBeenCalled();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);
    });

    it('removes the component from the list of invalidate components after validation', () => {
      let $comp = $('<div>').appendTo(session.$entryPoint);
      let htmlComp = HtmlComponent.install($comp, session);
      spyOn(htmlComp.layout, 'layout');

      htmlComp.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);

      session.layoutValidator.validate();
      expect(htmlComp.layout.layout).toHaveBeenCalled();
      // @ts-ignore
      expect(htmlComp.layout.layout.calls.count()).toEqual(1);
      expect(session.layoutValidator._invalidComponents.length).toBe(0);
    });

  });

  describe('cleanupInvalidObjects', () => {

    it('removes the widget from invalid components when a widget gets removed', () => {
      let widget = scout.create(StringField, {
        parent: session.desktop
      });
      widget.render();

      widget.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(widget.htmlComp);

      widget.remove();
      expect(session.layoutValidator._invalidComponents.length).toBe(0);
    });

  });

});
