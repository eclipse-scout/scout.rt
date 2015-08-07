scout.localObjects = {

  /**
   * Creates a new object instance based on the given model by using the object-factory.
   * This method should be used when you create Widgets or Adapters in the UI without a
   * model from the server-side client.
   *
   * The only model property required is 'objectType'. A unique ID is generated automatically,
   * when it is not provided by the model.
   */
  createObject: function(session, model) {
    if (typeof model === 'object') {
      if (!model.objectType) {
        throw new Error('missing property objectType');
      }
    } else {
      throw new Error('model must be an object');
    }
    if (model.id === undefined) {
      model.id = scout.createUniqueId();
    }
    return session.objectFactory.create(model, false);
  }

};
