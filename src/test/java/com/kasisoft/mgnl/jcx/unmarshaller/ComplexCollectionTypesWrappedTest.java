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
public class ComplexCollectionTypesWrappedTest extends AbstractJcxUnmarshaller {

  @Test(enabled = false)
  public void complexCollectionTypesWrapped() throws Exception {
    
    WithXmlAdapter withXmlAdapter1 = new WithXmlAdapter();
    withXmlAdapter1.setVarColor( Color.yellow );

    WithXmlAdapter withXmlAdapter2 = new WithXmlAdapter();
    withXmlAdapter2.setVarColor( Color.blue );

    WithXmlAdapter withXmlAdapter3 = new WithXmlAdapter();
    withXmlAdapter3.setVarColor( Color.red );

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
      .sContentNode( "complexCollectionTypesWrapped" )
        .sContentNode( "objects" )
          .sContentNode( "complexObjectType1" )
            .sContentNode( "withXmlAdapter" )
              .property( "varColor" , "yellow" )
            .sEnd()
          .sEnd()
          .sContentNode( "complexObjectType2" )
            .sContentNode( "withXmlAdapter" )
              .property( "varColor" , "blue" )
            .sEnd()
          .sEnd()
          .sContentNode( "complexObjectType3" )
            .sContentNode( "withXmlAdapter" )
              .property( "varColor" , "red" )
            .sEnd()
          .sEnd()
        .sEnd()
      .sEnd();
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, ComplexCollectionTypes.class, "complexCollectionTypesWrapped", expected );

  }

  @Test
  public void complexCollectionTypesWrappedCustomName() throws Exception {
    
    WithXmlAdapter withXmlAdapter1 = new WithXmlAdapter();
    withXmlAdapter1.setVarColor( Color.yellow );

    WithXmlAdapter withXmlAdapter2 = new WithXmlAdapter();
    withXmlAdapter2.setVarColor( Color.blue );

    WithXmlAdapter withXmlAdapter3 = new WithXmlAdapter();
    withXmlAdapter3.setVarColor( Color.red );

    ComplexObjectType complex1 = new ComplexObjectType();
    complex1.setWithXmlAdapter( withXmlAdapter1 );
    
    ComplexObjectType complex2 = new ComplexObjectType();
    complex2.setWithXmlAdapter( withXmlAdapter2 );
    
    ComplexObjectType complex3 = new ComplexObjectType();
    complex3.setWithXmlAdapter( withXmlAdapter3 );
    
    // setup the data
    ComplexCollectionTypesCustomName expected = new ComplexCollectionTypesCustomName();
    expected.setObjects( Arrays.asList( complex1, complex2, complex3 ) );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "complexCollectionTypesWrappedCustomName" )
        .sContentNode( "crow" )
          .sContentNode( "complexObjectType1" )
            .sContentNode( "withXmlAdapter" )
              .property( "varColor" , "yellow" )
            .sEnd()
          .sEnd()
          .sContentNode( "complexObjectType2" )
            .sContentNode( "withXmlAdapter" )
              .property( "varColor" , "blue" )
            .sEnd()
          .sEnd()
          .sContentNode( "complexObjectType3" )
            .sContentNode( "withXmlAdapter" )
              .property( "varColor" , "red" )
            .sEnd()
          .sEnd()
        .sEnd()
      .sEnd();
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, ComplexCollectionTypesCustomName.class, "complexCollectionTypesWrappedCustomName", expected );

  }
  
  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class ComplexCollectionTypesCustomName {
    
    @XmlElementWrapper(name = "crow")
    @XmlElement
    @GenericsType(ComplexObjectType.class)
    List<ComplexObjectType>    objects;

  } /* ENCLASS */
  
  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class ComplexCollectionTypes {
    
    @XmlElementWrapper
    @XmlElement
    @GenericsType(ComplexObjectType.class)
    List<ComplexObjectType>    objects;

  } /* ENCLASS */

  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class ComplexObjectType {

    @XmlElement
    WithXmlAdapter    withXmlAdapter;
    
  } /* ENCLASS */

  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class WithXmlAdapter {
    
    @XmlAttribute
    @XmlJavaTypeAdapter(XmlColorAdapter.class)
    Color   varColor;

  } /* ENCLASS */

  public static class XmlColorAdapter extends XmlToTypeAdapter {

    public XmlColorAdapter() {
      super( new ColorAdapter() );
    }
    
  } /* ENDCLASS */
  
} /* ENDCLASS */
