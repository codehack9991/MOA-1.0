package fast.common.agents.messaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;


import fast.common.agents.Agent;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

public class KafkaAgent extends Agent implements IMessagingAgent {
	public static final String CONFIG_SERVER = "server";
	public static final String CONFIG_TOPIC = "topic";
	public static final String CONFIG_GROUP_ID = "group_id";
	public static final String CONFIG_MESSAGE_FORMAT = "message_format";
	public static final String CONFIG_BATCH_SIZE = "batch_size";
	public static final String CONFIG_BUFFER_MEMORY = "buffer_memory";
	public static final String CONFIG_COMPRESSION_TYPE = "compression_type";
	public static final String CONFIG_MAX_REQUEST_SIZE = "max_request_size";
	
	public static final String CONFIG_AUTO_COMMIT = "auto_commit";
	public static final String CONFIG_AUTO_COMMIT_INTERVAL_MS = "auto_commit_interval";
	public static final String CONFIG_FETCH_MAX_WAIT = "fetch_max_wait";
	
	public static final String CONFIG_TIMEOUT = "time_out";
	
	//Configration by default
	private int batch_size = 10000; 
	private int buffer_memory = 3000000; 
	private String compression_type = "none"; 
	private int max_request_size = 3000000; 
	
	private String auto_commit = "false";
	private String auto_commit_interval = "1000";
	private String fetch_max_wait = "10000";
	
	private int timeout = 5; 
	
	private FastLogger _logger;
	private Producer<String, Object> producer;
	private ConsumerRunnable consumerRunnable;
	private Properties consumerProps;
	private Properties producerProps;
	private String topic;
	final ArrayList<Object> _receivedMessages = new ArrayList<>();
	private boolean isStarted = false;

	public KafkaAgent(String name, Map agentParams, Configurator configurator) throws Exception {
		super(name, agentParams, configurator);
		_logger = FastLogger.getLogger(String.format("%s:KafkaAgent", _name));
		topic = Configurator.getStringOr(agentParams, CONFIG_TOPIC, null);
		if (agentParams.containsKey(CONFIG_BATCH_SIZE)) {
			batch_size = Integer.parseInt(agentParams.get(CONFIG_BATCH_SIZE).toString());
		}
		if (agentParams.containsKey(CONFIG_BUFFER_MEMORY)) {
			buffer_memory = Integer.parseInt(agentParams.get(CONFIG_BUFFER_MEMORY).toString());
		}
		if (agentParams.containsKey(CONFIG_COMPRESSION_TYPE)) {
			compression_type = agentParams.get(CONFIG_COMPRESSION_TYPE).toString();
		}
		if (agentParams.containsKey(CONFIG_MAX_REQUEST_SIZE)) {
			max_request_size = Integer.parseInt(agentParams.get(CONFIG_MAX_REQUEST_SIZE).toString());
		}
		
		if (agentParams.containsKey(CONFIG_AUTO_COMMIT)) {
			auto_commit = agentParams.get(CONFIG_AUTO_COMMIT).toString();
		}
		if (agentParams.containsKey(CONFIG_AUTO_COMMIT_INTERVAL_MS)) {
			auto_commit_interval = agentParams.get(CONFIG_AUTO_COMMIT_INTERVAL_MS).toString();
		}
		if (agentParams.containsKey(CONFIG_FETCH_MAX_WAIT)) {
			fetch_max_wait = agentParams.get(CONFIG_FETCH_MAX_WAIT).toString();
		}
		
		if (agentParams.containsKey(CONFIG_TIMEOUT)) {
			timeout = Integer.parseInt(agentParams.get(CONFIG_TIMEOUT).toString());
		}
		
		start();

	}

	public void loadPorperties() {
		producerProps = new Properties();
		consumerProps = new Properties();
		producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, _agentParams.get(CONFIG_SERVER).toString());
		producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batch_size);
		producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, buffer_memory);
		producerProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compression_type);
		producerProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, max_request_size);
		
		if (_agentParams.get(CONFIG_MESSAGE_FORMAT).toString().equalsIgnoreCase("TEXT")) {
			producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
			producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
			consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
			consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
			
		} else {
			producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
			producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
			consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
			consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		}
		
		consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, _agentParams.get(CONFIG_SERVER).toString());
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, _agentParams.get(CONFIG_GROUP_ID).toString());
		consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, auto_commit);
		consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, auto_commit_interval);
		consumerProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, fetch_max_wait);
		
	}

	@Override
	public void close() throws Exception {
		consumerRunnable.close();
		if (producer != null) {
			producer.close();
			producer = null;
		}

		synchronized (_receivedMessages) {
			_receivedMessages.clear();
		}

	}

	@Override
	public void send(Object message) throws MessagingException {
		if ((message instanceof String)) {
			_logger.info("Message is type of string !");
		} else if (message instanceof byte[]) {
			_logger.info("Message is type of byte array !");
		} else {
			throw new MessagingException("This format message is not supported for now !");
		}

		producer.send(new ProducerRecord<String, Object>(topic, message),
				(metadata, exception) -> {
					if (exception != null) {
						_logger.error("Kafka message sending error : " + exception.getMessage());
					}
				});

	}

	@Override
	public MessagingStepResult receive() throws MessagingException {
		try {
			Thread.sleep(timeout * 1_000L);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		_logger.info("Receive " +_receivedMessages.size() + " kafka messages !");
		ArrayList<Object> result;
		synchronized (_receivedMessages) {
			result = new ArrayList<>(_receivedMessages);
			_receivedMessages.clear();
		}
		return new MessagingStepResult(result);
		
	}

	@Override
	public void start() {
		loadPorperties();
		producer = new KafkaProducer<>(producerProps);		
		consumerRunnable = new ConsumerRunnable(consumerProps);
		new Thread(consumerRunnable).start();
		isStarted = true;
	}

	public class ConsumerRunnable implements Runnable {
		private FastLogger _logger;
		private final KafkaConsumer<String, Object> consumer;

		private boolean flag = true;

		public ConsumerRunnable(Properties props) {
			_logger = FastLogger.getLogger(String.format("%s:ConsumerRunnable", _name));

			this.consumer = new KafkaConsumer<>(props);

		}

		@Override
		public void run() {
			consumer.subscribe(Arrays.asList(_agentParams.get(CONFIG_TOPIC).toString()));
			while (flag) {
				ConsumerRecords<String, Object> records = consumer.poll(100);

				long pollStartTime = System.currentTimeMillis();
				int count = 0;

				for (ConsumerRecord<String, Object> record : records) {
					String msgTopic = null;
					int msgPartition = 0;
					long offset = 0;
					String msgKey = null;
					try {
						msgTopic = record.topic();
						msgPartition = record.partition();
						offset = record.offset();
						StringBuilder keyStringBuilder = new StringBuilder();
						keyStringBuilder.append(msgTopic).append(":").append(msgPartition).append(":").append(offset);
						msgKey = keyStringBuilder.toString();
						_logger.info(msgKey + " received");
						_logger.info("offset = " + record.offset() + ", key = " + record.key() + ", value = "
								+ record.value());
						consumer.commitSync(Collections.singletonMap(new TopicPartition(msgTopic, msgPartition),
								new OffsetAndMetadata(offset + 1)));
						_receivedMessages.add(record);
					} finally {
						long pollStartTime2 = System.currentTimeMillis();
						consumer.commitSync(Collections.singletonMap(new TopicPartition(msgTopic, msgPartition),
								new OffsetAndMetadata(offset + 1)));
						long pollEndTime2 = System.currentTimeMillis() - pollStartTime2;
						_logger.info("Commit costs " + pollEndTime2 + "ms.");
						count++;
					}
				}
				long pollEndTime = System.currentTimeMillis() - pollStartTime;
				if (count != 0) {
					_logger.info("Processed " + count + " records, costs " + pollEndTime + "ms.");
				}
			}

			consumer.close();
		}

		public void close() {
			flag = false;
		}

	}

	@Override
	public boolean isStarted() {
		return isStarted;
	}

}
