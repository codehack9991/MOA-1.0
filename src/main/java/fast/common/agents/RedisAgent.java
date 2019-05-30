package fast.common.agents;

import java.util.Map;
import cucumber.api.java.lu.a;
import fast.common.context.CommonStepResult;
import fast.common.context.StepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

import redis.clients.jedis.HostAndPort; 
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
/**
 * The {@code RedisAgent} class defines automation of connecting redis server and do some actions.
 * 
 * 
 * <p>The basic actions includes: set redis cluster nodes, set redis node, get jedis ...</p>
 * 
 * <p>Details information for using a RedisAgent can see: 
 *  <a href="https://cedt-confluence.nam.nsroot.net/confluence/display/167813001/Redis+Automation+Example"> Examples</a>
 * 
 * @author QA Framework Team
 * @since 1.4
 */
public class RedisAgent extends Agent {

	private FastLogger _logger;
	private JedisCluster cs_jedisCluster;
	private Set<HostAndPort> cs_redisClusterSet = null;
	private Jedis cs_jedis = null;
	
	public static final String REDIS_CLUSTER_MASTER = "redisClusterMaster";
	public static final String REDIS_CLUSTER_SLAVE = "redisClusterSlave";
	public static final String REDIS_CLUSTER_SERVER_COUNT = "redisClusterServerCount";
	public static final String REDIS_CLUSTER_SERVER = "redisClusterServer";
	public static final String REDIS_PORT = "redisPort";
	public static final String REDIS_SERVER = "redisServer";
	/**
     * Constructs a new <tt>RedisAgent</tt> with
     * default configuration file (config.yml) and custom configuration files to
     * fetch required parameters.
     *
     * @param   name a string for naming the creating LeanftAgent 
     * @param   agentParams a map to get the required parameters for creating a LeanftAgent 
     * @param   configurator a Configurator instance to provide configuration info for the actions of the LeanftAgent
     * 
     * @since 1.5
     */
	public RedisAgent(String name, Map agentParams, Configurator configurator) {
		super(name, agentParams, configurator);
		_logger = FastLogger.getLogger(String.format("%s:RedisAgent", _name));
		if(agentParams.get(REDIS_CLUSTER_SERVER_COUNT).toString().trim().isEmpty()){
			_logger.info("Connecting to node");
			setRedisNode(agentParams.get(REDIS_SERVER).toString(),Integer.parseInt(agentParams.get(REDIS_PORT).toString()));
		}
		else{
			_logger.info("Connecting to Cluster");
			setRedisClusterNodes(agentParams.get(REDIS_CLUSTER_SERVER).toString().split(","), agentParams.get(REDIS_CLUSTER_MASTER).toString().split(","), agentParams.get(REDIS_CLUSTER_SLAVE).toString().split(","));
		}
	}
	/**
	 * Set redis cluster nodes with parameters
	 * 
	 * @param ag_servers
	 * 		servers for master and slaves
	 * @param ag_masters
	 * 		several masters for a server
	 * @param ag_slaves
	 * 		several slaves for a server
	 */
	public void setRedisClusterNodes(String[] ag_servers, String[] ag_masters, String[] ag_slaves) {
		String lv_message;
		Set<HostAndPort> redisCluster = new HashSet<HostAndPort>();
		_logger.info("In setRedisClusterNodes function");
		try {
			for (String lv_server : ag_servers) {
				for (String master : ag_masters)
					redisCluster.add(new HostAndPort(lv_server, Integer.parseInt(master)));
				for (String slave : ag_slaves)
					redisCluster.add(new HostAndPort(lv_server, Integer.parseInt(slave)));
			}
		} catch (Exception e) {
			lv_message = "\n[EXCEPTION] Exception occured while creating Cluster : " + e.getMessage().toString() + "\n"
					+ Arrays.toString(e.getStackTrace());
			_logger.error(lv_message);
			this.cs_jedisCluster = null;
		}
		this.cs_jedisCluster = new JedisCluster(redisCluster);
	}
	/**
	 * Set redis node in specify hostname with specify port using jedis.
	 * 
	 * @param ag_hostname
	 * 		the hostname of redis server
	 * @param ag_port
	 * 		the open port of redis server
	 */
	public void setRedisNode(String ag_hostname , int ag_port){
		String lv_message="";
		Jedis lv_jedis = null;
		_logger.info("In setRedisNode function");
		try{
			lv_jedis = new Jedis(ag_hostname,ag_port);
			_logger.info("Connected to Redis");
			
		}catch(Exception e){
			lv_message = "\n[EXCEPTION] Exception occured while fetching get-command result : " + e.getMessage().toString()
					+ "\n" + Arrays.toString(e.getStackTrace());
			_logger.error(lv_message);
			this.cs_jedis = lv_jedis;
		}
		this.cs_jedis = lv_jedis;
	}
	/**
	 * @return jedis
	 */
	public Jedis getJedis(){
		_logger.info("In getJedis function");
		return this.cs_jedis;
	}
	/**
	 * @return redis cluster set 
	 */
	public Set<HostAndPort> getJedisClsterSet(){
		_logger.info("In getJedisClsterSet function");
		return this.cs_redisClusterSet;
	}
	/**
	 * @return jedis cluster object
	 */
	public JedisCluster getJedisCluster(){
		_logger.info("In getJedisCluster function");
		return this.cs_jedisCluster;
	}
	/**
	 * Get the value of the specified key.
	 * 
	 * @param ag_key
	 * 		redis agent key
	 * @return the value of the specified key
	 */
	public String getGET(String ag_key){
		String lv_message="";
		String lv_result=null;
		_logger.info("In getGET function");
		try{
			if(this.getJedis()!=null){
				lv_result = this.getJedis().get(ag_key);
			}
			else{
				lv_result=this.getJedisCluster().get(ag_key);
			}
			
			_logger.info(lv_result);
		}catch(Exception e){
			lv_message = "\n[EXCEPTION] Exception occured while fetching get-command result : " + e.getMessage().toString()
					+ "\n" + Arrays.toString(e.getStackTrace());
			_logger.error(lv_message);
			return lv_result;
		}
		return lv_result;
	}
	/**
	 * Get the number of entries (fields) contained in the hash stored at key.
	 * 
	 * @param ag_key
	 * 		redis agent key
	 * @return the number of items in a hash.
	 */
	public long  getHLEN(String ag_key){
		long lv_length = 0;
		String lv_message;
		_logger.info("In getHLEN function");
		try{
			if(ag_key.isEmpty()){
				lv_message = "\n[ERROR] Empty key passed";
				_logger.error(lv_message);
			}
			else{
					if(this.getJedis()!=null){
						lv_length=this.getJedis().hlen(ag_key);
					}else{
						lv_length=this.getJedisCluster().hlen(ag_key);
					}
				_logger.info("HLEN Result: "+lv_length);
			}
		}catch(Exception e){
			lv_message = "\n[EXCEPTION] Exception occured while finding length of the key : " + e.getMessage().toString()
					+ "\n" + Arrays.toString(e.getStackTrace());
			_logger.error(lv_message);
		}
		return lv_length;
	}
	
	@Override
	public void close() throws Exception {
		_logger.info("In close function");
		this.cs_jedisCluster = null;
		this.cs_redisClusterSet = null;
		this.cs_jedis = null;
	}

}
