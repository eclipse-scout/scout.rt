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
describe('Widget', function() {

  var session, widget, parent;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    session.init();

    widget = new scout.Widget(),
    parent = new scout.Widget();
  });

  describe('rendering', function() {

    it('should set rendering, rendered flags correctly', function() {
      widget.init({session: session, parent: parent});
      expect(widget.rendered).toBe(false);
      expect(widget.rendering).toBe(false);
      widget.render(session.$entryPoint);
      expect(widget.rendered).toBe(true);
      expect(widget.rendering).toBe(false);
    });

    it('should set rendering flag to true _while_ the component is rendering', function() {
      var rendering;
      widget._render = function($parent) {
        rendering = this.rendering;
      };
      widget.init({session: session, parent: parent});
      widget.render(session.$entryPoint);
      expect(rendering).toBe(true);
    });

  });

  describe('attach/detach', function() {

    it('attached and rendered is false by default', function() {
      expect(widget.rendered).toBe(false);
      expect(widget.attached).toBe(false);
    });

    it('attached and rendered has the right value after render/remove and attach/detach', function() {
      var $parent = $('<div>');
      widget.init({session: session, parent: parent});
      widget.render($parent);
      expect(widget.rendered).toBe(true);
      expect(widget.attached).toBe(true);

      widget.detach();
      expect(widget.rendered).toBe(true);
      expect(widget.attached).toBe(false);

      widget.attach();
      expect(widget.rendered).toBe(true);
      expect(widget.attached).toBe(true);

      widget.remove();
      expect(widget.rendered).toBe(false);
      expect(widget.attached).toBe(false);
    });

  });

  describe('clone', function() {

    var model, widget, expectedProperties = ['id', 'session', 'objectType', 'parent', 'text'];

    beforeEach(function() {
      model = createSimpleModel('Menu', session);
      model.label = 'bar';
      widget = scout.create(model);
      widget.$container = 'dummy container property';
    });

    it('clones only properties marked as clone property', function() {
      var widgetClone = widget.cloneAdapter();
      // should contain the following properties:
      expectedProperties.forEach(function(propertyName) {
        expect(widgetClone[propertyName]).not.toBe(undefined);
      });
      // but not the $container property (which has been added later)
      expect(widgetClone.$container).toBe(undefined);
    });

    it('\'text\' must be recognized as clone property, but not \'$container\'', function() {
      expect(widget._isCloneProperty('text')).toBe(true);
      expect(widget._isCloneProperty('$container')).toBe(false);
    });

    it('prefers properties passed as modelOverride', function() {
      var widgetClone = widget.cloneAdapter({
        text: 'foo'
      });
      expect(widgetClone.text).toBe('foo');
    });

  });

});
