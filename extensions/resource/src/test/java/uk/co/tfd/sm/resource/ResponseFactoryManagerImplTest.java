package uk.co.tfd.sm.resource;

import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import uk.co.tfd.sm.api.resource.Adaptable;
import uk.co.tfd.sm.api.resource.ResponseFactory;
import uk.co.tfd.sm.api.resource.binding.ResponseBindingList;
import uk.co.tfd.sm.api.resource.binding.RuntimeResponseBinding;

public class ResponseFactoryManagerImplTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseFactoryManagerImplTest.class);

	@Test
	public void testBind() {
		ResponseFactoryManagerImpl r = new ResponseFactoryManagerImpl();
		
		ResponseFactory rf1 = new DefaultResponseFactory();
		ResponseFactory rf2 = Mockito.mock(ResponseFactory.class);
		ResponseFactory rf3 = Mockito.mock(ResponseFactory.class);
		Mockito.when(rf2.getBindings()).thenReturn( new ResponseBindingList(
				new RuntimeResponseBinding("GET",null,null,null),
				new RuntimeResponseBinding("GET","test/type",null,null),
				new RuntimeResponseBinding("POST","test/type","create",null),
				new RuntimeResponseBinding("GET","test/type","all","json"),
				new RuntimeResponseBinding("GET","test/type2",null,null)
		));
		Mockito.when(rf3.getBindings()).thenReturn( new ResponseBindingList(
				new RuntimeResponseBinding("GET",null,null,null),
				new RuntimeResponseBinding("GET","test/type",null,null),
				new RuntimeResponseBinding("POST","test/type","create",null),
				new RuntimeResponseBinding("GET","test/type","all","json"),
				new RuntimeResponseBinding("GET","test/type3",null,null)
		));
		r.bind(rf1);
		r.bind(rf2);
		r.bind(rf3);
		r.unbind(rf1);
		r.unbind(rf3);
		r.unbind(rf2);
	}
	
	@Test
	public void testDefaultGetResponse() {
		ResponseFactoryManagerImpl r = new ResponseFactoryManagerImpl();
		Adaptable resource = Mockito.mock(Adaptable.class);
		Mockito.when(resource.adaptTo(ResponseBindingList.class)).thenReturn(new ResponseBindingList(new RuntimeResponseBinding("GET","test/type",null,"json")));
		Adaptable response = r.createResponse(resource);
		Assert.assertNotNull(response);
		Assert.assertTrue(response instanceof DefaultResponse);
	}
	
	@Test
	public void testGetResponse() {
		ResponseFactoryManagerImpl r = new ResponseFactoryManagerImpl();
		ResponseFactory rf1 = Mockito.mock(ResponseFactory.class);
		Mockito.when(rf1.getBindings()).thenReturn( new ResponseBindingList(
				new RuntimeResponseBinding("GET",null,null,null),
				new RuntimeResponseBinding("GET","test/type",null,null),
				new RuntimeResponseBinding("POST","test/type","create",null),
				new RuntimeResponseBinding("GET","test/type","all","json"),
				new RuntimeResponseBinding("GET","test/type2",null,null)
		));
		r.bind(rf1);
		
		Adaptable resource = Mockito.mock(Adaptable.class);
		Mockito.when(resource.adaptTo(ResponseBindingList.class)).thenReturn(
				new ResponseBindingList(new RuntimeResponseBinding("POST","test/type","create","json")));
		Adaptable rf1response = Mockito.mock(Adaptable.class);
		Mockito.when(rf1.getResponse(resource)).thenReturn(rf1response);


		
		Adaptable response = r.createResponse(resource);
		Assert.assertNotNull(response);
		Assert.assertFalse(response instanceof DefaultResponse);
	}
	
	@Test
	public void testScalingGetResponse() {
		ResponseFactoryManagerImpl rm = new ResponseFactoryManagerImpl();
		Random r = new Random();
		String[] methods = populate(r,4);
		String[] types = populate(r,100);
		String[] selectors = populate(r,100);
		String[] extensions = populate(r,2);
		int nbindings = 500;
		long s = System.currentTimeMillis();
		for ( int i = 0; i < nbindings; i++) {
			int l = r.nextInt(4);
			List<RuntimeResponseBinding> rl = Lists.newArrayList();
			for ( int k = 0; k < l; k++) {
				int im = r.nextInt(methods.length);
				int it = r.nextInt(types.length);
				int is = r.nextInt(selectors.length);
				int ie = r.nextInt(extensions.length);
				rl.add(new RuntimeResponseBinding(methods[im], types[it], selectors[is], extensions[ie]));
			}
			ResponseFactory rf1 = Mockito.mock(ResponseFactory.class);
			Mockito.when(rf1.getBindings()).thenReturn( new ResponseBindingList(rl.toArray(new RuntimeResponseBinding[rl.size()])));
			rm.bind(rf1);
		}
		long e = System.currentTimeMillis();
		LOGGER.info("Average Add time for {} bindings  is {} ms",nbindings,(((double)(e-s))/(double)nbindings));
		
		s = System.currentTimeMillis();
		int ntest = 1000;
		Adaptable resource = Mockito.mock(Adaptable.class);
		for ( int i = 0; i < ntest; i++ ) {
			int im = r.nextInt(methods.length-1)+1;
			int it = r.nextInt(types.length-1)+1;
			int is = r.nextInt(selectors.length-1)+1;
			int ie = r.nextInt(extensions.length-1)+1;
			Mockito.when(resource.adaptTo(ResponseBindingList.class)).thenReturn(
					new ResponseBindingList(new RuntimeResponseBinding(methods[im], types[it], selectors[is], extensions[ie])));
			@SuppressWarnings("unused")
			Adaptable response = rm.createResponse(resource);
		}
		e = System.currentTimeMillis();
		LOGGER.info("Average Resolution time for {} bindings  is {} ms",nbindings,(((double)(e-s))/(double)ntest));

	}

	private String[] populate(Random r, int i) {
		String[] s = new String[i+2];
		s[0] = BindingSearchKey.NONE;
		s[1] = BindingSearchKey.ANY;
		for ( int k = 2; k < s.length; k++ ) {
			s[k] = String.valueOf(r.nextInt());
		}
		return s;
	}

}
