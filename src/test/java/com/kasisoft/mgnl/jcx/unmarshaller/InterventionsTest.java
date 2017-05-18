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
public class InterventionsTest extends AbstractJcxUnmarshaller {

  @Test
  public void complexCollectionTypes() throws Exception {
    
    WithXmlAdapter withXmlAdapter1 = new WithXmlAdapter();
    withXmlAdapter1.setVarColor( Color.yellow );
    withXmlAdapter1.setCounter( 17 );

    WithXmlAdapter withXmlAdapter2 = new WithXmlAdapter();
    withXmlAdapter2.setVarColor( Color.blue );
    withXmlAdapter2.setCounter( 17 );

    WithXmlAdapter withXmlAdapter3 = new WithXmlAdapter();
    withXmlAdapter3.setVarColor( Color.red );
    withXmlAdapter3.setCounter( 17 );

    ComplexObjectType complex1 = new ComplexObjectType();
    complex1.setWithXmlAdapter( withXmlAdapter1 );
    
    ComplexObjectType complex2 = new ComplexObjectType();
    complex2.setWithXmlAdapter( withXmlAdapter2 );
    
    ComplexObjectType complex3 = new ComplexObjectType();
    complex3.setWithXmlAdapter( withXmlAdapter3 );
    
    // setup the data
    ComplexCollectionTypes expected = new ComplexCollectionTypes();
    expected.setObjects( Arrays.asList( complex1, complex2, complex3 ) );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "complexCollectionTypes" )
        .sContentNode( "objects1" )
          .sContentNode( "withXmlAdapter" )
            .property( "varColor" , "yellow" )
          .sEnd()
        .sEnd()
        .sContentNode( "objects2" )
          .sContentNode( "withXmlAdapter" )
            .property( "varColor" , "blue" )
          .sEnd()
        .sEnd()
        .sContentNode( "objects3" )
          .sContentNode( "withXmlAdapter" )
            .property( "varColor" , "red" )
          .sEnd()
        .sEnd()
      .sEnd();
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    unmarshaller.registerIntervention( ComplexObjectType.class, WithXmlAdapter.class, this::configureWithXmlAdapter );
    
    assertCreations( unmarshaller, ComplexCollectionTypes.class, "complexCollectionTypes", expected );

  }
  
  private void configureWithXmlAdapter( WithXmlAdapter xmlAdapter ) {
    xmlAdapter.setCounter( 17 );
  }
  
  @ToString
  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class ComplexCollectionTypes {
    
    @XmlElement
    @GenericsType(ComplexObjectType.class)
    List<ComplexObjectType>    objects;

  } /* ENCLASS */

  @ToString
  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class ComplexObjectType {

    @XmlElement
    WithXmlAdapter    withXmlAdapter;
    
  } /* ENCLASS */

  @ToString
  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class WithXmlAdapter {
    
    @XmlAttribute
    @XmlJavaTypeAdapter(XmlColorAdapter.class)
    Color   varColor;
    
    @XmlTransient
    int     counter;

  } /* ENCLASS */

  public static class XmlColorAdapter extends XmlToTypeAdapter {

    public XmlColorAdapter() {
      super( new ColorAdapter() );
    }
    
  } /* ENDCLASS */
  
} /* ENDCLASS */
