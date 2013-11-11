package nz.co.ahc.activemqCfg;
import java.util.HashSet;
import java.util.Set;


public class NodeRepository {

	private static Set<Node> repository = new HashSet<Node>();

	public static Node getNode(String name) {
		for(Node n : repository) {
			if(n.getName().trim().equalsIgnoreCase(name)) {
				return n;
			}
		}
		throw new IllegalArgumentException("Unknown node: " + name);
	}

	public static void addNode(Node node) {
		repository.add(node);
	}
	
	public static void clear() {
		repository.clear();
	}
	
}
