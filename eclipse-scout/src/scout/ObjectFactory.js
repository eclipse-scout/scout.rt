import Scout from './Scout';

export default class ObjectFactory {

  constructor() {
    this.uniqueIdSeqNo = 0;
    this._registry = {};
  }

  /**
   * Creates an object from the given objectType. Only the constructor is called.
   *
   * OBJECT TYPE:
   *
   * An object type may consist of three parts: [name.space.]Class[:Variant]
   * 1. Name spaces (optional)
   *    All name space parts have to end with a dot ('.') character. If this part is omitted, the default
   *    name space 'scout.' is assumed.
   *    Examples: 'scout.', 'my.custom.namespace.'
   * 2. Scout class name (mandatory)
   *    Examples: 'Desktop', 'Session', 'StringField'
   * 3. Model variant (optional)
   *    Custom variants of a class can be created by adding the custom class prefix after
   *    the Scout class name and a colon character (':'). This prefix is then combined with
   *    the class name.
   *    Examples: ':Offline', ':Horizontal'
   *
   * Full examples:
   *   Object type: Outline                                      -> Constructor: scout.Outline
   *   Object type: myNamespace.Outline                          -> Constructor: myNamespace.Outline
   *   Object type: Outline:MyVariant                            -> Constructor: scout.MyVariantOutline
   *   Object type: myNamespace.Outline:MyVariant                -> Constructor: myNamespace.MyVariantOutline
   *   Object type: Outline:myNamespace.MyVariant                -> Constructor: myNamespace.MyVariantOutline
   *   Object type: myNamespace.Outline:yourNamespace.MyVariant  -> Constructor: yourNamespace.MyVariantOutline
   *
   * RESOLVING THE CONSTRUCTOR:
   *
   * When scout.objectFactories contains a create function for the given objectType, this function is called.
   *
   * Otherwise it tries to find the constructor function by the following logic:
   * If the objectType provides a name space, it is used. Otherwise it takes the default 'scout' name space.
   * If the object type provides a variant ('Type:Variant'), the final object type is built by prepending
   * the variant to the type ('VariantType'). If no such type can be found and the option 'variantLenient'
   * is set to true, a second attempt is made without the variant.
   *
   * @param objectType (mandatory) String describing the type of the object to be created.
   * @param options    (optional)  Options object, currently supporting the following two options:
   *                               - model = Model object to be passed to the constructor or create function
   *                               - variantLenient = Flag to allow a second attempt to resolve the class
   *                                 without variant (see description above).
   */
  _createObjectByType(objectType, options) {
    if (!Scout.isFunction(objectType)) {
      throw new Error('missing or invalid object type');
    }
    options = options || {};

    // FIXME [awe] ES6: The 'name' property is not standard and does not exist in Internet Explorer, thus we cannot use it!
    // additionally TerserPlugin changes class-names in minify-process, thus we cannot rely on name-magic when we instantiate
    // a class by string (which happens in Scout classic case). Proposal: assign a static property 'name' to every Scout class
    // and use this string for lookups when we come from Scout.create(string). The create function should also accept a Function
    // argument, so we can pass a function-reference in Scout JS applications (which is better for IDE support).
    var createFunc = this._registry[objectType.name];
    if (createFunc) {
      // 1. - Use factory function registered for the given objectType
      var scoutObject = createFunc(options.model);
      if (!scoutObject) {
        throw new Error('Failed to create object for objectType "' + objectType + '": Factory function did not return a valid object');
      }
      return scoutObject;
    } else {
      // 2. - Resolve class by name
      return new objectType(options.model);
      //return TypeDescriptor.newInstance(objectType, options);
    }
  };

  /**
   * Creates and initializes a new Scout object. When the created object has an init function, the
   * model object is passed to that function. Otherwise the init call is omitted.
   *
   * @param objectType A string with the requested objectType. This argument is optional, but if it
   *                   is omitted, the argument 'model' becomes mandatory and MUST contain a
   *                   property named 'objectType'. If both, objectType and model, are set, the
   *                   objectType parameter always wins before the model.objectType property.
   * @param model      The model object passed to the constructor function and to the init() method.
   *                   This argument is mandatory if it is the first argument, otherwise it is
   *                   optional (see above). This function may set/overwrite the properties 'id' and
   *                   'objectType' on the model object.
   * @param options    Options object, see table below. This argument is optional.
   *
   * An error is thrown if the argument list does not match this definition.
   *
   * List of options:
   *
   * OPTION                   DEFAULT VALUE   DESCRIPTION
   * ------------------------------------------------------------------------------------------------------
   * variantLenient           false           Controls if the object factory may try to resolve the
   *                                          scoutClass without the model variant part if the initial
   *                                          objectType could not be resolved.
   *
   * ensureUniqueId           true            Controls if the resulting object should be assigned the
   *                                          attribute 'id' if it is not defined. If the created object has an
   *                                          init() function, we also set the property 'id' on the model object
   *                                          to allow the init() function to copy the attribute from the model
   *                                          to the scoutObject.
   */
  create(objectType, model, options) {
    // Normalize arguments
    if (typeof objectType === 'function') {
      options = options || {};
    } else if (Scout.isPlainObject(objectType)) {
      options = model || {};
      model = objectType;
      if (!model.objectType) {
        throw new Error('Missing mandatory property "objectType" on model');
      }
      objectType = model.objectType;
    } else {
      throw new Error('Invalid arguments');
    }
    options.model = model;

    // Create object
    var scoutObject = this._createObjectByType(objectType, options);
    if (Scout.isFunction(scoutObject.init)) {
      if (model) {
        if (model.id === undefined && Scout.nvl(options.ensureUniqueId, true)) {
          model.id = this.createUniqueId();
        }
        model.objectType = objectType;
      }
      // Initialize object
      scoutObject.init(model);
    }

    if (scoutObject.id === undefined && Scout.nvl(options.ensureUniqueId, true)) {
      scoutObject.id = this.createUniqueId();
    }
    if (scoutObject.objectType === undefined) {
      scoutObject.objectType = objectType;
    }

    return scoutObject;
  };

  /**
   * Returns a new unique ID to be used for Widgets/Adapters created by the UI
   * without a model delivered by the server-side client.
   * @return string ID with prefix 'ui'
   */
  createUniqueId() {
    return 'ui' + (++this.uniqueIdSeqNo).toString();
  };

  register(objectType, createFunc) {
    this._registry[objectType] = createFunc;
  };

  unregister(objectType) {
    delete this._registry[objectType];
  };

  get(objectType) {
    return this._registry[objectType];
  };

  /**
   * Cannot init ObjectFactory until Log4Javascript is initialized.
   * That's why we call this method in the scout._init method.
   */
  init() {
    for (var objectType in Scout.objectFactories) {
      if (Scout.objectFactories.hasOwnProperty(objectType)) {
        this.register(objectType, Scout.objectFactories[objectType]);
      }
    }
  };

  static getInstance() {
    return objectFactory;
  }
}

const objectFactory = new ObjectFactory();
