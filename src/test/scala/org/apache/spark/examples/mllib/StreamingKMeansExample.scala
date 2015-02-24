package org.apache.spark.examples.mllib

import org.apache.spark.examples.AbstractSparkExample
import org.apache.spark.mllib.clustering.StreamingKMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.streaming.{Seconds, StreamingContext}


/**
 * Estimate clusters on one stream of data and make predictions
 * on another stream, where the data streams arrive as text files
 * into two different directories.
 *
 * The rows of the training text files must be vector data in the form
 * `[x1,x2,x3,...,xn]`
 * Where n is the number of dimensions.
 *
 * The rows of the test text files must be labeled data in the form
 * `(y,[x1,x2,x3,...,xn])`
 * Where y is some identifier. n must be the same for train and test.
 *
 * Usage:
 * StreamingKMeansExample <trainingDir> <testDir> <batchDuration> <numClusters> <numDimensions>
 *
 * To run on your local machine using the two directories `trainingDir` and `testDir`,
 * with updates every 5 seconds, 2 dimensions per data point, and 3 clusters, call:
 * $ bin/run-example mllib.StreamingKMeansExample trainingDir testDir 5 3 2
 *
 * As you add text files to `trainingDir` the clusters will continuously update.
 * Anytime you add text files to `testDir`, you'll see predicted labels using the current model.
 *
 */
class StreamingKMeansExample extends AbstractSparkExample {

  test("Streaming KMeans Example") {

    val trainingFile = "data/mllib/sample_libsvm_data.txt"
    val testFile = "data/mllib/sample_libsvm_data.txt"

    val conf = newSparkConf("Streaming KMeans Example")
    val ssc = new StreamingContext(conf, Seconds(2L))

    val trainingData = ssc.textFileStream(trainingFile).map(Vectors.parse)
    val testData = ssc.textFileStream(testFile).map(LabeledPoint.parse)

    val model = new StreamingKMeans()
                .setK(3)
                .setDecayFactor(1.0)
                .setRandomCenters(2, 0.0)

    model.trainOn(trainingData)
    model.predictOnValues(testData.map(lp => (lp.label, lp.features))).print()

    ssc.start()
    ssc.awaitTermination(10000)
  }
}
