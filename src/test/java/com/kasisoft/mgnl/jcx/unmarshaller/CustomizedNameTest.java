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
public class CustomizedNameTest extends AbstractJcxUnmarshaller {

  @Test
  public void customName() throws Exception {
    
    // setup the data
    CustomName expected = new CustomName();
    expected.setVarString( "dada" );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "customName" )
        .property( "oopsi" , expected.getVarString () )
      .sEnd();
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, CustomName.class, "customName", expected );

  }

  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class CustomName {
    
    @XmlAttribute(name = "oopsi")
    String      varString;

  } /* ENCLASS */

} /* ENDCLASS */
