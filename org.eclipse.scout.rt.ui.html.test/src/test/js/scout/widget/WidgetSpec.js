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

  function createWidget(model) {
    var defaults = {
      session: session
    };
    model = model || {};
    $.extend(model, defaults);
    return scout.create('NullWidget', model);
  }

  describe('rendering', function() {

    it('should set rendering, rendered flags correctly', function() {
      widget.init({
        session: session,
        parent: parent
      });
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
      widget.init({
        session: session,
        parent: parent
      });
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
      widget.init({
        session: session,
        parent: parent
      });
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

  describe('init', function() {

    it('links widget properties with the widget', function() {
      var child = createWidget({
        parent: parent
      });
      var widget = createWidget({
        parent: parent,
        childWidget: child
      });

      expect(child.parent).toBe(widget);
      expect(child.owner).toBe(parent);
    });

  });

  describe('destroy', function() {
    it('destroys the widget', function() {
      var widget = createWidget({
        parent: parent
      });
      expect(widget.destroyed).toBe(false);

      widget.destroy();
      expect(widget.destroyed).toBe(true);
    });

    it('destroys the children', function() {
      var widget = createWidget({
        parent: parent
      });
      var child0 = createWidget({
        parent: widget
      });
      var child1 = createWidget({
        parent: widget
      });
      expect(widget.destroyed).toBe(false);
      expect(child0.destroyed).toBe(false);
      expect(child1.destroyed).toBe(false);

      widget.destroy();
      expect(widget.destroyed).toBe(true);
      expect(child0.destroyed).toBe(true);
      expect(child1.destroyed).toBe(true);
    });

    it('does only destroy children if the parent is the owner', function() {
      var widget = createWidget({
        parent: parent
      });
      var another = createWidget({
        parent: parent
      });
      var child0 = createWidget({
        parent: widget,
        owner: another
      });
      var child1 = createWidget({
        parent: widget
      });
      expect(widget.destroyed).toBe(false);
      expect(another.destroyed).toBe(false);
      expect(child0.destroyed).toBe(false);
      expect(child1.destroyed).toBe(false);

      widget.destroy();
      expect(widget.destroyed).toBe(true);
      expect(another.destroyed).toBe(false);
      expect(child0.destroyed).toBe(false);
      expect(child1.destroyed).toBe(true);

      another.destroy();
      expect(another.destroyed).toBe(true);
      expect(child0.destroyed).toBe(true);
    });

    it('removes the link to parent and owner', function() {
      var widget = createWidget({
        parent: parent
      });
      expect(widget.parent).toBe(parent);
      expect(widget.owner).toBe(parent);
      expect(parent.children[0]).toBe(widget);

      widget.destroy();
      expect(widget.parent).toBe(null);
      expect(widget.owner).toBe(null);
      expect(parent.children.length).toBe(0);
    });
  });

  describe('setParent', function() {
    it('links the widget with the new parent', function() {
      var widget = createWidget({
        parent: parent
      });
      var another = createWidget({
        parent: parent
      });
      expect(widget.parent).toBe(parent);
      expect(another.parent).toBe(parent);

      another.setParent(widget);
      expect(widget.parent).toBe(parent);
      expect(another.parent).toBe(widget);
    });

    it('removes the widget from the old parent if the old is not the owner', function() {
      var widget = createWidget({
        parent: parent
      });
      var owner = createWidget({
        parent: new scout.NullWidget()
      });
      var another = createWidget({
        parent: parent,
        owner: owner
      });
      expect(parent.children[0]).toBe(widget);
      expect(parent.children[1]).toBe(another);
      expect(widget.children.length).toBe(0);

      another.setParent(widget);
      expect(parent.children[0]).toBe(widget);
      expect(parent.children.length).toBe(1);
      expect(widget.children.length).toBe(1);
      expect(widget.children[0]).toBe(another);
    });

    it('does not remove the widget from the old parent if the old is the owner', function() {
      var widget = createWidget({
        parent: parent
      });
      var another = createWidget({
        parent: parent
      });
      expect(another.owner).toBe(parent);
      expect(parent.children[0]).toBe(widget);
      expect(parent.children[1]).toBe(another);
      expect(widget.children.length).toBe(0);

      // The reference to the owner must always be maintained so that the widget will be destroyed eventually
      another.setParent(widget);
      expect(parent.children[0]).toBe(widget);
      expect(parent.children[1]).toBe(another);
      expect(parent.children.length).toBe(2);
      expect(widget.children.length).toBe(1);
      expect(widget.children[0]).toBe(another);
    });

    it('relinks parent destroy listener to the new parent', function() {
      var widget = createWidget({
        parent: parent
      });
      var another = createWidget({
        parent: parent
      });
      expect(widget.parent).toBe(parent);
      expect(another.parent).toBe(parent);

      var widgetListenerCount = widget.events._eventListeners.length;
      var parentListenerCount = parent.events._eventListeners.length;
      another.setParent(widget);
      expect(parent.events._eventListeners.length).toBe(parentListenerCount - 1);
      expect(widget.events._eventListeners.length).toBe(widgetListenerCount + 1);

      another.setParent(parent);
      expect(parent.events._eventListeners.length).toBe(parentListenerCount);
      expect(widget.events._eventListeners.length).toBe(widgetListenerCount);

      // Ensure parent destroy listener is removed on destroy
      another.destroy();
      expect(parent.events._eventListeners.length).toBe(parentListenerCount - 1);
    });
  });

  describe('setProperty', function() {

    describe('with widget property', function() {
      it('links the widget with the new child widget', function() {
        var widget = createWidget({
          parent: parent
        });
        var another = createWidget({
          parent: parent
        });
        var child = createWidget({
          parent: parent
        });

        widget.setChildWidget(child);
        expect(child.parent).toBe(widget);
        expect(child.owner).toBe(parent);

        another.setChildWidget(child);
        expect(child.parent).toBe(another);
        expect(child.owner).toBe(parent);
      });

      it('links the widget with the new child widgets if it is an array', function() {
        var widget = createWidget({
          parent: parent
        });
        var another = createWidget({
          parent: parent
        });
        var children = [
          createWidget({
            parent: parent
          }),
          createWidget({
            parent: parent
          })
        ];

        widget.setChildWidget(children);
        expect(children[0].parent).toBe(widget);
        expect(children[0].owner).toBe(parent);
        expect(children[1].parent).toBe(widget);
        expect(children[1].owner).toBe(parent);

        another.setChildWidget(children);
        expect(children[0].parent).toBe(another);
        expect(children[0].owner).toBe(parent);
        expect(children[1].parent).toBe(another);
        expect(children[1].owner).toBe(parent);
      });

      it('does not fail if new widget is null', function() {
        var widget = createWidget({
          parent: parent
        });
        var another = createWidget({
          parent: parent
        });
        var child = createWidget({
          parent: parent
        });

        widget.setChildWidget(child);
        widget.setChildWidget(null);
      });
    });

  });

});
