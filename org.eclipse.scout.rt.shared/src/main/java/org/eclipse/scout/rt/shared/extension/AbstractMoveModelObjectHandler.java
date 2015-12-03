/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

/**
 * The top level list is expected to be sorted by the caller.
 */
public abstract class AbstractMoveModelObjectHandler<ORDERED extends IOrdered> {

  private final OrderedCollection<ORDERED> m_rootModelObjects;
  private final String m_modelObjectTypeName;
  private final IInternalExtensionRegistry m_extensionRegistry;

  public AbstractMoveModelObjectHandler(String modelObjectTypeName, OrderedCollection<ORDERED> rootModelObjects) {
    m_rootModelObjects = rootModelObjects;
    m_modelObjectTypeName = modelObjectTypeName;
    m_extensionRegistry = BEANS.get(IInternalExtensionRegistry.class);
  }

  protected abstract ORDERED getParent(ORDERED child);

  protected abstract void removeChild(ORDERED parent, ORDERED child);

  protected abstract void addChild(ORDERED parent, ORDERED child);

  protected abstract void sortChildren(ORDERED parent);

  protected abstract List<ORDERED> collectAllModelObjects();

  protected OrderedCollection<? extends ORDERED> getRootModelObjects() {
    return m_rootModelObjects;
  }

  public void moveModelObjects() {
    // collect all model objects and move descriptors
    List<ORDERED> allModelObject = collectAllModelObjects();
    Set<MoveDescriptor<ORDERED>> moveDescriptors = collectMoveDescriptors(allModelObject);

    if (CollectionUtility.isEmpty(moveDescriptors)) {
      return;
    }

    StringBuilder sb = new StringBuilder();
    for (MoveDescriptor<ORDERED> moveDescriptor : moveDescriptors) {
      ORDERED modelObject = moveDescriptor.getModel();
      ORDERED oldParent = getParent(modelObject);

      Class<?> newContainer = null;
      ClassIdentifier newContainerIdentifer = moveDescriptor.getNewContainerIdentifer();
      if (newContainerIdentifer != null) {
        if (newContainerIdentifer.size() > 1) {
          throw new IllegalExtensionException("multi-segment move target class identifier are not supported by this handler. " + moveDescriptor);
        }
        newContainer = newContainerIdentifer.getLastSegment();
      }

      if (oldParent == null && (newContainer == null || newContainer == IMoveModelObjectToRootMarker.class)) {
        // 1. model object is in root list and should stay there -> just update order without sorting the list. The method expects, that the top level
        //    list is sorted by the caller.
        applyOrder(moveDescriptor, modelObject);
      }

      else if (oldParent != null && newContainer == IMoveModelObjectToRootMarker.class) {
        // 2. model object is not in root list, but should be moved into it
        removeChild(oldParent, modelObject);
        applyOrder(moveDescriptor, modelObject);
        m_rootModelObjects.addOrdered(modelObject);
      }

      else if (newContainer == null) {
        // 3. model object remains in its current container -> just update order and sort items
        applyOrder(moveDescriptor, modelObject);
        sortChildren(oldParent);
      }

      else {
        // 4. model object is moved into another container

        // find new parent
        ORDERED newParent = null;
        for (ORDERED a : allModelObject) {
          if (newContainer.isInstance(a) && a != modelObject) {
            newParent = a;
            break;
          }
        }

        if (newParent == null) {
          if (sb.length() == 0) {
            sb.append("Invalid ").append(m_modelObjectTypeName).append(" move commands:");
          }
          sb.append("  \n").append(m_modelObjectTypeName).append(" '").append(modelObject).append("' cannot be moved into container '").append(newParent).append("'");
          continue;
        }

        // remove from old parent
        if (oldParent == null) {
          // remove model object from root list
          m_rootModelObjects.remove(modelObject);
        }
        else {
          // remove model object from old parent
          removeChild(oldParent, modelObject);
        }

        // apply order and add to new parent
        applyOrder(moveDescriptor, modelObject);
        addChild(newParent, modelObject);
      }
    }

    if (sb.length() > 0) {
      throw new IllegalArgumentException(sb.toString());
    }
  }

  protected void applyOrder(MoveDescriptor<ORDERED> moveItem, ORDERED ordered) {
    Double newOrder = moveItem.getNewOrder();
    if (newOrder != null) {
      ordered.setOrder(newOrder);
    }
  }

  protected Set<MoveDescriptor<ORDERED>> collectMoveDescriptors(List<ORDERED> orderedObjects) {
    if (CollectionUtility.isEmpty(orderedObjects)) {
      return null;
    }
    Set<MoveDescriptor<ORDERED>> moveDescriptors = new HashSet<MoveDescriptor<ORDERED>>();
    for (ORDERED o : orderedObjects) {
      MoveDescriptor<ORDERED> moveDesc = m_extensionRegistry.createModelMoveDescriptorFor(o, null);
      if (moveDesc != null) {
        moveDescriptors.add(moveDesc);
      }
    }
    return moveDescriptors;
  }
}
