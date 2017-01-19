package com.kasisoft.mgnl.jcx;

import info.magnolia.objectfactory.*;

import com.kasisoft.mgnl.util.*;

import javax.annotation.*;
import javax.jcr.*;
import javax.xml.bind.annotation.adapters.*;

import java.util.function.*;

import java.util.*;

/**
 * Base class which provides several helper functions to access node settings. 
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
class AbstractJcrUnmarshaller {

  private Map<Class<?>, BiFunction<Node, String, ?>>                      attributeLoaders;
  private Map<Class<? extends XmlAdapter>, BiFunction<Node, String, ?>>   xmlAdapterLoaders;

  public AbstractJcrUnmarshaller() {
    attributeLoaders  = setupAttributeLoaders();
    xmlAdapterLoaders = new HashMap<>();
  }
  
  protected <R> BiFunction<Node, String, R> getXmlAdapterLoader( @Nonnull Class<? extends XmlAdapter> xmlAdapter ) {
    BiFunction<Node, String, R> result = (BiFunction<Node, String, R>) xmlAdapterLoaders.get( xmlAdapter );
    if( result == null ) {
      XmlAdapter<String, R> adapter = null;
      try {
        adapter = Components.getComponent( xmlAdapter );
      } catch( Exception ex ) {
        adapter = Components.newInstance( xmlAdapter );
      }
      final XmlAdapter<String, R> xml = adapter;
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
  
  protected <R> BiFunction<Node, String, R> getAttributeLoader( @Nonnull Class<R> type ) {
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
    
    result.put( Boolean   . TYPE , (n, p) -> PropertyLoaders.toBoolean    ( n, p, false     ) );
    result.put( Character . TYPE , (n, p) -> PropertyLoaders.toCharacter  ( n, p, '\0'      ) );
    result.put( Byte      . TYPE , (n, p) -> PropertyLoaders.toByte       ( n, p, (byte) 0  ) );
    result.put( Short     . TYPE , (n, p) -> PropertyLoaders.toShort      ( n, p, (short) 0 ) );
    result.put( Integer   . TYPE , (n, p) -> PropertyLoaders.toInteger    ( n, p, 0         ) );
    result.put( Long      . TYPE , (n, p) -> PropertyLoaders.toLong       ( n, p, 0L        ) );
    result.put( Float     . TYPE , (n, p) -> PropertyLoaders.toFloat      ( n, p, 0         ) );
    result.put( Double    . TYPE , (n, p) -> PropertyLoaders.toDouble     ( n, p, 0.0       ) );
    
    result.put( Boolean   . class, PropertyLoaders::toBoolean    );
    result.put( Character . class, PropertyLoaders::toCharacter  );
    result.put( Byte      . class, PropertyLoaders::toByte       );
    result.put( Short     . class, PropertyLoaders::toShort      );
    result.put( Integer   . class, PropertyLoaders::toInteger    );
    result.put( Long      . class, PropertyLoaders::toLong       );
    result.put( Float     . class, PropertyLoaders::toFloat      );
    result.put( Double    . class, PropertyLoaders::toDouble     );
    result.put( String    . class, PropertyLoaders::toString     );
    
    return result;
  }
  
} /* ENDCLASS */
