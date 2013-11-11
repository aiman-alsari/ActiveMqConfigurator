package nz.co.ahc.activemqCfg;
import java.io.File;
import java.util.List;


public class Amqcfg {

	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Usage: java -jar Amqcfg.jar <PATH_TO_JSON_FILE>");
			System.exit(1);
		}
		File jsonFile = new File(args[0]);
		List<Node> nodes = new JsonNodeMarshaller().unmarshall(jsonFile);
		
		ActiveMqConfigurator cfg = new ActiveMqConfigurator(nodes);
		cfg.createStartScript();
		cfg.createConfigs();
		
		GraphVizGenerator gen = new GraphVizGenerator(nodes);
		gen.draw();
		
		System.out.println("Finished.\nConfig files created in \"conf\" directory.\nStartup script (activemq-instance) and GraphViz file created here.\n\n" +
				"To see the graph run: dot -Tpdf -O visualisation.gv");
	}
	
}
