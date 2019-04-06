# README #

### by Khen Price ###

### Specifications ###

Task
Using Spark’s Streaming capabilities, create a Spark process which will read tweets from Twitter’s Streaming API. These tweets should be filtered for a particular topic of your choice such as programming languages or sports teams. With this stream of tweets calculate a 5 minute window outputting the top 25 hashtag count. Capture at least three windows worth of data. The result should be a list of tuples similar to:
(5,lakers) (4,cowboys) (4,rangers) ...

### Results ###

The result file can be seen in the project folder:

tweet-analysis_20160215_144541_L300_I300.txt

(L300 - 300 seconds window length, I300 - 300 seconds slide interval).

The output file shows the top 25 hashtags for the window, then lists the tweets with their respective sentiment analysis (higher number suggests a more positive sentiment).

### How do I get set up? ###

clone repo.

cd to project folder.

mvn exec:java -D exec.mainClass=com.khenprice.twitter.TwitterAnalyzer

### Dependencies ###
See pom.xml

### Workflow ###

1. Familiarization with Spark - installation, running examples.

2. Setting up Twitter api - attaining all the access codes and experimenting with setting up a stream, filtering it.

3. Working with Spark and Twitter (Java)
  1. Filtering for hashtags.
  2. Understanding windowing
  3. Working with RDDs
  4. Sorting.

4. Final steps.

  1. Code refactoring - upgraded to Java 8 to enable lambda expressions. Reiterated on design of classes and methods.
  2. Experimented with single token sentiment analysis (to make sure there StanfordNLP works as expected). For example, 'best', 'free' yielded high values.
  3. Experimented with various window sizes, shorter to longer.
  4. Realized finding most retweeted tweet seems doable but not as simple as it first was (i.e. when the tweet first appears, it's retweet_count is 0, but that may change before the 5 minute window has passed).


### Who do I talk to? ###
khenprice@gmail.com
