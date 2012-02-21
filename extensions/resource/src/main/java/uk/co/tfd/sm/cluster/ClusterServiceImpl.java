package uk.co.tfd.sm.cluster;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Service;

import uk.co.tfd.sm.api.cluster.ClusterService;

@Component(immediate=true, metatype=true)
@Service(value=ClusterService.class)
public class ClusterServiceImpl implements ClusterService {

	private String serverId;

	@Activate
	public void activate(Map<String, Object> properties) throws MalformedObjectNameException, AttributeNotFoundException, InstanceNotFoundException, NullPointerException, MBeanException, ReflectionException, NoSuchAlgorithmException, IOException {
		modified(properties);
	}

	@Modified
	public synchronized void modified(Map<String, Object> properties)
			throws IOException, MalformedObjectNameException,
			NullPointerException, AttributeNotFoundException,
			InstanceNotFoundException, MBeanException, ReflectionException,
			NoSuchAlgorithmException {

		String slingHome = System.getProperty("sling.home");
		if (slingHome == null) {
			slingHome = "sling";
		}

		File sid = new File(slingHome, "serverid");
		if (sid.exists()) {
			FileReader fr = new FileReader(sid);
			serverId = IOUtils.toString(fr);
			fr.close();
		} else {
			MBeanServer mbeanServer = ManagementFactory
					.getPlatformMBeanServer();
			ObjectName name = new ObjectName("java.lang:type=Runtime");
			String serverProcessName = ((String) mbeanServer.getAttribute(name,
					"Name")).replace("@", "-");
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			serverId = Base64.encodeBase64URLSafeString(md
					.digest(serverProcessName.getBytes("UTF-8")));
			sid.getParentFile().mkdirs();
			FileWriter fw = new FileWriter(sid);
			fw.write(serverId);
			fw.close();
		}

	}

	@Override
	public String getServerId() {
		return serverId;
	}

}
