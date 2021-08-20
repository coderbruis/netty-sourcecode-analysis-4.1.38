# netty-sourcecode-analysis
netty-sourcecode-analysis-4.1.38-Final

此版本主要用于研究jemalloc2算法在Netty中运用的原理以及Netty中的内存分配底层机制，仓库中另外一个Netty版本用的是4.1.6，而另一个Netty使用的最新的jemalloc3算法，
这两个版本对于内存分配机制由非常大的改动，需要详细研究一下，因为其他开源框架用的Netty都是比较旧的版本，都用的jemalloc2，例如：RocketMQ，因此再开一个4.1.38-Final的版本来分析旧版本的内存分配原理。
