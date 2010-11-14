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

