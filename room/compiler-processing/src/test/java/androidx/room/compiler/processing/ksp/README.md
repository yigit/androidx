You might be wondering why KSP implementation is not tested as part of XProcessing abstraction.

There are two reasons for this:
* Historically, XProcessing is implemented based on the java processing APIs so that implementation
is complete by the time we implement KSP. There is no easy way to pass those tests without
implementing most of the required APIs

* Java implementation relies on well tested Google Auto + javap while KSP is WIP hence requires more
in depth testing.