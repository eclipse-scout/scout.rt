/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {arrays, DisplayParent, Form, InitModelOf, ObjectModel, ObjectWithType, scout, Session, SomeRequired, Widget} from '../index';

/**
 * Controller with functionality to register and render display children.
 */
export class DisplayChildController implements DisplayChildControllerModel, ObjectWithType {
  declare model: DisplayChildControllerModel;
  declare initModel: SomeRequired<this['model'], 'displayParent' | 'session'>;

  objectType: string;
  displayParent: DisplayParent;
  session: Session;

  constructor(model: InitModelOf<DisplayChildController>) {
    this.displayParent = model.displayParent;
    this.session = model.session;
  }

  /**
   * Adds the given display child to this controller and renders it.
   */
  registerAndRender(child: DisplayChild) {
    scout.assertProperty(child, 'displayParent');
    this._render(child, true);
  }

  /**
   * Removes the given display child from this controller and DOM. However, the display child's adapter is not destroyed. That only happens once the display child is closed.
   */
  unregisterAndRemove(child: DisplayChild) {
    if (child) {
      this._unregister(child);
      this._remove(child);
    }
  }

  protected _render(child: DisplayChild, register?: boolean) {
    // missing displayParent (when render is called by reload), use displayParent of DisplayChildController
    if (!child.displayParent) {
      child._setProperty('displayParent', this.displayParent);
    }
    // Prevent "Already rendered" errors (see #162954).
    if (child.rendered) {
      return;
    }
    if (register) {
      this._register(child);
    }

    // Use parent's function or (if not implemented) our own.
    if (this.displayParent.acceptView) {
      if (!this.displayParent.acceptView(child)) {
        return;
      }
    } else if (!this.acceptView(child)) {
      return;
    }

    // Open all display children in the center of the desktop, except the ones that belong to a popup-window
    // Since the display child doesn't have a DOM element as parent when render is called, we must find the
    // entryPoint by using the model.
    let $parent;
    if (this.displayParent instanceof Form && this.displayParent.isPopupWindow()) {
      $parent = this.displayParent.popupWindow.$container;
    } else {
      $parent = this.session.desktop.$container;
    }
    // start focus tracking if not already started.
    child.setTrackFocus(true);
    child.render($parent);

    // Only display the display child if its 'displayParent' is visible to the user.
    if (!this.displayParent.inFront()) {
      child.detach();
    }
  }

  protected _remove(child: DisplayChild) {
    child.remove();
  }

  acceptView(view: DisplayChild): boolean {
    // Only render display child if displayParent is already rendered.
    // If not, the child will be rendered once the displayParent is rendered.
    return this.displayParent.rendered;
  }

  protected _register(child: DisplayChild) {
    // use _registerChild in subclass
  }

  protected _unregister(child: DisplayChild) {
    // use _unregisterChild in subclass
  }

  protected _registerChild(child: DisplayChild, children: DisplayChild[], propertyName: string, position?: number) {
    if (children.includes(child)) {
      return;
    }
    let newChildren;
    if (position !== undefined) {
      newChildren = [...children];
      arrays.insert(newChildren, child, position);
    } else {
      newChildren = [...children, child];
    }
    // Using _setProperty to just set the property and trigger the event without calling _set[propertyName] or any render function
    this.displayParent._setProperty(propertyName, newChildren);
  }

  protected _unregisterChild(child: DisplayChild, children: DisplayChild[], propertyName: string) {
    let newChildren = children.filter(f => f !== child);
    if (arrays.equals(children, newChildren)) {
      return;
    }
    this.displayParent._setProperty(propertyName, newChildren);
  }
}

export interface DisplayChild extends Widget {
  displayParent: DisplayParent;
}

export interface DisplayChildControllerModel extends ObjectModel<DisplayChild> {
  displayParent?: DisplayParent;
  session?: Session;
}
