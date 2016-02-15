package com.khenprice.twitter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import twitter4j.Status;

/**
 * A general utility class with helper functions
 * that handle tasks as initialization and text processing.
 * @author khenprice
 *
 */
public class MyUtils {
  static ArrayList<Tweet> tweets = new ArrayList<Tweet>();
  
  public static void initTwitterCredentials() throws IOException {
    Properties prop = new Properties();
    System.out.println("Working Directory = " + System.getProperty("user.dir"));
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
  
  public static void getSentiments(Status status) {
    String text = status.getText();
    Tweet tweet = new Tweet(text, status.getRetweetCount(), StanfordNLP.findSentiment(text)); 
    tweets.add(tweet);
  }
  
  public static void dumpSentiments() {
    for (Tweet t : tweets){
      System.out.println(t);
    }
    System.out.println("-------------------END-Sentiment-Analysis-------------------");
    System.out.println();
    tweets.clear();
  }
  
}
