package com.kasisoft.mgnl.jcx;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.MockitoAnnotations.*;
import static org.testng.Assert.*;

import info.magnolia.test.mock.jcr.*;

import info.magnolia.context.*;

import info.magnolia.jcr.util.*;

import com.kasisoft.mgnl.jcx.model.*;
import com.kasisoft.mgnl.versionhandler.*;

import org.testng.annotations.*;

import java.util.*;

import java.awt.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class JcxUnmarshallerTest {

  MockSession   jcrSession;
  String        targetedNodeUuid;
  
  @BeforeClass
  public void setup() throws Exception{
    
    initMocks( this );
    
    ExtendedMockWebContext.builder()
      .workspace( "bibo" )
      .workspace( "wuppiWorkspace" )
      .build()
      .install();
    
    jcrSession = (MockSession) MgnlContext.getJCRSession( "bibo" );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "targetedNode" )
        .property( "title", "zoidberg" )
      .sEnd();
    
    tb.build( new MockNodeProducer( (MockSession) MgnlContext.getJCRSession( "wuppiWorkspace" ) ) );
    
    targetedNodeUuid = SessionUtil.getNode( MgnlContext.getJCRSession( "wuppiWorkspace" ), "/targetedNode" ).getIdentifier();
    
  }
  
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
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, SimplePrimitiveTypes.class, "simplePrimitiveTypes", expected );

  }

  @Test
  public void simpleObjectTypes() throws Exception {
    
    // setup the data
    SimpleObjectTypes expected = new SimpleObjectTypes();
    expected.setVarBoolean( true );
    expected.setVarChar( 'a' );
    expected.setVarByte( (byte) 12 );
    expected.setVarShort( (short) 456 );
    expected.setVarInt( (int) 47988 );
    expected.setVarLong( (long) 8819292999112L );
    expected.setVarFloat( (float) 188.2 );
    expected.setVarDouble( -49992727.91 );
    expected.setVarString( "dodo" );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "simplePrimitiveTypes" )
        .property( "varBoolean" , expected.getVarBoolean () )
        .property( "varChar"    , expected.getVarChar    () )
        .property( "varByte"    , expected.getVarByte    () )
        .property( "varShort"   , expected.getVarShort   () )
        .property( "varInt"     , expected.getVarInt     () )
        .property( "varLong"    , expected.getVarLong    () )
        .property( "varFloat"   , expected.getVarFloat   () )
        .property( "varDouble"  , expected.getVarDouble  () )
        .property( "varString"  , expected.getVarString  () )
      .sEnd();
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, SimpleObjectTypes.class, "simplePrimitiveTypes", expected );

  }

  @Test
  public void fieldNameGenerator() throws Exception {
    
    // setup the data
    SimpleObjectTypes expected = new SimpleObjectTypes();
    expected.setVarBoolean( true );
    expected.setVarChar( 'a' );
    expected.setVarByte( (byte) 12 );
    expected.setVarShort( (short) 456 );
    expected.setVarInt( (int) 47988 );
    expected.setVarLong( (long) 8819292999112L );
    expected.setVarFloat( (float) 188.2 );
    expected.setVarDouble( -49992727.91 );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "fieldNameGenerator" )
        .property( "Boolean" , expected.getVarBoolean () )
        .property( "Char"    , expected.getVarChar    () )
        .property( "Byte"    , expected.getVarByte    () )
        .property( "Short"   , expected.getVarShort   () )
        .property( "Int"     , expected.getVarInt     () )
        .property( "Long"    , expected.getVarLong    () )
        .property( "Float"   , expected.getVarFloat   () )
        .property( "Double"  , expected.getVarDouble  () )
      .sEnd();
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    unmarshaller.setFieldNameGenerator( $ -> $.substring( "var".length() ) );
    
    assertCreations( unmarshaller, SimpleObjectTypes.class, "fieldNameGenerator", expected );

  }

  @Test
  public void customName() throws Exception {
    
    // setup the data
    CustomName expected = new CustomName();
    expected.setVarString( "dada" );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "customName" )
        .property( "oopsi" , expected.getVarString () )
      .sEnd();
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, CustomName.class, "customName", expected );

  }

  @Test
  public void withXmlAdapter() throws Exception {
    
    // setup the data
    WithXmlAdapter expected = new WithXmlAdapter();
    expected.setVarColor( Color.yellow );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "withXmlAdapter" )
        .property( "varColor" , "yellow" )
      .sEnd();
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, WithXmlAdapter.class, "withXmlAdapter", expected );
    
  }

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
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, ComplexObjectType.class, "complexObjectType1", expected1 );
    assertCreations( unmarshaller, ComplexObjectType.class, "complexObjectType2", expected2 );

  }

  @Test
  public void referenceWithoutRefProperty() throws Exception {
    
    // setup the data
    ReferenceWithoutRefProperty expected1 = new ReferenceWithoutRefProperty();
    expected1.setTitle( "lila" );

    ReferenceWithoutRefProperty expected2 = new ReferenceWithoutRefProperty();
    expected2.setTitle( "fry" );

    ReferenceWithoutRefProperty expected3 = new ReferenceWithoutRefProperty();
    expected3.setTitle( "zoidberg" );

    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "referenceWithoutRefProperty1" )
        .property( "title" , expected1.getTitle() )
      .sEnd()
      .sContentNode( "referenceWithoutRefProperty2" )
        .property( "title" , expected2.getTitle() )
        .property( "target", targetedNodeUuid )
      .sEnd()
      .sContentNode( "referenceWithoutRefProperty3" )
        .property( "target", targetedNodeUuid )
      .sEnd();
      ;
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    // the title comes from the content node
    assertCreations( unmarshaller, ReferenceWithoutRefProperty.class, "referenceWithoutRefProperty1", expected1 );

    // the target has a title, but the title of current node overrules it (JcxReference.before = true)
    assertCreations( unmarshaller, ReferenceWithoutRefProperty.class, "referenceWithoutRefProperty2", expected2 );

    // the current node has no title, so use the title from the target
    assertCreations( unmarshaller, ReferenceWithoutRefProperty.class, "referenceWithoutRefProperty3", expected3 );
    
  }

  @Test
  public void referenceWithRefProperty() throws Exception {
    
    // setup the data
    ReferenceWithRefProperty expected1 = new ReferenceWithRefProperty();
    expected1.setTitle( "lila" );

    ReferenceWithRefProperty expected2 = new ReferenceWithRefProperty();
    expected2.setTitle( "fry" );

    ReferenceWithRefProperty expected3 = new ReferenceWithRefProperty();
    expected3.setTitle( "zoidberg" );

    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "referenceWithRefProperty1" )
        .property( "title" , expected1.getTitle() )
      .sEnd()
      .sContentNode( "referenceWithRefProperty2" )
        .property( "title" , expected2.getTitle() )
        .property( "pointer", targetedNodeUuid )
      .sEnd()
      .sContentNode( "referenceWithRefProperty3" )
        .property( "pointer", targetedNodeUuid )
      .sEnd();
      ;
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    // the title comes from the content node
    assertCreations( unmarshaller, ReferenceWithRefProperty.class, "referenceWithRefProperty1", expected1 );

    // the target has a title, but the title of current node overrules it (JcxReference.before = true)
    assertCreations( unmarshaller, ReferenceWithRefProperty.class, "referenceWithRefProperty2", expected2 );

    // the current node has no title, so use the title from the target
    assertCreations( unmarshaller, ReferenceWithRefProperty.class, "referenceWithRefProperty3", expected3 );
    
  }

  @Test
  public void referenceWithoutRefPropertyAfter() throws Exception {
    
    // setup the data
    ReferenceWithoutRefPropertyAfter expected1 = new ReferenceWithoutRefPropertyAfter();
    expected1.setTitle( "lila" );

    ReferenceWithoutRefPropertyAfter expected2 = new ReferenceWithoutRefPropertyAfter();
    expected2.setTitle( "zoidberg" );

    ReferenceWithoutRefPropertyAfter expected3 = new ReferenceWithoutRefPropertyAfter();
    expected3.setTitle( "zoidberg" );

    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "referenceWithoutRefPropertyAfter1" )
        .property( "title" , expected1.getTitle() )
      .sEnd()
      .sContentNode( "referenceWithoutRefPropertyAfter2" )
        .property( "title" , expected2.getTitle() )
        .property( "target", targetedNodeUuid )
      .sEnd()
      .sContentNode( "referenceWithoutRefPropertyAfter3" )
        .property( "target", targetedNodeUuid )
      .sEnd();
      ;
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    // the title comes from the content node
    assertCreations( unmarshaller, ReferenceWithoutRefPropertyAfter.class, "referenceWithoutRefPropertyAfter1", expected1 );

    // the target has a title which overrules the title of the current node (JcxReference.before = false)
    assertCreations( unmarshaller, ReferenceWithoutRefPropertyAfter.class, "referenceWithoutRefPropertyAfter2", expected2 );

    // the current node has no title, so use the title from the target
    assertCreations( unmarshaller, ReferenceWithoutRefPropertyAfter.class, "referenceWithoutRefPropertyAfter3", expected3 );

  }

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
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreationsNull( unmarshaller, ContentObject.class, "content1" );
    assertCreationsNull( unmarshaller, ContentObject.class, "content2" );
    assertCreations( unmarshaller, ContentObject.class, "content3", expected );

  }
  
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
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, PostProcessObject.class, "postProcessor", expected );

  }

  @Test
  public void simpleCollectionTypes() throws Exception {
    
    // setup the data
    SimpleCollectionTypes expected = new SimpleCollectionTypes();
    expected.setStrings( Arrays.asList( "A", "B", "C" ) );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "simpleCollectionTypes" )
        .sContentNode( "strings" )
          .property( "0" , "A" )
          .property( "1" , "B" )
          .property( "2" , "C" )
        .sEnd()
      .sEnd();
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, SimpleCollectionTypes.class, "simpleCollectionTypes", expected );

  }

  @Test
  public void simpleCollectionTypesWithAdapter() throws Exception {
    
    // setup the data
    SimpleCollectionTypesWithAdapter expected = new SimpleCollectionTypesWithAdapter();
    expected.setColors( Arrays.asList( Color.red, Color.green, Color.blue ) );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "simpleCollectionTypesWithAdapter" )
        .sContentNode( "colors" )
          .property( "0" , "red" )
          .property( "1" , "green" )
          .property( "2" , "blue" )
        .sEnd()
      .sEnd();
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, SimpleCollectionTypesWithAdapter.class, "simpleCollectionTypesWithAdapter", expected );

  }

  @Test
  public void complexCollectionTypes() throws Exception {
    
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
      .sContentNode( "complexCollectionTypes" )
        .sContentNode( "objects" )
          .sContentNode( "0" )
            .sContentNode( "withXmlAdapter" )
              .property( "varColor" , "yellow" )
            .sEnd()
          .sEnd()
          .sContentNode( "2" )
            .sContentNode( "withXmlAdapter" )
              .property( "varColor" , "blue" )
            .sEnd()
          .sEnd()
          .sContentNode( "3" )
            .sContentNode( "withXmlAdapter" )
              .property( "varColor" , "red" )
            .sEnd()
          .sEnd()
        .sEnd()
      .sEnd();
    
    tb.build( new MockNodeProducer( jcrSession ) );

    // run the tests
    JcxUnmarshaller unmarshaller = new JcxUnmarshaller();
    
    assertCreations( unmarshaller, ComplexCollectionTypes.class, "complexCollectionTypes", expected );

  }

  private <T> void assertCreations( JcxUnmarshaller unmarshaller, Class<T> type, String name, T expected ) throws Exception {
    
    T read1 = unmarshaller.createCreator( type ).apply( jcrSession.getNode( "/" + name ) );
    assertNotNull( read1 );
    assertThat( read1, is( expected ) );
    
    T read2 = unmarshaller.createLoader( type ).apply( jcrSession.getNode( "/" + name ), type.newInstance() );
    assertNotNull( read2 );
    assertThat( read2, is( expected ) );

    T read3 = unmarshaller.createSubnodeCreator( type ).apply( jcrSession.getRootNode(), name );
    assertNotNull( read3 );
    assertThat( read3, is( expected ) );

    T read4 = unmarshaller.createSubnodeLoader( type ).apply( jcrSession.getRootNode(), name, type.newInstance() );
    assertNotNull( read4 );
    assertThat( read4, is( expected ) );
    
  }

  private <T> void assertCreationsNull( JcxUnmarshaller unmarshaller, Class<T> type, String name ) throws Exception {
    assertNull( unmarshaller.createCreator( type ).apply( jcrSession.getNode( "/" + name ) ) );
    assertNull( unmarshaller.createLoader( type ).apply( jcrSession.getNode( "/" + name ), type.newInstance() ) );
    assertNull( unmarshaller.createSubnodeCreator( type ).apply( jcrSession.getRootNode(), name ) );
    assertNull( unmarshaller.createSubnodeLoader( type ).apply( jcrSession.getRootNode(), name, type.newInstance() ) );
  }

} /* ENDCLASS */
