package nz.co.ahc.activemqCfg;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junitx.framework.FileAssert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class JsonMarshallerTest {
	
	@Before
	public void clearRepository() {
		NodeRepository.clear();
	}
	
	@After //Dont know why I have to do this, but it seems to fail on a test run of the entire package if I dont do it.
	public void clearRepositoryAgain() {
		NodeRepository.clear();
	}

	@Test
	public void testMarshallOneJmsServer() throws IOException {
		Node node1 = new Node("node1");
		DataCentre dc1 = new DataCentre("dc1");
		JmsServer jms = new JmsServer("jms1", "127.0.0.1:60601", true);
		
		node1.addDataCentre(dc1);
		dc1.addJmsServer(jms);
		
		JsonNodeMarshaller marshaller = new JsonNodeMarshaller();
		File marshalled = marshaller.marshall(node1);
		FileAssert.assertEquals(new File("test/expectations/activemq-node1.json"), marshalled);
		
		List<Node> nodes = marshaller.unmarshall(marshalled);
		assertEquals(1, nodes.size());
		Node loadedNode = nodes.get(0);
		assertEquals("node1", loadedNode.getName());
		assertEquals(0, loadedNode.getNodesToBackupList().size());
		assertEquals(1, loadedNode.getDataCentres().size());
		DataCentre loadedDc = loadedNode.getDataCentres().get(0);
		assertEquals("dc1", loadedDc.getName());
		assertEquals(1, loadedDc.getJmsServers().size());
		JmsServer loadedJms = loadedDc.getJmsServers().get(0);
		assertEquals("jms1", loadedJms.getName());
		assertEquals("127.0.0.1:60601", loadedJms.getIpAddress());
		assertTrue(loadedJms.isPublic());
	}
	
	@Test
	public void testMarshallMultipleNodesWithMultipleJmsServers() throws IOException {
		Node node1 = new Node("node1");
		DataCentre dc1 = new DataCentre("dc1");
		DataCentre backup = new DataCentre("backup");
		JmsServer jms = new JmsServer("jms1", "127.0.0.1:60601", true);
		JmsServer jms2 = new JmsServer("jms2", "127.0.0.1:60602", true);
		JmsServer jms3 = new JmsServer("jms3", "127.0.0.1:60603", false);
		JmsServer jms4 = new JmsServer("jms4", "127.0.0.1:60604", false);
		node1.addDataCentre(dc1);
		node1.addDataCentre(backup);
		dc1.addJmsServer(jms);
		dc1.addJmsServer(jms2);
		backup.addJmsServer(jms3);
		backup.addJmsServer(jms4);
		
		Node node2 = new Node("node2");
		DataCentre node2dc1 = new DataCentre("dc1");
		DataCentre node2dc2 = new DataCentre("dc2");
		JmsServer jmsNode2 = new JmsServer("jms1", "127.0.0.1:60605", true);
		JmsServer jms2Node2 = new JmsServer("jms2", "127.0.0.1:60606", false);
		JmsServer jms3Node2 = new JmsServer("jms3", "127.0.0.1:60607", false);
		JmsServer jms4Node2 = new JmsServer("jms4", "127.0.0.1:60608", false);
		node2.addDataCentre(node2dc1);
		node2.addDataCentre(node2dc2);
		node2dc1.addJmsServer(jmsNode2);
		node2dc1.addJmsServer(jms2Node2);
		node2dc2.addJmsServer(jms3Node2);
		node2dc2.addJmsServer(jms4Node2);
		
		node1.getNodesToBackupList().add(node2);
		
		JsonNodeMarshaller marshaller = new JsonNodeMarshaller();
		File marshalled = marshaller.marshall(node1, node2);
		FileAssert.assertEquals(new File("test/expectations/activemq-two.json"), marshalled);
		
		List<Node> nodes = marshaller.unmarshall(marshalled);
		assertEquals(2, nodes.size());
		Node loadedNode1 = nodes.get(0);
		assertEquals("node1", loadedNode1.getName());
		assertEquals("node2", loadedNode1.getNodesToBackupList().get(0).getName());
		assertEquals(2, loadedNode1.getDataCentres().size());
		DataCentre loadedDc = loadedNode1.getDataCentres().get(0);
		assertEquals("dc1", loadedDc.getName());
		assertEquals(2, loadedDc.getJmsServers().size());
		JmsServer loadedJms = loadedDc.getJmsServers().get(0);
		assertEquals("jms1", loadedJms.getName());
		assertEquals("127.0.0.1:60601", loadedJms.getIpAddress());
		assertTrue(loadedJms.isPublic());
	}
	
	@Test
	public void testParsingJsonFileTestInput1() {
		JsonNodeMarshaller marshaller = new JsonNodeMarshaller();
		List<Node> nodes = marshaller.unmarshall(new File("test/expectations/test-input1.json"));
		
		assertEquals(1, nodes.size());
		Node loadedNode = nodes.get(0);
		assertEquals("node1", loadedNode.getName());
		assertEquals(0, loadedNode.getNodesToBackupList().size());
		assertEquals(1, loadedNode.getDataCentres().size());
		DataCentre loadedDc = loadedNode.getDataCentres().get(0);
		assertEquals("dc1", loadedDc.getName());
		assertEquals(1, loadedDc.getJmsServers().size());
		JmsServer loadedJms = loadedDc.getJmsServers().get(0);
		assertEquals("jms1", loadedJms.getName());
		assertEquals("127.0.0.1:60601", loadedJms.getIpAddress());
		assertTrue(loadedJms.isPublic());
	}
	
	@Test
	public void testParsingJsonFileTestInput2() {
		JsonNodeMarshaller marshaller = new JsonNodeMarshaller();
		List<Node> nodes = marshaller.unmarshall(new File("test/expectations/test-input2.json"));
		
		assertEquals(1, nodes.size());
		Node loadedNode = nodes.get(0);
		assertEquals("node1", loadedNode.getName());
		assertEquals(0, loadedNode.getNodesToBackupList().size());
		assertEquals(1, loadedNode.getDataCentres().size());
		DataCentre loadedDc = loadedNode.getDataCentres().get(0);
		assertEquals("dc1", loadedDc.getName());
		assertEquals(1, loadedDc.getJmsServers().size());
		JmsServer loadedJms = loadedDc.getJmsServers().get(0);
		assertEquals("jms1", loadedJms.getName());
		assertEquals("127.0.0.1:60601", loadedJms.getIpAddress());
		assertFalse(loadedJms.isPublic());
	}
	
	@Test
	public void testParsingJsonFileTestInput3() {
		JsonNodeMarshaller marshaller = new JsonNodeMarshaller();
		List<Node> nodes = marshaller.unmarshall(new File("test/expectations/test-input3.json"));
		
		assertEquals(4, nodes.size());
		Node loadedNode = nodes.get(0);
		assertEquals("node1", loadedNode.getName());
		assertEquals(3, loadedNode.getNodesToBackupList().size());
		assertEquals(2, loadedNode.getDataCentres().size());
		DataCentre loadedDc = loadedNode.getDataCentres().get(0);
		assertEquals("dc1", loadedDc.getName());
		assertEquals(3, loadedDc.getJmsServers().size());
		JmsServer loadedJms = loadedDc.getJmsServers().get(0);
		assertEquals("jms1", loadedJms.getName());
		assertEquals("node1-dc1-jms1", loadedJms.getBrokerName());
		assertEquals("202.123.123.123:60601", loadedJms.getIpAddress());
		assertTrue(loadedJms.isPublic());
		
		DataCentre loadedBackup = loadedNode.getDataCentres().get(1);
		assertEquals("node1.dc1", loadedBackup.getJmsServers().get(0).getForwardTo());
		assertEquals("ssl://202.123.123.123:60601", loadedBackup.getJmsServers().get(0).getForwardToIps());
	}
	
}
