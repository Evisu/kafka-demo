## Apache Kafka

### Apache Kafka® 是 一个分布式流处理平台. 这到底意味着什么呢?

我们知道流处理平台有以下三种特性:

- 可以让你发布和订阅流式的记录。这一方面与消息队列或者企业消息系统类似。
- 可以储存流式的记录，并且有较好的容错性。
- 可以在流式记录产生时就进行处理。


Kafka适合什么样的场景?

它可以用于两大类别的应用:

- 构造实时流数据管道，它可以在系统或应用之间可靠地获取数据。 (相当于message queue)
- 构建实时流式应用程序，对这些流数据进行转换或者影响。 (就是流处理，通过kafka stream topic和topic之间内部进行变化)

为了理解Kafka是如何做到以上所说的功能，从下面开始，我们将深入探索Kafka的特性。

- Kafka作为一个集群，运行在一台或者多台服务器上.
- Kafka 通过 topic 对存储的流数据进行分类。
- 每条记录中包含一个key，一个value和一个timestamp（时间戳）。

Kafka有四个核心的API:

- The Producer API 允许一个应用程序发布一串流式的数据到一个或者多个Kafka topic。
- The Consumer API 允许一个应用程序订阅一个或多个 topic ，并且对发布给他们的流式数据进行处理。
- The Streams API 允许一个应用程序作为一个流处理器，消费一个或者多个topic产生的输入流，然后生产一个输出流到一个或多个topic中去，在输入输出流中进行有效的转换。
- The Connector API 允许构建并运行可重用的生产者或者消费者，将Kafka topics连接到已存在的应用程序或者数据系统。比如，连接到一个关系型数据库，捕捉表（table）的所有变更内容。

![](http://kafka.apachecn.org/10/images/kafka-apis.png)

**Topics和日志**

Topic 就是数据主题，是数据记录发布的地方,可以用来区分业务系统。Kafka中的Topics总是多订阅者模式，一个topic可以拥有一个或者多个消费者来订阅它的数据。

对于每一个topic， Kafka集群都会维持一个分区日志，如下所示：

![](http://kafka.apachecn.org/10/images/log_anatomy.png)

每个分区都是有序且顺序不可变的记录集，并且不断地追加到结构化的commit log文件。分区中的每一个记录都会分配一个id号来表示顺序，我们称之为offset，offset用来唯一的标识分区中每一条记录。

Kafka 集群保留所有发布的记录—无论他们是否已被消费—并通过一个可配置的参数——保留期限来控制. 举个例子， 如果保留策略设置为2天，一条记录发布后两天内，可以随时被消费，两天过后这条记录会被抛弃并释放磁盘空间。Kafka的性能和数据大小无关，所以长时间存储数据没有什么问题.

![](http://kafka.apachecn.org/10/images/log_consumer.png)


这些细节说明Kafka 消费者是非常廉价的—消费者的增加和减少，对集群或者其他消费者没有多大的影响。比如，你可以使用命令行工具，对一些topic内容执行 tail操作，并不会影响已存在的消费者消费数据。

日志中的 partition（分区）有以下几个用途。第一，当日志大小超过了单台服务器的限制，允许日志进行扩展。每个单独的分区都必须受限于主机的文件限制，不过一个主题可能有多个分区，因此可以处理无限量的数据。第二，可以作为并行的单元集

**分布式**

日志的分区partition （分布）在Kafka集群的服务器上。每个服务器在处理数据和请求时，共享这些分区。每一个分区都会在已配置的服务器上进行备份，确保容错性.

每个分区都有一台 server 作为 “leader”，零台或者多台server作为 follwers 。leader server 处理一切对 partition （分区）的读写请求，而follwers只需被动的同步leader上的数据。当leader宕机了，followers 中的一台服务器会自动成为新的 leader。每台 server 都会成为某些分区的 leader 和某些分区的 follower，因此集群的负载是平衡的

**生产者**

生产者可以将数据发布到所选择的topic（主题）中。生产者负责将记录分配到topic的哪一个 partition（分区）中。可以使用循环的方式来简单地实现负载均衡，也可以根据某些语义分区函数(例如：记录中的key)来完成。下面会介绍更多关于分区的使用。

**消费者**

消费者使用一个 消费组 名称来进行标识，发布到topic中的每条记录被分配给订阅消费组中的一个消费者实例.消费者实例可以分布在多个进程中或者多个机器上。

如果所有的消费者实例在同一消费组中，消息记录会负载平衡到每一个消费者实例.

如果所有的消费者实例在不同的消费组中，每条消息记录会广播到所有的消费者进程
![](http://kafka.apachecn.org/10/images/consumer-groups.png)

如图，这个 Kafka 集群有两台 server 的，四个分区(p0-p3)和两个消费者组。消费组A有两个消费者，消费组B有四个消费者。

通常情况下，每个 topic 都会有一些消费组，一个消费组对应一个"逻辑订阅者"。一个消费组由许多消费者实例组成，便于扩展和容错。这就是发布和订阅的概念，只不过订阅者是一组消费者而不是单个的进程。

在Kafka中实现消费的方式是将日志中的分区划分到每一个消费者实例上，以便在任何时间，每个实例都是分区唯一的消费者。维护消费组中的消费关系由Kafka协议动态处理。如果新的实例加入组，他们将从组中其他成员处接管一些 partition 分区;如果一个实例消失，拥有的分区将被分发到剩余的实例。

Kafka 只保证分区内的记录是有序的，而不保证主题中不同分区的顺序。每个 partition 分区按照key值排序足以满足大多数应用程序的需求。但如果你需要总记录在所有记录的上面，可使用仅有一个分区的主题来实现，这意味着每个消费者组只有一个消费者进程

### Kafka分区与消费者的关系


1. 前言

	我们知道，生产者发送消息到主题，消费者订阅主题（以消费者组的名义订阅），而主题下是分区，消息是存储在分区中的，所以事实上生产者发送消息到分区，消费者则从分区读取消息，那么，这里问题来了，生产者将消息投递到哪个分区？消费者组中的消费者实例之间是怎么分配分区的呢？接下来，就围绕着这两个问题一探究竟。


2. 主题的分区数设置

	
	在server.properties配置文件中可以指定一个全局的分区数设置，这是对每个主题下的分区数的默认设置，默认是1。
	
	![](https://img2018.cnblogs.com/blog/874963/201809/874963-20180917162057348-969057316.png)
	
	当然每个主题也可以自己设置分区数量，如果创建主题的时候没有指定分区数量，则会使用server.properties中的设置。
	
		bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic my-topic --partitions 2 --replication-factor 1
	
	在创建主题的时候，可以使用--partitions选项指定主题的分区数量
	
		[root@localhost kafka_2.11-2.0.0]# bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic abc
		Topic:abc       PartitionCount:2        ReplicationFactor:1     Configs:
	        Topic: abc      Partition: 0    Leader: 0       Replicas: 0     Isr: 0
	        Topic: abc      Partition: 1    Leader: 0       Replicas: 0     Isr: 0

3. 生产者与分区

	默认的分区策略是：

	- 如果在发消息的时候指定了分区，则消息投递到指定的分区
	- 如果没有指定分区，但是消息的key不为空，则基于key的哈希值来选择一个分区
	- 如果既没有指定分区，且消息的key也是空，则用轮询的方式选择一个分区	

4. 分区与消费者

	消费者以组的名义订阅主题，主题有多个分区，消费者组中有多个消费者实例，那么消费者实例和分区之前的对应关系是怎样的呢？

	换句话说，就是组中的每一个消费者负责那些分区，这个分配关系是如何确定的呢？

	![](https://img2018.cnblogs.com/blog/874963/201809/874963-20180917164755125-572862202.png)
	
	同一时刻，一条消息只能被组中的一个消费者实例消费

	消费者组订阅这个主题，意味着主题下的所有分区都会被组中的消费者消费到，如果按照从属关系来说的话就是，主题下的每个分区只从属于组中的一个消费者，不可能出现组中的两个消费者负责同一个分区。
	
	那么，问题来了。如果分区数大于或者等于组中的消费者实例数，那自然没有什么问题，无非一个消费者会负责多个分区，（PS：当然，最理想的情况是二者数量相等，这样就相当于一个消费者负责一个分区）；但是，如果消费者实例的数量大于分区数，那么按照默认的策略（PS：之所以强调默认策略是因为你也可以自定义策略），有一些消费者是多余的，一直接不到消息而处于空闲状态。
	
	话又说回来，假设多个消费者负责同一个分区，那么会有什么问题呢？
	
	我们知道，Kafka它在设计的时候就是要保证分区下消息的顺序，也就是说消息在一个分区中的顺序是怎样的，那么消费者在消费的时候看到的就是什么样的顺序，那么要做到这一点就首先要保证消息是由消费者主动拉取的（pull），其次还要保证一个分区只能由一个消费者负责。倘若，两个消费者负责同一个分区，那么就意味着两个消费者同时读取分区的消息，由于消费者自己可以控制读取消息的offset，就有可能C1才读到2，而C1读到1，C1还没处理完，C2已经读到3了，则会造成很多浪费，因为这就相当于多线程读取同一个消息，会造成消息处理的重复，且不能保证消息的顺序，这就跟主动推送（push）无异。

	消费者分区分配策略：
	- range

		range策略对应的实现类是org.apache.kafka.clients.consumer.RangeAssignor

		这是默认的分配策略
	
		可以通过消费者配置中partition.assignment.strategy参数来指定分配策略，它的值是类的全路径，是一个数组

			/**
			 * The range assignor works on a per-topic basis. For each topic, we lay out the available partitions in numeric order
			 * and the consumers in lexicographic order. We then divide the number of partitions by the total number of
			 * consumers to determine the number of partitions to assign to each consumer. If it does not evenly
			 * divide, then the first few consumers will have one extra partition.
			 *
			 * For example, suppose there are two consumers C0 and C1, two topics t0 and t1, and each topic has 3 partitions,
			 * resulting in partitions t0p0, t0p1, t0p2, t1p0, t1p1, and t1p2.
			 *
			 * The assignment will be:
			 * C0: [t0p0, t0p1, t1p0, t1p1]
			 * C1: [t0p2, t1p2]
			 */

		range策略是基于每个主题的
	
		对于每个主题，我们以数字顺序排列可用分区，以字典顺序排列消费者。然后，将分区数量除以消费者总数，以确定分配给每个消费者的分区数量。如果没有平均划分（PS：除不尽），那么最初的几个消费者将有一个额外的分区	

	- roundrobin（轮询）

		轮询分配策略是基于所有可用的消费者和所有可用的分区的
	
		与前面的range策略最大的不同就是它不再局限于某个主题
		
		如果所有的消费者实例的订阅都是相同的，那么这样最好了，可用统一分配，均衡分配
		
		例如，假设有两个消费者C0和C1，两个主题t0和t1，每个主题有3个分区，分别是t0p0，t0p1，t0p2，t1p0，t1p1，t1p2
		
		那么，最终分配的结果是这样的：
		
		C0: [t0p0, t0p2, t1p1]
		
		C1: [t0p1, t1p0, t1p2]
		
		用图形表示大概是这样的：

		![](https://img2018.cnblogs.com/blog/874963/201809/874963-20180917183049428-1942144917.png)

		