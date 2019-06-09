package fast.common.agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;

import fast.common.context.ElkStepResult;
import fast.common.context.StepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

public class ElkAgent extends Agent {

	public static final String CONFIG_HOSTNAME = "hostName";
	public static final String CONFIG_PORT = "port";
	public static final String CONFIG_SCHEME = "scheme";
	public static final String CONFIG_MAXSIZE = "maxSize";
	public static final String CONFIG_ALIVETIME = "aliveTime";
	public static final String CONFIG_SORTBYTIMESTAMP = "sortByTimestamp";
	

	private FastLogger logger;

	private String hostName;
	private int port;
	private String scheme;
	private int maxSize = 100;
	private long aliveTime = 1;
	private boolean sortByTimestamp = false;
	private static final int UPPER_LIMIT = 10000;
	private RestClient restClient;
	private RestHighLevelClient highLevelClient;
	private String CONST_TIMESTAMP = "@timestamp";
	/**
	 * Constructs a new <tt>ElkAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     *
     * @param   name a string for naming the creating ElkAgent 
     * @param   agentParams a map to get the required parameters for creating a ElkAgent 
     * @param   configurator a Configurator instance to provide configuration info for the actions of the ElkAgent
     * 
     * @since 1.5
	 * 
	 */
	public ElkAgent(String name, Map<?,?> agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		logger = FastLogger.getLogger(String.format("%s:ElkAgent", _name));
		_agentParams = agentParams;

		hostName = _agentParams.get(CONFIG_HOSTNAME).toString();
		port = Integer.parseInt(_agentParams.get(CONFIG_PORT).toString());
		scheme = _agentParams.get(CONFIG_SCHEME).toString();
		if (_agentParams.containsKey(CONFIG_MAXSIZE)) {
			maxSize = Integer.parseInt(_agentParams.get(CONFIG_MAXSIZE)
					.toString());
			if(maxSize>UPPER_LIMIT)maxSize=UPPER_LIMIT;
		}
		if (_agentParams.containsKey(CONFIG_ALIVETIME)) {
			aliveTime = Long.parseLong(_agentParams.get(CONFIG_ALIVETIME)
					.toString());
		}
		if (_agentParams.containsKey(CONFIG_SORTBYTIMESTAMP)) {
			sortByTimestamp = Boolean.parseBoolean(_agentParams.get(CONFIG_SORTBYTIMESTAMP)
					.toString());
		}
		restClient = RestClient.builder(new HttpHost(hostName, port, scheme))
				.build();
		highLevelClient = new RestHighLevelClient(restClient);

	}
	
	/**
	 * Create QueryBuilder with time range and matching condition.
	 * 
	 * @param startTime filter records whose @timestamp is greater than startTime
	 * @param endTime filter records whose @timestamp is less than endTime
	 * @param queryCondition used to filter records with provided field and text
	 * @return queryBuilder used to hit records 
	 * @since 1.5
	 * @see #query(String, String, String, String) 
	 */
	public BoolQueryBuilder getQueryBuilder(String startTime, String endTime,
			String queryCondition) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		queryBuilder.must(QueryBuilders.rangeQuery(CONST_TIMESTAMP)
				.gt(startTime).lt(endTime));
		String[] splitCondition = queryCondition.split("\\|");
		for (String condition : splitCondition) {
			int index = condition.indexOf(":");
			String field = condition.substring(0, index);
			String value = condition.substring((index + 1));
			if (value.contains("=") || value.contains(" ")) {
				String[] splitValue = value.split("=");
				String subValue = "";
				for (String s : splitValue) {
					subValue += s + " ";
				}
				value = subValue.trim();
			}
			queryBuilder.must(QueryBuilders.multiMatchQuery(value, field)
					.operator(Operator.AND));
		}
		return queryBuilder;
	}
	/**
	 * Create SearchSourceBuilder with params sortByTimestamp
	 * 
	 * @param boolQueryBuilder used to hit records
	 * @param sortByTimestamp define whether get search result order by timestamp
	 * @return searchSourceBuilder used to construct searchRequest
	 * @since 1.7
	 * @see #query(String, String, String, String) 
	 */
	public SearchSourceBuilder getSearchSourceBuilder(BoolQueryBuilder boolQueryBuilder, Boolean sortByTimestamp){
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		if(sortByTimestamp){
			searchSourceBuilder.sort(CONST_TIMESTAMP, SortOrder.DESC);
		}
		searchSourceBuilder.query(boolQueryBuilder);
		return searchSourceBuilder;
	}
	
	/**
	 * Query result according to index, startTime, endTime and queryCondition.
	 * 
	 * @param index restricts the hit records with this index
     * @param startTime restricts the @timestamp of hit records in a time range, required @timestamp to be greater than startTime
	 * @param endTime restricts the @timestamp of hit records in a time range, required @timestamp to be less than endTime
	 * @param queryCondition used to filter records with provided field and text
	 * @return a ElkStepResult object store hits result. 
	 * @throws IOException
	 * 
	 * @see fast.common.glue.CommonStepDefs#elkQuery(String, String,String, String,String, String)
	 * @see  #getQueryBuilder(String,String,String)
	 */
	public StepResult query(String index, String startTime, String endTime,
			String queryCondition) throws IOException {
		ElkStepResult elkStepResult = new ElkStepResult();
		final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(aliveTime));
		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.scroll(scroll);

		BoolQueryBuilder queryBuilder = getQueryBuilder(startTime, endTime,
				queryCondition);
		SearchSourceBuilder sourceBuilder = getSearchSourceBuilder(queryBuilder,sortByTimestamp);
		searchRequest.source(sourceBuilder);
		SearchResponse searchResponse = highLevelClient.search(searchRequest);
		String scrollId = searchResponse.getScrollId();
		SearchHit[] searchHits = searchResponse.getHits().getHits();
		int size = maxSize;
		ArrayList<Map<String, Object>> hitStrings = new ArrayList<Map<String, Object>>();
		logger.info("Querying from ELK...");
		loopHits: while (searchHits != null && searchHits.length > 0) {
			for (SearchHit s : searchHits) {
				if (size <= 0)
					break loopHits;
				Map<String, Object> sourceMap = s.getSourceAsMap();
				sourceMap.put("_id", s.getId());
				sourceMap.put("_index", s.getIndex());
				sourceMap.put("_Score", s.getScore());
				sourceMap.put("_type", s.getType());
				Map<String, Object> convertedMap = new HashMap<String, Object>();
				String parentKey = null;
				covertSubmapToFullMap(convertedMap, sourceMap, parentKey);
				hitStrings.add(convertedMap);
				size--;
			}
			SearchScrollRequest scrollRequest = new SearchScrollRequest(
					scrollId);
			scrollRequest.scroll(scroll);

			searchResponse = highLevelClient.searchScroll(scrollRequest);
			scrollId = searchResponse.getScrollId();
			searchHits = searchResponse.getHits().getHits();
		}
		logger.info("Totally get " + (maxSize - size) + " records from ELK!");
		elkStepResult.setResult(hitStrings);
		return elkStepResult;
	}

	private void covertSubmapToFullMap(Map<String, Object> convertedMap,
			Map<String, Object> sourceMap, String parentKey) {
		if (sourceMap == null)
			return;
		Iterator<String> srcIter = sourceMap.keySet().iterator();
		while (srcIter.hasNext()) {
			String key = (String) srcIter.next();
			String keyName = parentKey == null ? key : parentKey + "." + key;
			Object srcObj = sourceMap.get(key);
			if (srcObj instanceof Map) {
				Map<String, Object> subSrcMap = (Map<String, Object>) srcObj;
				covertSubmapToFullMap(convertedMap, subSrcMap, keyName);
			} else {
				convertedMap.put(keyName, srcObj);
			}
		}
	}
	/**
	 * @return restClient
	 */
	public RestClient getRestClient() {
		return restClient;
	}
	/**
	 * @return highLevelClient
	 */
	public RestHighLevelClient getHighLevelClient() {
		return highLevelClient;
	}

	@Override
	public void close() throws Exception {
		restClient.close();
	}

}
