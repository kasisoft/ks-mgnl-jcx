package com.kasisoft.mgnl.jcx.unmarshaller;

import static org.testng.Assert.*;

import com.kasisoft.libs.common.xml.adapters.*;

import com.kasisoft.mgnl.jcx.*;
import com.kasisoft.mgnl.versionhandler.*;

import org.testng.annotations.*;

import javax.jcr.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

import java.awt.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class RequiredElementTest extends AbstractJcxUnmarshaller {

  @Test(expectedExceptions = RequiredPropertyException.class)
  public void complexObjectType() throws Exception {
    
    // setup the data
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "requiredElement" )
        // no content so the inner object must stay null
      .sEnd()
      ;
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    unmarshaller.setUnsatisfiedRequireHandler( this::requireHandler );
    
    unmarshaller.createCreator( ComplexObjectType.class ).apply( biboSession.getNode( "/requiredElement" ) );
    fail();

  }
  
  private void requireHandler( Node node, String type, String property ) {
    throw new RequiredPropertyException();
  }

  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class ComplexObjectType {

    @XmlElement(required = true)
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

  public static class RequiredPropertyException extends JcxException { 
  } /* ENDCLASS */
  
} /* ENDCLASS */
