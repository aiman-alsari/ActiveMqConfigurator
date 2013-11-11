package nz.co.ahc.activemqCfg;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import junitx.framework.FileAssert;

import org.junit.Before;
import org.junit.Test;


public class ConfigurationTest {
	
	@Before
	public void clean() {
		NodeRepository.clear();
	}
	
	@Test
	public void testCreateStartScripts() throws IOException{
		ActiveMqConfigurator cfger = new ActiveMqConfigurator(new ArrayList<Node>());
		cfger.createStartScript();
		
		File instanceStartScript = new File("activemq-instance");
		
		assertTrue(instanceStartScript.exists());
		
		//check the contents
		FileAssert.assertEquals(new File("test/expectations/activemq-instance"), instanceStartScript);
	}
	
	@Test
	public void testCreateConfigsWithOneJmsServer() {
		Node node1 = new Node("node1");
		DataCentre dc1 = new DataCentre("dc1");
		JmsServer jms = new JmsServer("jms1", "127.0.0.1:60601", true);
		
		node1.addDataCentre(dc1);
		dc1.addJmsServer(jms);
		
		ActiveMqConfigurator cfger = new ActiveMqConfigurator(Arrays.asList(node1));
		cfger.createConfigs();
		
		//assert the config file is created
		File config = new File("conf/activemq-node1-dc1-jms1.xml");
		File config2 = new File("activemq-node1-dc1-jms1.xml");
		assertTrue(config.exists());
		assertFalse(config2.exists());
		
		//Check contents
		FileAssert.assertEquals(new File("test/expectations/activemq-node1-dc1-jms1.xml"), config);
	}
	
	@Test
	public void testCreateConfigsWithMultipleNodesDatacentresAndJmsServers() {
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
		jms3.setForwardTo("node1.dc1");
		jms4.setForwardTo("node1.dc1");
		
		Node node2 = new Node("node2");
		DataCentre node2dc1 = new DataCentre("dc1");
		DataCentre node2dc2 = new DataCentre("dc2");
		JmsServer jmsNode2 = new JmsServer("jms1", "127.0.0.1:60605", true);
		JmsServer jms2Node2 = new JmsServer("jms2", "127.0.0.1:60606", false);
		JmsServer jms3Node2 = new JmsServer("jms3", "127.0.0.1:60607", false);
		JmsServer jms4Node2 = new JmsServer("jms4", "127.0.0.1:60608", false);
		jms3Node2.setForwardTo("node2.dc1");
		jms4Node2.setForwardTo("node2.dc1");
		
		node2.addDataCentre(node2dc1);
		node2.addDataCentre(node2dc2);
		node2dc1.addJmsServer(jmsNode2);
		node2dc1.addJmsServer(jms2Node2);
		node2dc2.addJmsServer(jms3Node2);
		node2dc2.addJmsServer(jms4Node2);
		
		node1.getNodesToBackupList().add(node2);
		
		ActiveMqConfigurator cfger = new ActiveMqConfigurator(Arrays.asList(node1, node2));
		cfger.createConfigs();
		
		//assert the config files are created
		File configNode1Dc1Jms1 = new File("conf/activemq-node1-dc1-jms1.xml");
		File configNode1Dc1Jms2 = new File("conf/activemq-node1-dc1-jms2.xml");
		File configNode1BackupJms1 = new File("conf/activemq-node1-backup-jms3.xml");
		File configNode1BackupJms2 = new File("conf/activemq-node1-backup-jms4.xml");
		
		File configNode2Dc1Jms1 = new File("conf/activemq-node2-dc1-jms1.xml");
		File configNode2Dc1Jms2 = new File("conf/activemq-node2-dc1-jms2.xml");
		File configNode2Dc2Jms1 = new File("conf/activemq-node2-dc2-jms3.xml");
		File configNode2Dc2Jms2 = new File("conf/activemq-node2-dc2-jms4.xml");

		assertTrue(configNode1Dc1Jms1.exists());
		assertTrue(configNode1Dc1Jms2.exists());
		assertTrue(configNode1BackupJms1.exists());
		assertTrue(configNode1BackupJms2.exists());
		assertTrue(configNode2Dc1Jms1.exists());
		assertTrue(configNode2Dc1Jms2.exists());
		assertTrue(configNode2Dc2Jms1.exists());
		assertTrue(configNode2Dc2Jms2.exists());
		
		//Check contents of a couple of them
		FileAssert.assertEquals(new File("test/expectations/activemq-node1-backup-jms3.xml"), configNode1BackupJms1);
		FileAssert.assertEquals(new File("test/expectations/activemq-node1-dc1-jms2.xml"), configNode1Dc1Jms2);
		FileAssert.assertEquals(new File("test/expectations/activemq-node2-dc2-jms3.xml"), configNode2Dc2Jms1);
	}
	
	@Test
	public void testNodeBeingBackedUpHasAPublicServer() {
		Node a = new Node("a");
		Node b = new Node("b");
		a.getNodesToBackupList().add(b);
		
		ActiveMqConfigurator cfg = new ActiveMqConfigurator(Arrays.asList(a, b));
		try{
			cfg.validate();
			fail("Expected exception");
		} catch (IllegalArgumentException e) {
		}
		
		DataCentre dc = new DataCentre("dc");
		b.addDataCentre(dc);
		dc.addJmsServer(new JmsServer("jms1", "127.0.0.1", false));
		try{
			cfg.validate();
			fail("Expected exception");
		} catch (IllegalArgumentException e) {
		}
		
		dc.addJmsServer(new JmsServer("jms2", "127.0.0.1", true));
		cfg.validate();
	}
	
	@Test
	public void testShit() throws URISyntaxException {
		URL url = Test.class.getProtectionDomain().getCodeSource().getLocation();
		System.out.println(url);
		System.out.println(url.toURI());
		System.out.println(url.toString().substring(5));
	}

}
