// ========================================================================
// Copyright 199-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.sakaiproject.nakamura.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.http.base.internal.context.ServletContextImpl;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.WriterOutputStream;
import org.mortbay.io.nio.DirectNIOBuffer;
import org.mortbay.io.nio.IndirectNIOBuffer;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpContent;
import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.HttpHeaderValues;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.InclusiveByteRange;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.ResourceCache;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.nio.NIOConnector;
import org.mortbay.resource.FileResource;
import org.mortbay.resource.Resource;
import org.mortbay.resource.ResourceFactory;
import org.mortbay.util.IO;
import org.mortbay.util.MultiPartOutputStream;
import org.mortbay.util.TypeUtil;
import org.mortbay.util.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/* ------------------------------------------------------------ */
/** The default servlet.                                                 
 * This servlet, normally mapped to /, provides the handling for static 
 * content, OPTION and TRACE methods for the context.                   
 * The following initParameters are supported, these can be set either
 * on the servlet itself or as ServletContext initParameters with a prefix
 * of org.mortbay.jetty.servlet.Default. :                          
 * <PRE>                                                                      
 *   acceptRanges     If true, range requests and responses are         
 *                    supported                                         
 *                                                                      
 *   dirAllowed       If true, directory listings are returned if no    
 *                    welcome file is found. Else 403 Forbidden.        
 *
 *   welcomeServlets  If true, attempt to dispatch to welcome files 
 *                    that are servlets, but only after no matching static 
 *                    resources could be found. 
 *                    
 *                    This must be false if you want directory listings,
 *                    but have index.jsp in your welcome file list.
 *
 *   redirectWelcome  If true, welcome files are redirected rather than
 *                    forwarded to.
 *
 *   gzip             If set to true, then static content will be served as 
 *                    gzip content encoded if a matching resource is 
 *                    found ending with ".gz"
 *
 *  resourceBase      Set to replace the context resource base
 *
 *  relativeResourceBase    
 *                    Set with a pathname relative to the base of the
 *                    servlet context root. Useful for only serving static content out
 *                    of only specific subdirectories.
 * 
 *  aliases           If True, aliases of resources are allowed (eg. symbolic
 *                    links and caps variations). May bypass security constraints.
 *                    
 *  maxCacheSize      The maximum total size of the cache or 0 for no cache.
 *  maxCachedFileSize The maximum size of a file to cache
 *  maxCachedFiles    The maximum number of files to cache
 *  cacheType         Set to "bio", "nio" or "both" to determine the type resource cache. 
 *                    A bio cached buffer may be used by nio but is not as efficient as an
 *                    nio buffer.  An nio cached buffer may not be used by bio.    
 *  
 *  useFileMappedBuffer 
 *                    If set to true, it will use mapped file buffer to serve static content
 *                    when using NIO connector. Setting this value to false means that
 *                    a direct buffer will be used instead of a mapped file buffer. 
 *                    By default, this is set to true.
 *                    
 *  cacheControl      If set, all static content will have this value set as the cache-control
 *                    header.
 *                    
 * 
 * </PRE>
 *                                                                    
 *
 * @author Greg Wilkins (gregw)
 * @author Nigel Canonizado
 * @author ieb  - modified to make the servlet work in OSGi from a factory.
 */
public class StaticContentServlet extends HttpServlet implements ResourceFactory
{   
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticContentServlet.class);

    
    private boolean _acceptRanges=true;
    private boolean _dirAllowed=true;
    private boolean _welcomeServlets=false;
    private boolean _redirectWelcome=false;
    private boolean _gzip=true;
    
    private Resource _resourceBase;
    private NIOResourceCache _nioCache;
    private ResourceCache _bioCache;
    
    private String[] _welcomes;
    private boolean _aliases=false;
    private boolean _useFileMappedBuffer=false;
    ByteArrayBuffer _cacheControl;


	private MimeTypes _mimeTypes;


	private ServletContextImpl _context;
	
	private AtomicLong totalTime = new AtomicLong();
	private AtomicLong totalRequests = new AtomicLong();


    
    
    /* ------------------------------------------------------------ */
	@Override
	public void init(ServletConfig config) throws ServletException {
        ServletContext context = config.getServletContext();
        _context = (ServletContextImpl)context;
        _mimeTypes = new MimeTypes();
        _mimeTypes.setMimeMap(getMimeTypesMap());
        
        
        
        _welcomes=new String[] {"index.html"};
        
        _acceptRanges=getParameter(config.getInitParameter("acceptRanges"),_acceptRanges);
        _dirAllowed=getParameter(config.getInitParameter("dirAllowed"),_dirAllowed);
        _welcomeServlets=getParameter(config.getInitParameter("welcomeServlets"), _welcomeServlets);
        _redirectWelcome=getParameter(config.getInitParameter("redirectWelcome"),_redirectWelcome);
        _gzip=getParameter(config.getInitParameter("gzip"),_gzip);
        
        _aliases=getParameter(config.getInitParameter("aliases"),_aliases);

        if (!_aliases && !FileResource.getCheckAliases())
            throw new IllegalStateException("Alias checking disabled");
        if (_aliases)
            LOGGER.debug("Aliases are enabled");
        
        _useFileMappedBuffer=getParameter(config.getInitParameter("useFileMappedBuffer"),_useFileMappedBuffer);
        
        String rrb = config.getInitParameter("relativeResourceBase");
        if (!empty(rrb))
        {
            try
            {
                _resourceBase = Resource.newResource(_context.getContextPath()).addPath(URIUtil.SLASH).addPath(rrb);
            }
            catch (Exception e) 
            {
                LOGGER.warn(e.getMessage(),e);
                throw new UnavailableException(e.toString()); 
            }
        }
        
        String rb=config.getInitParameter("resourceBase");
        if (!empty(rrb) && !empty(rb))
            throw new  UnavailableException("resourceBase & relativeResourceBase");    
        
        if (!empty(rb))
        {
            try{_resourceBase=Resource.newResource(rb);}
            catch (Exception e) 
            {
                LOGGER.warn(e.getMessage(),e);
                throw new UnavailableException(e.toString()); 
            }
        }
        
        String t= config.getInitParameter("cacheControl");
        if (t!=null)
            _cacheControl=new ByteArrayBuffer(t);
        
        try
        {
            if (_resourceBase==null)
                _resourceBase = Resource.newResource(URIUtil.SLASH);

            String cache_type = config.getInitParameter("cacheType");
            int max_cache_size=getParameter(config.getInitParameter("maxCacheSize"), -2);
            int max_cached_file_size=getParameter(config.getInitParameter("maxCachedFileSize"), -2);
            int max_cached_files=getParameter(config.getInitParameter("maxCachedFiles"), -2);

            if (cache_type==null || "nio".equals(cache_type)|| "both".equals(cache_type))
            {
                if (max_cache_size==-2 || max_cache_size>0)
                {
                    _nioCache=new NIOResourceCache(_mimeTypes);
                    if (max_cache_size>0)
                        _nioCache.setMaxCacheSize(max_cache_size);    
                    if (max_cached_file_size>=-1)
                        _nioCache.setMaxCachedFileSize(max_cached_file_size);    
                    if (max_cached_files>=-1)
                        _nioCache.setMaxCachedFiles(max_cached_files);
                    _nioCache.start();
                }
            }
            if ("bio".equals(cache_type)|| "both".equals(cache_type))
            {
                if (max_cache_size==-2 || max_cache_size>0)
                {
                    _bioCache=new ResourceCache(_mimeTypes);
                    if (max_cache_size>0)
                        _bioCache.setMaxCacheSize(max_cache_size);    
                    if (max_cached_file_size>=-1)
                        _bioCache.setMaxCachedFileSize(max_cached_file_size);    
                    if (max_cached_files>=-1)
                        _bioCache.setMaxCachedFiles(max_cached_files);
                    _bioCache.start();
                }
            }
            if (_nioCache==null)
                _bioCache=null;
        }
        catch (Exception e) 
        {
            LOGGER.warn(e.getMessage(),e);
            throw new UnavailableException(e.toString()); 
        }
        
        
        if (LOGGER.isDebugEnabled()) LOGGER.debug("resource base = "+_resourceBase);
    }

    private boolean getParameter(String param, boolean defaultValue) {
    	if ( param == null )  {
    		return defaultValue;
    	}
    	return "TtYy1".indexOf(param.codePointAt(0)) >= 0;
	}

    private int getParameter(String param, int defaultValue) {
    	if ( param == null )  {
    		return defaultValue;
    	}
    	return Integer.parseInt(param);
	}

	private boolean empty(String v) {
		return v == null || v.trim().length() == 0;
	}

	private Map getMimeTypesMap() {
        Map<String, String> mmap = new HashMap<String, String>();
        mmap.put("abs", "audio/x-mpeg");
        mmap.put("ai", "application/postscript");
        mmap.put("aif", "audio/x-aiff");
        mmap.put("aifc", "audio/x-aiff");
        mmap.put("aiff", "audio/x-aiff");
        mmap.put("aim", "application/x-aim");
        mmap.put("art", "image/x-jg");
        mmap.put("asf", "video/x-ms-asf");
        mmap.put("asx", "video/x-ms-asf");
        mmap.put("au", "audio/basic");
        mmap.put("avi", "video/x-msvideo");
        mmap.put("avx", "video/x-rad-screenplay");
        mmap.put("bcpio", "application/x-bcpio");
        mmap.put("bin", "application/octet-stream");
        mmap.put("bmp", "image/bmp");
        mmap.put("body", "text/html");
        mmap.put("cdf", "application/x-cdf");
        mmap.put("cer", "application/x-x509-ca-cert");
        mmap.put("class", "application/java");
        mmap.put("cpio", "application/x-cpio");
        mmap.put("csh", "application/x-csh");
        mmap.put("css", "text/css");
        mmap.put("dib", "image/bmp");
        mmap.put("doc", "application/msword");
        mmap.put("dtd", "application/xml-dtd");
        mmap.put("dv", "video/x-dv");
        mmap.put("dvi", "application/x-dvi");
        mmap.put("eps", "application/postscript");
        mmap.put("etx", "text/x-setext");
        mmap.put("exe", "application/octet-stream");
        mmap.put("gif", "image/gif");
        mmap.put("gk", "application/octet-stream");
        mmap.put("gtar", "application/x-gtar");
        mmap.put("gz", "application/x-gzip");
        mmap.put("hdf", "application/x-hdf");
        mmap.put("hqx", "application/mac-binhex40");
        mmap.put("htc", "text/x-component");
        mmap.put("htm", "text/html");
        mmap.put("html", "text/html");
        mmap.put("hqx", "application/mac-binhex40");
        mmap.put("ief", "image/ief");
        mmap.put("jad", "text/vnd.sun.j2me.app-descriptor");
        mmap.put("jar", "application/java-archive");
        mmap.put("java", "text/plain");
        mmap.put("jnlp", "application/x-java-jnlp-file");
        mmap.put("jpe", "image/jpeg");
        mmap.put("jpeg", "image/jpeg");
        mmap.put("jpg", "image/jpeg");
        mmap.put("js", "text/javascript");
        mmap.put("kar", "audio/x-midi");
        mmap.put("latex", "application/x-latex");
        mmap.put("m3u", "audio/x-mpegurl");
        mmap.put("mac", "image/x-macpaint");
        mmap.put("man", "application/x-troff-man");
        mmap.put("mathml", "application/mathml+xml");
        mmap.put("me", "application/x-troff-me");
        mmap.put("mid", "audio/x-midi");
        mmap.put("midi", "audio/x-midi");
        mmap.put("mif", "application/x-mif");
        mmap.put("mov", "video/quicktime");
        mmap.put("movie", "video/x-sgi-movie");
        mmap.put("mp1", "audio/x-mpeg");
        mmap.put("mp2", "audio/x-mpeg");
        mmap.put("mp3", "audio/x-mpeg");
        mmap.put("mpa", "audio/x-mpeg");
        mmap.put("mpe", "video/mpeg");
        mmap.put("mpeg", "video/mpeg");
        mmap.put("mpega", "audio/x-mpeg");
        mmap.put("mpg", "video/mpeg");
        mmap.put("mpv2", "video/mpeg2");
        mmap.put("ms", "application/x-wais-source");
        mmap.put("nc", "application/x-netcdf");
        mmap.put("oda", "application/oda");
        mmap.put("ogg", "application/ogg");
        mmap.put("pbm", "image/x-portable-bitmap");
        mmap.put("pct", "image/pict");
        mmap.put("pdf", "application/pdf");
        mmap.put("pgm", "image/x-portable-graymap");
        mmap.put("pic", "image/pict");
        mmap.put("pict", "image/pict");
        mmap.put("pls", "audio/x-scpls");
        mmap.put("png", "image/png");
        mmap.put("pnm", "image/x-portable-anymap");
        mmap.put("pnt", "image/x-macpaint");
        mmap.put("ppm", "image/x-portable-pixmap");
        mmap.put("ppt", "application/powerpoint");
        mmap.put("ps", "application/postscript");
        mmap.put("psd", "image/x-photoshop");
        mmap.put("qt", "video/quicktime");
        mmap.put("qti", "image/x-quicktime");
        mmap.put("qtif", "image/x-quicktime");
        mmap.put("ras", "image/x-cmu-raster");
        mmap.put("rdf", "application/rdf+xml");
        mmap.put("rgb", "image/x-rgb");
        mmap.put("rm", "application/vnd.rn-realmedia");
        mmap.put("roff", "application/x-troff");
        mmap.put("rtf", "application/rtf");
        mmap.put("rtx", "text/richtext");
        mmap.put("sh", "application/x-sh");
        mmap.put("shar", "application/x-shar");
        mmap.put("shtml", "text/x-server-parsed-html");
        mmap.put("sit", "application/x-stuffit");
        mmap.put("smf", "audio/x-midi");
        mmap.put("snd", "audio/basic");
        mmap.put("src", "application/x-wais-source");
        mmap.put("sv4cpio", "application/x-sv4cpio");
        mmap.put("sv4crc", "application/x-sv4crc");
        mmap.put("svg", "image/svg+xml");
        mmap.put("svgz", "image/svg+xml");
        mmap.put("swf", "application/x-shockwave-flash");
        mmap.put("t", "application/x-troff");
        mmap.put("tar", "application/x-tar");
        mmap.put("tcl", "application/x-tcl");
        mmap.put("tex", "application/x-tex");
        mmap.put("texi", "application/x-texinfo");
        mmap.put("texinfo", "application/x-texinfo");
        mmap.put("tif", "image/tiff");
        mmap.put("tiff", "image/tiff");
        mmap.put("tr", "application/x-troff");
        mmap.put("tsv", "text/tab-separated-values");
        mmap.put("txt", "text/plain");
        mmap.put("ulw", "audio/basic");
        mmap.put("ustar", "application/x-ustar");
        mmap.put("xbm", "image/x-xbitmap");
        mmap.put("xml", "text/xml");
        mmap.put("xpm", "image/x-xpixmap");
        mmap.put("xsl", "application/xml");
        mmap.put("xslt", "application/xslt+xml");
        mmap.put("xwd", "image/x-xwindowdump");
        mmap.put("vsd", "application/x-visio");
        mmap.put("vxml", "application/voicexml+xml");
        mmap.put("wav", "audio/x-wav");
        mmap.put("wbmp", "image/vnd.wap.wbmp");
        mmap.put("wml", "text/vnd.wap.wml");
        mmap.put("wmlc", "application/vnd.wap.wmlc");
        mmap.put("wmls", "text/vnd.wap.wmls");
        mmap.put("wmlscriptc", "application/vnd.wap.wmlscriptc");
        mmap.put("wrl", "x-world/x-vrml");
        mmap.put("xht", "application/xhtml+xml");
        mmap.put("xhtml", "application/xhtml+xml");
        mmap.put("xls", "application/vnd.ms-excel");
        mmap.put("xul", "application/vnd.mozilla.xul+xml");
        mmap.put("Z", "application/x-compress");
        mmap.put("z", "application/x-compress");
        mmap.put("zip", "application/zip");
        return mmap;
	}
    
    
    /* ------------------------------------------------------------ */
    /** get Resource to serve.
     * Map a path to a resource. The default implementation calls
     * HttpContext.getResource but derived servlets may provide
     * their own mapping.
     * @param pathInContext The path to find a resource for.
     * @return The resource to serve.
     */
    public Resource getResource(String pathInContext)
    {
    	LOGGER.debug("Getting Resource [{}] [{}] ",_resourceBase, pathInContext);
        if (_resourceBase==null)
            return null;
        Resource r=null;
        try
        {
            r = _resourceBase.addPath(pathInContext);
            if (!_aliases && r.getAlias()!=null)
            {
                if (r.exists())
                    LOGGER.warn("Aliased resource: {} == {} ",r,r.getAlias());
                return null;
            }
            if (LOGGER.isDebugEnabled()) LOGGER.debug("RESOURCE= }",r);
             
        }
        catch (IOException e)
        {
            LOGGER.warn(e.getMessage(), e);
        }
        return r;
    }
    
    /* ------------------------------------------------------------ */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    	throws ServletException, IOException
    {
    	
    	LOGGER.debug("GET {} ",request.getRequestURL().toString());
        String servletPath=null;
        String pathInfo=null;
        Enumeration reqRanges = null;
        servletPath=request.getServletPath();
        pathInfo=request.getPathInfo();
        
        // Is this a range request?
        reqRanges = request.getHeaders(HttpHeaders.RANGE);
        if (reqRanges!=null && !reqRanges.hasMoreElements())
            reqRanges=null;
        
        String pathInContext= pathInfo;
        if ( pathInContext == null ) pathInContext = "";
        boolean endsWithSlash=pathInContext.endsWith(URIUtil.SLASH);
        
        // Can we gzip this request?
        String pathInContextGz=null;
        boolean gzip=false;
        if (_gzip && reqRanges==null && !endsWithSlash )
        {
            String accept=request.getHeader(HttpHeaders.ACCEPT_ENCODING);
            if (accept!=null && accept.indexOf("gzip")>=0)
                gzip=true;
        }
        
        // Find the resource and content
        Resource resource=null;
        HttpContent content=null;
        
        Connector connector = HttpConnection.getCurrentConnection().getConnector();
        ResourceCache cache=(connector instanceof NIOConnector) ?_nioCache:_bioCache;
        try
        {   
            // Try gzipped content first
            if (gzip)
            {
                pathInContextGz=pathInContext+".gz";  
                resource=getResource(pathInContextGz);

                if (resource==null || !resource.exists()|| resource.isDirectory())
                {
                    gzip=false;
                    pathInContextGz=null;
                }
                else if (cache!=null)
                {
                    content=cache.lookup(pathInContextGz,resource);
                    if (content!=null)
                        resource=content.getResource();
                }

                if (resource==null || !resource.exists()|| resource.isDirectory())
                {
                    gzip=false;
                    pathInContextGz=null;
                }
            }
        
            // find resource
            if (!gzip)
            {
                if (cache==null)
                    resource=getResource(pathInContext);
                else
                {
                    content=cache.lookup(pathInContext,this);

                    if (content!=null)
                        resource=content.getResource();
                    else
                        resource=getResource(pathInContext);
                }
            }
            
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("resource="+resource+(content!=null?" content":""));
                        
            // Handle resource
            if (resource==null || !resource.exists())
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            else if (!resource.isDirectory())
            {   
                if (endsWithSlash && _aliases && pathInContext.length()>1)
                {
                    String q=request.getQueryString();
                    pathInContext=pathInContext.substring(0,pathInContext.length()-1);
                    if (q!=null&&q.length()!=0)
                        pathInContext+="?"+q;
                    response.sendRedirect(response.encodeRedirectURL(URIUtil.addPaths( _context.getContextPath(),pathInContext)));
                }
                else
                {
                    // ensure we have content
                    if (content==null)
                        content=new UnCachedContent(resource);

                    if (passConditionalHeaders(request,response, resource,content))  
                    {
                        if (gzip)
                        {
                            response.setHeader(HttpHeaders.CONTENT_ENCODING,"gzip");
                            String mt=_context.getMimeType(pathInContext);
                            if (mt!=null)
                                response.setContentType(mt);
                        }
                        sendData(request,response,false,resource,content,reqRanges);  
                    }
                }
            }
            else
            {
                String welcome=null;
                
                if (!endsWithSlash || (pathInContext.length()==1 && request.getAttribute("org.mortbay.jetty.nullPathInfo")!=null))
                {
                    StringBuffer buf=request.getRequestURL();
                    int param=buf.lastIndexOf(";");
                    if (param<0)
                        buf.append('/');
                    else
                        buf.insert(param,'/');
                    String q=request.getQueryString();
                    if (q!=null&&q.length()!=0)
                    {
                        buf.append('?');
                        buf.append(q);
                    }
                    response.setContentLength(0);
                    response.sendRedirect(response.encodeRedirectURL(buf.toString()));
                }
                // else look for a welcome file
                else if (null!=(welcome=getWelcomeFile(pathInContext)))
                {
                        // Redirect to the index
                        response.setContentLength(0);
                        String q=request.getQueryString();
                        if (q!=null&&q.length()!=0)
                            response.sendRedirect(response.encodeRedirectURL(URIUtil.addPaths( _context.getContextPath(),welcome)+"?"+q));
                        else
                            response.sendRedirect(response.encodeRedirectURL(URIUtil.addPaths( _context.getContextPath(),welcome)));
                }
                else 
                {
                    content=new UnCachedContent(resource);
                    if (passConditionalHeaders(request,response, resource,content))
                        sendDirectory(request,response,resource,pathInContext.length()>1);
                }
            }
        }
        catch(IllegalArgumentException e)
        {
            LOGGER.warn(e.getMessage(),e);
            if(!response.isCommitted())
                response.sendError(500, e.getMessage());
        }
        finally
        {
            if (content!=null)
                content.release();
            else if (resource!=null)
                resource.release();
        }
        
    }
    
    /* ------------------------------------------------------------ */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doGet(request,response);
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doTrace(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /* ------------------------------------------------------------ */
    /**
     * Finds a matching welcome file for the supplied {@link Resource}. This will be the first entry in the list of 
     * configured {@link #_welcomes welcome files} that existing within the directory referenced by the <code>Resource</code>.
     * If the resource is not a directory, or no matching file is found, then it may look for a valid servlet mapping. 
     * If there is none, then <code>null</code> is returned.
     * The list of welcome files is read from the {@link ContextHandler} for this servlet, or
     * <code>"index.jsp" , "index.html"</code> if that is <code>null</code>.
     * @param pathInContext The path in Context in which we are looking for a welcome
     * @return The path of the matching welcome file in context
     * @throws IOException
     * @throws MalformedURLException
     */
    private String getWelcomeFile(String pathInContext) throws MalformedURLException, IOException
    {
        if (_welcomes==null)
            return null;
       
        String welcome_servlet=null;
        for (int i=0;i<_welcomes.length;i++)
        {
            String welcome_in_context=URIUtil.addPaths(pathInContext,_welcomes[i]);
            Resource welcome=getResource(welcome_in_context);
            if (welcome!=null && welcome.exists()) {
                return _welcomes[i];           	
            }
            
        }
        return welcome_servlet;
        
    }

    /* ------------------------------------------------------------ */
    /* Check modification date headers.
     */
    protected boolean passConditionalHeaders(HttpServletRequest request,HttpServletResponse response, Resource resource, HttpContent content)
    throws IOException
    {
        try
        {
            if (!request.getMethod().equals(HttpMethods.HEAD) )
            {
                String ifms=request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
                if (ifms!=null)
                {
                    if (content!=null)
                    {
                        Buffer mdlm=content.getLastModified();
                        if (mdlm!=null)
                        {
                            if (ifms.equals(mdlm.toString()))
                            {
                                response.reset();
                                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                                response.flushBuffer();
                                return false;
                            }
                        }
                    }
                        
                    long ifmsl=request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
                    if (ifmsl!=-1)
                    {
                        if (resource.lastModified()/1000 <= ifmsl/1000)
                        {
                            response.reset();
                            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                            response.flushBuffer();
                            return false;
                        }
                    }
                }

                // Parse the if[un]modified dates and compare to resource
                long date=request.getDateHeader(HttpHeaders.IF_UNMODIFIED_SINCE);
                
                if (date!=-1)
                {
                    if (resource.lastModified()/1000 > date/1000)
                    {
                        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                        return false;
                    }
                }
                
            }
        }
        catch(IllegalArgumentException iae)
        {
            if(!response.isCommitted())
                response.sendError(400, iae.getMessage());
            throw iae;
        }
        return true;
    }
    
    
    /* ------------------------------------------------------------------- */
    protected void sendDirectory(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Resource resource,
                                 boolean parent)
    throws IOException
    {
        if (!_dirAllowed)
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        byte[] data=null;
        String base = URIUtil.addPaths(request.getRequestURI(),URIUtil.SLASH);
        String dir = resource.getListHTML(base,parent);
        if (dir==null)
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
            "No directory");
            return;
        }
        
        data=dir.getBytes("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
    }
    
    /* ------------------------------------------------------------ */
    protected void sendData(HttpServletRequest request,
                            HttpServletResponse response,
                            boolean include,
                            Resource resource,
                            HttpContent content,
                            Enumeration reqRanges)
    throws IOException
    {
        long content_length=resource.length();
        
        // Get the output stream (or writer)
        OutputStream out =null;
        try{out = response.getOutputStream();}
        catch(IllegalStateException e) {out = new WriterOutputStream(response.getWriter());}
        
        if ( reqRanges == null || !reqRanges.hasMoreElements())
        {
            //  if there were no ranges, send entire entity
            if (include)
            {
                resource.writeTo(out,0,content_length);
            }
            else
            {
                // See if a direct methods can be used?
                if (out instanceof HttpConnection.Output)
                {
                    if (response instanceof Response)
                    {
                        writeOptionHeaders(((Response)response).getHttpFields());
                        ((HttpConnection.Output)out).sendContent(content);
                    }
                    else if (content.getBuffer()!=null)
                    {
                        writeHeaders(response,content,content_length);
                        ((HttpConnection.Output)out).sendContent(content.getBuffer());
                    }
                    else
                    {
                        writeHeaders(response,content,content_length);
                        resource.writeTo(out,0,content_length);
                    }
                }
                else
                {
                    // Write content normally
                    writeHeaders(response,content,content_length);
                    resource.writeTo(out,0,content_length);
                }
            }
        }
        else
        {
            // Parse the satisfiable ranges
            List ranges =InclusiveByteRange.satisfiableRanges(reqRanges,content_length);
            
            //  if there are no satisfiable ranges, send 416 response
            if (ranges==null || ranges.size()==0)
            {
                writeHeaders(response, content, content_length);
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setHeader(HttpHeaders.CONTENT_RANGE, 
                        InclusiveByteRange.to416HeaderRangeString(content_length));
                resource.writeTo(out,0,content_length);
                return;
            }
            
            
            //  if there is only a single valid range (must be satisfiable 
            //  since were here now), send that range with a 216 response
            if ( ranges.size()== 1)
            {
                InclusiveByteRange singleSatisfiableRange =
                    (InclusiveByteRange)ranges.get(0);
                long singleLength = singleSatisfiableRange.getSize(content_length);
                writeHeaders(response,content,singleLength                     );
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader(HttpHeaders.CONTENT_RANGE, 
                        singleSatisfiableRange.toHeaderRangeString(content_length));
                resource.writeTo(out,singleSatisfiableRange.getFirst(content_length),singleLength);
                return;
            }
            
            
            //  multiple non-overlapping valid ranges cause a multipart
            //  216 response which does not require an overall 
            //  content-length header
            //
            writeHeaders(response,content,-1);
            String mimetype=content.getContentType().toString();
            MultiPartOutputStream multi = new MultiPartOutputStream(out);
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            
            // If the request has a "Request-Range" header then we need to
            // send an old style multipart/x-byteranges Content-Type. This
            // keeps Netscape and acrobat happy. This is what Apache does.
            String ctp;
            if (request.getHeader(HttpHeaders.REQUEST_RANGE)!=null)
                ctp = "multipart/x-byteranges; boundary=";
            else
                ctp = "multipart/byteranges; boundary=";
            response.setContentType(ctp+multi.getBoundary());
            
            InputStream in=resource.getInputStream();
            long pos=0;
            
            // calculate the content-length
            int length=0;
            String[] header = new String[ranges.size()];
            for (int i=0;i<ranges.size();i++)
            {
                InclusiveByteRange ibr = (InclusiveByteRange) ranges.get(i);
                header[i]=ibr.toHeaderRangeString(content_length);
                length+=
                    ((i>0)?2:0)+
                    2+multi.getBoundary().length()+2+ 
                    HttpHeaders.CONTENT_TYPE.length()+2+mimetype.length()+2+ 
                    HttpHeaders.CONTENT_RANGE.length()+2+header[i].length()+2+ 
                    2+
                    (ibr.getLast(content_length)-ibr.getFirst(content_length))+1;
            }
            length+=2+2+multi.getBoundary().length()+2+2;
            response.setContentLength(length);
            
            for (int i=0;i<ranges.size();i++)
            {
                InclusiveByteRange ibr = (InclusiveByteRange) ranges.get(i);
                multi.startPart(mimetype,new String[]{HttpHeaders.CONTENT_RANGE+": "+header[i]});
                
                long start=ibr.getFirst(content_length);
                long size=ibr.getSize(content_length);
                if (in!=null)
                {
                    // Handle non cached resource
                    if (start<pos)
                    {
                        in.close();
                        in=resource.getInputStream();
                        pos=0;
                    }
                    if (pos<start)
                    {
                        in.skip(start-pos);
                        pos=start;
                    }
                    IO.copy(in,multi,size);
                    pos+=size;
                }
                else
                    // Handle cached resource
                    (resource).writeTo(multi,start,size);
                
            }
            if (in!=null)
                in.close();
            multi.close();
        }
        return;
    }
    
    /* ------------------------------------------------------------ */
    protected void writeHeaders(HttpServletResponse response,HttpContent content,long count)
        throws IOException
    {   
        if (content.getContentType()!=null && response.getContentType()==null)
            response.setContentType(content.getContentType().toString());
        
        if (response instanceof Response)
        {
            Response r=(Response)response;
            HttpFields fields = r.getHttpFields();

            if (content.getLastModified()!=null)  
                fields.put(HttpHeaders.LAST_MODIFIED_BUFFER,content.getLastModified(),content.getResource().lastModified());
            else if (content.getResource()!=null)
            {
                long lml=content.getResource().lastModified();
                if (lml!=-1)
                    fields.putDateField(HttpHeaders.LAST_MODIFIED_BUFFER,lml);
            }
                
            if (count != -1)
                r.setLongContentLength(count);

            writeOptionHeaders(fields);
        }
        else
        {
            long lml=content.getResource().lastModified();
            if (lml>=0)
                response.setDateHeader(HttpHeaders.LAST_MODIFIED,lml);

            if (count != -1)
            {
                if (count<Integer.MAX_VALUE)
                    response.setContentLength((int)count);
                else 
                    response.setHeader(HttpHeaders.CONTENT_LENGTH,TypeUtil.toString(count));
            }

            writeOptionHeaders(response);
        }
    }

    /* ------------------------------------------------------------ */
    protected void writeOptionHeaders(HttpFields fields) throws IOException
    { 
        if (_acceptRanges)
            fields.put(HttpHeaders.ACCEPT_RANGES_BUFFER,HttpHeaderValues.BYTES_BUFFER);

        if (_cacheControl!=null)
            fields.put(HttpHeaders.CACHE_CONTROL_BUFFER,_cacheControl);
    }
    
    /* ------------------------------------------------------------ */
    protected void writeOptionHeaders(HttpServletResponse response) throws IOException
    { 
        if (_acceptRanges)
            response.setHeader(HttpHeaders.ACCEPT_RANGES,"bytes");

        if (_cacheControl!=null)
            response.setHeader(HttpHeaders.CACHE_CONTROL,_cacheControl.toString());
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy()
    {
        try
        {
            if (_nioCache!=null)
                _nioCache.stop();
            if (_bioCache!=null)
                _bioCache.stop();
        }
        catch(Exception e)
        {
            LOGGER.warn(e.getMessage(),e);
        }
        finally
        {
            super.destroy();
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class UnCachedContent implements HttpContent
    {
        Resource _resource;
        
        UnCachedContent(Resource resource)
        {
            _resource=resource;
        }
        
        /* ------------------------------------------------------------ */
        public Buffer getContentType()
        {
            return _mimeTypes.getMimeByExtension(_resource.toString());
        }

        /* ------------------------------------------------------------ */
        public Buffer getLastModified()
        {
            return null;
        }

        /* ------------------------------------------------------------ */
        public Buffer getBuffer()
        {
            return null;
        }

        /* ------------------------------------------------------------ */
        public long getContentLength()
        {
            return _resource.length();
        }

        /* ------------------------------------------------------------ */
        public InputStream getInputStream() throws IOException
        {
            return _resource.getInputStream();
        }

        /* ------------------------------------------------------------ */
        public Resource getResource()
        {
            return _resource;
        }

        /* ------------------------------------------------------------ */
        public void release()
        {
            _resource.release();
            _resource=null;
        }
        
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    class NIOResourceCache extends ResourceCache
    {
        /* ------------------------------------------------------------ */
        public NIOResourceCache(MimeTypes mimeTypes)
        {
            super(mimeTypes);
        }

        /* ------------------------------------------------------------ */
        protected void fill(Content content) throws IOException
        {
            Buffer buffer=null;
            Resource resource=content.getResource();
            long length=resource.length();

            if (_useFileMappedBuffer && resource.getFile()!=null) 
            {    
                buffer = new DirectNIOBuffer(resource.getFile());
            } 
            else 
            {
                InputStream is = resource.getInputStream();
                try
                {
                    Connector connector = HttpConnection.getCurrentConnection().getConnector();
                    buffer = ((NIOConnector)connector).getUseDirectBuffers()?
                            (NIOBuffer)new DirectNIOBuffer((int)length):
                            (NIOBuffer)new IndirectNIOBuffer((int)length);
                                
                }
                catch(OutOfMemoryError e)
                {
                    LOGGER.warn(e.toString());
                    LOGGER.debug(e.getMessage(),e);
                    buffer = new IndirectNIOBuffer((int) length);
                }
                buffer.readFrom(is,(int)length);
                is.close();
            }
            content.setBuffer(buffer);
        }
    }
}
