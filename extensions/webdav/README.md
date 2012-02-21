Please note, the web dav bundle should only be used in trusted situations were its 
absolutely safe to allow users to view the content of other users from the application
host. If this can't be guaranteed, don't use webdav. WebDav clients to not reliably
follow redirects so its not possible to redirect to a content host and prevent
a user from uploading untrusted content.

One simple solution is to only expose webdav on a separate server, that only 
serves webdav and nothing else.