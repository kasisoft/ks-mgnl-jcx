package com.kasisoft.mgnl.jcx;

import static com.kasisoft.mgnl.jcx.internal.Messages.*;

import com.kasisoft.libs.common.text.*;

import com.kasisoft.libs.common.annotation.*;
import com.kasisoft.libs.common.function.*;
import com.kasisoft.mgnl.jcx.internal.*;
import com.kasisoft.mgnl.util.*;

import javax.annotation.*;
import javax.inject.*;
import javax.jcr.*;
import javax.jcr.Node;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import java.lang.reflect.*;

import java.util.function.*;

import java.util.stream.*;

import java.util.*;

import lombok.extern.slf4j.*;

import lombok.experimental.*;

import lombok.*;

import info.magnolia.objectfactory.*;
import info.magnolia.objectfactory.guice.*;

/**
 * This helper allows to mark attributes using jaxb annotations in order to fill them automatically. 
 * There are three types of property values that can be loaded:
 * 
 *  <ol>
 *    <li>ordinary attributes such as strings, integers, booleans etc. these are loaded through the attribute loaders.</li>
 *    <li>XmlAdapter annotated values. the value will be loaded as a string and then converted using the XmlAdapter.</li>
 *    <li>complex types which will be created with the help of {@link TypeUnmarshaller} which is responsible to
 *    process the complete structure.</li>
 *  </ol>
 * 
 * NOTE: be aware that not all java xml annotations are supported as this is more or less an ad-hoc development.
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@Singleton
public class JcxUnmarshaller {

  public  static final String NAME_DIRECT   = "##direct";
  private static final String NAME_DEFAULT  = "##default";
  
  private static final Consumer<Object> DO_NOTHING = $ -> {};
  
  Function<String, String>                                        fieldNameGenerator;
  Map<Class<?>, TypeUnmarshaller>                                 unmarshallers;
  Map<Class<?>, BiFunction<Node, String, ?>>                      attributeLoaders;
  Map<Class<? extends XmlAdapter>, BiFunction<Node, String, ?>>   xmlAdapterLoaders;

  public JcxUnmarshaller() {
    fieldNameGenerator  = Function.identity();
    unmarshallers       = new HashMap<>();
    xmlAdapterLoaders   = new HashMap<>();
    attributeLoaders    = setupAttributeLoaders();
  }
  
  protected <R> XmlAdapter<String, R> newXmlAdapter( Class<? extends XmlAdapter> xmlAdapterType ) {
    XmlAdapter<String, R> result = null;
    try {
      result = Components.getComponent( xmlAdapterType );
    } catch( Exception ex ) {
      result = Components.newInstance( xmlAdapterType );
    }
    return result;
  }
  
  protected <R> BiFunction<Node, String, R> getXmlAdapterLoader( @Nonnull Class<? extends XmlAdapter> xmlAdapter ) {
    BiFunction<Node, String, R> result = (BiFunction<Node, String, R>) xmlAdapterLoaders.get( xmlAdapter );
    if( result == null ) {
      XmlAdapter<String, R> xml = newXmlAdapter( xmlAdapter );
      result = ($1, $2) -> exceptionWrapper( xml, $1, $2 );
      xmlAdapterLoaders.put( xmlAdapter, result );
    }
    return result;
  }
  
  private <R> R exceptionWrapper( XmlAdapter<String, R> adapter, Node node, String property ) {
    try {
      BiFunction<Node, String, String> strLoader = getAttributeLoader( String.class );
      return adapter.unmarshal( strLoader.apply( node, property ) );
    } catch( Exception ex ) {
      throw JcxException.wrap( ex );
    }
  }
  
  protected synchronized <R> BiFunction<Node, String, R> getAttributeLoader( @Nonnull Class<R> type ) {
    return (BiFunction<Node, String, R>) attributeLoaders.get( type );
  }
  
  public synchronized void addAttributeLoader( @Nonnull Class<?> type, @Nonnull BiFunction<Node, String, ?> propertyAccessor ) {
    attributeLoaders.put( type, propertyAccessor );
  }

  public synchronized void removeAttributeLoader( @Nonnull Class<?> type ) {
    attributeLoaders.remove( type );
  }

  private Map<Class<?>, BiFunction<Node, String, ?>> setupAttributeLoaders() {
    
    Map<Class<?>, BiFunction<Node, String, ?>> result = new HashMap<>();
    
    result.put( Boolean   . TYPE , PropertyLoaders::toBoolean     );
    result.put( Character . TYPE , PropertyLoaders::toCharacter   );
    result.put( Byte      . TYPE , PropertyLoaders::toByte        );
    result.put( Short     . TYPE , PropertyLoaders::toShort       );
    result.put( Integer   . TYPE , PropertyLoaders::toInteger     );
    result.put( Long      . TYPE , PropertyLoaders::toLong        );
    result.put( Float     . TYPE , PropertyLoaders::toFloat       );
    result.put( Double    . TYPE , PropertyLoaders::toDouble      );

    result.put( Boolean   . class, PropertyLoaders::toBoolean     );
    result.put( Character . class, PropertyLoaders::toCharacter   );
    result.put( Byte      . class, PropertyLoaders::toByte        );
    result.put( Short     . class, PropertyLoaders::toShort       );
    result.put( Integer   . class, PropertyLoaders::toInteger     );
    result.put( Long      . class, PropertyLoaders::toLong        );
    result.put( Float     . class, PropertyLoaders::toFloat       );
    result.put( Double    . class, PropertyLoaders::toDouble      );
    result.put( String    . class, PropertyLoaders::toString      );
    
    return result;
    
  }
  
  /**
   * Configures a name generator. The default uses the identity which maps the field name to the property name.
   * However if you're using any kind of coding convention (f.e. prefixing an underscore) this function be
   * overriden to deliver the right name. 
   * 
   * @param nameGen   The new name generation function to be used.
   *                  <code>null</code> <=> The identify function is being used.
   */
  public synchronized void setFieldNameGenerator( @Nullable Function<String, String> nameGen ) {
    fieldNameGenerator = nameGen != null ? nameGen : Function.identity();
  }

  /**
   * Returns a function that loads a nodes data into a certain type (only the properties annotation with 
   * jaxb annotations).
   * 
   * @param type   The type of data that is supposed to be loaded.
   * 
   * @return   The loading function. Not <code>null</code>.
   */
  public synchronized <T> BiFunction<Node, T, T> createLoader( @Nonnull Class<T> type ) {
    return getUnmarshaller( type )::apply;
  }

  /**
   * Like {@link #createLoader(Class)} with the difference that the function allows to supply the name of 
   * subnode that provides the data.
   * 
   * @param type   The type of data that is supposed to be loaded.
   * 
   * @return   The loading function. Not <code>null</code>.
   */
  public synchronized <T> TriFunction<Node, String, T, T> createSubnodeLoader( @Nonnull Class<T> type ) {
    return getUnmarshaller( type )::applySubnode;
  }

  /**
   * Like {@link #createLoader(Class)} with the difference that the resulting object will be created, too.
   * 
   * @param type   The type of data that is supposed to be created.
   * 
   * @return   The creation function. Not <code>null</code>.
   */
  public synchronized <T> Function<Node, T> createCreator( @Nonnull Class<T> type ) {
    return getUnmarshaller( type )::create;
  }

  /**
   * Like {@link #create(Class)} with the difference that the resulting object will be created, too.
   * 
   * @param type   The type of data that is supposed to be created.
   * 
   * @return   The creation function. Not <code>null</code>.
   */
  public synchronized <T> BiFunction<Node, String, T> createSubnodeCreator( @Nonnull Class<T> type ) {
    return getUnmarshaller( type )::createSubnode;
  }

  private Object create( Node dataNode, Class<?> clazz ) {
    return createCreator( clazz ).apply( dataNode ); 
  }

  private <T> TypeUnmarshaller<T> getUnmarshaller( Class<T> type ) {
    TypeUnmarshaller<T> result = unmarshallers.get( type );
    if( result == null ) {
      result = buildUnmarshaller( type );
      unmarshallers.put( type, result );
    }
    return result;
  }

  /**
   * The main entry point to create a type specific unmarshaller.
   * 
   * @param type   The type that will be processed depending on the contained jaxb annotations. Not <code>null</code>.
   * 
   * @return   The unmarshaller. Not <code>null</code>.
   */
  private TypeUnmarshaller buildUnmarshaller( Class<?> type ) {
    try {
      
      List<PropertyDescription> properties = introspect( type ).stream()
        .filter( this::hasGenericsType )
        .filter( this::isNotTransient )
        .filter( this::hasSetter )
        .filter( this::isInteresting )
        .collect( Collectors.toList() );
      
      for( PropertyDescription property : properties ) {
        if( isAttribute( property ) ) {
          if( property.getCollectionType() != null ) {
            if( property.getXmlAdapter() != null ) {
              property.setLoader( ($1, $2) -> getAttributesUsingXmlAdapter( $1, $2, property.getType(), property.getXmlAdapter() ) );
            } else {
              property.setLoader( ($1, $2) -> getAttributes( $1, $2, property.getType() ) );
            }
          } else {
            if( property.getXmlAdapter() != null ) {
              property.setLoader( getXmlAdapterLoader( property.getXmlAdapter() ) );
            } else {
              property.setLoader( getAttributeLoader( property.getType() ) );
            }
          }
        } else if( property.getCollectionType() != null ) {
          property.setLoader( ($1, $2) -> getElements( $1, $2, property.getSubProperty(), property.getType() ) );
        } else {
          property.setLoader( ($1, $2) -> getElement( $1, $2, property.getType() ) );
        }
      }
      
      Consumer<Object> postprocess = DO_NOTHING;
      if( IPostProcessor.class.isAssignableFrom( type ) ) {
        postprocess = $ -> ((IPostProcessor) $).postprocess();
      }
      
      String       refproperty  = null;
      String       refworkspace = null;
      boolean      before       = true;
      JcxReference jcxReference = type.getAnnotation( JcxReference.class );
      if( jcxReference != null ) {
        refproperty  = jcxReference.property();
        refworkspace = jcxReference.value();
        before       = jcxReference.before();
      }
      
      return new TypeUnmarshaller( properties, newSupplier( type ), postprocess, refworkspace, refproperty, before );
      
    } catch( Exception ex ) {
      log.error( msg_failed_to_create_unmarshaller.format( type.getName(), ex.getLocalizedMessage() ), ex );
      throw JcxException.wrap( ex );
    }
  }
  
  private Supplier newSupplier( Class<?> type ) {
    Supplier          result            = null;
    ComponentProvider componentProvider = Components.getComponentProvider();
    if( componentProvider instanceof GuiceComponentProvider ) {
      result = () -> componentProvider.newInstanceWithParameterResolvers( type, new GuiceParameterResolver( (GuiceComponentProvider) componentProvider ) );
    } else {
      log.warn( msg_non_guice_component_provider.format( type.getName() ) );
      result = () -> componentProvider.newInstance( type );
    }
    return result;
  }
  
  private <R> List<R> getElements( Node node, String nodeName, String subProperty, Class<R> type ) {
    try {
      List<R> result = Collections.emptyList();
      if( subProperty != null ) {
        if( node.hasNode( subProperty ) ) {
          Node       containerNode = node.getNode( subProperty );
          List<Node> nodes         = getNodes( containerNode );
          if( ! nodes.isEmpty() ) {
            Function<Node, R> creator = createCreator( type );
            result                    = nodes.stream()
              .map( creator::apply )
              .collect( Collectors.toList() );
            
          }
        }
      } else {
        List<String> names  = getNodeNames( node, nodeName );
        if( ! names.isEmpty() ) {
          BiFunction<Node, String, R> subloader = createSubnodeCreator( type );
          result = names.stream()
            .map( $ -> subloader.apply( node, $ ) )
            .collect( Collectors.toList() );
        }
      }
      return result;
    } catch( Exception ex ) {
      throw JcxException.wrap( ex );
    }
  }
  
  private List<Node> getNodes( Node parent ) throws RepositoryException {
    List<Node>   result   = new ArrayList<>();
    NodeIterator iterator = parent.getNodes();
    while( iterator.hasNext() ) {
      result.add( iterator.nextNode() );
    }
    return result;
  }
  
  private <R> List<R> getAttributesUsingXmlAdapter( Node node, String nodeName, Class<R> type, Class<? extends XmlAdapter> xmlAdapterType ) {
    try {
      List<R>      result    = Collections.emptyList();
      List<String> strValues = getAttributes( node, nodeName, String.class );
      if( ! strValues.isEmpty() ) {
        XmlAdapter<String, R> adapter = newXmlAdapter( xmlAdapterType );
        result = new ArrayList<>( strValues.size() );
        for( String str : strValues ) {
          result.add( adapter.unmarshal( str ) );
        }
      }
      return result;
    } catch( Exception ex ) {
      throw JcxException.wrap( ex );
    }
  }

  private <R> List<R> getAttributes( Node node, String nodeName, Class<R> type ) { 
    try {
      List<R>      result = Collections.emptyList();
      List<String> names  = getPropertyNames( node, nodeName );
      if( ! names.isEmpty() ) {
        BiFunction<Node, String, R> subloader = getAttributeLoader( type );
        result = names.stream()
          .map( $ -> subloader.apply( node, $ ) )
          .collect( Collectors.toList() );
      }
      return result;
    } catch( RepositoryException ex ) {
      throw JcxException.wrap( ex );
    }
  }
  
  private List<String> getPropertyNames( Node node, String nodeName ) throws RepositoryException {
    List<String>     result   = Collections.emptyList();
    PropertyIterator iterator = node.getProperties( nodeName + "*" );
    if( iterator.hasNext() ) {
      result = new ArrayList<>();
      while( iterator.hasNext() ) {
        result.add( iterator.nextProperty().getName() );
      }
      Collections.sort( result );
    }
    return result;
  }

  private List<String> getNodeNames( Node node, String nodeName ) throws RepositoryException {
    List<String> result   = Collections.emptyList();
    NodeIterator iterator = node.getNodes( nodeName + "*" );
    if( iterator.hasNext() ) {
      result = new ArrayList<>();
      while( iterator.hasNext() ) {
        result.add( iterator.nextNode().getName() );
      }
      Collections.sort( result );
    }
    return result;
  }

  private <R> R getElement( Node node, String nodeName, Class<R> type ) {
    R    result   = null;
    Node dataNode = getNode( node, nodeName );
    if( dataNode != null ) {
      result = (R) create( dataNode, type );
    }
    return result;
  }

  private Node getNode( Node node, String nodeName ) {
    Node result = null;
    if( NAME_DIRECT.equals( nodeName ) ) {
      result = node;
    } else {
      try {
        if( node.hasNode( nodeName ) ) {
          result = node.getNode( nodeName );
        }
      } catch( Exception ex ) {
        throw JcxException.wrap( ex );
      }
    }
    return result;
  }

  private boolean isNotTransient( PropertyDescription description ) {
    return description.getField().getAnnotation( XmlTransient.class ) == null;
  }
  
  private boolean hasGenericsType( PropertyDescription description ) {
    boolean result = description.getCollectionType() == null || description.getType() != null;
    if( (! result) && isNotTransient( description ) ) {
      log.warn( msg_missing_generics_type.format( description.getOwningType().getName(), description.getPropertyName() ) ); 
    }
    return result;
  }

  private boolean hasSetter( PropertyDescription description ) {
    boolean result = description.getSetter() != null;
    if( ! result ) {
      log.warn( msg_missing_setter_method.format( description.getOwningType().getName(), description.getPropertyName() ) ); 
    }
    return result;
  }

  private boolean isInteresting( PropertyDescription description ) {
    XmlAttribute      xmlAttr     = description.getField().getAnnotation( XmlAttribute.class );
    XmlElement        xmlElem     = description.getField().getAnnotation( XmlElement.class );
    XmlElementWrapper xmlWrapper  = description.getField().getAnnotation( XmlElementWrapper.class );
    return (xmlAttr != null) || (xmlElem != null) || (xmlWrapper != null);
  }
  
  private boolean isAttribute( PropertyDescription description ) {
    XmlAttribute xmlAttr = description.getField().getAnnotation( XmlAttribute.class );
    return xmlAttr != null;
  }
  
  private List<PropertyDescription> introspect( Class<?> type ) throws NoSuchMethodException, SecurityException {
    Map<String, PropertyDescription> descriptions = new HashMap<>();
    introspect( type, type, descriptions );
    return new ArrayList<>( descriptions.values() );
  }
  
  private void introspect( Class<?> basetype, Class<?> type, Map<String, PropertyDescription> properties ) throws NoSuchMethodException, SecurityException {
    
    Class<?> superclass = type.getSuperclass();
    if( (superclass != null) && (superclass != Object.class) ) {
      introspect( basetype, superclass, properties );
    }
    
    for( Field field : type.getDeclaredFields() ) {
      if( field.isSynthetic() || ((field.getModifiers() & Modifier.STATIC) != 0) ) {
        continue;
      }
      introspectField( basetype, type, field, properties );
    }
    
  }
  
  private void introspectField( Class<?> basetype, Class<?> type, Field field, Map<String, PropertyDescription> properties ) {
    String                name          = fieldNameGenerator.apply( field.getName() );
    String                propertyName  = getPropertyName( field, name );
    String                subProperty   = null;
    XmlJavaTypeAdapter    xmlAdapter    = field.getAnnotation( XmlJavaTypeAdapter.class ); 
    XmlElementWrapper     elemWrapper   = field.getAnnotation( XmlElementWrapper.class );
    if( elemWrapper != null ) {
      subProperty = StringFunctions.cleanup( elemWrapper.name() );
      if( subProperty == null ) {
        subProperty = propertyName;
      }
    }
    PropertyDescription description   = new PropertyDescription();
    description.setXmlAdapter( xmlAdapter != null ? xmlAdapter.value() : null );
    description.setOwningType( basetype );
    description.setPropertyName( propertyName );
    description.setSubProperty( subProperty );
    description.setField( field );
    if( Collection.class.isAssignableFrom( field.getType() ) ) {
      description.setCollectionType( field.getType() );
      GenericsType genericsType = field.getAnnotation( GenericsType.class );
      if( genericsType != null ) {
        description.setType( genericsType.value() );
      }
    } else {
      description.setType( field.getType() );
    }
    try {
      String setterName = String.format( "set%s", StringFunctions.firstUp( field.getName() ) );
      description.setSetter( type.getDeclaredMethod( setterName, field.getType() ) );
    } catch( NoSuchMethodException | SecurityException ex ) {
      // will be reported later
    }
    properties.put( name, description );
  }
  
  private String getPropertyName( Field field, String defaultName ) {
    String        result  = defaultName;
    String        name    = null;
    XmlAttribute  xmlAttr = field.getAnnotation( XmlAttribute.class );
    name                  = xmlAttr != null ? cleanup( xmlAttr.name() ) : null;
    if( name == null ) {
      XmlElement xmlElem = field.getAnnotation( XmlElement.class );
      name               = xmlElem != null ? cleanup( xmlElem.name() ) : null; 
    }
    if( name != null ) {
      result = name;
    }
    return result;
  }
  
  private String cleanup( String str ) {
    String result = str;
    if( NAME_DEFAULT.equals( result ) ) {
      result = null;
    }
    result = StringFunctions.cleanup( result );
    return result;
  }

} /* ENDCLASS */
