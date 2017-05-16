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
describe("LayoutValidator", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    session.layoutValidator.validate();
    // Remove root component
    session.desktop.$container.removeData('htmlComponent');
  });

  describe("invalidateTree", function() {

    it("keeps track of invalid html components", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      var htmlComp = scout.HtmlComponent.install($comp, session);

      htmlComp.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);
    });

    it("considers only the topmost component", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      var htmlComp = scout.HtmlComponent.install($comp, session);

      var $compChild = $('<div>').appendTo($comp);
      var htmlCompChild = scout.HtmlComponent.install($compChild, session);

      htmlCompChild.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);
    });

    it("and validate roots", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      scout.HtmlComponent.install($comp, session);

      var $compChild = $('<div>').appendTo($comp);
      var htmlCompChild = scout.HtmlComponent.install($compChild, session);
      htmlCompChild.validateRoot = true;

      htmlCompChild.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlCompChild);
    });

    it("makes sure parent components are put in front of child components", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      var htmlComp = scout.HtmlComponent.install($comp, session);
      var $grandchild = $comp.appendDiv().appendDiv();
      var htmlGrandChild = scout.HtmlComponent.install($grandchild, session);

      htmlGrandChild.invalidateLayoutTree(false);
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlGrandChild);

      htmlComp.invalidateLayoutTree(false);
      expect(session.layoutValidator._invalidComponents.length).toBe(2);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);
      expect(session.layoutValidator._invalidComponents[1]).toBe(htmlGrandChild);
    });
  });

  describe("validate", function() {

    it("calls layout for each invalid html component", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      var htmlComp = scout.HtmlComponent.install($comp, session);
      spyOn(htmlComp.layout, 'layout');

      htmlComp.invalidateLayoutTree();
      session.layoutValidator.validate();
      expect(htmlComp.layout.layout).toHaveBeenCalled();
      expect(htmlComp.layout.layout.calls.count()).toEqual(1);
    });

    it("does not call layout if component has been removed", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      var htmlComp = scout.HtmlComponent.install($comp, session);
      spyOn(htmlComp.layout, 'layout');

      htmlComp.invalidateLayoutTree();
      $comp.remove();
      session.layoutValidator.validate();
      expect(htmlComp.layout.layout).not.toHaveBeenCalled();
    });

    it("does not call layout if component has been detached, but does not remove from invalid components either", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      var htmlComp = scout.HtmlComponent.install($comp, session);
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

    it("removes the component from the list of invalidate components after validation", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      var htmlComp = scout.HtmlComponent.install($comp, session);
      spyOn(htmlComp.layout, 'layout');

      htmlComp.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);

      session.layoutValidator.validate();
      expect(htmlComp.layout.layout).toHaveBeenCalled();
      expect(htmlComp.layout.layout.calls.count()).toEqual(1);
      expect(session.layoutValidator._invalidComponents.length).toBe(0);
    });

  });

  describe("cleanupInvalidObjects", function() {

    it("removes the widget from invalid components when a widget gets removed", function() {
      var widget = scout.create('StringField', {
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
