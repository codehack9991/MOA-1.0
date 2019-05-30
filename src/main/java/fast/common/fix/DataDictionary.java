package fast.common.fix;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import fast.common.logging.FastLogger;
import quickfix.ConfigError;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class DataDictionary extends quickfix.DataDictionary {
	FastLogger logger = FastLogger.getLogger("DataDictionary");
	String _filename;
	HashMap<String, Set<Integer>> groupTags = null;

	DataDictionary(String location) throws ConfigError {
		super(location);
		_filename = location;
	}

	String getReverseValueName(int tag, String name) throws XPathExpressionException {
		HashMap<Integer, HashMap<String, String>> reverseLookup = getReverseLookup();
		HashMap<String, String> tagValues = reverseLookup.get(tag);
		if ((tagValues != null) && tagValues.containsKey(name))
			return tagValues.get(name);
		return name;
	}

	private HashMap<Integer, HashMap<String, String>> _reverseLookup = null;

	private HashMap<Integer, HashMap<String, String>> getReverseLookup() throws XPathExpressionException {
		if (_reverseLookup == null) {
			_reverseLookup = parseXml();
		}
		return _reverseLookup;
	}

	private HashMap<Integer, HashMap<String, String>> parseXml() throws XPathExpressionException {
		HashMap<Integer, HashMap<String, String>> lookup = new HashMap<Integer, HashMap<String, String>>();

		XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inpputSource = new InputSource(_filename);
		NodeList nodes = (NodeList) xpath.evaluate("fix/fields/field", inpputSource, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node f = nodes.item(i);
			String number = f.getAttributes().getNamedItem("number").getNodeValue();
			int tag = Integer.parseInt(number);

			HashMap<String, String> tag_map;
			if (!lookup.containsKey(tag)) {
				tag_map = new HashMap<String, String>();
				lookup.put(tag, tag_map);
			} else {
				tag_map = lookup.get(tag);
			}

			NodeList childNodes = f.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node childNode = childNodes.item(j);
				if (childNode.getNodeName().equals("value")) {
					String description = childNode.getAttributes().getNamedItem("description").getNodeValue();
					String enum_val = childNode.getAttributes().getNamedItem("enum").getNodeValue(); // can be few chars
					tag_map.put(description, enum_val);
				}
			}
		}

		nodes = (NodeList) xpath.evaluate("fix/messages/message", inpputSource, XPathConstants.NODESET);
		HashMap<String, String> tag_map = lookup.get(35);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String name = node.getAttributes().getNamedItem("name").getNodeValue();
			String msgtype = node.getAttributes().getNamedItem("msgtype").getNodeValue();
			tag_map.put(name, msgtype);
		}

		return lookup;
	}

	private void getGroupTagsInfo() throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inpputSource = new InputSource(_filename);
		groupTags = new HashMap<String, Set<Integer>>();
		NodeList nodes = (NodeList) xpath.evaluate("fix/messages/message", inpputSource, XPathConstants.NODESET);
		// Get group tags in each message and save as Set
		for (int msgCount = 0; msgCount < nodes.getLength(); msgCount++) {
			Node message = nodes.item(msgCount);
			String msgtype = message.getAttributes().getNamedItem("msgtype").getNodeValue();
			Set<Integer> resultSet = findGroupFields(msgtype, message);
			groupTags.put(msgtype, resultSet);
		}
	}

	private Set<Integer> findGroupFields(String msgType, Node message) throws XPathExpressionException {
		Set<Integer> fields = new TreeSet<Integer>();
		Set<String> componentNames = new TreeSet<String>();
		NodeList subNodes = message.getChildNodes();
		for (int subNodeCount = 0; subNodeCount < subNodes.getLength(); subNodeCount++) {
			Node subNode = subNodes.item(subNodeCount);
			if (subNode.getNodeName().equalsIgnoreCase("group")) {
				Set<Integer> allFields = getGroupFields(msgType, subNode);
				fields.addAll(allFields);
			} else if (subNode.getNodeName().equalsIgnoreCase("component")) {
				String componentName = subNode.getAttributes().getNamedItem("name").getNodeValue();
				componentNames.add(componentName);
			}
		}
		for (String componentName : componentNames) {
			Set<Integer> allFieldsInComponent = getComponentFields(msgType, componentName);
			if (!allFieldsInComponent.isEmpty()) {
				fields.addAll(allFieldsInComponent);
			}
		}
		return fields;
	}

	private Set<Integer> getComponentFields(String msgType, String componentName) throws XPathExpressionException {
		Set<Integer> fields = new TreeSet<Integer>();
		XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inpputSource = new InputSource(_filename);
		try {
			NodeList subNodes = (NodeList) xpath.evaluate("fix/components/component", inpputSource,
					XPathConstants.NODESET);
			for (int subNodeCount = 0; subNodeCount < subNodes.getLength(); subNodeCount++) {
				Node subNode = subNodes.item(subNodeCount);
				if (subNode.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase(componentName)) {
					Set<Integer> allFields = findGroupFields(msgType, subNode);
					fields.addAll(allFields);
				}
			}
		} catch (XPathExpressionException e) {
			logger.error("Failed to parse component data for " + msgType + " component - " + componentName);
			throw e;
		}
		return fields;
	}

	private Set<Integer> getGroupFields(String msgType, Node groupNode) {
		Set<Integer> fields = new TreeSet<Integer>();
		NodeList subNodes = groupNode.getChildNodes();
		for (int subNodeCount = 0; subNodeCount < subNodes.getLength(); subNodeCount++) {
			Node subNode = subNodes.item(subNodeCount);
			if (subNode.getNodeName().equalsIgnoreCase("group")) {
				Set<Integer> allFields = getGroupFields(msgType, subNode);
				fields.addAll(allFields);
			} else if (subNode.getNodeName().equalsIgnoreCase("field")) {
				String fieldName = subNode.getAttributes().getNamedItem("name").getNodeValue();
				Integer tag = getFieldTag(fieldName);
				if (!isGroup(msgType, tag)) {
					fields.add(tag);
				}
			}
		}
		return fields;
	}

	protected static final Integer[] RELEATING_TAGS_ARRAY = new Integer[] { 448, 447, 452, 2376 }; // TODO: make it onfigurable or loadable from XML
	protected static final Set<Integer> RELEATING_TAGS_SET = new HashSet<Integer>(Arrays.asList(RELEATING_TAGS_ARRAY));

	boolean IsRepeatingTag(int tag_ing) {
		logger.debug("Using fixed set for getting repeating group tags");
		return RELEATING_TAGS_SET.contains(tag_ing);
	}

	boolean IsRepeatingTag(String msgType, int tag_ing) {
		// If we dont know the message type then we cannot decide on whether the
		// tag is group tag or not.
		// Fallback to fixed array
		String msgId;
		if (msgType == null) {
			return IsRepeatingTag(tag_ing);
		}
		if (groupTags == null) {
			try {
				getGroupTagsInfo();
			} catch (XPathExpressionException e) {
				logger.error("Failed to parse group data");
			}
		}
		Set<Integer> fields = groupTags.get(msgType);
		if(fields == null){
			// Get MessageTypeId from MessageTypeName ie NewOrderSingle -> D
			msgId = getMsgType(msgType);
			fields = groupTags.get(msgId);
		}
		if (fields != null)
			return fields.contains(tag_ing);
		return false;
	}
}
