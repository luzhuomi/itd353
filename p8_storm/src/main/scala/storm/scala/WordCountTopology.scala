package storm.scala

import scala.language.postfixOps
import storm.scala.dsl._
import storm.scala.dsl.StormSpout
import org.apache.storm.Config
import org.apache.storm.LocalCluster
import org.apache.storm.StormSubmitter
import org.apache.storm.generated._
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.tuple.{Fields, Tuple, Values}
import collection.mutable.{Map, HashMap}
import util.Random
import org.slf4j.LoggerFactory
import org.slf4j.Logger



object WordCountTopology {
  class RandomSentenceSpout extends StormSpout(outputFields = List("sentence")) {
    val sentences = List("the cow jumped over the moon",
                         "an apple a day keeps the doctor away",
                         "four score and seven years ago",
                         "snow white and the seven dwarfs",
                         "i am at two with nature")
    def nextTuple = {
      Thread sleep 100
      emit (sentences(Random.nextInt(sentences.length)))
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
    val logger = LoggerFactory.getLogger(classOf[WordCount]);
    var counts: Map[String, Int] = _
    var lastLogTime: Long = _
    setup {
      counts = new HashMap[String, Int]().withDefaultValue(0)
      lastLogTime = System.currentTimeMillis;
    }
    def execute(t: Tuple) = t matchSeq {
      case Seq(word: String) =>
        counts(word) += 1
        using anchor t emit (word, counts(word))
        t ack
        val now = System.currentTimeMillis
        val diff = (now - lastLogTime) / 1000
	if (diff > 5) {
	  lastLogTime = now;
	  logger.info("Word Count: "+counts.size);
	  publishList();
	}
    }

    def publishList() {
      counts.toList.foreach( keyVal => {
	val word = keyVal._1
	val count = keyVal._2
	logger.info(s"$word:$count") 
	
      })
      counts = new HashMap[String, Int]().withDefaultValue(0)
    }
  }


  def main(args: Array[String]) = {
    val builder = new TopologyBuilder

    builder.setSpout("randsentence", new RandomSentenceSpout, 5)
    builder.setBolt("split", new SplitSentence, 8)
        .shuffleGrouping("randsentence")
    builder.setBolt("count", new WordCount, 12)
        .fieldsGrouping("split", new Fields("word"))

    val conf = new Config
    conf.setDebug(true)
    conf.setMaxTaskParallelism(3)

    args.toList match 
    { 
      case "remote"::_ => {
	try {
	  StormSubmitter.submitTopology("word-count", conf, builder.createTopology)
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
	cluster.submitTopology("word-count", conf, builder.createTopology)
	Thread sleep 10000
	cluster.shutdown
      }
    }
  }
}
