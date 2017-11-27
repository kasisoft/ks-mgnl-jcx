package com.kasisoft.mgnl.jcx.unmarshaller;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.MockitoAnnotations.*;
import static org.testng.Assert.*;

import info.magnolia.test.mock.jcr.*;

import info.magnolia.jcr.util.*;

import com.kasisoft.mgnl.jcx.*;
import com.kasisoft.mgnl.versionhandler.*;

import org.testng.annotations.*;

import javax.jcr.*;

import java.util.function.*;

import java.util.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class AbstractJcxUnmarshaller {

  public static final String WS_BIBO  = "biboWs";
  public static final String WS_WUPPI = "wuppiWs";
  
  MockSession              biboSession;
  MockSession              wuppiSession;
  String                   targetedNodeUuid1;
  String                   targetedNodeUuid2;
  String                   targetedNodeUuid3;
  ExtendedMockWebContext   context;
  
  protected ExtendedMockWebContext.ExtendedMockWebContextBuilder newWebContextBuilder() {
    return ExtendedMockWebContext.builder()
      .workspace( WS_BIBO  )
      .workspace( WS_WUPPI );
  }
  
  protected String getTargetNodeUuid() {
    return targetedNodeUuid1;
  }
  
  protected MockSession getBiboSession() {
    return biboSession;
  }
  
  protected MockSession getWuppiSession() {
    return wuppiSession;
  }
  
  protected ExtendedMockWebContext getContext() {
    return context;
  }
  
  @BeforeClass
  public void setup() throws Exception{
    
    initMocks( this );
    
    context = newWebContextBuilder().build();
    
    context.install();
    
    biboSession  = context.getMockSession( WS_BIBO  );
    wuppiSession = context.getMockSession( WS_WUPPI );
    
    TreeBuilder tb = new TreeBuilder()
      .sContentNode( "targetedNode" )
        .property( "title", "zoidberg" )
        .property( "value", 120 )
      .sEnd()
      .sContentNode( "targetedNode2" )
        .property( "title", "fry" )
        .property( "value", 30 )
      .sEnd()
      .sContentNode( "targetedNode3" )
        .property( "title", "lila" )
        .property( "value", 70 )
      .sEnd()
      ;
    
    tb.build( new MockNodeProducer( wuppiSession ) );
    
    targetedNodeUuid1 = SessionUtil.getNode( wuppiSession, "/targetedNode" ).getIdentifier();
    targetedNodeUuid2 = SessionUtil.getNode( wuppiSession, "/targetedNode2" ).getIdentifier();
    targetedNodeUuid3 = SessionUtil.getNode( wuppiSession, "/targetedNode3" ).getIdentifier();
    
  }
  
  protected <T> void assertCreations( JcxUnmarshaller unmarshaller, Class<T> type, String name, T expected ) throws Exception {
    
    T read1 = unmarshaller.createCreator( type ).apply( biboSession.getNode( "/" + name ) );
    assertNotNull( read1 );
    assertThat( read1, is( expected ) );
    
    T read2 = unmarshaller.createLoader( type ).apply( biboSession.getNode( "/" + name ), type.newInstance() );
    assertNotNull( read2 );
    assertThat( read2, is( expected ) );

    T read3 = unmarshaller.createSubnodeCreator( type ).apply( biboSession.getRootNode(), name );
    assertNotNull( read3 );
    assertThat( read3, is( expected ) );

    T read4 = unmarshaller.createSubnodeLoader( type ).apply( biboSession.getRootNode(), name, type.newInstance() );
    assertNotNull( read4 );
    assertThat( read4, is( expected ) );
    
  }

  protected <T extends Supplier<Node>> void assertNodes( JcxUnmarshaller unmarshaller, Class<T> type, String name ) throws Exception {
    
    T read1 = unmarshaller.createCreator( type ).apply( biboSession.getNode( "/" + name ) );
    assertNotNull( read1 );
    assertTrue( read1.get() instanceof Node );
    
    T read2 = unmarshaller.createLoader( type ).apply( biboSession.getNode( "/" + name ), type.newInstance() );
    assertNotNull( read2 );
    assertTrue( read2.get() instanceof Node );

    T read3 = unmarshaller.createSubnodeCreator( type ).apply( biboSession.getRootNode(), name );
    assertNotNull( read3 );
    assertTrue( read3.get() instanceof Node );

    T read4 = unmarshaller.createSubnodeLoader( type ).apply( biboSession.getRootNode(), name, type.newInstance() );
    assertNotNull( read4 );
    assertTrue( read4.get() instanceof Node );
    
  }

  protected <T extends Supplier<List<Node>>> void assertNodeLists( JcxUnmarshaller unmarshaller, Class<T> type, String name ) throws Exception {
    
    T read1 = unmarshaller.createCreator( type ).apply( biboSession.getNode( "/" + name ) );
    assertNotNull( read1 );
    assertNodeList( read1.get() );
    
    T read2 = unmarshaller.createLoader( type ).apply( biboSession.getNode( "/" + name ), type.newInstance() );
    assertNotNull( read2 );
    assertNodeList( read2.get() );

    T read3 = unmarshaller.createSubnodeCreator( type ).apply( biboSession.getRootNode(), name );
    assertNotNull( read3 );
    assertNodeList( read3.get() );

    T read4 = unmarshaller.createSubnodeLoader( type ).apply( biboSession.getRootNode(), name, type.newInstance() );
    assertNotNull( read4 );
    assertNodeList( read4.get() );
    
  }
  
  private void assertNodeList( List<Node> list ) {
    assertNotNull( list );
    assertFalse( list.isEmpty() );
    list.stream().forEach( $ -> assertTrue( $ instanceof Node ) );
  }
  

  protected <T> void assertCreationsNull( JcxUnmarshaller unmarshaller, Class<T> type, String name ) throws Exception {
    assertNull( unmarshaller.createCreator( type ).apply( biboSession.getNode( "/" + name ) ) );
    assertNull( unmarshaller.createLoader( type ).apply( biboSession.getNode( "/" + name ), type.newInstance() ) );
    assertNull( unmarshaller.createSubnodeCreator( type ).apply( biboSession.getRootNode(), name ) );
    assertNull( unmarshaller.createSubnodeLoader( type ).apply( biboSession.getRootNode(), name, type.newInstance() ) );
  }

} /* ENDCLASS */
