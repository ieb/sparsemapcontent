# Request Processing

Request Processing for Posts is performed by the ModificationRequest class. It does the processing according to the protocol, converting the post parameters from string into the types that have been requested. At the end of processing, the ModificationRequest can be passed to a to a Helper to apply those accumulated changes to an object.

The ModificationRequest class streams the request, and if its provided with a RequestStreamProcessorImplementation, it will call for every non form field, giving it a stream for a body. File uploads are handled like this. We could handle other types of multi operations in the same way. 


Multi Operations to update content, authorizables, access control (ie not batch post operations)
I think the best way of handling multiple operations is to use multipart posts containing application/x-www-form-urlencoded parts

The name of the part is the path to the content to be updated.
The content is the post to that content.

This makes it possible for a single post operation to update many content items in sequence.


