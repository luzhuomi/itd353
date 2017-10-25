package itd302.practical9.storm;

import backtype.storm.Config;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import com.google.common.base.Preconditions;

import java.util.ArrayList;

/**
 * Topology class that sets up the Storm topology for this sample.
 * Please note that Twitter credentials have to be provided as VM args, otherwise you'll get an Unauthorized error.
 * @link http://twitter4j.org/en/configuration.html#systempropertyconfiguration
 */
public class TopologyCluster {
    static final String TOPOLOGY_NAME = "twitter-wc";
    public static void main(String[] args) {
	Config config = new Config();
	config.setMessageTimeoutSecs(120);

	TopologyBuilder b = new TopologyBuilder();
	b.setSpout("TwitterSampleSpout", new TwitterSampleSpout());
        b.setBolt("WordSplitterBolt", new WordSplitterBolt(5))
	    .shuffleGrouping("TwitterSampleSpout");
        b.setBolt("IgnoreWordsBolt", new IgnoreWordsBolt())
	    .shuffleGrouping("WordSplitterBolt");
        b.setBolt("WordCounterBolt", new WordCounterBolt(10, 5 * 60, 50))
	    .shuffleGrouping("IgnoreWordsBolt");

	/*
	ArrayList<String> zookeeperAddresses = new ArrayList<String>();
	zookeeperAddresses.add("127.0.0.1");
	int zookeeperPort = 2181 ;
	config.put(Config.NIMBUS_THRIFT_PORT,6627);
	config.put(Config.STORM_ZOOKEEPER_PORT,zookeeperPort);
	config.put(Config.STORM_ZOOKEEPER_SERVERS,zookeeperAddresses);
	*/
	config.setNumWorkers(3);
	config.setDebug(true);
	try {
	    StormSubmitter.submitTopology(TOPOLOGY_NAME,config, b.createTopology());
	} 
	catch (AlreadyAliveException e) {
	    System.out.println("Topology Exists!");
	}
	catch (InvalidTopologyException e) {
	    System.out.println("Invalid Topology!");
	}
    }

}
