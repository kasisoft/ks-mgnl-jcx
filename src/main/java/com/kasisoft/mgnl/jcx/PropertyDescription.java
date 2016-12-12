package com.kasisoft.mgnl.jcx;

import javax.jcr.*;

import java.lang.reflect.*;

import java.util.function.*;

/**
 * A proprietary description of a property.
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
class PropertyDescription {

  // the class the property belongs to
  private Class<?>                      owningType;
  
  // the name of the property
  private String                        propertyName;
  
  // if set, the element is organized below propertyName
  private String                        subProperty;
  
  // the reflection field for the property
  private Field                         field;
  
  // the setter method for the property. only has one argument
  private Method                        setter;
  
  // the type of the collection/map. maybe null
  private Class<?>                      collectionType;
  
  // the type of the field or if the collection type is set the generics type
  private Class<?>                      type;
  
  // the function to load the property value (node, property name, target object)
  private BiFunction<Node, String, ?>   loader;

  public Class<?> getOwningType() {
    return owningType;
  }

  public void setOwningType( Class<?> newOwningType ) {
    owningType = newOwningType;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName( String newPropertyName ) {
    propertyName = newPropertyName;
  }
  
  public String getSubProperty() {
    return subProperty;
  }
  
  public void setSubProperty( String newSubProperty ) {
    subProperty = newSubProperty;
  }

  public Field getField() {
    return field;
  }

  public void setField( Field newField ) {
    field = newField;
  }

  public Method getSetter() {
    return setter;
  }

  public void setSetter( Method newSetter ) {
    setter = newSetter;
  }

  public Class<?> getCollectionType() {
    return collectionType;
  }

  public void setCollectionType( Class<?> newCollectionType ) {
    collectionType = newCollectionType;
  }

  public Class<?> getType() {
    return type;
  }

  public void setType( Class<?> newType ) {
    type = newType;
  }

  public BiFunction<Node, String, ?> getLoader() {
    return loader;
  }

  public void setLoader( BiFunction<Node, String, ?> newLoader ) {
    loader = newLoader;
  }

} /* ENDCLASS */
