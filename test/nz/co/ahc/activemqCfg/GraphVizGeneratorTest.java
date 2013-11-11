package nz.co.ahc.activemqCfg;
import java.io.File;
import java.util.Arrays;

import junitx.framework.FileAssert;

import org.junit.Before;
import org.junit.Test;


public class GraphVizGeneratorTest {
	
	@Before
	public void clean() {
		NodeRepository.clear();
	}

	@Test
	public void testSimpleCase() {
		Node node1 = new Node("node1");
		DataCentre dc1 = new DataCentre("dc1");
		JmsServer jms = new JmsServer("jms1", "127.0.0.1:60601", true);
		
		node1.addDataCentre(dc1);
		dc1.addJmsServer(jms);
		
		GraphVizGenerator g = new GraphVizGenerator(Arrays.asList(node1));
		g.draw();
		
		FileAssert.assertEquals(new File("test/expectations/graph1.gv"), new File("visualisation.gv"));
	}
	
	@Test
	public void testTwoJmsServers() {
		Node node1 = new Node("node1");
		DataCentre dc1 = new DataCentre("dc1");
		JmsServer jms = new JmsServer("jms1", "127.0.0.1:60601", true);
		JmsServer jms2 = new JmsServer("jms2", "127.0.0.1:60602", true);
		
		node1.addDataCentre(dc1);
		dc1.addJmsServer(jms);
		dc1.addJmsServer(jms2);
		
		GraphVizGenerator g = new GraphVizGenerator(Arrays.asList(node1));
		g.draw();
		
		FileAssert.assertEquals(new File("test/expectations/graph2.gv"), new File("visualisation.gv"));
	}
	
	@Test
	public void testTwoDataCentres() {
		Node node1 = new Node("node1");
		DataCentre dc1 = new DataCentre("dc1");
		DataCentre backup = new DataCentre("backup");
		JmsServer jms = new JmsServer("jms1", "127.0.0.1:60601", true);
		JmsServer jms2 = new JmsServer("jms2", "127.0.0.1:60602", true);
		
		JmsServer jms3 = new JmsServer("jms3", "127.0.0.1:60602", true);
		
		node1.addDataCentre(dc1);
		node1.addDataCentre(backup);
		dc1.addJmsServer(jms);
		dc1.addJmsServer(jms2);
		backup.addJmsServer(jms3);
		
		GraphVizGenerator g = new GraphVizGenerator(Arrays.asList(node1));
		g.draw();
		
		FileAssert.assertEquals(new File("test/expectations/graph3.gv"), new File("visualisation.gv"));
	}
	
	@Test
	public void testTwoDataCentresWithSecondaryForwardingToPrimary() {
		Node node1 = new Node("node1");
		DataCentre dc1 = new DataCentre("dc1");
		DataCentre backup = new DataCentre("backup");
		JmsServer jms = new JmsServer("jms1", "127.0.0.1:60601", true);
		JmsServer jms2 = new JmsServer("jms2", "127.0.0.1:60602", true);
		JmsServer jmsPrivate = new JmsServer("jms3", "127.0.0.1:60602", false);
		
		JmsServer jms3 = new JmsServer("jms3", "127.0.0.1:60602", true);
		jms3.setForwardTo("node1.dc1");
		
		node1.addDataCentre(dc1);
		node1.addDataCentre(backup);
		dc1.addJmsServer(jms);
		dc1.addJmsServer(jms2);
		dc1.addJmsServer(jmsPrivate);
		backup.addJmsServer(jms3);
		
		GraphVizGenerator g = new GraphVizGenerator(Arrays.asList(node1));
		g.draw();
		
		FileAssert.assertEquals(new File("test/expectations/graph4.gv"), new File("visualisation.gv"));
	}
	
	@Test
	public void testTwoNodes() {
		Node node1 = new Node("node1");
		DataCentre dc1 = new DataCentre("dc1");
		JmsServer jms = new JmsServer("jms1", "127.0.0.1:60601", true);
		JmsServer jms2 = new JmsServer("jms2", "127.0.0.1:60602", false);
		
		Node node2 = new Node("node2");
		DataCentre node2dc = new DataCentre("dc1");
		JmsServer node2Jms = new JmsServer("jms1", "127.0.0.1:60601", true);
		JmsServer node2Jms2 = new JmsServer("jms2", "127.0.0.1:60601", true);
		
		node1.addDataCentre(dc1);
		dc1.addJmsServer(jms);
		dc1.addJmsServer(jms2);
		
		node2.addDataCentre(node2dc);
		node2dc.addJmsServer(node2Jms);
		node2dc.addJmsServer(node2Jms2);
		
		node1.getNodesToBackupList().add(node2);
		
		GraphVizGenerator g = new GraphVizGenerator(Arrays.asList(node1, node2));
		g.draw();
		
		FileAssert.assertEquals(new File("test/expectations/graph5.gv"), new File("visualisation.gv"));
	}
}
