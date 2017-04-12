package com.kasisoft.mgnl.jcx.unmarshaller;

import com.kasisoft.libs.common.text.*;

import com.kasisoft.mgnl.jcx.*;
import com.kasisoft.mgnl.versionhandler.*;

import org.testng.annotations.*;

import javax.xml.bind.annotation.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class IContentTest extends AbstractJcxUnmarshaller {

  @Test
  public void content() throws Exception {
    
    // setup the data
    ContentObject expected = new ContentObject();
    expected.setVarString( "dodo" );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "content1" )
      .sEnd()
      .sContentNode( "content2" )
        .property( "varString", "" )
      .sEnd()
      .sContentNode( "content3" )
        .property( "varString", expected.getVarString() )
      .sEnd()
      ;
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreationsNull( unmarshaller, ContentObject.class, "content1" );
    assertCreationsNull( unmarshaller, ContentObject.class, "content2" );
    assertCreations( unmarshaller, ContentObject.class, "content3", expected );

  }
  
  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class ContentObject implements IContent {
    
    @XmlAttribute
    String      varString;

    @Override
    public boolean hasContent() {
      return StringFunctions.cleanup( varString ) != null;
    }

  } /* ENCLASS */

} /* ENDCLASS */
