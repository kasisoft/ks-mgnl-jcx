package com.kasisoft.mgnl.jcx.internal;

import com.kasisoft.mgnl.jcx.*;

import javax.jcr.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import java.lang.reflect.*;

import java.util.function.*;

import java.util.*;

import lombok.experimental.*;

import lombok.*;

/**
 * A proprietary description of a property.
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class PropertyDescription {

  private static final Set<String> META_ATTRIBUTES = new HashSet<>( Arrays.asList(
    "@depth", "@uuid", "@identifier", "@name", "@path", "@nodeType"
  ) );

  // the class the property belongs to
  Class<?>                                  owningType;
  
  // the name of the property
  String                                    propertyName;
  
  // if set, the element is organized below propertyName
  String                                    subProperty;
  
  // the reflection field for the property
  Field                                     field;
  
  // the setter method for the property. only has one argument
  Method                                    setter;
  
  // a getter method for this property
  Method                                    getter;
  
  // the type of the collection/map. maybe null
  Class<?>                                  collectionType;
  
  // the type of the field or if the collection type is set the generics type
  Class<?>                                  type;
  
  // the function to load the property value (node, property name, target object)
  BiFunction<Node, String, ?>               loader;

  Class<? extends XmlAdapter>               xmlAdapter;
  
  boolean                                   required = false;
  
  // possibility to use referenced nodes as a source
  JcxReference                              jcxRef = null;
  
  public boolean isAttribute() {
    return (getField().getAnnotation( XmlAttribute.class ) != null) && (!isMetaAttribute());
  }
  
  public boolean isMetaAttribute() {
    return META_ATTRIBUTES.contains( propertyName );
  }

} /* ENDCLASS */
