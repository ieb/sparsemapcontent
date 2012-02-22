# Sparse Map post OAE Fork.


This version of Sparse Map was created after the 1.5 release.

That release was cut to enable the Sakai OAE managed project to take full responsibility
for the code base underlying Sakai OAE. At the time of writing there are 2 branches 
in the Git Repository. master and postOAEFork. master is the branch made available 
for Sakai OAE to submit pull requests to should the feel it necessary, however
that branch will not be actively developed from the fork point onwards.

This branch postOAEFork is where the development will continue.

At some point in the future, when Sakai OAE has stabalised and taken full responsibility for 
its code base, I will switch the branches over and update this document.


# What is SparseMapi ?

Sparse Map is a content repository supporting shallow wide hierachies of content typical of 
Social Media content systems. It has support for storing that content on sharded databases.

# Back end persistence.

There are several storage implementations although only a few of the JDBC storage implementations are 
actively suported and released. The main JDBC storage implementation is supported for PostgreSQL and Oracle 
with a development configuration for Apache Derby. There are unsupported configuations for MySQL. In addition
there are experimental and unsupported storage implementations for MongoDB, Cassandra, HBase. None of the
unsupported storage implementations are intended for real use, which is why they have never been released.

# How did SparseMap start ?

Sparse Map was developed as a replacement content system for Sakai OAE. It was designed and built solely to support 
the needs of that project with no other use cases considered. Overtime, to ensure releases were stable and of high quality
a testing environment that allowed integration and load testing was built under the extensions sub folder. Critically 
sparse map delivers gainst the use case of highly concurrent updates of wide shallow content hierachies, with many ACLs.


# Why not a ColumnDB.

At the time, the Sakai OAE community was not prepared to deploy something as scary and new as a column database. Cassandra
on which the first iteration was built was not considered safe and the more stable column DBs at the time were thought
too complext for most istitutions intending to use Sakai OAE in production. Most deployers already knew how to manage and deploy 
Oracle, MySQL, PostgreSQL which is why its supported storage layers used those approaches. It would have been far easier
to build the whole system using a column DB but no one was prepared to deploy.

# Does it Shard ?

yes, ColumnDB's shard, howver the JDBC Storage implementations in Sparsemap was designed to shard from day 1. All keys are constructed
to create a uniform distribution of load over the entire key space which may be sharded by adjusting the database configuration. No
code changes should be requried.

# Can you tune the SQL ?

yes. All SQL queries are stored in the configuration file and when used correctly an application will name its queries enabling a DBA
to single out queries for tuning.

# Is the DB schema fixed ?

no. The schema is designed to be changed by configuration by DBAs, so that if it is discovered some query tables are too expensive, to 
sparse, the schema can be reconfigured to store data in more tables. The maping between the operation of the storage layer and
the underlying database is entirely in 2 configuration files.

# How do the ACLs work.

ACLs are constructed as 32bit bitmaps which are ored together to form access control bit maps for a user on a specific document.
The result of the access control resolution results in the access control list being cached in memory for all users so that subsequent
access control operations only require a singled bitwise or operation if in the same session, or a lookup and a handfull of bitwise or's
if in a new session. Since this cache only stores 4 bytes + a small key per access control statement, a small ammount of memory is capable
of holding a very large number of compiled bitmaps, eliminating most if not all database traffic to resolve access control statements.

# How does access control work on searching.

We store the principals of who can read what in the database to control the density of all search results. We are not looking for a fully dense
set of results, but we do want to get the density of results a user can access up above 80%, so that retrieving the first 10 pages of a sorted
result set quick. It is expected that the application will actively manage the number of principals any one user has to automaticly create holding principals
representing a larger set. If this is not done, then queries for those users will be slow.

# Can free text searching be done ?

There is a companion bundle in extensions/solr that takes content and indexes it in Solr. For development environments it uses an embedded Solr 
instance. For production environments the Solr instance is presumed to be a Solr Cluster or a Solr cloud. Clients using the Solr bundle must remember
that it is not real time and should not expect documents added to the index to appear instantly everyhwere. The storage layers are real time.

# Other questions 

Please just ask, I will add the question and answere here.
