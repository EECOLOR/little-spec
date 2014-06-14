Little Spec
===========

A small test framework for [sbt](http://www.scala-sbt.org/) and [Scala.js](http://www.scala-js.org/) that allows you to write specifications.

Installation
============

sbt
---

Add the framework as library dependency

```scala
libraryDependencies += "org.qirx" %% "little-spec" % "version" % "test"
```

Add the test framework

```scala
testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework")
```

Scala.js
---

Add the framework as library dependency

```scala
libraryDependencies += "org.qirx" %%% "little-spec" % "version" % "test"
```

Add the test framework

```scala
ScalaJSKeys.scalaJSTestFramework in Test := "org.qirx.littlespec.scalajs.TestFramework"
```

Usage
=====