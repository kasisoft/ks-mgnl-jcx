package com.kasisoft.mgnl.jcx.internal;

import static com.kasisoft.mgnl.jcx.internal.Messages.*;

import info.magnolia.jcr.util.*;

import com.kasisoft.libs.common.text.*;

import com.kasisoft.libs.common.function.*;
import com.kasisoft.mgnl.jcx.*;
import com.kasisoft.mgnl.util.*;

import javax.annotation.*;
import javax.jcr.*;

import java.util.function.*;

import java.util.*;

import lombok.extern.slf4j.*;

import lombok.experimental.*;

import lombok.*;

/**
 * This implementation provides several functions allowing to load/create several varieties of data.
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Slf4j 
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TypeUnmarshaller<R> {

  JcxUnmarshaller                       jcxUnmarshaller;
  
  Class<R>                              type;
  
  // a list of properties managed by a certain type
  List<PropertyDescription>             descriptions;

  // will be invoked to create new instances of this type
  Supplier<R>                           supplier;
  
  // will be invoked when the properties had been set. the postprocessing allows to check/handle inconsistencies
  // between dependent properties
  Consumer<R>                           postprocess;
  
  // a reference workspace (see JcxReference)
  String                                refWorkspace;
  
  // the property which is used to establish a reference
  String                                refProperty;
  
  // decide whether the referred property will be processed before or after the current node
  boolean                               before;
  
  TriConsumer<Node, String, String>     unsatisfiedRequireHandler;
  
  /**
   * Creates a new instance which properties will be set to the values provided with the supplied node.
   * 
   * @param jcrNode   The node providing the values.
   * 
   * @return   A new instance.
   */
  @Nullable
  public R create( @Nonnull Node jcrNode, @Nullable Class<?> owningType ) {
    R result = (R) supplier.get();
    configure( result, owningType );
    if( result != null ) {
      result = apply( jcrNode, result );
    }
    return result;
  }
  
  /**
   * Like {@link #create(Node)} but with the difference that the data for the properties is provided by a 
   * subnode.
   * 
   * @param jcrNode    A parental node.
   * @param nodeName   The name of the subnode.
   * 
   * @return   A new instance.
   */
  @Nullable
  public R createSubnode( @Nonnull Node jcrNode, @Nonnull String nodeName, @Nullable Class<?> owningType ) {
    try {
      R result = (R) supplier.get();
      configure( result, owningType );
      if( jcrNode.hasNode( nodeName ) ) {
        result = apply( jcrNode.getNode( nodeName ), result );
      }
      return result;
    } catch( Exception ex ) {
      throw JcxException.wrap( ex );
    }
  }

  private void configure( R instance, @Nullable Class<?> owningType ) {
    if( owningType != null ) {
      jcxUnmarshaller.getIntervention( owningType, type ).accept( instance );
    }
  }
  
  /**
   * This is the main function which applies the jcr content from a certain node to the destination object.
   * 
   * @param jcrNode       The jcr node providing the data.
   * @param destination   The destination object.
   * 
   * @return   A possibly recreated object.
   */
  @Nullable
  public R apply( @Nonnull Node jcrNode, @Nonnull R destination ) {
    
    try {
      
      log.trace( msg_applying_node_to_destination.format( NodeFunctions.getPath( jcrNode ), destination ) );
      
      Node refNode = getRefNode( jcrNode );
      
      if( (refNode != null) && before ) {
        // if there's a reference we're using the target as an additional source
        descriptions.forEach( $ -> apply( refNode, destination, $ ) );
      }
      
      // apply the values for each property first
      descriptions.forEach( $ -> apply( jcrNode, destination, $ ) );
      
      if( (refNode != null) && (! before) ) {
        // if there's a reference we're using the target as an additional source
        descriptions.forEach( $ -> apply( refNode, destination, $ ) );
      }

      // let the post process run if some has been configured
      postprocess.accept( destination );
      
      R result = destination;
      
      if( result != null ) {
        // test if all required elements had been set
        final Object o = result;
        boolean allset = descriptions.stream().map( $ -> test( $, o ) ).reduce( true, ($a, $b) -> $a && $b );
        if( ! allset ) {
          result = null;
        }
      }
      
      // make sure the current state constitutes available content
      if( (result instanceof IContent) && (! ((IContent) result).hasContent()) ) {
        log.warn( msg_incomplete_content.format( NodeFunctions.getPath( jcrNode ) ) );
        result = null;
      }
      return result;
      
    } catch( Exception ex ) {
      throw JcxException.wrap( ex );
    }
    
  }
  
  private boolean test( PropertyDescription description, Object instance ) {
    boolean result = ! description.isRequired();
    if( ! result ) {
      try {
        // test if the required value had been set
        Object val = description.getGetter().invoke( instance );
        result     = val != null;
      } catch( Exception ex ) {
        log.error( ex.getLocalizedMessage(), ex );
      }
    }
    return result;
  }
  
  /**
   * This node determines if a there's a referenced node (caused by JcxReference).
   * 
   * @param jcrNode   The node providing the reference.
   * 
   * @return   The referenced node or <code>null</code>.
   */
  private Node getRefNode( Node jcrNode ) {
    Node result = null;
    if( refWorkspace != null ) {
      // get the UUID for the reference
      String uuid = StringFunctions.cleanup( PropertyUtil.getString( jcrNode, refProperty ) );
      if( uuid != null ) {
        /** @todo [12-Apr-2017:KASI]   rething this. */
        uuid = StringFunctions.cleanup( StringFunctions.trim( uuid, "[]{} \t", null ) );
      }
      if( uuid != null ) {
        result = SessionUtil.getNodeByIdentifier( refWorkspace, uuid );
        if( result == null ) {
          log.error( msg_invalid_reference.format( NodeFunctions.getPath( jcrNode ), refProperty, refWorkspace, uuid ) );
        }
      }
    }
    return result;
  }
  
  private void apply( Node jcrNode, R destination, PropertyDescription desc ) {
    Object value = desc.getLoader().apply( jcrNode, desc.getPropertyName() );
    if( value != null ) {
      try {
        desc.getSetter().invoke( destination, value );
      } catch( Exception ex ) {
        throw JcxException.wrap( ex );
      }
    } else if( desc.isRequired() ) {
      unsatisfiedRequireHandler.accept( jcrNode, desc.getOwningType().getName(), desc.getPropertyName() );
    }
  }
  
  /**
   * Like {@link #apply(Node, Object)} but with the difference that the data is being loaded from a subnode.
   * 
   * @param jcrNode       The node providing the subnode.
   * @param nodeName      The name ofthe subnode.
   * @param destination   The object that will be filled.
   * 
   * @return   The initialised object or <code>null</code>.
   */
  @Nullable
  public R applySubnode( @Nonnull Node jcrNode, @Nonnull String nodeName, @Nonnull R destination ) {
    try {
      if( jcrNode.hasNode( nodeName ) ) {
        destination = apply( jcrNode.getNode( nodeName ), destination );
      }
      return destination;
    } catch( Exception ex ) {
      throw JcxException.wrap( ex );
    }
  }
  
} /* ENDCLASS */
