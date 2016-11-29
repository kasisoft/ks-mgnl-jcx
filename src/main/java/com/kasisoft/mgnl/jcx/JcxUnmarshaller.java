package com.kasisoft.mgnl.jcx;

import com.kasisoft.libs.common.text.*;

import com.kasisoft.libs.common.annotation.*;
import com.kasisoft.libs.common.function.*;

import javax.inject.*;
import javax.jcr.*;
import javax.jcr.Node;
import javax.xml.bind.annotation.*;

import java.lang.reflect.*;

import java.util.function.*;

import java.util.stream.*;

import java.util.*;

import lombok.extern.slf4j.*;

import lombok.experimental.*;

import lombok.*;

import info.magnolia.objectfactory.*;

/**
 * This helper allows to mark attributes using jaxb annotations in order to fill them automatically. 
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Singleton
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JcxUnmarshaller extends AbstractJcrUnmarshaller {

  static final Set<String> IGNORABLES = new HashSet<>( Arrays.asList(
    "log", "contentMap", "unmarshaller", "content", "definition", "parentModel", "loader"
  ) );

  static final Consumer<Object> DO_NOTHING = $ -> {};
  
  Function<String, String>          fieldNameGenerator;
  Map<Class<?>, TypeUnmarshaller>   unmarshallers;
  
  public JcxUnmarshaller() {
    fieldNameGenerator  = Function.identity();
    unmarshallers       = new HashMap<>();
  }
  
  /**
   * Configures a name generator. The default uses the identity which maps the field name to the property name.
   * However if you're using any kind of coding convention (f.e. prefixing an underscore) this function be
   * overriden to deliver the right name. 
   * 
   * @param nameGen   The new name generation function to be used.
   *                  <code>null</code> <=> The identify function is being used.
   */
  public void setFieldNameGenerator( Function<String, String> nameGen ) {
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
  public synchronized <T> BiConsumer<Node, T> createLoader( @NonNull Class<T> type ) {
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
  public synchronized <T> TriConsumer<Node, String, T> createSubnodeLoader( @NonNull Class<T> type ) {
    return getUnmarshaller( type )::applySubnode;
  }

  /**
   * Like {@link #createLoader(Class)} with the difference that the resulting object will be created, too.
   * 
   * @param type   The type of data that is supposed to be created.
   * 
   * @return   The creation function. Not <code>null</code>.
   */
  public synchronized <T> Function<Node, T> createCreator( @NonNull Class<T> type ) {
    return getUnmarshaller( type )::create;
  }

  private Object create( Node dataNode, Class<?> clazz ) {
    return createCreator( clazz ).apply( dataNode ); 
  }

  private TypeUnmarshaller getUnmarshaller( Class<?> type ) {
    TypeUnmarshaller result = unmarshallers.get( type );
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
        .filter( this::hasSetter )
        .filter( this::isInteresting )
        .collect( Collectors.toList() );
      
      for( PropertyDescription property : properties ) {
        if( isAttribute( property ) ) {
          property.setLoader( getAttributeLoader( property.getType() ) );
        } else if( property.getCollectionType() != null ) {
          property.setLoader( ($1, $2) -> getElements( $1, $2, property.getType() ) );
        } else {
          property.setLoader( ($1, $2) -> getElement( $1, $2, property.getType() ) );
        }
      }
      
      Consumer<Object> postprocess = DO_NOTHING;
      if( IPostProcessor.class.isAssignableFrom( type ) ) {
        postprocess = $ -> ((IPostProcessor) $).postprocess();
      }
      
      return new TypeUnmarshaller( properties, () -> Components.newInstance( type ), postprocess );
      
    } catch( Exception ex ) {
      log.error( "Failed to create jcx unmarshaller for type '{}'. Cause: {}", type.getName(), ex.getLocalizedMessage(), ex );
      throw JcxException.wrap( ex );
    }
  }
  
  private <R> List<R> getElements( Node node, String nodeName, Class<R> type ) {
    List<Node> nodes  = getNodes( node, nodeName );
    List<R>    result = Collections.emptyList();
    if( ! nodes.isEmpty() ) {
      result = new ArrayList<>();
      for( Node childNode : nodes ) {
        Object obj = create( childNode, type );
        if( obj != null ) {
          result.add( (R) obj );
        }
      }
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

  private List<Node> getNodes( Node node, String nodeName ) {
    List<Node> result = Collections.emptyList();
    try {
      if( node.hasNode( nodeName ) ) {
        result = new ArrayList<>();
        Node dataNode = node.getNode( nodeName );
        NodeIterator iterator = dataNode.getNodes();
        while( iterator.hasNext() ) {
          result.add( iterator.nextNode() );
        }
      }
    } catch( Exception ex ) {
      throw JcxException.wrap( ex );
    }
    return result;
  }

  private Node getNode( Node node, String nodeName ) {
    Node result = null;
    try {
      if( node.hasNode( nodeName ) ) {
        result = node.getNode( nodeName );
      }
    } catch( Exception ex ) {
      throw JcxException.wrap( ex );
    }
    return result;
  }

  private boolean hasGenericsType( PropertyDescription description ) {
    boolean result = description.getCollectionType() == null || description.getType() != null;
    if( ! result ) {
      log.warn( "{}.{} - missing GenericsType annotation", description.getOwningType().getName(), description.getPropertyName() ); 
    }
    return result;
  }

  private boolean hasSetter( PropertyDescription description ) {
    boolean result = description.getSetter() != null;
    if( (! result) && (! IGNORABLES.contains( description.getPropertyName())) ) {
      log.warn( "{}.{} - missing setter method", description.getOwningType().getName(), description.getPropertyName() ); 
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
    String              name        = fieldNameGenerator.apply( field.getName() );
    PropertyDescription description = new PropertyDescription();
    description.setOwningType( basetype );
    description.setPropertyName( name );
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
      String setterName = String.format( "set%s", StringFunctions.firstUp( name ) );
      description.setSetter( type.getDeclaredMethod( setterName, field.getType() ) );
    } catch( NoSuchMethodException | SecurityException ex ) {
      // will be reported later
    }
    properties.put( name, description );
  }

  @AllArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  private static class TypeUnmarshaller {

    List<PropertyDescription>   descriptions;
    Supplier<?>                 supplier;
    Consumer<Object>            postprocess;
    
    public void applySubnode( Node jcrNode, String nodeName, Object destination ) {
      try {
        if( jcrNode.hasNode( nodeName ) ) {
          apply( jcrNode.getNode( nodeName ), destination );
        }
      } catch( Exception ex ) {
        throw JcxException.wrap( ex );
      }
    }
  
    public void apply( Node jcrNode, Object destination ) {
      try {
        descriptions.forEach( $ -> apply( jcrNode, destination, $ ) );
        postprocess.accept( destination );
      } catch( Exception ex ) {
        throw JcxException.wrap( ex );
      }
    }
    
    public <R> R create( Node jcrNode ) {
      R result = (R) supplier.get();
      if( result != null ) {
        apply( jcrNode, result );
      }
      return result;
    }
    
    private void apply( Node jcrNode, Object destination, PropertyDescription desc ) {
      Object value = desc.getLoader().apply( jcrNode, desc.getPropertyName() );
      if( value != null ) {
        try {
          desc.getSetter().invoke( destination, value );
        } catch( Exception ex ) {
          throw JcxException.wrap( ex );
        }
      }
    }
    
  } /* ENDCLASS */
  
} /* ENDCLASS */
