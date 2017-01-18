package com.kasisoft.mgnl.jcx;

import info.magnolia.objectfactory.*;

import info.magnolia.jcr.util.*;

import com.kasisoft.libs.common.text.*;

import com.kasisoft.libs.common.annotation.*;
import com.kasisoft.libs.common.function.*;

import org.slf4j.*;

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

/**
 * This helper allows to mark attributes using jaxb annotations in order to fill them automatically. 
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Singleton
public class JcxUnmarshaller extends AbstractJcrUnmarshaller {

  private static final Logger log = LoggerFactory.getLogger( JcxUnmarshaller.class );
  
  public  static final String NAME_DIRECT   = "##direct";
  private static final String NAME_DEFAULT  = "##default";
  
  private static final Set<String> IGNORABLES = new HashSet<>( Arrays.asList(
    "log", "contentMap", "unmarshaller", "content", "definition", "parentModel", "loader", "cmsfn", "imgfn",
    "damfn"
  ) );

  private static final Consumer<Object> DO_NOTHING = $ -> {};
  
  private Function<String, String>          fieldNameGenerator;
  private Map<Class<?>, TypeUnmarshaller>   unmarshallers;
  
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
        .filter( this::isNotTransient )
        .filter( this::hasSetter )
        .filter( this::isInteresting )
        .collect( Collectors.toList() );
      
      for( PropertyDescription property : properties ) {
        if( isAttribute( property ) ) {
          if( property.getCollectionType() != null ) {
            // TODO: support XmlAdapter
            property.setLoader( ($1, $2) -> getAttributes( $1, $2, property.getSubProperty(), property.getType() ) );
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
      JcxReference jcxReference = type.getAnnotation( JcxReference.class );
      if( jcxReference != null ) {
        refproperty  = jcxReference.property();
        refworkspace = jcxReference.value();
      }
      
      return new TypeUnmarshaller( properties, () -> Components.newInstance( type ), postprocess, refworkspace, refproperty );
      
    } catch( Exception ex ) {
      log.error( "Failed to create jcx unmarshaller for type '{}'. Cause: {}", type.getName(), ex.getLocalizedMessage(), ex );
      throw JcxException.wrap( ex );
    }
  }
  
  private <R> List<R> getElements( Node node, String nodeName, String subProperty, Class<R> type ) {
    List<Node> nodes  = getNodes( node, nodeName, subProperty );
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

  private <R> List<R> getAttributes( Node node, String nodeName, String subProperty, Class<R> type ) {
    List<Node> nodes  = getAttributeNodes( node, nodeName );
    List<R>    result = Collections.emptyList();
    if( ! nodes.isEmpty() ) {
      BiFunction<Node, String, R> subloader = getAttributeLoader( type );
      result = new ArrayList<>();
      for( Node childNode : nodes ) {
        R obj = subloader.apply( childNode, subProperty );
        if( obj != null ) {
          result.add( obj );
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

  private List<Node> getNodes( Node node, String nodeName, String subProperty ) {
    List<Node> result = Collections.emptyList();
    try {
      if( subProperty == null ) {
        if( node.hasNode( nodeName ) ) {
          // children are organized directly below the named node
          result                = new ArrayList<>();
          Node         dataNode = node.getNode( nodeName );
          NodeIterator iterator = dataNode.getNodes();
          while( iterator.hasNext() ) {
            result.add( iterator.nextNode() );
          }
        }
      } else {
        // multivalue based persistence (multivalue/delegation)
        result                = new ArrayList<>();
        NodeIterator iterator = node.getNodes( String.format( "%s*", nodeName ) );
        while( iterator.hasNext() ) {
          Node parent = iterator.nextNode();
          Node child  = parent.getNode( subProperty );
          if( child != null ) {
            result.add( child );
          }
        }
      }
    } catch( Exception ex ) {
      throw JcxException.wrap( ex );
    }
    return result;
  }

  private List<Node> getAttributeNodes( Node node, String nodeName ) {
    List<Node> result = Collections.emptyList();
    try {
      result                = new ArrayList<>();
      NodeIterator iterator = node.getNodes( String.format( "%s*", nodeName ) );
      while( iterator.hasNext() ) {
        result.add( iterator.nextNode() );
      }
    } catch( Exception ex ) {
      throw JcxException.wrap( ex );
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
    String                name          = fieldNameGenerator.apply( field.getName() );
    String                propertyName  = getPropertyName( field, name );
    String                subProperty   = null;
    XmlJavaTypeAdapter    xmlAdapter    = field.getAnnotation( XmlJavaTypeAdapter.class ); 
    XmlElementWrapper     elemWrapper   = field.getAnnotation( XmlElementWrapper.class );
    if( elemWrapper != null ) {
      subProperty   = propertyName;
      propertyName  = elemWrapper.name();
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
      String setterName = String.format( "set%s", StringFunctions.firstUp( name ) );
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

  private static class TypeUnmarshaller {

    private List<PropertyDescription>   descriptions;
    private Supplier<?>                 supplier;
    private Consumer<Object>            postprocess;
    private String                      refProperty;
    private String                      refWorkspace;
    
    public TypeUnmarshaller( List<PropertyDescription> descs, Supplier<?> sup, Consumer<Object> post, String rWorkspace, String rProperty ) {
      descriptions  = descs;
      supplier      = sup;
      postprocess   = post;
      refProperty   = rProperty;
      refWorkspace  = rWorkspace;
    }
    
    public <R> R applySubnode( Node jcrNode, String nodeName, R destination ) {
      try {
        if( jcrNode.hasNode( nodeName ) ) {
          destination = apply( jcrNode.getNode( nodeName ), destination );
        }
        return destination;
      } catch( Exception ex ) {
        throw JcxException.wrap( ex );
      }
    }
  
    public <R> R apply( Node jcrNode, R destination ) {
      try {
        log.debug( "Applying to '{}'", destination );
        descriptions.forEach( $ -> apply( jcrNode, destination, $ ) );
        Node refNode = getRefNode( jcrNode );
        if( refNode != null ) {
          descriptions.forEach( $ -> apply( refNode, destination, $ ) );
        }
        postprocess.accept( destination );
        R result = destination;
        if( (result instanceof IContent) && (! ((IContent) result).hasContent()) ) {
          result = null;
        }
        return result;
      } catch( Exception ex ) {
        throw JcxException.wrap( ex );
      }
    }
    
    private Node getRefNode( Node jcrNode ) {
      Node result = null;
      if( refWorkspace != null ) {
        String uuid = StringFunctions.cleanup( PropertyUtil.getString( jcrNode, refProperty ) );
        if( uuid != null ) {
          uuid = StringFunctions.cleanup( StringFunctions.trim( uuid, "[]{} \t", null ) );
        }
        if( uuid != null ) {
          result = SessionUtil.getNodeByIdentifier( refWorkspace, uuid );
        }
      }
      return result;
    }
    
    public <R> R create( Node jcrNode ) {
      R result = (R) supplier.get();
      if( result != null ) {
        result = apply( jcrNode, result );
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
