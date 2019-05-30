package fast.common.agents;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import fast.common.context.ElkStepResult;
import fast.common.core.Configurator;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({RestClient.class,QueryBuilders.class,TimeValue.class})
public class TestElkAgent {
	public static final String CONFIG_HOSTNAME = "hostName";
	public static final String CONFIG_PORT = "port";
	public static final String CONFIG_SCHEME = "scheme";
	public static final String CONFIG_MAXSIZE = "maxSize";
	public static final String CONFIG_ALIVETIME = "aliveTime";
	public static final String CONFIG_SORTBYTIMESTAMP = "sortByTimestamp";
	private String CONST_TIMESTAMP = "@timestamp";
	private long aliveTime = 1;
	private ElkAgent agent;
	private String name;
	private Configurator configurator;
	
	@Mock
	private SearchScrollRequest searchScrollRequest;
	
	@Mock
	private SearchResponse searchResponse;
	
	@Mock
	private SearchRequest searchRequest;
	
	@Mock
	private TimeValue timeValue;
	
	@Mock
	private Scroll scroll;
	
	@Mock
	private ElkStepResult elkStepResult;
	
	@Mock
	private SearchSourceBuilder searchSourceBuilder;
	
	@Mock
	private MultiMatchQueryBuilder multiMatchQueryBuilder;
	
	@Mock
	private RangeQueryBuilder rangeQueryBuilder;
	
	@Mock
	private BoolQueryBuilder queryBuilder;
	
	@Mock
	private RestHighLevelClient highLevelClient;
	
	@Mock
	private RestClient restClient;
	
	@Mock
	private Map agentParams;
	
	@Mock
	private RestClientBuilder restClientBuilder;
	
	@Before
	public void setUp() throws Exception {
		name = "nonMeaningName";
		configurator = Configurator.getInstance();
		when(agentParams.get(CONFIG_HOSTNAME)).thenReturn("invalid_CONFIG_HOSTNAME");
		when(agentParams.get(CONFIG_PORT)).thenReturn(1000);
		when(agentParams.get(CONFIG_SCHEME)).thenReturn("invalid_CONFIG_SCHEME");
		when(agentParams.get(CONFIG_MAXSIZE)).thenReturn(1000);
		when(agentParams.get(CONFIG_ALIVETIME)).thenReturn((long)100);
		when(agentParams.get(CONFIG_SORTBYTIMESTAMP)).thenReturn(true);
		when(agentParams.containsKey(CONFIG_MAXSIZE)).thenReturn(true);
		when(agentParams.containsKey(CONFIG_ALIVETIME)).thenReturn(true);
		when(agentParams.containsKey(CONFIG_SORTBYTIMESTAMP)).thenReturn(true);
		PowerMockito.mockStatic(RestClient.class);
		when(RestClient.builder(any())).thenReturn(restClientBuilder);
		when(restClientBuilder.build()).thenReturn(restClient);
		PowerMockito.whenNew(RestHighLevelClient.class).withArguments(restClient).thenReturn(highLevelClient);
		agent = new ElkAgent(name, agentParams, configurator);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void construct_elkAgent_allParams() throws Exception{
		assertEquals("nonMeaningName",Whitebox.getInternalState(agent,"_name"));
	}
	
	@Test
	public void construct_elkAgent_noCONFIG_MAXSIZE() throws Exception{
		when(agentParams.containsKey(CONFIG_MAXSIZE)).thenReturn(false);
		ElkAgent agent =  new ElkAgent(name, agentParams, configurator);
		assertEquals(Integer.valueOf(100),Whitebox.getInternalState(agent,"maxSize"));
	}
	
	@Test
	public void construct_elkAgent_UPPER_LIMIT() throws Exception{
		when(agentParams.get(CONFIG_MAXSIZE)).thenReturn(Integer.valueOf(20000));
		ElkAgent agent =  new ElkAgent(name, agentParams, configurator);
		assertEquals(Integer.valueOf(10000),Whitebox.getInternalState(agent,"maxSize"));
	}
	
	@Test
	public void construct_elkAgent_noCONFIG_ALIVETIME() throws Exception{
		when(agentParams.containsKey(CONFIG_ALIVETIME)).thenReturn(false);
		ElkAgent agent =  new ElkAgent(name, agentParams, configurator);
		assertEquals(Long.valueOf(1),Whitebox.getInternalState(agent,"aliveTime"));
	}
	
	@Test
	public void construct_elkAgent_noCONFIG_SORTBYTIMESTAMP() throws Exception{
		when(agentParams.containsKey(CONFIG_SORTBYTIMESTAMP)).thenReturn(false);
		ElkAgent agent =  new ElkAgent(name, agentParams, configurator);
		assertEquals(false,Whitebox.getInternalState(agent,"sortByTimestamp"));
	}
	
	@Test
	public void test_getQueryBuilder(){
		String startTime = "invalidStartTime";
		String endTime = "invalidEndTime";
		String queryCondition = "message:BLACKROCK 8=FIX.4.4|beat.name:zts_zas";
		PowerMockito.mockStatic(QueryBuilders.class);
		when(QueryBuilders.boolQuery()).thenReturn(queryBuilder);
		when(QueryBuilders.rangeQuery(CONST_TIMESTAMP)).thenReturn(rangeQueryBuilder);
		when(rangeQueryBuilder.gt(startTime)).thenReturn(rangeQueryBuilder);
		when(rangeQueryBuilder.lt(endTime)).thenReturn(rangeQueryBuilder);
		when(queryBuilder.must(rangeQueryBuilder)).thenReturn(queryBuilder);
		
		when(QueryBuilders.multiMatchQuery(any(),any())).thenReturn(multiMatchQueryBuilder);
		when(multiMatchQueryBuilder.operator(Operator.AND)).thenReturn(multiMatchQueryBuilder);
		when(queryBuilder.must(multiMatchQueryBuilder)).thenReturn(queryBuilder);
		
		assertEquals(null,agent.getQueryBuilder(startTime, endTime, queryCondition).getName());
	}
	
	@Test
	public void test_getQueryBuilder_conditionNotContain(){
		String startTime = "invalidStartTime";
		String endTime = "invalidEndTime";
		String queryCondition = "message:BLACKROCK=FIX.4.4|message:beat.name zts_zas";
		PowerMockito.mockStatic(QueryBuilders.class);
		when(QueryBuilders.boolQuery()).thenReturn(queryBuilder);
		when(QueryBuilders.rangeQuery(CONST_TIMESTAMP)).thenReturn(rangeQueryBuilder);
		when(rangeQueryBuilder.gt(startTime)).thenReturn(rangeQueryBuilder);
		when(rangeQueryBuilder.lt(endTime)).thenReturn(rangeQueryBuilder);
		when(queryBuilder.must(rangeQueryBuilder)).thenReturn(queryBuilder);
		
		when(QueryBuilders.multiMatchQuery(any(),any())).thenReturn(multiMatchQueryBuilder);
		when(multiMatchQueryBuilder.operator(Operator.AND)).thenReturn(multiMatchQueryBuilder);
		when(queryBuilder.must(multiMatchQueryBuilder)).thenReturn(queryBuilder);
		
		assertEquals(null,agent.getQueryBuilder(startTime, endTime, queryCondition).getName());
	}
	
	@Test
	public void test_getSearchSourceBuilder_sortTrue() throws Exception{
		PowerMockito.whenNew(SearchSourceBuilder.class).withNoArguments().thenReturn(searchSourceBuilder);
		when(searchSourceBuilder.sort(CONST_TIMESTAMP, SortOrder.DESC)).thenReturn(searchSourceBuilder);
		when(searchSourceBuilder.query(queryBuilder)).thenReturn(searchSourceBuilder);
		assertEquals(queryBuilder,agent.getSearchSourceBuilder(queryBuilder, true).query());
	}
	
	@Test
	public void test_getSearchSourceBuilder_sortFalse() throws Exception{
		PowerMockito.whenNew(SearchSourceBuilder.class).withNoArguments().thenReturn(searchSourceBuilder);
		when(searchSourceBuilder.query(queryBuilder)).thenReturn(searchSourceBuilder);
		assertEquals(queryBuilder,agent.getSearchSourceBuilder(queryBuilder, false).query());
		
	}
	
	@Test
	public void test_query() throws Exception{
		String index = "invalidIndex";
		String startTime = "invalidStartTime";
		String endTime = "invalidEndTime";
		String queryCondition = "message:BLACKROCK 8=FIX.4.4|beat.name:zts_zas";
		
	
		PowerMockito.mockStatic(TimeValue.class);
		when(TimeValue.timeValueMinutes(aliveTime)).thenReturn(timeValue);
		PowerMockito.whenNew(Scroll.class).withArguments(timeValue).thenReturn(scroll);
		when(searchRequest.scroll(scroll)).thenReturn(searchRequest);
		
		ElkAgent agent = mock(ElkAgent.class);
		when(agent.getQueryBuilder(startTime, endTime, queryCondition)).thenReturn(queryBuilder);
		when(agent.getSearchSourceBuilder(queryBuilder, true)).thenReturn(searchSourceBuilder);
		when(highLevelClient.search(searchRequest)).thenReturn(searchResponse);
		when(searchResponse.getScrollId()).thenReturn("scrollId");
		
		SearchHits searchHits = PowerMockito.mock(SearchHits.class);
		when(searchResponse.getHits()).thenReturn(searchHits);
		SearchHit[] testSearchHits = null;
		when(searchHits.getHits()).thenReturn(testSearchHits);
		PowerMockito.whenNew(SearchScrollRequest.class).withArguments("scrollId").thenReturn(searchScrollRequest);
		when(searchScrollRequest.scroll(scroll)).thenReturn(searchScrollRequest);
		when(highLevelClient.searchScroll(searchScrollRequest)).thenReturn(searchResponse);
		assertEquals(null,agent.query(index, startTime, endTime, queryCondition));
	}
	
	@Test
	public void test_getRestClient(){
		assertEquals(restClient, agent.getRestClient());
	}
	
	@Test
	public void test_getHighLevelClient(){
		assertNotEquals(highLevelClient, agent.getHighLevelClient());
	}
	
	@Test
	public void test_close() throws Exception{
		agent.close();
	}
}
