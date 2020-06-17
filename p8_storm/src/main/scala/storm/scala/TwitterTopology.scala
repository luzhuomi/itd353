package storm.scala.examples


import org.apache.storm.spout.SpoutOutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.{BasicOutputCollector, OutputFieldsDeclarer, TopologyBuilder}
import org.apache.storm.topology.base.{BaseRichSpout, BaseBasicBolt}
import org.apache.storm.tuple.{Fields, Tuple, Values}
import org.apache.storm.utils.Utils
import org.apache.storm.{Config, LocalCluster, StormSubmitter}
import org.apache.storm.generated.{AlreadyAliveException, InvalidTopologyException}

import collection.mutable.{Map, HashMap}
import util.Random
import twitter4j._
import twitter4j.conf.ConfigurationBuilder
import twitter4j.auth.AccessToken
import java.util.concurrent.LinkedBlockingQueue



object TwitterTopology {
  class TwitterSampleSpout extends BaseRichSpout {

    var _collector:SpoutOutputCollector = _
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
    override def nextTuple():Unit = {
      val ret:Status = queue.poll()
      if (ret == null) { Utils.sleep(100) }
      else {
	      _collector.emit(new Values(ret.getText()))
      }
    }
    override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
      declarer.declare(new Fields("tweet"))
    } 
  }


  // An example of using matchSeq for Scala pattern matching of Storm tuples
  // plus using the emit and ack DSLs.
  class SplitSentence extends BaseBasicBolt {
    override def execute(input: Tuple, collector: BasicOutputCollector): Unit = {
      val sentence = input.getString(0)
      val words = sentence.split("\\s+")

      for (word <- words) {
        collector.emit(new Values(word))
      }
    }
    override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
      declarer.declare(new Fields("word"))
    }
  }


  class WordCount extends BaseBasicBolt {
    val counts: Map[String, Int] = new HashMap[String, Int]().withDefaultValue(0)
    override def execute(input: Tuple, collector: BasicOutputCollector): Unit = {
      
      val word = input.getString(0)
      
      val optCount = counts.get(word)
      if(optCount.isEmpty) {
	      counts.put(word,1)
      } else {
	      counts.put(word,optCount.get+1)
      }
      for (entry <- counts) {
        println(s"Word Count: ${entry._1}: ${entry._2}")
      }

      collector.emit(new Values(word,counts))
    }
    override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {      
      declarer.declare(new Fields("word","count"));
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

    args.toList match 
    { 
      case "remote"::_ => {
        try {
          StormSubmitter.submitTopology("twitter", conf, builder.createTopology)
        } catch {
          case (e:AlreadyAliveException) => {
            println("Topology Exists!")
          }
          case (e:InvalidTopologyException) => {
            println("Invalid Topology!")
          }
        }
      }
      case _ => {
        val cluster = new LocalCluster
        cluster.submitTopology("twitter", conf, builder.createTopology)
        Thread sleep 10000
        cluster.shutdown
      }
    }
  }
}
