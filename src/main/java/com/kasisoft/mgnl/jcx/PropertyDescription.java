package com.kasisoft.mgnl.jcx;

import javax.jcr.*;

import java.lang.reflect.*;

import java.util.function.*;

import lombok.experimental.*;

import lombok.*;

/**
 * A proprietary description of a property.
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Setter @Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
class PropertyDescription {

  // the class the property belongs to
  Class<?>                      owningType;
  
  // the name of the property
  String                        propertyName;
  
  // the reflection field for the property
  Field                         field;
  
  // the setter method for the property. only has one argument
  Method                        setter;
  
  // the type of the collection/map. maybe null
  Class<?>                      collectionType;
  
  // the type of the field or if the collection type is set the generics type
  Class<?>                      type;
  
  // the function to load the property value (node, property name, target object)
  BiFunction<Node, String, ?>   loader;

} /* ENDCLASS */
