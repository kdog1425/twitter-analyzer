# README #

### Code exercise for Yewno by Khen Price ###

### Specifications (as given by Yewno) ###

Task
Using Spark’s Streaming capabilities, create a Spark process which will read tweets from Twitter’s Streaming API. These tweets should be filtered for a particular topic of your choice such as programming languages or sports teams. With this stream of tweets calculate a 5 minute window outputting the top 25 hashtag count. Capture at least three windows worth of data. The result should be a list of tuples similar to:
(5,lakers) (4,cowboys) (4,rangers) ...


### How do I get set up? ###

cd to project folder.

mvn exec:java -D exec.mainClass=com.khenprice.twitter.TwitterAnalyzer

** Configuration ** 


** Dependencies **
See pom.xml

### Who do I talk to? ###
khenprice@gmail.com