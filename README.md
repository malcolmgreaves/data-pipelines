# data-pipelines
Training code for Datapalooza

### Thursday 11/12: Marek and Malcolm, Research Engineers at Nitro Inc.

During this all-day live coding session, we will demonstrate the power and elegance of functional programming for building data pipelines. To this end, our session will culminate in constructing efficient, clean implementations of learning algorithms. We will use the live coding medium as an educational tool to teach attendees the concepts necessary to build modern and scalable data processing programs. To eat our own dogfood, we’ll use the code we write during the session to explore publicly available datasets.

We have structured the day into two sessions. The first session will be led by Malcolm. The second by Marek.

In the first session, we will drill-down into the essential ideas underlying data pipelines. We will assume an absolute minimum of audience knowledge; the session will cover Scala syntax, programming with functions and types, and innovative design patterns. We will follow up this information by integrating our functional abstractions with Spark’s RDDs. Critically, as we’ll show, this allows us to define algorithms that will work on any scale of data. This first part will culminate in using our abstractions to implement unsupervised learning and enable data exploration.

In the second session, we will take a deeper dive into machine learning and the science surrounding data. This session will focus on convex optimization and supervised learning. We will cover the principles of convex optimization, including linear algebra. We’ll see how we can get performant numerical vector operations on the JVM. Progressing, we will cover loss functions and show how we can distribute stochastic gradient descent using Spark. Time and audience interest permitting, we might cover a distributed implementation of either linear regression or a multi-layer perceptron.

Below, we present the sequence of topics for the entire day:

##### First Session: Malcolm (3 hours)
* [20 min.] Introduction to the Scala language and essential functional programming concepts.
  * Language syntax.
  * Functions as values.
  * Using the type system to eliminate classes of errors.
* [40 min.] Coding with functions and self-contained types: modules in Scala.
  * Deep dive into practical functional programming with Scala.
  * Present data structures, patterns common to all languages in idiomatic Scala.
* [60 min.] Better than inheritance: ad-hoc polymorphism with type classes.
  * Contrast to classic OO: inheritance for polymorphism.
  * Target focus: how do we make an interface for data, big and small? Spark and Scala collections.
* [60 min.] Data exploration and unsupervised machine learning.
  * Live coding of elegant k-means and k-nearest neighbors algorithms.
  * Visualization using WISP.

##### Second Session: Marek (3 hours)
* Guaranteed topics
  * introduction to convex optimization
  * introduction to performant linear algebra (vectorization)
  * supervised learning and loss functions 
  * how to distribute the stochastic gradient descent (SGD) optimizer on Spark
* One of the following:
  * distributed implementation of linear regression
  * introduction to deep learning and distributed implementation of the multi-layer perceptron (MLP) neural network
