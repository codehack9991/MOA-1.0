package fast.common.agents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.json.JSONException;
import org.json.JSONObject;

import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;

import fast.common.agents.messaging.IMessagingAgent;
import fast.common.agents.messaging.MessagingException;
import fast.common.context.MessagingStepResult;
import fast.common.core.Configurator;
import fast.common.logging.FastLogger;

public class AvroAgent extends Agent implements IStartable{
	
	public static final String CONFIG_MESSAGING_AGENT = "messaging_agent";
	public static final String CONFIG_SCHEMA_PATH = "schemaPath";
	public static final String CONFIG_DATA_PATH= "dataPath";
	
	private FastLogger _logger;
	final ArrayList<GenericRecord> _receivedMessages = new ArrayList<>();
	IMessagingAgent _messagingAgent;
	Injection<GenericRecord, byte[]> recordInjection;
	Schema schema;
	String schemaPath;
	List<GenericData.Record> records;
	
	public AvroAgent(String name, Map agentParams, Configurator configurator) throws Exception {
		super(name, agentParams, configurator);
		_logger = FastLogger.getLogger(String.format("%s:AvroAgent", _name));
		String messaingAgentName = Configurator.getStringOr(_agentParams, CONFIG_MESSAGING_AGENT, null);		 
		_messagingAgent = AgentsManager.getAgent(messaingAgentName);
		schemaPath = Configurator.getStringOr(_agentParams, CONFIG_SCHEMA_PATH, null);
		schema = loadSchema(schemaPath);
		recordInjection = GenericAvroCodecs.toBinary(schema);
		records = createAvroMessage(schema, Configurator.getStringOr(_agentParams, CONFIG_DATA_PATH, null));
	}
	
	public Schema loadSchema(String schemaPath) throws IOException{
        return new Schema.Parser().parse(new File(schemaPath));
	}
	
	public void start() throws Exception {
		if (_messagingAgent == null) {
			throw new NullPointerException("messaging agent is null");
		}
		if (_messagingAgent.isStarted()) {
			_logger.info("Messaging agent is already started !");
			return;
		}
		_messagingAgent.start();
		_logger.info("Starting messaging agent");

	}
	
	
	@Override
	public void close() throws Exception {
		if(_messagingAgent != null){
			_messagingAgent.close();
		}		

	}
	
	public byte[] convertToByteArray(GenericData.Record avroRecord) {
		return recordInjection.apply(avroRecord);

	}
	public String readFileContent(String dataPath) throws IOException{
		File file = new File(dataPath);
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
		String fileContent = null;
		records = new ArrayList<>();
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			String line = null;
			StringBuilder stringBuilder = new StringBuilder((int) file.length());
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}
			fileContent = stringBuilder.toString();
		} catch (FileNotFoundException e) {
			_logger.error("File is not found with path : " + dataPath);
		}
		finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (fileReader != null) {
					fileReader.close();
				}
			} catch (IOException e) {
				_logger.error("Reader Stream closed with exception");
			}

		}
		if (fileContent == null) {
			_logger.warn("Avro schema data is null !");
		}
		return fileContent;
	}
	
	public List<GenericData.Record> createAvroMessage(Schema schema, String dataPath) throws JSONException, IOException {
		
		records = new ArrayList<>();
		String ret = readFileContent(dataPath);
		
		String[] datas = ret.split("\\|");
		
		for(String data : datas){
			JSONObject jo = new JSONObject(data);
			GenericData.Record avroRecord = new GenericData.Record(schema);
			
			List<Field> fields = schema.getFields();
			for (Field field : fields) {
				if (field.defaultValue() != null) {
					Type type = field.schema().getType();
					switch (type) {
					case INT:
						avroRecord.put(field.name(), field.defaultValue().getIntValue());
						break;
					case STRING:
						avroRecord.put(field.name(), field.defaultValue().getTextValue());
						break;
					case BOOLEAN:
						avroRecord.put(field.name(), field.defaultValue().getBooleanValue());
						break;
					case LONG:
						avroRecord.put(field.name(), field.defaultValue().getLongValue());
						break;
					case DOUBLE:
						avroRecord.put(field.name(), field.defaultValue().getDoubleValue());
						break;
					case BYTES:
						avroRecord.put(field.name(), field.defaultValue().getBinaryValue());
						break;
					case FLOAT:
						avroRecord.put(field.name(), field.defaultValue().getDoubleValue());
						break;
					default:
						break;
					}
				}
				if (jo.has(field.name()))
					avroRecord.put(field.name(), jo.get(field.name()));
			}
			records.add(avroRecord);
		}
		
		return records;
	}
	
	
	public void send() throws MessagingException {
		
		for(GenericData.Record record : records){
			byte[] message = convertToByteArray(record);
			_messagingAgent.send(message);
			_logger.info("send avro message successfully!");
		}	
	}
	
	public void receive() throws MessagingException {
		_receivedMessages.clear();
		MessagingStepResult result = _messagingAgent.receive();
		ArrayList<Object> results = result.getMessages();
		
		for (Object record : results) {
			byte[] byteValue = ((ConsumerRecord<String, byte[]>) record).value();
			GenericRecord genericRecord = recordInjection.invert(byteValue).get();
			_receivedMessages.add(genericRecord);
		}
		
		_logger.info("Received " + _receivedMessages.size() + " Avro Message :  + " + _receivedMessages);
	}

	public Schema getSchema() {
		return schema;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public List<GenericData.Record> getRecords() {
		return records;
	}

	public void setRecords(List<GenericData.Record> records) {
		this.records = records;
	}

	@Override
	public boolean isStarted() {
		return _messagingAgent != null &&_messagingAgent.isStarted();
	}
	
}
