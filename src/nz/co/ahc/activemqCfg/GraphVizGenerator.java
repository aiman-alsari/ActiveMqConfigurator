package nz.co.ahc.activemqCfg;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;


public class GraphVizGenerator {

	private final List<Node> nodes;
	private File file;
	

	public GraphVizGenerator(List<Node> nodes) {
		this.nodes = nodes;
	}
	
	public void draw() {
		init();
		header();
		drawNodes();
		drawLinks();
		footer();
	}
	
	private void drawLinks() {
		for(Node n : nodes) {
			drawInternalLinks(n);
			drawExternalLinks(n);
		}
	}

	private void drawExternalLinks(Node n) {
		if(n.getCountOfSecondaryJmsServers() > 0 || !n.getNodesToBackupList().isEmpty()) {
			write("	//"+n.getName()+" External Edges\n");
		} else {
			return;
		}
		for(DataCentre dc : n.getDataCentres()) {
			for(JmsServer server : dc.getJmsServers()) {
				if(server.isSecondary()) {
					linkForwardToServers(server);
				}
			}
		}
		linkBackupNodes(n);
		
		write("\n");
	}

	private void linkBackupNodes(Node n) {
		//We always receive into our primary datacentres public servers, and it comes from the remote nodes primary public servers
		List<JmsServer> remotePublicPrimaries = new ArrayList<JmsServer>();
		for(Node remote : n.getNodesToBackupList()) {
			remotePublicPrimaries.addAll(remote.getPrimaryPublicJmsServers());
		}
		List<JmsServer> localPublicPrimaries = n.getPrimaryPublicJmsServers();
		for(JmsServer left : remotePublicPrimaries) {
			for(JmsServer right : localPublicPrimaries) {
				write("	"+left.getBrokerNameUnderscore()+"->"+right.getBrokerNameUnderscore()+";\n");
			}
		}
	}

	private void drawInternalLinks(Node n) {
		if(n.getCountOfAllJmsServers() == 1) {
			return;
		}
		write("	//"+n.getName()+" Internal Edges\n");
		for(DataCentre dc : n.getDataCentres()) {
			linkServersWithinDatacentre(dc);
		}
	}

	private void linkForwardToServers(JmsServer left) {
		DataCentre dc = left.getDatacentreToForwardTo();
		List<JmsServer> publics = dc.getPublicJmsServers();
		for(JmsServer right : publics) {
			write("	"+left.getBrokerNameUnderscore()+"->"+right.getBrokerNameUnderscore()+";\n");
		}
	}

	private void linkServersWithinDatacentre(DataCentre dc) {
		for(JmsServer left : dc.getJmsServers()) {
			for(JmsServer right : dc.getJmsServers()) {
				if(left != right) {
					write("	"+left.getBrokerNameUnderscore()+"->"+right.getBrokerNameUnderscore()+";\n");
				}
			}
		}
		if(dc.getJmsServers().size() > 1) {
			write("\n");
		}
	}

	private void footer() {
		write("}");
	}

	private void drawNodes() {
		for(Node n : nodes) {
			drawNode(n);
		}
	}

	private void drawNode(Node n) {
		write(
			"	subgraph cluster_"+n.getName()+" {\n" + 
			"		label=\""+n.getName()+"\";\n");
		for(DataCentre dc : n.getDataCentres()) {
			drawDataCentre(dc);
		}
		write("	}\n\n");
	}

	private void drawDataCentre(DataCentre dc) {
		write(
				"		subgraph cluster_"+dc.getBrokerNameUnderscore()+" {\n" + 
				"			label=\""+dc.getName()+"\";\n");
		for(JmsServer server : dc.getJmsServers()) {
			drawJmsServer(server);
		}
		drawRankingLayout(dc.getJmsServers());
		write(
				"		}\n");
	}

	private void drawRankingLayout(List<JmsServer> list) {
		if(list.size() < 2) {
			return;
		}
		int rowWidth = (int) Math.ceil(Math.sqrt(list.size()));
		write("\n");
		for(int i = 0; i < list.size(); i = i + rowWidth) {
			JmsServer[] row = new JmsServer[rowWidth];
			for(int j = 0; j < rowWidth; j++) {
				if(i+j == list.size()) {
					break;
				}
				row[j] = list.get(i+j);
			}
			write(getRowStringForRanking(row));
		}
		write("\n");
	}

	private String getRowStringForRanking(JmsServer[] row) {
		String ret = "            { rank = same; ";
		for(JmsServer server : row) {
			//node1-dc1-jms1; node1-dc1-jms2;
			if(server != null) {
				ret += server.getBrokerNameUnderscore()+"; "; 
			}
		}
		ret += "}\n";
		
		if(ret.split(";").length < 4) {
			//This means we dont have at least 2 servers, so we dont need a ranking string.
			return "";
		}
		return ret;
	}

	private void drawJmsServer(JmsServer server) {
		String name = server.getName();
		if(server.isSecondary()) {
			name += " (secondary)";
		}
		write(
				"			node [label=\""+name+"\" ] "+server.getBrokerNameUnderscore()+";\n");
	}

	private void header() {
		write(
				"digraph callgraph {\n" + 
				"	concentrate=true;\n" + 
				"	nodesep=1.0; \n" + 
				"	splines=true; \n" + 
				"	ranksep=\"1.5 equally\";\n" + 
				"	node [fontname=\"verdana\"];\n" + 
				"	fontname=\"Verdana\";\n\n");
	}

	private void init() {
		this.file = new File("visualisation.gv");
		try {
			file.createNewFile();
			Files.write("", file, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void write(String toWrite){
		try {
			Files.append(toWrite, file, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
}
