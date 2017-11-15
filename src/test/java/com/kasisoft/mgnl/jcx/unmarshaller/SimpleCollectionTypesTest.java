package com.kasisoft.mgnl.jcx.unmarshaller;

import com.kasisoft.libs.common.xml.adapters.*;

import com.kasisoft.libs.common.annotation.*;
import com.kasisoft.mgnl.jcx.*;
import com.kasisoft.mgnl.versionhandler.*;

import org.testng.annotations.*;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import java.util.*;
import java.util.List;

import java.awt.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class SimpleCollectionTypesTest extends AbstractJcxUnmarshaller {

  @Test
  public void basic() throws Exception {
    
    // setup the data
    SimpleCollectionTypes expected = new SimpleCollectionTypes();
    expected.setStrings( Arrays.asList( "A", "B", "C" ) );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "simpleCollectionTypes" )
        .property( "strings0" , "A" )
        .property( "strings1" , "B" )
        .property( "strings2" , "C" )
      .sEnd();
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, SimpleCollectionTypes.class, "simpleCollectionTypes", expected );

  }

  @Test
  public void withAdapter() throws Exception {
    
    // setup the data
    SimpleCollectionTypesWithAdapter expected = new SimpleCollectionTypesWithAdapter();
    expected.setColors( Arrays.asList( Color.red, Color.green, Color.blue ) );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "simpleCollectionTypesWithAdapter" )
        .property( "colors0" , "red"   )
        .property( "colors1" , "green" )
        .property( "colors2" , "blue"  )
      .sEnd();
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, SimpleCollectionTypesWithAdapter.class, "simpleCollectionTypesWithAdapter", expected );

  }

  @Getter @Setter
  @ToString
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class SimpleCollectionTypes {
    
    @XmlAttribute
    @GenericsType(String.class)
    List<String>      strings;

  } /* ENCLASS */

  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class SimpleCollectionTypesWithAdapter {
    
    @XmlAttribute
    @XmlJavaTypeAdapter(XmlColorAdapter.class)
    @GenericsType(Color.class)
    List<Color>      colors;

  } /* ENCLASS */

  public static class XmlColorAdapter extends XmlToTypeAdapter {

    public XmlColorAdapter() {
      super( new ColorAdapter() );
    }
    
  } /* ENDCLASS */

} /* ENDCLASS */
