/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.platform.extension.Extends;
import org.eclipse.scout.rt.platform.extension.InheritOuterExtensionScope;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionRegistry implements IInternalExtensionRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(ExtensionRegistry.class);

  private final Map<ClassIdentifier, List<ExtensionRegistryItem>> m_extensionItemMap;
  private final Map<ClassIdentifier, List<ExtensionRegistryItem>> m_contributionItemMap;
  private final Map<ClassIdentifier, List<ExtensionRegistryMoveItem>> m_modelMoveItemMap;
  private final ThreadLocal<ExtensionStack> m_extensionStack;
  private final ThreadLocal<ScopeStack> m_scopeStack;
  private final AtomicLong m_registrationOrder;
  private final ReadWriteLock m_readWriteLock;

  private ExtensionScope<ExtensionRegistryItem> m_globalExtensionScope;
  private ExtensionScope<ExtensionRegistryItem> m_globalContributionScope;
  private ExtensionScope<ExtensionRegistryMoveItem> m_globalModelMoveItemScope;

  public ExtensionRegistry() {
    m_readWriteLock = new ReentrantReadWriteLock(/*not fair*/);
    m_extensionItemMap = new HashMap<>();
    m_contributionItemMap = new HashMap<>();
    m_modelMoveItemMap = new HashMap<>();
    m_extensionStack = new ThreadLocal<>();
    m_scopeStack = new ThreadLocal<>();
    m_registrationOrder = new AtomicLong();
    m_globalContributionScope = createGlobalScope(m_contributionItemMap, true);
    m_globalExtensionScope = createGlobalScope(m_extensionItemMap, true);
    m_globalModelMoveItemScope = createGlobalScope(m_modelMoveItemMap, false);
  }

  @Override
  public void register(Class<?> extensionClass) {
    register(extensionClass, (ClassIdentifier) null);
  }

  @Override
  public void register(Class<?> extensionClass, Class<?> ownerClass) {
    register(extensionClass, ownerClass, null);
  }

  @Override
  public void register(Class<?> extensionClass, Class<?> ownerClass, Double order) {
    if (extensionClass == null) {
      throw new IllegalExtensionException("Extension class must not be null.");
    }
    register(extensionClass, ownerClass == null ? null : new ClassIdentifier(ownerClass), order);
  }

  @Override
  public void register(Class<?> extensionClass, ClassIdentifier ownerClassIdentifier) {
    register(extensionClass, ownerClassIdentifier, null);
  }

  @Override
  public void register(Class<?> extensionClass, ClassIdentifier ownerClassIdentifier, Double order) {
    if (extensionClass.getEnclosingClass() != null && !Modifier.isStatic(extensionClass.getModifiers())) {
      throw new IllegalExtensionException("Extension class [" + extensionClass.getName() + "] is a non-static inner class.");
    }
    m_readWriteLock.writeLock().lock();
    try {
      autoRegisterInternal(extensionClass, null, ownerClassIdentifier, null, order);
    }
    finally {
      m_readWriteLock.writeLock().unlock();
    }
  }

  protected Extends getExtendsAnnotation(Class<?> c) {
    Extends result = null;
    Class<?> currentClass = c;
    while (result == null && currentClass != null) {
      result = currentClass.getAnnotation(Extends.class);
      currentClass = currentClass.getSuperclass();
    }
    return result;
  }

  protected void autoRegisterInternal(Class<?> extensionClass, Class<?> declaringClass, ClassIdentifier ownerClassIdentifier, ClassIdentifier ownerClassIdentifierFromDeclaring, Double order) {
    if (extensionClass == null) {
      throw new IllegalExtensionException("Extension or contribution cannot be null.");
    }

    boolean isExtension = IExtension.class.isAssignableFrom(extensionClass);
    Class<?> extensionGeneric = null;
    if (isExtension) {
      extensionGeneric = TypeCastUtility.getGenericsParameterClass(extensionClass, IExtension.class);
    }

    if (ownerClassIdentifier == null) {
      // no owner passed: detect!

      if (isExtension) {
        // 1. try type parameter of extension
        InheritOuterExtensionScope inheritOuterExtensionScope = extensionClass.getAnnotation(InheritOuterExtensionScope.class);
        if (inheritOuterExtensionScope == null || inheritOuterExtensionScope.value()) {
          ownerClassIdentifier = new ClassIdentifier(ownerClassIdentifierFromDeclaring, extensionGeneric);
        }
        else {
          ownerClassIdentifier = new ClassIdentifier(extensionGeneric);
        }
      }

      if (ownerClassIdentifier == null) {
        // 2. try @Extends annotation
        Extends extendsAnnotation = getExtendsAnnotation(extensionClass);
        if (extendsAnnotation != null) {
          int pathToContainerLength = extendsAnnotation.pathToContainer().length;
          if (pathToContainerLength > 0) {
            Class[] segments = new Class[pathToContainerLength + 1];
            System.arraycopy(extendsAnnotation.pathToContainer(), 0, segments, 0, pathToContainerLength);
            segments[pathToContainerLength] = extendsAnnotation.value();
            ownerClassIdentifier = new ClassIdentifier(segments);
          }
          else {
            ownerClassIdentifier = new ClassIdentifier(extendsAnnotation.value());
          }
        }

        if (ownerClassIdentifier == null) {
          // 3. inherit owner from declaring type
          ownerClassIdentifier = ownerClassIdentifierFromDeclaring;
        }
      }
    }

    if (Modifier.isStatic(extensionClass.getModifiers())) {
      declaringClass = null; // not required
    }

    registerInternal(ownerClassIdentifier, declaringClass, extensionClass, order, extensionGeneric, isExtension);

    if (isExtension) {
      // only step in if it is an "unmanaged" extension.
      // managed scout objects (e.g. AbstractCompositeField) create their inner classes itself, therefore inner classes must not be registered
      List<Class<?>> innerExtensionClasses = collectInnerExtensionClasses(extensionClass);
      for (Class<?> innerExtensionClass : innerExtensionClasses) {
        autoRegisterInternal(innerExtensionClass, extensionClass, null, ownerClassIdentifier, null);
      }
    }
    else {
      // step into all static inner classes for form field data if the inner class itself has an @Extends annotation: this is a row data extension
      boolean isFormFieldData = AbstractFormFieldData.class.isAssignableFrom(extensionClass);
      if (isFormFieldData) {
        List<Class<?>> innerExtensionClasses = collectInnerExtensionClasses(extensionClass);
        for (Class<?> innerExtensionClass : innerExtensionClasses) {
          if (innerExtensionClass.isAnnotationPresent(Extends.class) && Modifier.isStatic(innerExtensionClass.getModifiers())) {
            autoRegisterInternal(innerExtensionClass, extensionClass, null, ownerClassIdentifier, null);
          }
        }
      }
    }
  }

  /**
   * @param extensionClass
   * @return
   */
  protected List<Class<?>> collectInnerExtensionClasses(Class<?> extensionClass) {
    Class<?>[] innerClasses = extensionClass.getClasses();
    List<Class<?>> result = new ArrayList<>(innerClasses.length);
    for (Class<?> innerClass : innerClasses) {
      if (!Modifier.isAbstract(innerClass.getModifiers())) {
        result.add(innerClass);
      }
    }
    return result;
  }

  protected void validateRegister(ClassIdentifier ownerClassIdentifier, Class<?> extensionClass, Double modelOrder, Class<?> extensionGeneric, boolean isExtension) {
    if (ownerClassIdentifier == null) {
      throw new IllegalExtensionException("No owner information available for [" + extensionClass.getName() + "]. Either add an [@" + Extends.class.getSimpleName()
          + "] annotation, move it into a [" + IExtension.class.getName() + "] class or register explicitly on the [" + IExtensionRegistry.class.getName() + "] service.");
    }
    Class<?> ownerClass = ownerClassIdentifier.getLastSegment();
    // Check if an owner is available
    if (ownerClass == null) {
      throw new IllegalExtensionException("No owner information available for [" + extensionClass.getName() + "]. Either add an [@" + Extends.class.getSimpleName()
          + "] annotation, move it into a [" + IExtension.class.getName() + "] class or register explicitly on the [" + IExtensionRegistry.class.getName() + "] service.");
    }

    if (isExtension) {
      // Check if extension owner is compatible with extension generic
      if (!ownerClass.equals(extensionGeneric)) {
        if (extensionGeneric == null) {
          LOG.warn("Could not parse owner generic of extension [{}].", extensionClass.getName());
        }
        else if (!extensionGeneric.isAssignableFrom(ownerClass)) {
          throw new IllegalExtensionException("Owner [" + ownerClass.getName() + "] is not compatible with the generic [" + extensionGeneric.getName() + "] of extension [" + extensionClass.getName() + "].");
        }
      }
    }
    else {
      boolean isContributionOwner = IContributionOwner.class.isAssignableFrom(ownerClass);
      if (!isContributionOwner) {
        throw new IllegalExtensionException("The owner [" + ownerClass.getName() + "] of contribution [" + extensionClass.getName() + "] does not implement [" + IContributionOwner.class.getName() + "].");
      }

      // contributions: check container
      if (!isValidContribution(extensionClass, ownerClass)) {
        throw new IllegalExtensionException("Contribution [" + extensionClass.getName() + "] is not supported for owner [" + ownerClass.getName() + "].");
      }
    }

    // Warn if there is an order information for a non ordered object.
    boolean isOrdered = IOrdered.class.isAssignableFrom(extensionClass);
    if (!isOrdered && (modelOrder != null || isOrderAnnotationPresentInSuperClasses(extensionClass))) {
      LOG.warn("Order information not valid for extension [{}]. This extension is not an [{}] object.", extensionClass.getName(), IOrdered.class.getName());
    }
  }

  protected boolean isValidContribution(Class<?> contribution, Class<?> container) {
    for (IExtensionRegistrationValidatorService validator : BEANS.all(IExtensionRegistrationValidatorService.class)) {
      if (validator.isValidContribution(contribution, container)) {
        return true;
      }
    }
    return false;
  }

  protected boolean isOrderAnnotationPresentInSuperClasses(Class<?> clazz) {
    Class<?> cls = clazz;
    while (cls != null) {
      if (cls.isAnnotationPresent(Order.class)) {
        return true;
      }
      cls = cls.getSuperclass();
    }
    return false;
  }

  protected void registerInternal(ClassIdentifier ownerClassIdentifier, Class<?> declaringClass, Class<?> extensionClass, Double modelOrder, Class<?> extensionGeneric, boolean isExtension) {
    validateRegister(ownerClassIdentifier, extensionClass, modelOrder, extensionGeneric, isExtension);
    Map<ClassIdentifier, List<ExtensionRegistryItem>> map = getInsertionMap(isExtension);
    List<ExtensionRegistryItem> extensions = map.computeIfAbsent(ownerClassIdentifier, k -> new LinkedList<>());
    ExtensionRegistryItem item = new ExtensionRegistryItem(ownerClassIdentifier, declaringClass, extensionClass, modelOrder, m_registrationOrder.incrementAndGet());
    extensions.add(item);

    // recalculate global scope
    if (isExtension) {
      m_globalExtensionScope = createGlobalScope(map, true);
    }
    else {
      m_globalContributionScope = createGlobalScope(map, true);
    }
  }

  @Override
  public void registerMoveToRoot(Class<? extends IOrdered> modelClass, Double newOrder) {
    if (modelClass == null) {
      throw new IllegalExtensionException("modelClass class must not be null.");
    }
    registerMoveToRoot(new ClassIdentifier(modelClass), newOrder);
  }

  @Override
  public void registerMoveToRoot(ClassIdentifier modelClassIdentifer, Double newOrder) {
    registerMove(modelClassIdentifer, newOrder, IMoveModelObjectToRootMarker.class);
  }

  @Override
  public void registerMove(Class<? extends IOrdered> modelClass, double newOrder) {
    if (modelClass == null) {
      throw new IllegalExtensionException("modelClass class must not be null.");
    }
    registerMove(new ClassIdentifier(modelClass), newOrder);
  }

  @Override
  public void registerMove(ClassIdentifier modelClassIdentifier, double newOrder) {
    registerMove(modelClassIdentifier, newOrder, (ClassIdentifier) null);
  }

  @Override
  public void registerMove(Class<? extends IOrdered> modelClass, Double newOrder, Class<? extends IOrdered> newContainerClass) {
    if (modelClass == null) {
      throw new IllegalExtensionException("modelClass class must not be null.");
    }
    registerMove(new ClassIdentifier(modelClass), newOrder, newContainerClass);
  }

  @Override
  public void registerMove(ClassIdentifier modelClassIdentifer, Double newOrder, Class<? extends IOrdered> newContainerClass) {
    registerMove(modelClassIdentifer, newOrder, newContainerClass == null ? null : new ClassIdentifier(newContainerClass));
  }

  @Override
  public void registerMove(ClassIdentifier modelClassIdentifer, Double newOrder, ClassIdentifier newContainerClassIdentifier) {
    validateRegisterMove(modelClassIdentifer, newOrder, newContainerClassIdentifier);

    m_readWriteLock.writeLock().lock();
    try {
      Map<ClassIdentifier, List<ExtensionRegistryMoveItem>> modelMoveItemMap = getModelMoveItemMap();
      List<ExtensionRegistryMoveItem> moveItems = modelMoveItemMap.computeIfAbsent(modelClassIdentifer, k -> new LinkedList<>());
      ExtensionRegistryMoveItem item = new ExtensionRegistryMoveItem(modelClassIdentifer, newContainerClassIdentifier, newOrder, m_registrationOrder.incrementAndGet());
      moveItems.add(item);
      m_globalModelMoveItemScope = createGlobalScope(getModelMoveItemMap(), false);
    }
    finally {
      m_readWriteLock.writeLock().unlock();
    }
  }

  protected void validateRegisterMove(ClassIdentifier modelClassIdentifier, Double newOrder, ClassIdentifier newContainerClassIdentifier) {
    if (modelClassIdentifier == null) {
      throw new IllegalExtensionException("modelClassIdentifier class must not be null.");
    }
    Class<?> lastSegment = modelClassIdentifier.getLastSegment();
    if (!IOrdered.class.isAssignableFrom(lastSegment)) {
      throw new IllegalExtensionException("modelClassIdentifier's last segment is not an " + IOrdered.class.getSimpleName() + ".");
    }
    @SuppressWarnings("unchecked")
    Class<? extends IOrdered> modelClass = (Class<? extends IOrdered>) lastSegment;
    if (modelClass == null) {
      throw new IllegalExtensionException("modelClass class must not be null.");
    }
    if (newOrder == null && newContainerClassIdentifier == null) {
      throw new IllegalExtensionException("At least a new order or a new container must be specified.");
    }

    if (newContainerClassIdentifier != null) {
      Class<?> newContainerClass = newContainerClassIdentifier.getLastSegment();
      if (modelClass == newContainerClass) {
        throw new IllegalExtensionException("model class cannot be moved into itself.");
      }
      if (!IOrdered.class.isAssignableFrom(newContainerClass)) {
        throw new IllegalExtensionException("new container class must be instanceof " + IOrdered.class.getSimpleName() + ".");
      }
      @SuppressWarnings("unchecked")
      Class<? extends IOrdered> newOrderedContainerClass = (Class<? extends IOrdered>) newContainerClass;
      if (!isValidMove(modelClass, newOrderedContainerClass)) {
        String newOrderStr = null;
        if (newOrder == null) {
          newOrderStr = "null";
        }
        else {
          newOrderStr = newOrder.toString();
        }
        throw new IllegalExtensionException("Move of element [" + modelClass.getName() + "] is not supported for order [" + newOrderStr + "] and container [" + newContainerClass.getName() + "].");
      }
    }
  }

  protected boolean isValidMove(Class<? extends IOrdered> modelClass, Class<? extends IOrdered> newContainerClass) {
    for (IExtensionRegistrationValidatorService validator : BEANS.all(IExtensionRegistrationValidatorService.class)) {
      if (validator.isValidMove(modelClass, newContainerClass)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean deregisterMove(Class<? extends IOrdered> modelClass, double newOrder) {
    return deregisterMove(modelClass, newOrder, null);
  }

  @Override
  public boolean deregisterMove(Class<? extends IOrdered> modelClass, Double newOrder, Class<? extends IOrdered> newContainerClass) {
    if (modelClass == null) {
      throw new IllegalExtensionException("modelClass class must not be null.");
    }
    return deregisterMove(new ClassIdentifier(modelClass), newOrder, newContainerClass);
  }

  @Override
  public boolean deregisterMove(ClassIdentifier modelClassIdentifier, Double newOrder, Class<? extends IOrdered> newContainerClass) {
    return deregisterMove(modelClassIdentifier, newOrder, newContainerClass == null ? null : new ClassIdentifier(newContainerClass));
  }

  @Override
  public boolean deregisterMove(ClassIdentifier modelClassIdentifier, Double newOrder, ClassIdentifier newContainerClassIdentifier) {
    if (newOrder == null && newContainerClassIdentifier == null) {
      throw new IllegalExtensionException("At least a new order or a new container must be specified.");
    }

    m_readWriteLock.writeLock().lock();
    try {
      boolean changed = false;
      Map<ClassIdentifier, List<ExtensionRegistryMoveItem>> modelMoveItemMap = getModelMoveItemMap();
      List<ExtensionRegistryMoveItem> moveItems = modelMoveItemMap.get(modelClassIdentifier);
      if (CollectionUtility.hasElements(moveItems)) {
        Iterator<ExtensionRegistryMoveItem> iterator = moveItems.iterator();
        while (iterator.hasNext()) {
          ExtensionRegistryMoveItem moveItem = iterator.next();
          if (ObjectUtility.equals(moveItem.getNewModelOrder(), newOrder) && ObjectUtility.equals(moveItem.getNewModelContainerClassIdentifier(), newContainerClassIdentifier)) {
            iterator.remove();
            changed = true;
          }
        }

        if (moveItems.isEmpty()) {
          modelMoveItemMap.remove(modelClassIdentifier);
          changed = true;
        }
      }

      if (changed) {
        m_globalModelMoveItemScope = createGlobalScope(getModelMoveItemMap(), false);
      }

      return changed;
    }
    finally {
      m_readWriteLock.writeLock().unlock();
    }
  }

  protected Map<ClassIdentifier, List<ExtensionRegistryItem>> getInsertionMap(boolean isExtension) {
    if (isExtension) {
      return getExtensionMap();
    }
    return getContributionMap();
  }

  protected Map<ClassIdentifier, List<ExtensionRegistryItem>> getExtensionMap() {
    return m_extensionItemMap;
  }

  protected Map<ClassIdentifier, List<ExtensionRegistryItem>> getContributionMap() {
    return m_contributionItemMap;
  }

  protected Map<ClassIdentifier, List<ExtensionRegistryMoveItem>> getModelMoveItemMap() {
    return m_modelMoveItemMap;
  }

  /**
   * Returns top-level extensions for the given ownerClass, but not their nested extension classes.
   *
   * @param ownerClass
   * @return
   */
  protected Set<ExtensionRegistryItem> getModelExtensionItemsFor(Class<?> ownerClass) {
    ScopeStack scopeStack = m_scopeStack.get();
    ExtensionScope<ExtensionRegistryItem> extensionScope = null;
    if (scopeStack != null) {
      extensionScope = scopeStack.getExtensionScope();
    }
    if (extensionScope == null) {
      extensionScope = m_globalExtensionScope;
    }
    return extensionScope.getRegistryItems(ownerClass);
  }

  protected Set<ExtensionRegistryItem> getModelContributionItemsFor(Class<?> ownerClass) {
    ScopeStack scopeStack = m_scopeStack.get();
    ExtensionScope<ExtensionRegistryItem> contributionScope = null;
    if (scopeStack != null) {
      contributionScope = scopeStack.getContributionScope();
    }
    if (contributionScope == null) {
      contributionScope = m_globalContributionScope;
    }
    return contributionScope.getRegistryItems(ownerClass);
  }

  protected Set<ExtensionRegistryMoveItem> getModelMoveItemsFor(Class<?> modelClass, Iterator<?> parentModelObjectIterator) {
    if (m_globalModelMoveItemScope == null) {
      return null;
    }
    Set<ScopeItem> scopeItems = m_globalModelMoveItemScope.filterScopeItems(modelClass, parentModelObjectIterator);
    return m_globalModelMoveItemScope.resolveRegistryItems(scopeItems);
  }

  @Override
  public boolean deregister(Class<?> extensionOrContributionClass) {
    if (extensionOrContributionClass == null) {
      throw new IllegalExtensionException("extensionOrContributionClass cannot be null.");
    }

    m_readWriteLock.writeLock().lock();
    try {
      boolean changed = false;
      boolean isExtension = IExtension.class.isAssignableFrom(extensionOrContributionClass);
      Map<ClassIdentifier, List<ExtensionRegistryItem>> mapToRemoveFrom = getInsertionMap(isExtension);
      Iterator<List<ExtensionRegistryItem>> iterator = mapToRemoveFrom.values().iterator();
      while (iterator.hasNext()) {
        List<ExtensionRegistryItem> items = iterator.next();
        Iterator<ExtensionRegistryItem> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
          ExtensionRegistryItem item = itemIterator.next();
          Class<?> extensionClass = item.getExtensionClass();
          if (extensionOrContributionClass.equals(extensionClass)) {
            itemIterator.remove();
            changed = true;
          }
        }

        if (items.isEmpty()) {
          iterator.remove();
          changed = true;
        }
      }

      if (isExtension) {
        // only step in if it is an "unmanaged" extension.
        List<Class<?>> innerExtensionClasses = collectInnerExtensionClasses(extensionOrContributionClass);
        for (Class<?> innerExtensionClass : innerExtensionClasses) {
          boolean innerChanged = deregister(innerExtensionClass);
          if (innerChanged) {
            changed = true;
          }
        }
      }

      // clear scopes if needed
      if (changed) {
        if (isExtension) {
          m_globalExtensionScope = createGlobalScope(getExtensionMap(), true);
        }
        else {
          m_globalContributionScope = createGlobalScope(getContributionMap(), true);
        }
      }

      return changed;
    }
    finally {
      m_readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public <T extends IExtension<?>> List<T> createExtensionsFor(Object owner) {
    if (owner == null) {
      throw new IllegalArgumentException("owner must not be null.");
    }

    Set<ExtensionRegistryItem> extensionItems = null;
    m_readWriteLock.readLock().lock();
    try {
      extensionItems = getModelExtensionItemsFor(owner.getClass());
    }
    finally {
      m_readWriteLock.readLock().unlock();
    }

    LinkedList<T> extensions = new LinkedList<>();
    if (CollectionUtility.isEmpty(extensionItems)) {
      return extensions;
    }

    ExtensionStack extensionStack = m_extensionStack.get();
    for (ExtensionRegistryItem extensionItem : extensionItems) {
      T ext = extensionItem.createInstance(owner, extensionStack);
      extensions.addFirst(ext);
    }
    return extensions;
  }

  @Override
  public <T> List<T> createContributionsFor(Object container, Class<T> filterType) {
    if (container == null) {
      throw new IllegalArgumentException("container must not be null.");
    }

    Set<ExtensionRegistryItem> contributionItems = null;

    m_readWriteLock.readLock().lock();
    try {
      contributionItems = getModelContributionItemsFor(container.getClass());
    }
    finally {
      m_readWriteLock.readLock().unlock();
    }

    LinkedList<T> contributions = new LinkedList<>();
    if (CollectionUtility.isEmpty(contributionItems)) {
      return contributions;
    }

    ExtensionStack extensionStack = m_extensionStack.get();
    for (ExtensionRegistryItem item : contributionItems) {
      if (filterType == null || filterType.isAssignableFrom(item.getExtensionClass())) {
        contributions.addFirst(item.createInstance(container, extensionStack));
      }
    }
    return contributions;
  }

  @Override
  public Set<Class<?>> getContributionsFor(Class<?> container) {
    if (container == null) {
      throw new IllegalArgumentException("container must not be null.");
    }
    Set<ExtensionRegistryItem> contributionItems = getModelContributionItemsFor(container);
    Set<Class<?>> result = new LinkedHashSet<>(contributionItems.size());
    for (ExtensionRegistryItem item : contributionItems) {
      result.add(item.getExtensionClass());
    }
    return result;
  }

  @Override
  public <T> MoveDescriptor<T> createModelMoveDescriptorFor(T modelObject, Iterator<?> parentModelObjectIterator) {
    if (modelObject == null) {
      throw new IllegalExtensionException("modelObject must not be null.");
    }

    Set<ExtensionRegistryMoveItem> moveItems = null;
    m_readWriteLock.readLock().lock();
    try {
      moveItems = getModelMoveItemsFor(modelObject.getClass(), parentModelObjectIterator);
    }
    finally {
      m_readWriteLock.readLock().unlock();
    }

    if (CollectionUtility.isEmpty(moveItems)) {
      return null;
    }

    // collapse multiple move items into one move descriptor
    ClassIdentifier newContainer = null;
    Double newOrder = null;
    for (ExtensionRegistryMoveItem item : moveItems) {
      if (item.getNewModelContainerClassIdentifier() != null) {
        newContainer = item.getNewModelContainerClassIdentifier();
      }
      if (item.getNewModelOrder() != null) {
        newOrder = item.getNewModelOrder();
      }
    }
    return new MoveDescriptor<>(modelObject, newContainer, newOrder);
  }

  protected <T extends AbstractExtensionRegistryItem> ExtensionScope<T> createGlobalScope(Map<ClassIdentifier, List<T>> registryItems, boolean topDownStrategy) {
    Map<ClassIdentifier, List<T>> clonedRegistryItems = new HashMap<>(registryItems.size());
    for (Entry<ClassIdentifier, List<T>> entry : registryItems.entrySet()) {
      clonedRegistryItems.put(entry.getKey(), new LinkedList<>(entry.getValue()));
    }
    return new ExtensionScope<>(clonedRegistryItems, topDownStrategy);
  }

  @Override
  public void pushExtensions(List<? extends IExtension<?>> extensions) {
    ExtensionStack extensionStack = m_extensionStack.get();
    if (extensionStack == null) {
      extensionStack = new ExtensionStack();
      m_extensionStack.set(extensionStack);
    }
    extensionStack.pushExtensions(extensions);
  }

  @Override
  public void popExtensions(List<? extends IExtension<?>> extensions) {
    ExtensionStack extensionStack = m_extensionStack.get();
    if (extensionStack == null) {
      throw new IllegalStateException("popExtensions from empty stack");
    }
    extensionStack.popExtensions(extensions);
    if (extensionStack.isEmpty()) {
      m_extensionStack.remove();
    }
  }

  @Override
  public void pushScope(Class<?> scopeClass) {
    ScopeStack scopeStack = m_scopeStack.get();
    if (scopeStack == null) {
      scopeStack = new ScopeStack(m_globalContributionScope, m_globalExtensionScope);
      m_scopeStack.set(scopeStack);
    }
    scopeStack.pushScope(scopeClass);
  }

  @Override
  public void popScope() {
    ScopeStack scopeStack = m_scopeStack.get();
    if (scopeStack == null) {
      throw new IllegalStateException("popScope from empty stack");
    }
    scopeStack.popScope();
    if (scopeStack.isEmpty()) {
      m_scopeStack.remove();
    }
  }

  @Override
  public ExtensionContext backupExtensionContext() {
    final ScopeStack scopeStack = Assertions.assertNotNull(m_scopeStack.get());
    final ExtensionStack extensionStack = Assertions.assertNotNull(m_extensionStack.get());
    return new ExtensionContext(scopeStack, extensionStack);
  }

  @Override
  public void runInContext(ExtensionContext ctx, Runnable runnable) {
    Assertions.assertNotNull(runnable, "Runnable must not be null");
    // backup current extension context
    final ScopeStack scopeStack = m_scopeStack.get();
    final ExtensionStack extensionStack = m_extensionStack.get();
    try {
      if (ctx == null) {
        m_scopeStack.remove();
        m_extensionStack.remove();
      }
      else {
        m_scopeStack.set(ctx.getScopeStack());
        m_extensionStack.set(ctx.getExtensionStack());
      }
      runnable.run();
    }
    finally {
      // restore extension context
      restoreStack(m_scopeStack, scopeStack);
      restoreStack(m_extensionStack, extensionStack);
    }
  }

  private <STACK> void restoreStack(ThreadLocal<STACK> threadLocal, STACK value) {
    if (value == null) {
      threadLocal.remove();
    }
    else {
      threadLocal.set(value);
    }
  }
}
