/**
 * Taken from the storm-starter project on GitHub
 * https://github.com/nathanmarz/storm-starter/ 
 */
package itd302.practical9.storm;

import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import com.google.common.base.Preconditions;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.auth.AccessToken;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Reads Twitter's sample feed using the twitter4j library.
 * @author davidk
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class TwitterSampleSpout extends BaseRichSpout {

    private SpoutOutputCollector collector;
    private LinkedBlockingQueue<Status> queue;
    private TwitterStream twitterStream;

    @Override
    public void open(Map conf, TopologyContext context,
		     SpoutOutputCollector collector) {
	queue = new LinkedBlockingQueue<Status>(1000);
	this.collector = collector;

	StatusListener listener = new StatusListener() {
		@Override
		public void onStatus(Status status) {
		    queue.offer(status);
		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice sdn) {
		}

		@Override
		public void onTrackLimitationNotice(int i) {
		}

		@Override
		public void onScrubGeo(long l, long l1) {
		}

		@Override
		public void onStallWarning(StallWarning stallWarning) {
		}

		@Override
		public void onException(Exception e) {
		}
	    };

	TwitterStream stream = new TwitterStreamFactory(new ConfigurationBuilder().build()).getInstance();
	
	stream.setOAuthConsumer("qCBhuPCty0sd7PtBJ1w","fhF69R4UYqvp8Qk11gt6RYvAGJqcHpeVRO7Zep4w4");
	AccessToken token = new AccessToken("16414559-bsfElcz4nSOdFn6O38mQwSmdf20k3xqZLHURS2lYB","52wpAZuJlCMlnBOpX6Fsl7pYNBDaEfh4l4wO266J5Q");
	stream.setOAuthAccessToken(token);
	stream.addListener(listener);
	stream.sample();
    }
    @Override
    public void nextTuple() {
	Status ret = queue.poll();
	if (ret == null) {
	    Utils.sleep(50);
	} else {
	    collector.emit(new Values(ret));
	}
    }

    @Override
    public void close() {
	twitterStream.shutdown();
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
	Config ret = new Config();
	ret.setMaxTaskParallelism(1);
	return ret;
    }

    @Override
    public void ack(Object id) {
    }

    @Override
    public void fail(Object id) {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
	declarer.declare(new Fields("tweet"));
    }

}
