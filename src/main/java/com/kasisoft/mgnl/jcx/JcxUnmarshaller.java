package com.kasisoft.mgnl.jcx;

import static com.kasisoft.mgnl.jcx.internal.Messages.*;

import info.magnolia.repository.*;

import info.magnolia.context.*;

import info.magnolia.jcr.*;

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

  // allows to specify that the current node is the source of the data
  public  static final String NAME_DIRECT   = "##direct";
  
  private static final String NAME_DEFAULT  = "##default";
  
  private static final Consumer<Object> DO_NOTHING = $ -> {};
  
  Function<String, String>                                        fieldNameGenerator;
  Map<Class<?>, TypeUnmarshaller>                                 unmarshallers;
  Map<Class<?>, BiFunction<Node, String, ?>>                      attributeLoaders;
  Map<String, BiFunction<Node, String, ?>>                        metaAttributeLoaders;
  Map<Class<? extends XmlAdapter>, BiFunction<Node, String, ?>>   xmlAdapterLoaders;
  TriConsumer<Node, String, String>                               unsatisfiedRequireHandler;
  Map<Class<?>, Set<String>>                                      xmlTransientProperties;
  Set<String>                                                     xmlTransientPropertyNames;
  Map<String, Consumer>                                           interventions;
  
  public JcxUnmarshaller() {
    fieldNameGenerator        = Function.identity();
    unmarshallers             = new HashMap<>();
    interventions             = new HashMap<>();
    xmlAdapterLoaders         = new HashMap<>();
    xmlTransientProperties    = new HashMap<>();
    xmlTransientPropertyNames = new HashSet<>();
    attributeLoaders          = setupAttributeLoaders();
    metaAttributeLoaders      = setupMetaAttributeLoaders();
    unsatisfiedRequireHandler = null;
  }
  
  public synchronized <T> void registerIntervention( @Nonnull Class<?> parentType, @Nonnull Class<T> type, Consumer<T> intervention ) {
    String key = String.format( "%s:%s", parentType.getName(), type.getName() );
    interventions.put( key, intervention );
  }

  public synchronized <T> void unregisterIntervention( @Nonnull Class<?> parentType, @Nonnull Class<T> type ) {
    String key = String.format( "%s:%s", parentType.getName(), type.getName() );
    interventions.remove( key );
  }

  @Nonnull
  public synchronized <T> Consumer<T> getIntervention( @Nonnull Class<?> parentType, @Nonnull Class<T> type ) {
    String      key    = String.format( "%s:%s", parentType.getName(), type.getName() );
    Consumer<T> result = interventions.get( key );
    if( result == null ) {
      result = $ -> {};
    }
    return result;
  }

  public synchronized void registerXmlTransientProperties( @Nonnull Class<?> clazz, String ... properties ) {
    Set<String> props = xmlTransientProperties.get( clazz );
    if( props == null ) {
      props = new HashSet<>();
      xmlTransientProperties.put( clazz, props );
    }
    List<String> list = Arrays.asList( properties );
    props.addAll( list );
    xmlTransientPropertyNames.addAll( list );
  }
  
  public synchronized void unregisterXmlTransientProperties( @Nonnull Class<?> clazz ) {
    xmlTransientProperties.remove( clazz );
    xmlTransientPropertyNames.clear();
    for( Set<String> names : xmlTransientProperties.values() ) {
      xmlTransientPropertyNames.addAll( names );
    }
  }

  private synchronized void unsatisfiedRequire( Node jcrNode, String owningType, String propertyName ) {
    if( unsatisfiedRequireHandler != null ) {
      unsatisfiedRequireHandler.accept( jcrNode, owningType, propertyName );
    } else {
      log.error( msg_missing_required_property.format( owningType, propertyName, NodeFunctions.getPath( jcrNode ) ) );
    }
  }
  
  protected synchronized <R> XmlAdapter<String, R> newXmlAdapter( Class<? extends XmlAdapter> xmlAdapterType ) {
    XmlAdapter<String, R> result = null;
    try {
      result = Components.getComponent( xmlAdapterType );
    } catch( Exception ex ) {
      result = Components.newInstance( xmlAdapterType );
    }
    return result;
  }
  
  protected synchronized <R> BiFunction<Node, String, R> getXmlAdapterLoader( @Nonnull Class<? extends XmlAdapter> xmlAdapter ) {
    BiFunction<Node, String, R> result = (BiFunction<Node, String, R>) xmlAdapterLoaders.get( xmlAdapter );
    if( result == null ) {
      XmlAdapter<String, R> xml = newXmlAdapter( xmlAdapter );
      result = ($1, $2) -> exceptionWrapper( xml, $1, $2 );
      xmlAdapterLoaders.put( xmlAdapter, result );
    }
    return result;
  }
  
  private synchronized <R> R exceptionWrapper( XmlAdapter<String, R> adapter, Node node, String property ) {
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

  protected synchronized Map<Class<?>, BiFunction<Node, String, ?>> setupAttributeLoaders() {
    
    Map<Class<?>, BiFunction<Node, String, ?>> result = new HashMap<>();
    
    result.put( Boolean   . TYPE , PropertyLoaders::toBoolean     );
    result.put( Character . TYPE , PropertyLoaders::toCharacter   );
    result.put( Byte      . TYPE , PropertyLoaders::toByte        );
    result.put( Short     . TYPE , PropertyLoaders::toShort       );
    result.put( Integer   . TYPE , PropertyLoaders::toInteger     );
    result.put( Long      . TYPE , PropertyLoaders::toLong        );
    result.put( Float     . TYPE , PropertyLoaders::toFloat       );
    result.put( Double    . TYPE , PropertyLoaders::toDouble      );

    result.put( Calendar  . class, PropertyLoaders::toCalendar    );
    result.put( Date      . class, PropertyLoaders::toDate        );
    result.put( Boolean   . class, PropertyLoaders::toBoolean     );
    result.put( Character . class, PropertyLoaders::toCharacter   );
    result.put( Byte      . class, PropertyLoaders::toByte        );
    result.put( Short     . class, PropertyLoaders::toShort       );
    result.put( Integer   . class, PropertyLoaders::toInteger     );
    result.put( Long      . class, PropertyLoaders::toLong        );
    result.put( Float     . class, PropertyLoaders::toFloat       );
    result.put( Double    . class, PropertyLoaders::toDouble      );
    result.put( String    . class, PropertyLoaders::toString      );
    result.put( Node      . class, this::toNode                   );
    
    return result;
    
  }
  
  private Node toNode( Node source, String property ) {
    Node result = null;
    if( source != null ) {
      try {
        String value = PropertyLoaders.toString( source, property );
        if( value != null ) {
          Session session = MgnlContext.getJCRSession( RepositoryConstants.WEBSITE );
          String uuid     = getUuid( value );
          if( uuid != null ) {
            result = session.getNodeByIdentifier( uuid );
          } else {
            result = session.getNode( value );
          }
        }
      } catch( Exception ex ) {
        throw NodeFunctions.toRuntimeRepositoryException(ex);
      }
    }
    return result;
  }
  
  private String getUuid( String value ) {
    int open  = value.indexOf('{');
    int close = value.indexOf('}');
    if( (open != -1) && (close > open) ) {
      value = StringFunctions.cleanup( value.substring( open + 1, close ) ); 
    }
    String result = null;
    if( value != null ) {
      try {
        UUID.fromString( value );
        result = value;
      } catch( Exception ex ) {
        // invalid UUID so there's none
      }
    }
    return result;
  }

  protected synchronized Map<String, BiFunction<Node, String, ?>> setupMetaAttributeLoaders() {
    Map<String, BiFunction<Node, String, ?>> result = new HashMap<>();
    result.put( "@depth"      , this::getMetaDepth      );
    result.put( "@uuid"       , this::getMetaIdentifier );
    result.put( "@identifier" , this::getMetaIdentifier );
    result.put( "@name"       , this::getMetaName       );
    result.put( "@path"       , this::getMetaPath       );
    result.put( "@nodeType"   , this::getMetaNodeType   );
    return result;
  }

  private String getMetaNodeType( Node node, String attribute ) {
    try {
      return node.getPrimaryNodeType().getName();
    } catch( RepositoryException ex ) {
      throw new RuntimeRepositoryException(ex);
    }
  }

  private String getMetaPath( Node node, String attribute ) {
    try {
      return node.getPath();
    } catch( RepositoryException ex ) {
      throw new RuntimeRepositoryException(ex);
    }
  }

  private String getMetaName( Node node, String attribute ) {
    try {
      return node.getName();
    } catch( RepositoryException ex ) {
      throw new RuntimeRepositoryException(ex);
    }
  }

  private String getMetaIdentifier( Node node, String attribute ) {
    try {
      return node.getIdentifier();
    } catch( RepositoryException ex ) {
      throw new RuntimeRepositoryException(ex);
    }
  }

  private int getMetaDepth( Node node, String attribute ) {
    try {
      return node.getDepth();
    } catch( RepositoryException ex ) {
      throw new RuntimeRepositoryException(ex);
    }
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
   * This function allows to specify a handler that deals with situations where an attribute/element is required
   * but not available. By default these situations will only be logged.
   * The handler accepts the following parameters:
   * <ul>
   *   <li>the node which doesn't provide the value</li>
   *   <li>the missing/erraneous property</li>
   * </ul>
   * NOTE: If you intend to raise a {@link RuntimeException} it MUST inherit {@link JcxException} as it won't
   *       be passed otherwise. If it's not inheriting from the {@link JcxException} you will find your exception
   *       as a causing one.
   * 
   * @param handler   The new handler.
   *                  <code>null</code> <=> A conflict will be logged only.
   */
  public synchronized void setUnsatisfiedRequireHandler( @Nullable TriConsumer<Node, String, String> handler ) {
    unsatisfiedRequireHandler = handler;
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
    return createCreator( type, null );
  }

  /**
   * Like {@link #createLoader(Class)} with the difference that the resulting object will be created, too.
   * 
   * @param type   The type of data that is supposed to be created.
   * 
   * @return   The creation function. Not <code>null</code>.
   */
  private synchronized <T> Function<Node, T> createCreator( @Nonnull Class<T> type, @Nullable Class<?> owningType ) {
    return $n -> getUnmarshaller( type ).create( $n, owningType );
  }

  /**
   * Like {@link #create(Class)} with the difference that the resulting object will be created, too.
   * 
   * @param type   The type of data that is supposed to be created.
   * 
   * @return   The creation function. Not <code>null</code>.
   */
  public synchronized <T> BiFunction<Node, String, T> createSubnodeCreator( @Nonnull Class<T> type ) {
    return createSubnodeCreator( type, null );
  }

  private synchronized <T> BiFunction<Node, String, T> createSubnodeCreator( @Nonnull Class<T> type, Class<?> owningType ) {
    return ($n, $s) -> getUnmarshaller( type ).createSubnode( $n, $s, owningType );
  }

  private synchronized Object create( Node dataNode, Class<?> clazz, Class<?> owningType ) {
    return createCreator( clazz, owningType ).apply( dataNode ); 
  }

  protected synchronized <T> TypeUnmarshaller<T> getUnmarshaller( Class<T> type ) {
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
  private synchronized TypeUnmarshaller buildUnmarshaller( Class<?> type ) {
    try {
      
      List<PropertyDescription> properties = introspect( type ).stream()
          
        // we require the specific generic types for instantiation
        .filter( this::hasGenericsType )
        
        // transient marked properties won't be processed
        .filter( this::isNotTransient )
        
        // we reqire getters and setters
        .filter( this::hasSetter )
        .filter( this::hasGetter )
        
        // last but not least we need an annotation allowing for us to process the serialization
        .filter( this::isInteresting )
        
        .collect( Collectors.toList() );
      
      for( PropertyDescription property : properties ) {
        if( property.isAttribute() ) {
          // @XmlAttribute
          if( property.getCollectionType() != null ) {
            // we're dealing with a collection here
            if( property.getXmlAdapter() != null ) {
              property.setLoader( ($1, $2) -> getAttributesUsingXmlAdapter( $1, $2, property.getType(), property.getXmlAdapter() ) );
            } else {
              property.setLoader( ($1, $2) -> getAttributes( $1, $2, property.getType() ) );
            }
          } else {
            // a simple attribute
            if( property.getXmlAdapter() != null ) {
              property.setLoader( getXmlAdapterLoader( property.getXmlAdapter() ) );
            } else {
              property.setLoader( getAttributeLoader( property.getType() ) );
            }
          }
        } else if( property.isMetaAttribute() ) {
          property.setLoader( metaAttributeLoaders.get( property.getPropertyName() ) );
        } else if( property.getJcxRef() != null ) {
          property.setLoader( ($1, $2) -> getElementByReference( $1, $2, type, property.getType(), property.getJcxRef() ) );
        } else if( property.getCollectionType() != null ) {
          // @XmlElement on a collection
          property.setLoader( ($1, $2) -> getElements( $1, $2, type, property.getSubProperty(), property.getType() ) );
        } else {
          // @XmlElement on a single element
          property.setLoader( ($1, $2) -> getElement( $1, $2, type, property.getType() ) );
        }
      }

      List<PropertyDescription> invalids = properties.parallelStream().filter( $ -> $.getLoader() == null ).collect( Collectors.toList() );
      if( ! invalids.isEmpty() ) {
        String list = invalids.parallelStream()
          .map( $ -> String.format( "%s$%s[%s]", $.getOwningType(), $.getPropertyName(), $.getType() ) )
          .reduce( "", ($1, $2) -> $1 + ", " + $2 )
          ;
        throw new JcxException( msg_incomplete_type.format( list ) );
      }
      
      Consumer<Object> postprocess = DO_NOTHING;
      if( IPostProcessor.class.isAssignableFrom( type ) ) {
        postprocess = $ -> ((IPostProcessor) $).postprocess();
      }
      
      String                        refproperty  = null;
      String                        refworkspace = null;
      JcxReference.UseOriginalNode  useOriginal  = null;
      JcxReference jcxReference = type.getAnnotation( JcxReference.class );
      if( jcxReference != null ) {
        refproperty  = jcxReference.property();
        refworkspace = jcxReference.value();
        useOriginal  = jcxReference.useOriginalNode();
      }
      
      TriConsumer<Node, String, String> requireHandler = this::unsatisfiedRequire;
      return new TypeUnmarshaller( this, type, properties, newSupplier( type ), postprocess, refworkspace, refproperty, useOriginal, requireHandler );
      
    } catch( Exception ex ) {
      log.error( msg_failed_to_create_unmarshaller.format( type.getName(), ex.getLocalizedMessage() ), ex );
      throw JcxException.wrap( ex );
    }
  }
  
  private synchronized Supplier newSupplier( Class<?> type ) {
    Supplier          result            = null;
    ComponentProvider componentProvider = Components.getComponentProvider();
    if( componentProvider instanceof GuiceComponentProvider ) {
      result = () -> componentProvider.newInstanceWithParameterResolvers( type, new GuiceParameterResolver( (GuiceComponentProvider) componentProvider ) );
    } else {
      log.trace( msg_non_guice_component_provider.format( type.getName() ) );
      result = () -> componentProvider.newInstance( type );
    }
    return result;
  }
  
  private synchronized <R> List<R> getElements( Node node, String nodeName, Class<?> owningType, String subProperty, Class<R> type ) {
    try {
      List<R> result = Collections.emptyList();
      if( subProperty != null ) {
        if( node.hasNode( subProperty ) ) {
          Node       containerNode = node.getNode( subProperty );
          List<Node> nodes         = getNodes( containerNode );
          if( ! nodes.isEmpty() ) {
            if( Node.class.isAssignableFrom( type ) ) {
              result = (List<R>) nodes;
            } else {
              Function<Node, R> creator = createCreator( type, owningType );
              result                    = nodes.stream()
                  .map( creator::apply )
                  .collect( Collectors.toList() );
            }
            
          }
        }
      } else {
        List<String> names  = getNodeNames( node, nodeName );
        if( ! names.isEmpty() ) {
          if( Node.class.isAssignableFrom( type ) ) {
            result = (List<R>) names.stream()
              .map( $ -> NodeFunctions.getNode( node, $ ) )
              .collect( Collectors.toList() );
          } else {
            BiFunction<Node, String, R> subloader = createSubnodeCreator( type, owningType );
            result = names.stream()
                .map( $ -> subloader.apply( node, $ ) )
                .collect( Collectors.toList() );
          }
        }
      }
      for( int i = result.size() - 1; i >= 0; i-- ) {
        if( result.get(i) == null ) {
          // an element is null due to the fact that there's no 'require' handling
          result.remove(i);
        }
      }
      return result;
    } catch( Exception ex ) {
      throw JcxException.wrap( ex );
    }
  }
  
  private synchronized List<Node> getNodes( Node parent ) throws RepositoryException {
    List<Node>   result   = new ArrayList<>();
    NodeIterator iterator = parent.getNodes();
    while( iterator.hasNext() ) {
      result.add( iterator.nextNode() );
    }
    return result;
  }
  
  private synchronized <R> List<R> getAttributesUsingXmlAdapter( Node node, String nodeName, Class<R> type, Class<? extends XmlAdapter> xmlAdapterType ) {
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

  private synchronized <R> List<R> getAttributes( Node node, String nodeName, Class<R> type ) { 
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
  
  private synchronized List<String> getPropertyNames( Node node, String nodeName ) throws RepositoryException {
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

  private synchronized List<String> getNodeNames( Node node, String nodeName ) throws RepositoryException {
    List<String> result   = Collections.emptyList();
    NodeIterator iterator = node.getNodes( nodeName + "*" );
    if( iterator.hasNext() ) {
      result = new ArrayList<>();
      while( iterator.hasNext() ) {
        result.add( iterator.nextNode().getName() );
      }
    }
    return result;
  }

  private synchronized <R> R getElementByReference( Node node, String nodeName, Class<?> owningType, Class<R> type, JcxReference jcxReference ) {
    R       result    = null;
    Node    reference = getReferredNode( getAttributeLoader( String.class ).apply( node, jcxReference.property() ), jcxReference ); 
    if( reference != null ) {
      
      if( Node.class.isAssignableFrom( type ) ) {
        result = (R) reference;
      } else {
        
        if( jcxReference.useOriginalNode() == JcxReference.UseOriginalNode.before ) {
          result = (R) create( node, type, owningType );
        }
        
        // load the data of the referred node
        if( result != null ) {
          result = createLoader( type ).apply( reference, result );
        } else {
          result = (R) create( reference, type, owningType );
        }
        
        if( jcxReference.useOriginalNode() == JcxReference.UseOriginalNode.after ) {
          result = createLoader( type ).apply( node, result );
        }
        
      }
      
    }
    return result;
  }
  
  private Node getReferredNode( String pathOrUuid, JcxReference jcxReference ) {
    pathOrUuid  = StringFunctions.cleanup( pathOrUuid );
    Node result = null;
    if( pathOrUuid != null ) {
      int open  = pathOrUuid.charAt(0);
      int close = pathOrUuid.indexOf('}');
      if( open == '[' ) {
        // external link
        pathOrUuid = null;
      } else if( open == '{' ) {
        if( close != -1 ) {
          pathOrUuid = pathOrUuid.substring( 1, close );
        } else {
          //
          pathOrUuid = null;
        }
      }
    }
    if( pathOrUuid != null ) {
      try {
        Session session = MgnlContext.getJCRSession( jcxReference.value() );
        try {
          UUID.fromString( pathOrUuid );
          result = session.getNodeByIdentifier( pathOrUuid );
        } catch( Exception ex ) {
          result = session.getNode( pathOrUuid );
        }
      } catch( Exception ex ) {
        throw JcxException.wrap( ex );
      }
    }
    return result;
  }
  
  private synchronized <R> R getElement( Node node, String nodeName, Class<?> owningType, Class<R> type ) {
    R    result   = null;
    Node dataNode = getNode( node, nodeName );
    if( dataNode != null ) {
      if( Node.class.isAssignableFrom( type ) ) {
        result = (R) dataNode;
      } else {
        result = (R) create( dataNode, type, owningType );
      }
    }
    return result;
  }

  private synchronized Node getNode( Node node, String nodeName ) {
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

  private synchronized boolean isNotTransient( PropertyDescription description ) {
    boolean result = (description.getField().getAnnotation( XmlTransient.class ) == null);
    if( ! result ) {
      result = description.getJcxRef() != null;
    }
    if( result && (! xmlTransientProperties.isEmpty()) && xmlTransientPropertyNames.contains( description.getPropertyName() ) ) {
      // this property has been registered as XmlTransient by default. this is helpful to disable properties
      // of classes which aren't under our control so we can't mark them explicitly as xml transient
      for( Map.Entry<Class<?>, Set<String>> entry : xmlTransientProperties.entrySet() ) {
        if( entry.getValue().contains( description.getPropertyName() ) ) {
          if( entry.getKey().isAssignableFrom( description.getOwningType() ) ) {
            // this property is marked explicitly as xml transient
            result = false;
            break;
          }
        }
      }
    }
    return result;
  }
  
  private synchronized boolean hasGenericsType( PropertyDescription description ) {
    boolean result = description.getCollectionType() == null || description.getType() != null;
    if( (! result) && isNotTransient( description ) ) {
      log.warn( msg_missing_generics_type.format( description.getOwningType().getName(), description.getPropertyName() ) ); 
    }
    return result;
  }

  private synchronized boolean hasSetter( PropertyDescription description ) {
    boolean result = description.getSetter() != null;
    if( ! result ) {
      log.warn( msg_missing_setter_method.format( description.getOwningType().getName(), description.getPropertyName() ) ); 
    }
    return result;
  }

  private synchronized boolean hasGetter( PropertyDescription description ) {
    boolean result = true;
    if( description.isRequired() ) {
      // we only need the getter if the property is required
      result = description.getGetter() != null; 
      if( ! result ) {
        log.warn( msg_missing_getter_method.format( description.getOwningType().getName(), description.getPropertyName() ) );
      }
    }
    return result;
  }

  private synchronized boolean isInteresting( PropertyDescription description ) {
    XmlAttribute      xmlAttr     = description.getField().getAnnotation( XmlAttribute.class );
    XmlElement        xmlElem     = description.getField().getAnnotation( XmlElement.class );
    XmlElementWrapper xmlWrapper  = description.getField().getAnnotation( XmlElementWrapper.class );
    JcxReference      jcxRef      = description.getJcxRef();
    return (xmlAttr != null) || (xmlElem != null) || (xmlWrapper != null) || (jcxRef != null);
  }
  
  private synchronized List<PropertyDescription> introspect( Class<?> type ) throws NoSuchMethodException, SecurityException {
    Map<String, PropertyDescription> descriptions = new HashMap<>();
    introspect( type, type, descriptions );
    return new ArrayList<>( descriptions.values() );
  }
  
  private synchronized void introspect( Class<?> basetype, Class<?> type, Map<String, PropertyDescription> properties ) throws NoSuchMethodException, SecurityException {
    
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
  
  private synchronized boolean isRequired( Field field ) {
    boolean      result  = false;
    XmlAttribute xmlattr = field.getAnnotation( XmlAttribute . class );
    XmlElement   xmlelem = field.getAnnotation( XmlElement   . class );
    if( xmlattr != null ) {
      result = xmlattr.required();
    } else if( xmlelem != null ) {
      result = xmlelem.required();
    }
    return result;
  }
  
  private synchronized void introspectField( Class<?> basetype, Class<?> type, Field field, Map<String, PropertyDescription> properties ) {
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
    description.setRequired( isRequired( field ) );
    if( Collection.class.isAssignableFrom( field.getType() ) ) {
      description.setCollectionType( field.getType() );
      GenericsType genericsType = field.getAnnotation( GenericsType.class );
      if( genericsType != null ) {
        description.setType( genericsType.value() );
      }
    } else {
      description.setType( field.getType() );
    }
    if( (description.getCollectionType() == null) && (!description.getType().isPrimitive()) && (description.getType() != String.class) ) {
      // we only support jcx reference for non collections, non primitives an no strings
      description.setJcxRef( field.getAnnotation( JcxReference.class ) );
    }
    description.setSetter( lookupMethod( field.getName(), type, new Class[] { field.getType() }, "set" ) );
    description.setGetter( lookupMethod( field.getName(), type, new Class[0], "get", "is" ) );
    properties.put( name, description );
  }
  
  private Method lookupMethod( String name, Class<?> type, Class[] params, String ... prefixes ) {
    Method result   = null;
    String basename = StringFunctions.firstUp( name );
    for( String prefix : prefixes ) {
      try {
        result = type.getDeclaredMethod( String.format( "%s%s", prefix, basename ), params );
        if( result != null ) {
          break;
        }
      } catch( Exception ex ) {
        // will be reported later
      }
    }
    return result;
  }
  
  private synchronized String getPropertyName( Field field, String defaultName ) {
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
  
  private synchronized String cleanup( String str ) {
    String result = str;
    if( NAME_DEFAULT.equals( result ) ) {
      result = null;
    }
    result = StringFunctions.cleanup( result );
    return result;
  }

} /* ENDCLASS */
