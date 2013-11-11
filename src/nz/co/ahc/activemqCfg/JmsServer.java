package nz.co.ahc.activemqCfg;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;


public class JmsServer {

	private String name;
	private DataCentre dataCentre;
	private String ipAddress;
	private boolean isPublic;
	private String forwardTo = "";

	@SuppressWarnings("unused")
	private JmsServer(){}
	
	public JmsServer(String name, String ip, boolean isPublic) {
		this.name = name;
		ipAddress = ip;
		this.isPublic = isPublic;
	}

	@JsonIgnore
	public String getBrokerName() {
		return getDataCentre().getBrokerName() + "-" + name;
	}
	
	@JsonIgnore
	public String getBrokerNameUnderscore() {
		return getBrokerName().replaceAll("-", "_");
	}

	@JsonIgnore Node getNode() {
		return getDataCentre().getNode();
	}

	@JsonIgnore
	private DataCentre getDataCentre() {
		return dataCentre;
	}
	
	public void setDataCentre(DataCentre dataCentre) {
		this.dataCentre = dataCentre;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public boolean isPublic() {
		return isPublic;
	}
	
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@JsonIgnore
	public boolean isSecondary() {
		return !"".equals(forwardTo);
	}
	
	public String getForwardTo() {
		return forwardTo;
	}

	public void setForwardTo(String forwardTo) {
		this.forwardTo = forwardTo;
	}

	@JsonIgnore
	public String getForwardToIps() {
		if("".equals(forwardTo)) {
			return "";
		}
		DataCentre forwardToDc = getDatacentreToForwardTo();
		List<JmsServer> publicServers = forwardToDc.getPublicJmsServers();
		if(publicServers.isEmpty()) {
			throw new IllegalArgumentException("Forwarding to a datacentre with no public JMS servers ("+ forwardTo +") will not work.");
		}
		String ips = "";
		for(JmsServer server : publicServers) {
			String ip = server.getIpAddress();
			String prefix = (ips.length() == 0 ? "" : ",") + "ssl://";
			ips += prefix + ip;
		}
		return ips;
	}

	@JsonIgnore
	public DataCentre getDatacentreToForwardTo() {
		if("".equals(forwardTo)) {
			return null;
		}
		String[] split = forwardTo.split("\\.");
		if(forwardTo.indexOf('.') < 0 || split.length != 2) {
			throw new IllegalArgumentException("\nInvalid syntax for forwardTo: \"" + forwardTo + "\"\nThe correct format should be 'NodeName.DataCentre' e.g. 'node1.dc1'");
		}
		String nodeName = split[0];
		String dataCentreName = split[1];
		Node forwardToNode = NodeRepository.getNode(nodeName);
		DataCentre forwardToDc = forwardToNode.getDataCentreByName(dataCentreName);
		if(forwardToDc.getName().equals(getName())) {
			throw new IllegalArgumentException("You can not forward to the same data centre ("+ forwardTo +"). This is ludicrous.");
		}
		return forwardToDc;
	}
}
