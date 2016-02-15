package com.khenprice.twitter;

/**
 * A class representing a Twitter tweet.
 * 
 * @author khenprice
 *
 */
public class Tweet {
  String text;
  int sentiment;
  int retweetCount;;

  public int getRetweetCount() {
    return retweetCount;
  }

  public void setRetweetCount(int retweetCount) {
    this.retweetCount = retweetCount;
  }

  public Tweet(String text, int retweetCount, int sentiment) {
    this.text = text;
    this.sentiment = sentiment;
    this.retweetCount = retweetCount;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public int getSentiment() {
    return sentiment;
  }

  public void setSentiment(int sentiment) {
    this.sentiment = sentiment;
  }

  public String toString() {
    return new String("Text:[" + text + "]" + " | " + "Retweets: "
        + retweetCount + " | " + "Sentiment: [" + sentiment + "]");
  }
}
