package nz.co.ahc.activemqCfg;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;


public class ConfigFile {

	private final File file;
	private final JmsServer server;

	public ConfigFile(JmsServer server, String fileName){
		this.server = server;
		this.file = new File(fileName);
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

	private void writeHeader() {
		String header = 
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:amq=\"http://activemq.apache.org/schema/core\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
						"	xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\n" + 
						"  http://activemq.apache.org/schema/core http://activemq.apache.org/schema/core/activemq-core.xsd\">\n" + 
						"\n" + 
						"	<bean class=\"org.springframework.beans.factory.config.PropertyPlaceholderConfigurer\">\n" + 
						"		<property name=\"locations\">\n" + 
						"			<value>file:${activemq.base}/conf/credentials.properties</value>\n" + 
						"		</property>\n" + 
						"	</bean>\n" + 
						"\n" + 
						"	<broker xmlns=\"http://activemq.apache.org/schema/core\" brokerName=\"" + server.getBrokerName() + "\" dataDirectory=\"${activemq.base}/data/" + server.getBrokerName() + "\" destroyApplicationContextOnStop=\"true\">\n" + 
						"\n" + 
						"		<destinationPolicy>\n" + 
						"			<policyMap>\n" + 
						"				<policyEntries>\n" + 
						"					<policyEntry topic=\">\" producerFlowControl=\"true\" memoryLimit=\"1mb\">\n" + 
						"						<pendingSubscriberPolicy>\n" + 
						"							<vmCursor />\n" + 
						"						</pendingSubscriberPolicy>\n" + 
						"					</policyEntry>\n" + 
						"\n" + 
						"					<!-- \n" + 
						"						All backend queues should continue writing to disk when running out of memory. \n" + 
						"						They should not block producers. This allows the backend or DB to be down \n" + 
						"						for days on end without loosing any data. \n" + 
						"					-->\n" + 
						"					<policyEntry queue=\"backend.>\" producerFlowControl=\"false\" memoryLimit=\"128mb\">\n" + 
						"					</policyEntry>\n" + 
						"\n" + 
						"				</policyEntries>\n" + 
						"			</policyMap>\n" + 
						"		</destinationPolicy>\n" + 
						"";
		write(header);
	}

	private void writeQueueList(List<Node> allNodes) {
		String queueList = "\n" + 
				"		<destinations>\n" + 
				"			<!-- \n" + 
				"				While it is possible to allow ActiveMQ to create queues on-demand, \n" + 
				"				for security purposes we've locked it down so queues must be \n" + 
				"				explicitly named. \n" + 
				"			-->\n"; 

		for(Node n : allNodes){
			queueList += "			<queue physicalName=\"backend."+n.getName()+"\" />\n";
		}

		queueList +=
				"		</destinations>\n\n";
		write(queueList);
	}

	//remoteNodes is the list of nodes that this node backs up.
	private void writeRemoteConnectors(List<Node> remoteNodes) {
		String toWrite = "";
		if(server.isSecondary()) {
			//It wants to forward all its messages to another DC
			toWrite = remoteDataCentreBridgedConnectors(remoteNodes);
		} else if (!remoteNodes.isEmpty()) {
			//It wants to forward its messages appropriately.
			toWrite = remoteNodeBridgedConnectors(remoteNodes);
		} //If its neither of these, then it doesnt need to do anything with remote servers.
		toWrite += 
					"		<managementContext>\n" + 
					"			<managementContext createConnector=\"false\" />\n" + 
					"		</managementContext>\n\n";
		
		write(toWrite);
	}

	private String remoteDataCentreBridgedConnectors(List<Node> remoteNodes) {
		String toWrite = "";
		List<Node> nodesOfMessagesToForward = new ArrayList<Node>(remoteNodes);
		nodesOfMessagesToForward.add(server.getNode());
		toWrite += 
				"		<jmsBridgeConnectors>\n" + 
				"              <!-- \n" + 
				"                      Defines JMS to JMS Bridges, which store-and-forward any data destined for other backends \n" + 
				"                      In this case, we are the secondary DC, so we forward all data to the primary DC\n" + 
				"                      which will then on-forward to the appropriate backend.\n" + 
				"              -->\n"; 
		
			toWrite +=
				"              <jmsQueueConnector localQueueConnectionFactory=\"#localFactory\" outboundQueueConnectionFactory=\"#primaryFactory\" localUsername=\"system\"\n" + 
				"                      localPassword=\"${system.password}\" outboundUsername=\"system\" outboundPassword=\"${system.password}\">\n" + 
				"                      <outboundQueueBridges>\n";
			for(Node n : nodesOfMessagesToForward) {
				toWrite += 
				"                              <outboundQueueBridge outboundQueueName=\"backend."+n.getName()+"\" localQueueName=\"backend."+n.getName()+"\" />\n";
			}
			toWrite +=
				"                      </outboundQueueBridges>\n" + 
				"              </jmsQueueConnector>\n" + 
				"      	</jmsBridgeConnectors>\n\n"; 
		return toWrite;
	}

	private String remoteNodeBridgedConnectors(List<Node> remoteNodes) {
		String toWrite = "";
		toWrite += 
				"		<jmsBridgeConnectors>\n" + 
				"			<!-- Defines JMS to JMS Bridges, which store-and-forward any data destined for other backends -->\n";

		for(Node n : remoteNodes){
			String name = n.getName();
			toWrite +=
				"			<jmsQueueConnector localQueueConnectionFactory=\"#localFactory\" outboundQueueConnectionFactory=\"#"+name+"Factory\" localUsername=\"system\"\n" + 
				"				localPassword=\"${system.password}\" outboundUsername=\"system\" outboundPassword=\"${system.password}\">\n" + 
				"				<outboundQueueBridges>\n" + 
				"					<outboundQueueBridge outboundQueueName=\"backend."+name+"\" localQueueName=\"backend."+name+"\" />\n" + 
				"				</outboundQueueBridges>\n" + 
				"			</jmsQueueConnector>\n";
		}

		toWrite+= 
				"		</jmsBridgeConnectors>\n" + 
						"\n";
		return toWrite;
	}

	private void writeLocalConnectors(List<JmsServer> otherServers) {
		if(otherServers.isEmpty()){
			return;
		}
		String localConnections =
				"		<networkConnectors>\n" + 
				"			<!-- This section establishes a network of brokers Add connections to all other JMS servers within the same datacentre for high availability -->\n";
		for(JmsServer server : otherServers){
			localConnections += 
					"			<networkConnector userName=\"system\" password=\"${system.password}\" dynamicOnly=\"true\" prefetchSize=\"1\" networkTTL=\"5\"\n" + 
							"				uri=\"static:(ssl://"+server.getIpAddress()+")\" duplex=\"false\" />\n";
		}
		localConnections += "		</networkConnectors>\n\n";
		write(localConnections);
	}

	private void writeKahaDb() {
		String persistence = "		<persistenceAdapter>\n" + 
				"			<!-- Defines where persistent data is stored locally -->\n" + 
				"			<kahaDB directory=\"${activemq.base}/data/"+server.getBrokerName()+"/db\" />\n" + 
				"		</persistenceAdapter>\n";
		write(persistence);
	}

	private void writeAuthentication() {
		String auth = "\n" + 
				"		<plugins>\n" + 
				"			<simpleAuthenticationPlugin>\n" + 
				"				<users>\n" + 
				"					<authenticationUser username=\"system\" password=\"${system.password}\" groups=\"producers,consumers,admins\" />\n" + 
				"					<authenticationUser username=\"producer\" password=\"${producer.password}\" groups=\"producers\" />\n" + 
				"					<authenticationUser username=\"consumer\" password=\"${consumer.password}\" groups=\"consumers\" />\n" + 
				"				</users>\n" + 
				"			</simpleAuthenticationPlugin>\n" + 
				"\n" + 
				"			<authorizationPlugin>\n" + 
				"				<map>\n" + 
				"					<authorizationMap>\n" + 
				"						<authorizationEntries>\n" + 
				"							<authorizationEntry topic=\">\" read=\"admins\" write=\"admins\" admin=\"admins\" />\n" + 
				"							<authorizationEntry queue=\">\" read=\"admins\" write=\"admins\" admin=\"admins\" />\n" + 
				"							<authorizationEntry queue=\"backend.>\" read=\"consumers\" write=\"producers\" admin=\"admins\" />\n" + 
				"							<authorizationEntry topic=\"ActiveMQ.Advisory.>\" read=\"producers,consumers\" write=\"producers,consumers\" admin=\"producers,consumers\" />\n" + 
				"						</authorizationEntries>\n" + 
				"					</authorizationMap>\n" + 
				"				</map>\n" + 
				"			</authorizationPlugin>\n" + 
				"		</plugins>\n\n";
		write(auth);
	}

	private void writeMemoryOptions() {
		String toWrite = "		<systemUsage>\n" + 
				"			<systemUsage>\n" + 
				"				<memoryUsage>\n" + 
				"					<memoryUsage limit=\"512 mb\" />\n" + 
				"				</memoryUsage>\n" + 
				"				<storeUsage>\n" + 
				"					<storeUsage limit=\"50 gb\" />\n" + 
				"				</storeUsage>\n" + 
				"				<tempUsage>\n" + 
				"					<tempUsage limit=\"5 gb\" />\n" + 
				"				</tempUsage>\n" + 
				"			</systemUsage>\n" + 
				"		</systemUsage>\n" + 
				"\n";
		write(toWrite);
	}

	private void writeServerIpAndSSLStuff(){
		String toWrite =
				"		<transportConnectors>\n" + 
				"			<!-- Defines what ports this JMS server operates on -->\n" + 
				"			<transportConnector name=\"ssl\" uri=\"ssl://"+server.getIpAddress()+"?transport.enabledCipherSuites=TLS_RSA_WITH_AES_256_CBC_SHA\" />\n" + 
				"		</transportConnectors>\n" + 
				"\n" + 
				"	</broker>\n\n";
		write(toWrite);
	}

	private void writeRemoteFactories(List<Node> remoteNodes){
		if(server.isSecondary()) {
			return;
		}
		for(Node n : remoteNodes){
			DataCentre primaryDataCentre = n.getDataCentres().get(0);
			
			String ips = "";
			for(JmsServer server : primaryDataCentre.getJmsServers()) {
				if(!server.isPublic()) { 
					continue; 
				}
				//Validation should have caught out the scenario where there are no public ips.
				//So there will always be something in "ips" hopefully.
				String sslAddress = "ssl://" + server.getIpAddress();
				ips += ips.isEmpty() ? sslAddress : "," + sslAddress;
			}
			                                                                                                                            
			write(
					"	<bean id=\""+n.getName()+"Factory\" class=\"org.apache.activemq.ActiveMQConnectionFactory\">\n" + 
					"		<property name=\"brokerURL\" value=\"failover:("+ips+")\" />\n" + 
					"		<property name=\"userName\" value=\"system\" />\n" + 
					"		<property name=\"password\" value=\"${system.password}\" />\n" + 
					"	</bean>\n\n" + 
					"");
		}
	}
	
	private void writeLocalFactory(){
		write(
				"	<bean id=\"localFactory\" class=\"org.apache.activemq.ActiveMQConnectionFactory\">\n" + 
				"		<property name=\"brokerURL\" value=\"vm://localhost:61616?create=false&amp;waitForStart=10000\" />\n" + 
				"		<property name=\"userName\" value=\"system\" />\n" + 
				"		<property name=\"password\" value=\"${system.password}\" />\n" + 
				"	</bean>\n\n");
	}
	
	private void writeLocalNodeBackupFactory() {
		String forwardTo = server.getForwardToIps();
		if(!forwardTo.isEmpty()) {
			write(
					"    <!-- This is used by secondary backup datacentres to forward their messages to the primary datacentre's queue -->\n" + 
					"    <bean id=\"primaryFactory\" class=\"org.apache.activemq.ActiveMQConnectionFactory\">\n" + 
					"        <property name=\"brokerURL\" value=\"failover:("+forwardTo+")\" />\n" + 
					"        <property name=\"userName\" value=\"system\" />\n" + 
					"        <property name=\"password\" value=\"${system.password}\" />\n" + 
					"    </bean>\n\n");
		}
	}

	private void writeFooter() {
		write("	<!-- Remove below if you don't want to run the web console -->\n" + 
				"	<import resource=\"jetty.xml\" />\n" + 
				"</beans>");		
	}

	/*Following structure:
	  header
	  queue list
	  remote connectors
	  local connectors 
	  kahadb
	  authentication
	  systemusage
	  ssl port
	  remote factory beans
	  jetty import
	  footer
	*/ 
	public void create(List<Node> allNodes, List<Node> remoteNodes, List<JmsServer> otherDataCentreServers ) {
		writeHeader();
		writeQueueList(allNodes);
		writeRemoteConnectors(remoteNodes);
		writeLocalConnectors(otherDataCentreServers);
		writeKahaDb();
		writeAuthentication();
		writeMemoryOptions();
		writeServerIpAndSSLStuff();
		writeRemoteFactories(remoteNodes);
		writeLocalNodeBackupFactory();
		writeLocalFactory();
		writeFooter();		
	}

}
