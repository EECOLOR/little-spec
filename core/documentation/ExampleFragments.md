**This documentation is generated from `documentation.ExampleFragments`**

---
Sometimes you want to show how your library is used, the problem with
documenting code examples is that they 'rot'. In case you change your
library you will not be notified of any compile errors in your
documentation. On top of that, the code might not be doing what you
inteded. The `example` fragment helps you in these cases.
```scala
def specialMethod(x:Int) = 1 + x

example {
  def result = specialMethod(1)
  result is 2
}
```
