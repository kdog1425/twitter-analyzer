package com.khenprice.twitter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;

import scala.Tuple2;
import twitter4j.Status;

/**
 * This class is the entry point. It handles the streaming
 * data from Twitter and manages the analysis.
 * 
 * @author khenprice
 *
 */
public class TwitterAnalyzer {
  public static void main(String[] argv) throws IOException {
    Logger.getLogger("org").setLevel(Level.OFF);
    Logger.getLogger("akka").setLevel(Level.OFF);

    // prepare for sentiment analysis
    StanfordNLP.init();

    // set twitter creds
    MyUtils.initTwitterCredentials();

    // init spark
    SparkConf conf = new SparkConf().setAppName("Twitter Analyzer")
        .setMaster("local[*]");
    JavaStreamingContext jsc = new JavaStreamingContext(conf,
        org.apache.spark.streaming.Durations.seconds(5));

    // set filters submitted to Twitter API
    String[] filters = new String[3];
    filters[0] = "party";
    filters[1] = "nyc";
    filters[2] = "output";

    // twitter stream
    JavaReceiverInputDStream<Status> twitterStream = TwitterUtils
        .createStream(jsc, null, filters);

    // get hashtags from stream of statuses
    JavaDStream<String> statuses = twitterStream
        .flatMap(new FlatMapFunction<Status, String>() {
          public Iterable<String> call(Status status) {
            MyUtils.getSentiments(status);
            return MyUtils.getHashtags(status);
          }
        });

    // Count each word in each batch, forming pairs
    JavaPairDStream<String, Integer> pairs = statuses
        .mapToPair(s -> new Tuple2<>(s, 1));

    // Windowing Reduce Function
    JavaPairDStream<String, Integer> windowedWordCounts = pairs
        .reduceByKeyAndWindow((a, b) -> a + b, Durations.seconds(10), Durations.seconds(10));

    // Reverse Pairs so we can sort
    JavaPairDStream<Integer, String> reversedCounts = windowedWordCounts
        .mapToPair(p -> new Tuple2<Integer, String>(p._2, p._1));

    // Sort pairs
    JavaPairDStream<Integer, String> sortedCounts = reversedCounts
        .transformToPair(rdd -> rdd.sortByKey(false));

    // print top 25 hashtags
    print(sortedCounts);

    jsc.start();
    jsc.awaitTermination();
  }

  public static void print(JavaPairDStream<Integer, String> stream) {
    stream.foreach(new Function<JavaPairRDD<Integer, String>, Void>() {
      public Void call(JavaPairRDD<Integer, String> v1) throws Exception {
        int count = 1;
        System.out.println();
        System.out.println("-------------------------------------------");
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss")
            .format(Calendar.getInstance().getTime());
        System.out.println("Time: " + time);
        System.out.println("-------------------------------------------");
        for (Tuple2<Integer, String> t : v1.collect()) {
          if (count > 25)
            break;
          System.out.println(count + ": (" + t._2 + ", " + t._1 + ")");
          count++;
        }
        if (count > 1) {
          System.out.println(
              "-------------------END-Hashtag-Count-------------------");
        }
        System.out.println();
        MyUtils.dumpSentiments();
        return null;
      }
    });
  }

}
