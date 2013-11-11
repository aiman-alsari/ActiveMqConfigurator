package nz.co.ahc.activemqCfg;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

	
public class Node {

	private String name;
	private List<DataCentre> dataCentres = new ArrayList<DataCentre>();
	private List<String> nodesToBackupNames = new ArrayList<String>();
	private List<Node> nodesToBackup = new ArrayList<Node>();

	private Node(){
		NodeRepository.addNode(this);
	}
	
	public Node(String name) {
		this();
		this.name = name;
	}
	
	@JsonIgnore
	public List<Node> getNodesToBackupList() {
		if(nodesToBackup.isEmpty() && !nodesToBackupNames.isEmpty()){
			for(String name : nodesToBackupNames) {
				if(name != null && name.length() > 0) {
					nodesToBackup.add(NodeRepository.getNode(name));
				}
			}
		}
		return nodesToBackup;
	}
	
	//Dont use, Jackson only.
	public List<String> getNodesToBackup(){
		List<String> ret = new ArrayList<String>();
		for(Node n : getNodesToBackupList()){
			ret.add(n.getName());
		}
		return ret;
	}
	
	public List<DataCentre> getDataCentres() {
		//HACK... Jackson doesnt go through the 'addDataCentre' method.
		for(DataCentre dc : dataCentres) {
			dc.setNode(this);
		}
		return dataCentres;
	}
	
	@SuppressWarnings("unused") //JSON
	private void setNodesToBackup(List<String> nodesToBackup){
		nodesToBackupNames = nodesToBackup;
	}

	public void addDataCentre(DataCentre dc) {
		dc.setNode(this);
		dataCentres.add(dc);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public DataCentre getDataCentreByName(String dataCentreName) {
		for(DataCentre dc : dataCentres) {
			if(dc.getName().equals(dataCentreName)) {
				return dc;
			}
		}
		throw new IllegalArgumentException("No such datacentre: " + dataCentreName + " belonging to Node: " + getName());
	}

	@JsonIgnore
	public int getCountOfAllJmsServers() {
		int ret = 0;
		for(DataCentre dc : dataCentres) {
			ret += dc.getJmsServers().size();
		}
		return ret;
	}

	@JsonIgnore
	public int getCountOfSecondaryJmsServers() {
		int ret = 0;
		for(DataCentre dc : dataCentres) {
			for(JmsServer s : dc.getJmsServers()) {
				if(s.isSecondary()) {
					ret++;
				}
			}
		}
		return ret;
	}

	@JsonIgnore
	public List<JmsServer> getPrimaryPublicJmsServers() {
		ArrayList<JmsServer> list = new ArrayList<JmsServer>();
		for(JmsServer server : dataCentres.get(0).getJmsServers()) {
			if(server.isPublic()) {
				list.add(server);
			}
		}
		return list;
	}
	
}
