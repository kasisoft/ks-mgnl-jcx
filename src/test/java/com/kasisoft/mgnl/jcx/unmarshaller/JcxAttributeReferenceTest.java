package com.kasisoft.mgnl.jcx.unmarshaller;

import com.kasisoft.mgnl.jcx.*;
import com.kasisoft.mgnl.versionhandler.*;

import org.testng.annotations.*;

import javax.xml.bind.annotation.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class JcxAttributeReferenceTest extends AbstractJcxUnmarshaller {

  @Test
  public void withRefProperty() throws Exception {
    
    // setup the data
    PojoType                 pojotype1 = new PojoType();
    pojotype1.setTitle( "zoidberg" );
    pojotype1.setValue( 120 );
    ReferenceWithRefProperty expected1 = new ReferenceWithRefProperty();
    expected1.setPojo( pojotype1 );
    expected1.setPointer( targetedNodeUuid1 );

    PojoType                 pojotype2 = new PojoType();
    pojotype2.setTitle( "amy" );
    pojotype2.setValue( 30 );
    ReferenceWithRefProperty expected2 = new ReferenceWithRefProperty();
    expected2.setPojo( pojotype2 );
    expected2.setPointer( targetedNodeUuid2 );

    PojoType                 pojotype3 = new PojoType();
    pojotype3.setTitle( "lila" );
    pojotype3.setValue( 40 );
    ReferenceWithRefProperty expected3 = new ReferenceWithRefProperty();
    expected3.setPojo( pojotype3 );
    expected3.setPointer( targetedNodeUuid3 );

    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "referenceWithRefProperty1" )
        .property( "pointer", targetedNodeUuid1 )
      .sEnd()
      .sContentNode( "referenceWithRefProperty2" )
        .property( "title" , "amy" )
        .property( "pointer", targetedNodeUuid2 )
      .sEnd()
      .sContentNode( "referenceWithRefProperty3" )
        .property( "pointer", targetedNodeUuid3 )
        .property( "value"  , 40 )
      .sEnd();
      ;
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    // the title/value comes from the content node
    assertCreations( unmarshaller, ReferenceWithRefProperty.class, "referenceWithRefProperty1", expected1 );

    // the target has a title, but the title of current node overrules it (JcxReference.before = true)
    assertCreations( unmarshaller, ReferenceWithRefProperty.class, "referenceWithRefProperty2", expected2 );

    // the current node has no title, so use the title from the target
    assertCreations( unmarshaller, ReferenceWithRefProperty.class, "referenceWithRefProperty3", expected3 );
    
  }

  @Test
  public void withRefPropertyAfter() throws Exception {
    
    // setup the data
    PojoType                 pojotype1 = new PojoType();
    pojotype1.setTitle( "zoidberg" );
    pojotype1.setValue( 120 );
    ReferenceWithRefPropertyAfter expected1 = new ReferenceWithRefPropertyAfter();
    expected1.setPojo( pojotype1 );
    expected1.setPointer( targetedNodeUuid1 );

    PojoType                 pojotype2 = new PojoType();
    pojotype2.setTitle( "fry" );
    pojotype2.setValue( 30 );
    ReferenceWithRefPropertyAfter expected2 = new ReferenceWithRefPropertyAfter();
    expected2.setPojo( pojotype2 );
    expected2.setPointer( targetedNodeUuid2 );

    PojoType                 pojotype3 = new PojoType();
    pojotype3.setTitle( "lila" );
    pojotype3.setValue( 70 );
    ReferenceWithRefPropertyAfter expected3 = new ReferenceWithRefPropertyAfter();
    expected3.setPojo( pojotype3 );
    expected3.setPointer( targetedNodeUuid3 );

    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "referenceWithRefProperty1" )
        .property( "pointer", targetedNodeUuid1 )
      .sEnd()
      .sContentNode( "referenceWithRefProperty2" )
        .property( "title" , "amy" )
        .property( "pointer", targetedNodeUuid2 )
      .sEnd()
      .sContentNode( "referenceWithRefProperty3" )
        .property( "pointer", targetedNodeUuid3 )
        .property( "value"  , 40 )
      .sEnd();
      ;
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    // the title/value comes from the content node
    assertCreations( unmarshaller, ReferenceWithRefPropertyAfter.class, "referenceWithRefProperty1", expected1 );

    // the target has a title, but the title of current node overrules it (JcxReference.before = true)
    assertCreations( unmarshaller, ReferenceWithRefPropertyAfter.class, "referenceWithRefProperty2", expected2 );

    // the current node has no title, so use the title from the target
    assertCreations( unmarshaller, ReferenceWithRefPropertyAfter.class, "referenceWithRefProperty3", expected3 );
    
  }
  
  @Getter @Setter
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class ReferenceWithRefProperty {
    
    @JcxReference(value = "wuppiWs", property = "pointer")
    PojoType    pojo;

    // default name used for the referring property
    @XmlAttribute
    String      pointer;

  } /* ENCLASS */
  
  @Getter @Setter
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class ReferenceWithRefPropertyAfter {
    
    @JcxReference(value = "wuppiWs", property = "pointer", before = false)
    PojoType    pojo;

    // default name used for the referring property
    @XmlAttribute
    String      pointer;

  } /* ENCLASS */

  @Getter @Setter
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class PojoType {
    
    @XmlAttribute
    String      title;
    
    @XmlAttribute
    int         value;
    
  } /* ENDCLASS */
  
} /* ENDCLASS */
