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
public class IPostProcessorTest extends AbstractJcxUnmarshaller {

  @Test
  public void postProcessor() throws Exception {
    
    // setup the data
    PostProcessObject expected = new PostProcessObject();
    expected.setStrA( "A" );
    expected.setStrB( "B" );
    expected.setStrC( "AB" );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "postProcessor" )
        .property( "strA" , "A" )
        .property( "strB" , "B" )
      .sEnd();
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, PostProcessObject.class, "postProcessor", expected );

  }

  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class PostProcessObject implements IPostProcessor {
    
    @XmlAttribute
    String      strA;
    
    @XmlAttribute
    String      strB;
    
    String      strC;

    @Override
    public void postprocess() {
      strC = strA + strB;
    }

  } /* ENCLASS */

} /* ENDCLASS */
