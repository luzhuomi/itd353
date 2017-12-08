package storm.scala

import storm.scala.dsl._
import storm.scala.dsl.StormSpout
import org.apache.storm.Config
import org.apache.storm.LocalCluster
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.task.TopologyContext
import org.apache.storm.spout.SpoutOutputCollector
import org.apache.storm.tuple.{Fields, Tuple, Values}
import collection.mutable.{Map, HashMap}
import util.Random
import twitter4j._
import twitter4j.conf.ConfigurationBuilder
import twitter4j.auth.AccessToken
import java.util.concurrent.LinkedBlockingQueue



object TwitterTopology {
  class TwitterSampleSpout extends StormSpout(outputFields = List("tweet")) {
    val queue : LinkedBlockingQueue[Status] = new LinkedBlockingQueue[Status](1000)
    val stream:TwitterStream = new TwitterStreamFactory(new ConfigurationBuilder().build()).getInstance()
    override def open(conf: java.util.Map[_, _], context:TopologyContext, collector:SpoutOutputCollector) =  {
      val listener : StatusListener = new StatusListener() {
	def onStatus(status:Status) = queue.offer(status)
	def onDeletionNotice(sdn:StatusDeletionNotice) = {}
	def onTrackLimitationNotice(i:Int) = {}
	def onScrubGeo(l:Long, l1:Long) = {}
	def onStallWarning(stallWarning:StallWarning) = {}
	def onException(e:Exception) = {}
      }
      stream.setOAuthConsumer("qCBhuPCty0sd7PtBJ1w","fhF69R4UYqvp8Qk11gt6RYvAGJqcHpeVRO7Zep4w4")
      val token:AccessToken = new AccessToken("16414559-bsfElcz4nSOdFn6O38mQwSmdf20k3xqZLHURS2lYB","52wpAZuJlCMlnBOpX6Fsl7pYNBDaEfh4l4wO266J5Q")
      stream.setOAuthAccessToken(token)
      stream.addListener(listener)
      stream.sample()
    }
    override def close() =  {
      stream.shutdown()
    }
    def nextTuple = {
      val ret:Status = queue.poll()
      if (ret == null) { Thread sleep 100 }
      else {
	emit(ret)
      }
    }
  }


  // An example of using matchSeq for Scala pattern matching of Storm tuples
  // plus using the emit and ack DSLs.
  class SplitSentence extends StormBolt(outputFields = List("word")) {
    def execute(t: Tuple) = t matchSeq {
      case Seq(sentence: String) => sentence split " " foreach
        { word => using anchor t emit (word) }
      t ack
    }
  }


  class WordCount extends StormBolt(List("word", "count")) {
    var counts: Map[String, Int] = _
    setup {
      counts = new HashMap[String, Int]().withDefaultValue(0)
    }
    def execute(t: Tuple) = t matchSeq {
      case Seq(word: String) =>
        counts(word) += 1
        using anchor t emit (word, counts(word))
        t ack
    }
  }


  def main(args: Array[String]) = {
    val builder = new TopologyBuilder

    builder.setSpout("twitter", new TwitterSampleSpout, 5)
    builder.setBolt("split", new SplitSentence, 8)
        .shuffleGrouping("twitter")
    builder.setBolt("count", new WordCount, 12)
        .fieldsGrouping("split", new Fields("word"))

    val conf = new Config
    conf.setDebug(true)
    conf.setMaxTaskParallelism(3)

    val cluster = new LocalCluster
    cluster.submitTopology("twitter", conf, builder.createTopology)
    Thread sleep 10000
    cluster.shutdown
  }
}
