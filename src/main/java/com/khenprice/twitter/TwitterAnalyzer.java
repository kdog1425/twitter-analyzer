package com.khenprice.twitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
 * This class is the entry point. It handles the streaming data from Twitter and
 * manages the analysis.
 * 
 * @author khenprice
 *
 */
public class TwitterAnalyzer {
  public static void main(String[] argv) throws IOException {
    Logger.getLogger("org").setLevel(Level.OFF);
    Logger.getLogger("akka").setLevel(Level.OFF);

    try {
      System.setOut(new PrintStream(new File("output-file.txt")));
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    // prepare for sentiment analysis
    StanfordNLP.init();

    // set twitter creds
    MyUtils.initTwitterCredentials();

    // init spark
    SparkConf conf = new SparkConf().setAppName("Twitter Analyzer")
        .setMaster("local[*]");
    JavaStreamingContext jsc = new JavaStreamingContext(conf,
        org.apache.spark.streaming.Durations.seconds(60));

    // set filters submitted to Twitter API
    String[] filters = new String[2];
    filters[0] = "Bernie";
    filters[1] = "Sanders";

    // twitter stream
    JavaReceiverInputDStream<Status> twitterStream = TwitterUtils
        .createStream(jsc, null, filters);

    // process each tweet
    JavaDStream<String> statuses = twitterStream
        .flatMap(status -> {
            MyUtils.processTweet(status);
            return MyUtils.getHashtags(status);
          });

    // Count each word in each batch, forming pairs
    JavaPairDStream<String, Integer> pairs = statuses
        .mapToPair(s -> new Tuple2<>(s, 1));

    // Windowing Reduce Function
    JavaPairDStream<String, Integer> windowedWordCounts = pairs
        .reduceByKeyAndWindow((a, b) -> a + b, Durations.seconds(60*5),
            Durations.seconds(60*5));

    // Reverse Pairs so we can sort
    JavaPairDStream<Integer, String> reversedCounts = windowedWordCounts
        .mapToPair(p -> new Tuple2<Integer, String>(p._2, p._1));

    // Sort pairs
    JavaPairDStream<Integer, String> sortedCounts = reversedCounts
        .transformToPair(rdd -> rdd.sortByKey(false));

    // output analysis
    MyUtils.print(sortedCounts);

    jsc.start();
    jsc.awaitTermination();
  }

}
