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
public class SimplePrimitiveTypesTest extends AbstractJcxUnmarshaller {

  @Test
  public void simplePrimitiveTypes() throws Exception {
    
    // setup the data
    SimplePrimitiveTypes expected = new SimplePrimitiveTypes();
    expected.setVarBoolean( true );
    expected.setVarChar( 'a' );
    expected.setVarByte( (byte) 12 );
    expected.setVarShort( (short) 456 );
    expected.setVarInt( (int) 47988 );
    expected.setVarLong( (long) 8819292999112L );
    expected.setVarFloat( (float) 188.2 );
    expected.setVarDouble( -49992727.91 );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "simplePrimitiveTypes" )
        .property( "varBoolean" , expected.isVarBoolean () )
        .property( "varChar"    , expected.getVarChar   () )
        .property( "varByte"    , expected.getVarByte   () )
        .property( "varShort"   , expected.getVarShort  () )
        .property( "varInt"     , expected.getVarInt    () )
        .property( "varLong"    , expected.getVarLong   () )
        .property( "varFloat"   , expected.getVarFloat  () )
        .property( "varDouble"  , expected.getVarDouble () )
      .sEnd();
    
    tb.build( new MockNodeProducer( biboSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, SimplePrimitiveTypes.class, "simplePrimitiveTypes", expected );

  }

  /**
   * @author daniel.kasmeroglu@kasisoft.net
   */
  @Getter @Setter
  @EqualsAndHashCode
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static final class SimplePrimitiveTypes {
    
    @XmlAttribute
    boolean     varBoolean;
    
    @XmlAttribute
    char        varChar;
    
    @XmlAttribute
    byte        varByte;
    
    @XmlAttribute
    short       varShort;
    
    @XmlAttribute
    int         varInt;
    
    @XmlAttribute
    long        varLong;
    
    @XmlAttribute
    float       varFloat;
    
    @XmlAttribute
    double      varDouble;
    
    @XmlAttribute
    int         willStayTheSameAsThereIsNoJcrProperty = 34;

  } /* ENCLASS */

} /* ENDCLASS */
