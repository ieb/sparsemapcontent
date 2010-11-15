# Map Content System.

## Rational
  In the Q1 release of Nakamura we had major scalability and concurrency problems caused mainly by our use cases for a content
store not being closely aligned with those of Jackrabbit. We were not able to work around those problems and although we did manage
to release the code, its quite clear that in certain areas Jackrabbit wont work for us. This should not reflect badly on Jackrabbit, 
but it is a realization that our use cases are not compatible with Jackrabbit when exposed to scale.

This code base is a reaction to that. It aims to be really simple, completely concurrent with no synchronization and designed to scale
linearly with the number of cores and number of servers in a cluster. To do this it borrows some of the concepts from JCR at a very
abstract level, but is making a positive effort and selfish effort to only provide those things that we absolutely need to have. 

This code provides User, Group, Access Control and Content functionality using a sparse Map as a storage abstraction. 

The Implementation works on manipulating sparse objects in the Map with operations like get, insert and delete, but 
has no understanding of the underlying implementation of the storage mechanism. 

At the moment we have 2 storage mechanisms implemented, In Memory using a HashMap, and Cassandra. The approach should 
work on any Column Store (Dynamo, BigTable, Riak, Voldomort, Hbase etc) and can also work on RDBMS's including sharded storage.

At the moment there is no query support, expecting all access to be via column IDs, and multiple views to be written to the 
underlying store.

The intention is to provide write through caches based on EhCache or Infinispan.

Transactions are supported, if supported by the underlying implementation of the storage, otherwise all operations are BASIC, non Atomic and immediate in nature.
We will add search indexes at some point using Lucene, perhaps in the form of Zoie


At this stage its pre-alpha, untested for performance and scalability and incomplete.



## Backlog

1. Provide Read Cache implementation of StorageClient that chains to a real storage client.
1. Provide Write Through Cache implementation of StorageClient that chains to a real storage client.
1. Provide Scoped (as in Transaction Scoped) implementation of StorageClient that chains to a real storage client.
1. Do scalability testing on MySQL and Cassandra
1. Implement Infinispan StorageClient cache (Read or Write Through)


## Completed Backlog
1. Check all byte[] usage and limit to only real bodies. (14/11/2010) no byte[] are used for content bodies.
1. Replace all byte[] usage with InputStreams or a resetable holder, pushing down into the StorageClient. (14/11/2010)




## Tests


### Memory
All performed on a MackBook Pro which is believed to have 4 cores.
Add a user, 1 - 10 threads. Storage is a Concurrent Hash Map. This tests the code base for concurrency.
`
Threads,Time(s),Throughput, Throughput per thread
      1,  0.402,   2487.56,      2487.56 
      2,  0.144,   6944.44,      3472.22
      3,  0.051,  19607.84,      6535.94 
      4,  0.129,   7751.93,      1937.98 
      5,  0.05,   20000.0,       4000.0 
      6,  0.215,   4651.16,       775.19 
      7,  0.025,  40000.0,       5714.28 
      8,  0.026,  38461.53,      4807.69 
      9,  0.078,  12820.51,      1424.50 
     10,  0.037,  27027.02,      2702.70
` 
Throughput is users added per second.


### JDBC
Same as above, using a local MySQL Instance.

`
Threads,Time(s),Throughput, Throughput per thread
      1, 12.186,     82.06,        82.06 
      2,  9.646,    103.66,        51.83 
      3, 11.177,     89.46,        29.82 
      4, 15.894,     62.91,        15.72 
      5,  9.652,    103.60,        20.72 
      6, 16.734,     59.75,         9.95 
      7, 21.761,     45.95,         6.56 
      8, 13.962,     71.62,         8.95 
      9, 10.173,     98.29,        10.92 
     10, 11.466,     87.21,         8.72      
`    
Throughput is users added per second.


So far it looks like the code is concurrent, but MySQL is not.


