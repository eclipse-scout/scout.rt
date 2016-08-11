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

    widget = new scout.NullWidget(),
    parent = new scout.NullWidget();
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

  describe('_firePropertyChange', function() {

    var propertyChangeEvent, widget;

    beforeEach(function() {
      widget = new scout.NullWidget();
    });

    function firePropertyChange(oldValue, newValue) {
      widget.on('propertyChange', function(event) {
        propertyChangeEvent = event;
      });
      widget._firePropertyChange('selected', oldValue, newValue);
    }

    it('fires the expected event object', function() {
      firePropertyChange(false, true);

      expect(scout.objects.countOwnProperties(propertyChangeEvent.oldProperties)).toBe(1);
      expect(scout.objects.countOwnProperties(propertyChangeEvent.newProperties)).toBe(1);
      expect(propertyChangeEvent.changedProperties.length).toBe(1);

      expect(propertyChangeEvent.oldProperties.selected).toBe(false);
      expect(propertyChangeEvent.newProperties.selected).toBe(true);
      expect(propertyChangeEvent.changedProperties[0]).toBe('selected');
    });

    // TODO [awe] 6.1: discuss with B.SH - when a property has _not_ changed, should it be
    // fired as new/old property anyway? When no property has changed, should the propertyChange
    // event be fired anyway?
    it('changedProperties is only set when new and old value are not equals', function() {
      firePropertyChange(true, true);
      expect(scout.objects.countOwnProperties(propertyChangeEvent.oldProperties)).toBe(1);
      expect(scout.objects.countOwnProperties(propertyChangeEvent.newProperties)).toBe(1);
      expect(propertyChangeEvent.changedProperties.length).toBe(0);
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
      var widgetClone = widget.clone();
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
      var widgetClone = widget.clone({
        text: 'foo'
      });
      expect(widgetClone.text).toBe('foo');
    });

  });

});
