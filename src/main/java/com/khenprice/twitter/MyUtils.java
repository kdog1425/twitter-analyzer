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

import org.apache.spark.streaming.api.java.JavaPairDStream;

import scala.Tuple2;
import twitter4j.Status;

/**
 * A general utility class with helper functions that handle tasks as
 * initialization and text processing.
 * 
 * @author khenprice
 *
 */
public class MyUtils {
  static ArrayList<Tweet> processedTweets = new ArrayList<Tweet>();
  static Tweet mostRetweetedTweet = null;

  public static void initTwitterCredentials() throws IOException {
    Properties prop = new Properties();
    String propFileName = "./conf/twitter4j.properties";
    InputStream inputStream = new FileInputStream(propFileName);
    prop.load(inputStream);

    System.setProperty("twitter4j.oauth.debug", prop.getProperty("debug"));
    System.setProperty("twitter4j.oauth.consumerKey",
        prop.getProperty("consumerKey"));
    System.setProperty("twitter4j.oauth.consumerSecret",
        prop.getProperty("consumerSecret"));
    System.setProperty("twitter4j.oauth.accessToken",
        prop.getProperty("accessToken"));
    System.setProperty("twitter4j.oauth.accessTokenSecret",
        prop.getProperty("accessTokenSecret"));
  }

  public static Iterable<String> getHashtags(Status status) {
    List<java.lang.String> tokens = Arrays.asList(status.getText().split(" "));
    List<java.lang.String> hashtags = new ArrayList<java.lang.String>();
    for (String s : tokens) {
      if (s.length() > 0 && s.charAt(0) == '#') {
        hashtags.add(s.substring(1, s.length()));
      }
    }
    return hashtags;
  }

  public static void processTweet(Status status) {
    String text = status.getText();
    Tweet tweet = new Tweet(text, status.getRetweetCount(),
        StanfordNLP.findSentiment(text));
    if (mostRetweetedTweet == null) {
      mostRetweetedTweet = tweet;
    } else {
      mostRetweetedTweet = mostRetweetedTweet.getRetweetCount() > tweet
          .getRetweetCount() ? mostRetweetedTweet : tweet;
    }
    processedTweets.add(tweet);
  }

  public static void dumpSentiments() {
    for (Tweet t : processedTweets) {
      System.out.println(t);
    }
    System.out.println(
        "-------------------END-Sentiment-Analysis-------------------");
    System.out.println();
    
  }
  
  public static void dumpMostRetweeted() {
    System.out.println("Most retweeted: " + mostRetweetedTweet);
    System.out.println(
        "-------------------END-Most-Retweeted-------------------");
    System.out.println();
    
  }

  private static void reset() {
    processedTweets.clear();
    mostRetweetedTweet = null;
  }
  
  public static void print(JavaPairDStream<Integer, String> stream) {
    stream.foreachRDD(pairRdd -> {
      System.out.println();
      System.out.println("-------------------------------------------");
      String time = new SimpleDateFormat("yyyyMMdd_HHmmss")
          .format(Calendar.getInstance().getTime());
      System.out.println("Time: " + time);
      System.out.println("-------------------------------------------");
      int count = 1;
      for (Tuple2<Integer, String> t : pairRdd.collect()) {
        if (count > 25)
          break;
        System.out.println(count + ": (" + t._2 + ", " + t._1 + ")");
        count++;
      }
      if (count > 1) {
        System.out
            .println("-------------------END-Hashtag-Count-------------------");
      }
      System.out.println();
      dumpSentiments();
      dumpMostRetweeted();
      reset();
    });
  }

}
