# Request Processing

Request Processing for Posts is performed by the ModificationRequest class. It does the processing according to the protocol, converting the post parameters from string into the types that have been requested. At the end of processing, the ModificationRequest can be passed to a to a Helper to apply those accumulated changes to an object.

The ModificationRequest class streams the request, and if its provided with a RequestStreamProcessorImplementation, it will call for every non form field, giving it a stream for a body. File uploads are handled like this. We could handle other types of multi operations in the same way. 


Multi Operations to update content, authorizables, access control (ie not batch post operations)
I think the best way of handling multiple operations is to use multipart posts containing application/x-www-form-urlencoded parts

The name of the part is the path to the content to be updated.
The content is the post to that content.

This makes it possible for a single post operation to update many content items in sequence.


# Authentication

The AuhenticationService holds references to AuthenticationHandlers which inspect the request and if 
the handler is able to perform authentication it will respond with an Implementation of an 
AuthenticationServiceCredentials. This may include the password and it may not. If its trusted
then it will be used to log the user in administratively as the authentication was performed 
by a provider. If its not trusted it the password will be used to log the request in against
the repository. Once logged in, the credential as appropriate the a token principal is added to the 
response so that subsequent request authentication binds to the token processing.

Authentcation Types:
Basic, OpenID, OpenID with OAuth, Token and TrustedHeader, TrustedRequest.


