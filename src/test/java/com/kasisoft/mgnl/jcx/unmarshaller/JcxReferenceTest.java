package com.kasisoft.mgnl.jcx.unmarshaller;

import com.kasisoft.mgnl.jcx.*;
import com.kasisoft.mgnl.jcx.JcxReference.*;
import com.kasisoft.mgnl.versionhandler.*;

import org.testng.annotations.*;

import javax.xml.bind.annotation.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class JcxReferenceTest extends AbstractJcxUnmarshaller {

  @Test
  public void withoutRefProperty() throws Exception {
    
    // setup the data
    ReferenceWithoutRefProperty expected1 = new ReferenceWithoutRefProperty();
    expected1.setTitle( "lila" );

    ReferenceWithoutRefProperty expected2 = new ReferenceWithoutRefProperty();
    expected2.setTitle( "zoidberg" );

    ReferenceWithoutRefProperty expected3 = new ReferenceWithoutRefProperty();
    expected3.setTitle( "zoidberg" );

    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "referenceWithoutRefProperty1" )
        .property( "title" , expected1.getTitle() )
      .sEnd()
      .sContentNode( "referenceWithoutRefProperty2" )
        .property( "title" , expected2.getTitle() )
        .property( "target", targetedNodeUuid1 )
      .sEnd()
      .sContentNode( "referenceWithoutRefProperty3" )
        .property( "target", targetedNodeUuid1 )
      .sEnd();
      ;
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    // the title comes from the content node
    assertCreations( unmarshaller, ReferenceWithoutRefProperty.class, "referenceWithoutRefProperty1", expected1 );

    // the target has a title, but the title of current node overrules it (JcxReference.before = true)
    assertCreations( unmarshaller, ReferenceWithoutRefProperty.class, "referenceWithoutRefProperty2", expected2 );

    // the current node has no title, so use the title from the target
    assertCreations( unmarshaller, ReferenceWithoutRefProperty.class, "referenceWithoutRefProperty3", expected3 );
    
  }

  @Test
  public void withRefProperty() throws Exception {
    
    // setup the data
    ReferenceWithRefProperty expected1 = new ReferenceWithRefProperty();
    expected1.setTitle( "lila" );

    ReferenceWithRefProperty expected2 = new ReferenceWithRefProperty();
    expected2.setTitle( "zoidberg" );

    ReferenceWithRefProperty expected3 = new ReferenceWithRefProperty();
    expected3.setTitle( "zoidberg" );

    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "referenceWithRefProperty1" )
        .property( "title" , expected1.getTitle() )
      .sEnd()
      .sContentNode( "referenceWithRefProperty2" )
        .property( "title" , expected2.getTitle() )
        .property( "pointer", targetedNodeUuid1 )
      .sEnd()
      .sContentNode( "referenceWithRefProperty3" )
        .property( "pointer", targetedNodeUuid1 )
      .sEnd();
      ;
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    // the title comes from the content node
    assertCreations( unmarshaller, ReferenceWithRefProperty.class, "referenceWithRefProperty1", expected1 );

    // the target has a title, but the title of current node overrules it (JcxReference.before = true)
    assertCreations( unmarshaller, ReferenceWithRefProperty.class, "referenceWithRefProperty2", expected2 );

    // the current node has no title, so use the title from the target
    assertCreations( unmarshaller, ReferenceWithRefProperty.class, "referenceWithRefProperty3", expected3 );
    
  }

  @Test
  public void withoutRefPropertyAfter() throws Exception {
    
    // setup the data
    ReferenceWithoutRefPropertyAfter expected1 = new ReferenceWithoutRefPropertyAfter();
    expected1.setTitle( "lila" );

    ReferenceWithoutRefPropertyAfter expected2 = new ReferenceWithoutRefPropertyAfter();
    expected2.setTitle( "zoidberg" );

    ReferenceWithoutRefPropertyAfter expected3 = new ReferenceWithoutRefPropertyAfter();
    expected3.setTitle( "zoidberg" );

    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "referenceWithoutRefPropertyAfter1" )
        .property( "title" , expected1.getTitle() )
      .sEnd()
      .sContentNode( "referenceWithoutRefPropertyAfter2" )
        .property( "title" , expected2.getTitle() )
        .property( "target", targetedNodeUuid1 )
      .sEnd()
      .sContentNode( "referenceWithoutRefPropertyAfter3" )
        .property( "target", targetedNodeUuid1 )
      .sEnd();
      ;
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    // the title comes from the content node
    assertCreations( unmarshaller, ReferenceWithoutRefPropertyAfter.class, "referenceWithoutRefPropertyAfter1", expected1 );

    // the target has a title which overrules the title of the current node (JcxReference.before = false)
    assertCreations( unmarshaller, ReferenceWithoutRefPropertyAfter.class, "referenceWithoutRefPropertyAfter2", expected2 );

    // the current node has no title, so use the title from the target
    assertCreations( unmarshaller, ReferenceWithoutRefPropertyAfter.class, "referenceWithoutRefPropertyAfter3", expected3 );

  }
  
  @Getter @Setter @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @JcxReference(value = "wuppiWs", useOriginalNode = UseOriginalNode.before)
  public static final class ReferenceWithoutRefProperty {
    
    @XmlAttribute
    String      title;

    // default name used for the referring property
    String      target;

  } /* ENCLASS */
  
  @Getter @Setter @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @JcxReference(value = "wuppiWs", useOriginalNode = UseOriginalNode.after)
  public static final class ReferenceWithoutRefPropertyAfter {
    
    @XmlAttribute
    String      title;

    // default name used for the referring property
    String      target;

  } /* ENCLASS */
  
  @Getter @Setter @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @JcxReference(value = "wuppiWs", property = "pointer", useOriginalNode = UseOriginalNode.before)
  public static final class ReferenceWithRefProperty {
    
    @XmlAttribute
    String      title;

    // default name used for the referring property
    String      pointer;

  } /* ENCLASS */
  
} /* ENDCLASS */
