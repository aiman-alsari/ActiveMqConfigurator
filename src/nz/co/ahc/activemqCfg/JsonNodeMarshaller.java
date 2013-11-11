package nz.co.ahc.activemqCfg;
import java.io.File;
import java.util.List;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class JsonNodeMarshaller {

	public File marshall(Node... nodes) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			File marshalledFile = new File("activemqConfigurator.json");
			mapper.defaultPrettyPrintingWriter().writeValue(marshalledFile,
					nodes);
			return marshalledFile;
		} catch (Exception e) {
			throw new RuntimeException("WTF", e);
		}
	}

	public List<Node> unmarshall(File jsonFile) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		try {
			List<Node> readValue = mapper.readValue(jsonFile, new TypeReference<List<Node>>() {});
			return readValue;
		} catch (Exception e) {
			throw new RuntimeException("Error parsing the json file, please check your syntax", e);
		}
	}

}
