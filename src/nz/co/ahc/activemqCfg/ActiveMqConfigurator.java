package nz.co.ahc.activemqCfg;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.io.Files;


public class ActiveMqConfigurator {

	private List<Node> nodes;

	public ActiveMqConfigurator(List<Node> nodes) {
		this.nodes = nodes;
	}

	public void createStartScript(){
		try {
			File startInstanceScript = new File("activemq-instance");
			startInstanceScript.createNewFile();
			Files.write("#!/bin/sh\n" +
					"# Usage: activemq-instance instance_name start 8100\n"+
					"# Where instance_name is the instance you wish to start, and 8100 is the port number you want the jetty diagnostics server to run on.\n"+
					"INSTANCE_NAME=$1\n"+
					"echo \"Invoking $2 on ${INSTANCE_NAME}\"\n"+
					"export ACTIVEMQ_DATA_DIR=/opt/amq/data/${INSTANCE_NAME}/\n"+
					"export ACTIVEMQ_OPTS=\"-Djetty.port=$3 -Dinstance.name=${INSTANCE_NAME}\"\n"+
					"bin/activemq $2 xbean:conf/activemq-${INSTANCE_NAME}.xml\n"
					, startInstanceScript, Charset.defaultCharset());


		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void validate() {
		isBackupListValid();
	}

	public void createConfigs() {
		if(!nodes.isEmpty()){
			new File("conf").mkdir();
		}
		for(Node n : nodes){
			System.out.println("Creating configs for node: " + n.getName());
			createConfig(n);
		}
	}

	private void createConfig(Node n) {
		for(DataCentre dc : n.getDataCentres()){
			for(JmsServer server : dc.getJmsServers()){
				List<JmsServer> otherServers = new ArrayList<JmsServer>(dc.getJmsServers());
				otherServers.remove(server);
				removeForwardingOnlyServers(otherServers);
				String brokerName = server.getBrokerName();
				List<Node> remoteNodes = n.getNodesToBackupList();
				List<Node> allNodes = new ArrayList<Node>(remoteNodes);
				allNodes.add(n);
				
				ConfigFile file = new ConfigFile(server, "conf/activemq-" + brokerName + ".xml");
				file.create(allNodes, remoteNodes, otherServers);
			}
		}
	}
	
	private void removeForwardingOnlyServers(List<JmsServer> otherServers) {
		for(Iterator<JmsServer> it = otherServers.iterator(); it.hasNext(); ) {
			JmsServer serv = it.next();
			if(!serv.getForwardTo().isEmpty()) {
				it.remove();
			}
		}
	}

	private boolean isBackupListValid() {
		Set<Node> nodesBeingBackedUp = new HashSet<Node>();
		for(Node n : nodes) {
			nodesBeingBackedUp.addAll(n.getNodesToBackupList());
		}
		return isListContainsPublicJmsServer(nodesBeingBackedUp);
	}

	private boolean isListContainsPublicJmsServer(Set<Node> nodesBeingBackedUp) {
		for(Node n : nodesBeingBackedUp) {
			for(DataCentre dc : n.getDataCentres()) {
				for(JmsServer jms : dc.getJmsServers()) {
					if(jms.isPublic()) {
						return true;
					}
				}
			}
			throw new IllegalArgumentException("Node " + n.getName() + " is being backed up by another node, but does not have a public JMS server.");
		}
		return false;
	}

}
