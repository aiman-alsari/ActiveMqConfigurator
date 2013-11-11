package nz.co.ahc.activemqCfg;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;


public class DataCentre {

	private String name;
	private Node node;
	private List<JmsServer> jmsServers = new ArrayList<JmsServer>();
	
	public DataCentre(String name) {
		this.name = name;
	}
	
	@SuppressWarnings("unused")
	private DataCentre() {
	}
	
	public List<JmsServer> getJmsServers() {
		//HACK... Jackson doesnt go through the 'addJmsServer' method.
		for(JmsServer s : jmsServers) {
			s.setDataCentre(this);
		}
		return jmsServers;
	}

	public void addJmsServer(JmsServer server) {
		server.setDataCentre(this);
		jmsServers.add(server);
	}

	public String getName() {
		return name;
	}

	@JsonIgnore
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@JsonIgnore
	public String getBrokerName() {
		return getNode().getName() + "-" + getName();
	}
	
	@JsonIgnore
	public String getBrokerNameUnderscore() {
		return getBrokerName().replaceAll("-", "_");
	}

	@JsonIgnore
	public List<JmsServer> getPublicJmsServers() {
		List<JmsServer> publics = new ArrayList<JmsServer>();
		for(JmsServer server : getJmsServers()) {
			if(server.isPublic()) {
				publics.add(server);
			}
		}
		return publics;
	}

}
