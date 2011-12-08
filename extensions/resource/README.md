This bundle provides resource processing support and basic protocol support for 
POST and GET operations.

# Resource Processing Support

Processing is managed by JAX-RS bean (uk.co.tfd.sm.resource.DefaultResourceHandler) that implements uk.co.tfd.sm.api.jaxrs.JaxRestService. This is registered by OSGi with the JAX-RS provider bundle. The path is set to /, namely all requests.

The DefaultResourceHandler uses a JAX-RS annotation to associate uk.co.tfd.sm.resource.DefaultResourceHandler.getResource(HttpServletRequest, HttpServletResponse, String) with all requests. That initiates a session if one is already associated with the request or authenticates a new session if there is no active session associated with the request. Having got a session it takes the URL and parses it looking for a match in the content system.

The last part of the URL is parsed first, splitting on '.', removing more and more of the last element of the URL looking for an exact match. If no match is found, then the path is split on '/', searching for matching resources. When a match is found, even if its a root match, then a ResourceImpl is created that defines the match. That ResourceImpl is passed to the ResponseFactoryManagerImpl to find a suitable JAX-RS response bean for the Resource. The ResponseFactoryManagerImpl builds a hash of the characteristics of the Resource based on the path, the underlying resource, selectors, extension and the HTTP method. This hash is then used as a key to lookup a list of ResponseFactoryies that might provide a JAX-RS response bean for the for the Resource. ResponseFactgory(s) are comparable and the most significant ResponseFactory is used to create a Response bean for the Resource. This is a 1 step operation.

The Bean that is produced is a JAX-RS Bean that is returned by the getResource method at which point the JAX-RS provider takes over an inspects the annotations on the bean to complete processing. Those annotations may be anything that JAX-RS supports as we have made no assertions about the request in finding the Resource or binding a Response to the Resource. The Response bean has access to the parent Adaptables giving it the ability to Adapt to anything that any parent adaptable adapts to. eg Session, ContentManager, Request, Response etc.

# Default Protocol

Resource parsing protocol is derived from Apache Sling.
Urls as split into three parts. The resource path, selectors and the extension. Selectors and extensions are only relevant in the last path element of the Url after the resource path. If after removing the resource path and selecting the remainder of the last element of the URL, there are no '.', then the remainder is an extension and there are no selectors. If the remainder has dots then the last element of that dotted string is the extension and the previous ones are selectors

Some examples

* /a/resource/path/to/a/resource.selector.selector.extension
* /a/resource/path/to/a/resource.selector.extension
* /a/resource/path/to/a/resource.extension
* /a/resource/path/to/a/resource
* /a/reso.urce/pa.th/t.o/a/resource
* /a/reso.urce/pa.th/t.o/a/resource/with/some/extra/path/selector.selector.extension

The last example shows a resource with extra path info associated with the resource. The selectors and extensions are always taken from the last element.


## GET
* If the URL matches the resource the body is streamed
* If the URL does not match the resource some other action is performed depending on the selectors an extensions.
    * An extension of json sends the properties of the resource out as a json stream.
    * An extension of xml sends the properties of the resource out as an xml stream.
    * A selector that is a number causes that stream to iterate that number of levels into the content tree.
    * A selector of -1 or infinity causes that stream to contain the entire subtree
    * A selector of tidy or pp (pretty print) formats


## POST
* The URL identifies the resource
* If the post is a standard post the names of the POST parameters are the properties and the values are the values.
* Standard POSTS (uri encoded)
    * Multiple POST parameters of the same name automatically convert the property into an array.
    * A property can be forced to become an array by appending [] to the POST parameter name eg myarray[]
    * A property can be forced to have a type by appending the type name eg myinteger@Integer
    * Available types are registered in uk.co.tfd.sm.resource.RequestUtils.TYPE_CLASSES
    * Types implement uk.co.tfd.sm.resource.types.RequestParameterType
    * The conversion from POST parameter string to the internal Type is type specific.
* Multipart Posts
    * If the part of the multipart post is a form field, then its processes as if it was from a standard post
    * If the part of the post is a stream body, the name of the part is the name of the child node and the body is streamed to that node.
    * If the name contains a @ then the second part of the name is the alternative stream name where the body is stored. eg fileName@alternativeStream1
    * A client may send as as many body parts as they desire in the request intermixed with as many property values, however the client should be aware that the request will be processed in sequence and streams will be saved to their final destination before all property values are committed.

Note: the POST implementation uses Request Streaming and connects the HTTP socket directly to the final output stream with minimal buffering. There is no intermediate save to disk or save to memory.


## Standard Types
* Integer, Double, String, Long, Boolean all use the standard Java type representation.
* Calendar uses ISO8601Date which will parse any valid ISO8601Date and store either date or timestamp information honoring Timzone and daylight saving. 

## Extending

At present this bundle does not support alternative resource providers. Its been written the way it has to be fast and simple. To add a different repository, write a new JaxRestService implementation and register it at a new location. 
To add ResponseFacotries that implement alternative functionality, write a new ResponseFactoryImplementation, annotate it with the Resource characteristics it should respond to and register it with OSGi. This can/should be done in a separate bundle.


## Integration

There are integration tests under src/test/java in package uk.co.tfd.sm.integration.resource that are run with mvn -Pintegration test. And unit test that contains integration in the package name will get run by that profile. Here these integration tests test the above protocol in a live server over HTTP. They include tests for all the data types, file streaming and full tests UTF8 upto 3 byte charsets.