package com.kasisoft.mgnl.jcx.unmarshaller;

import com.kasisoft.libs.common.xml.adapters.*;

import com.kasisoft.mgnl.jcx.*;
import com.kasisoft.mgnl.versionhandler.*;

import org.testng.annotations.*;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import java.awt.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class ComplexObjectTypesTest extends AbstractJcxUnmarshaller {

  @Test
  public void complexObjectType() throws Exception {
    
    // setup the data
    WithXmlAdapter withXmlAdapter = new WithXmlAdapter();
    withXmlAdapter.setVarColor( Color.yellow );
    
    ComplexObjectType expected1 = new ComplexObjectType();
    expected1.setWithXmlAdapter( withXmlAdapter );

    ComplexObjectType expected2 = new ComplexObjectType();

    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "complexObjectType1" )
        .sContentNode( "withXmlAdapter" )
          .property( "varColor" , "yellow" )
        .sEnd()
      .sEnd()
      .sContentNode( "complexObjectType2" )
        // no content so the inner object must stay null
      .sEnd()
      ;
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, ComplexObjectType.class, "complexObjectType1", expected1 );
    assertCreations( unmarshaller, ComplexObjectType.class, "complexObjectType2", expected2 );

  }

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
