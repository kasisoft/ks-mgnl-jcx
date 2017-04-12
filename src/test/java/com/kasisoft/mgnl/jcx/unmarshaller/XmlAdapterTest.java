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
public class XmlAdapterTest extends AbstractJcxUnmarshaller {

  @Test
  public void withXmlAdapter() throws Exception {
    
    // setup the data
    WithXmlAdapter expected = new WithXmlAdapter();
    expected.setVarColor( Color.yellow );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "withXmlAdapter" )
        .property( "varColor" , "yellow" )
      .sEnd();
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, WithXmlAdapter.class, "withXmlAdapter", expected );
    
  }

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
