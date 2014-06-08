unmanagedSourceDirectories in Compile := Seq(baseDirectory.value)

unmanagedSourceDirectories in Test := Seq()

managedSourceDirectories in Compile := Seq()

managedSourceDirectories in Test := Seq()

classDirectory in Compile := baseDirectory.value
