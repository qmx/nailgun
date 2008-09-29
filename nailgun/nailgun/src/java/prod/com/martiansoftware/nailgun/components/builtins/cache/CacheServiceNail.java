/*   

  Copyright 2008, Martian Software, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

*/

package com.martiansoftware.nailgun.components.builtins.cache;

import java.lang.reflect.Method;

import com.martiansoftware.nailgun.NGContext;

/**
 * <p>Title:CacheServiceNail</p>
 * <p>Description: Nail service to provide output on cache operations that are otherwise silent.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class CacheServiceNail {
	public static void nailMain(NGContext context)  throws java.security.NoSuchAlgorithmException, java.io.IOException {
		// Get the cache service reference
		CacheService cache = (CacheService)context.getApplicationContext().getBean("NGCache");
		// Get the parameters
		String[] args = context.getArgs();
		// Get possible method Name
		String cacheMethodName = args[0];
		// Create args array minus the first param which is the method name
		// and the according method signature
		String[] methodArgs = new String[args.length-1];
		Class[] methodSignature = new Class[args.length-1];
		System.arraycopy(args, 1,methodArgs, 0, args.length-1);
		for(int i = 0; i < args.length-1; i++) {
			methodSignature[i] = String.class;
		}
		// See if the methodArgs match a method
		Method cacheMethod = null;
		try {
			cacheMethod = cache.getClass().getMethod(cacheMethodName, methodSignature);
		} catch (NoSuchMethodException noMethod) {
			context.err.println("ERROR: [CacheService] No Method Found for [" + cacheMethodName + "]");
			return;
		}
		try {
			Object result = cacheMethod.invoke(cache, methodArgs);
			if(cacheMethod.getReturnType().equals(Void.class)) {
				context.out.println("Invocation on [" + cacheMethodName + "] Successful.");
			} else {
				context.out.println(renderObject(result));
			}
			
		} catch (Exception e) {
			context.err.println("ERROR: [CacheService] Exception Invoking [" + cacheMethodName + "]:" + e);
			return;			
		}
		
		
	}
	
	/**
	 * Renders the result object to a readable string.
	 * @param result The result object to render.
	 * @return A readable string rendering of the result object.
	 */
	public static String renderObject(Object result) {
		if(result==null) return "NULL";
		else if(result.getClass().equals(String[].class)) {
			StringBuffer b = new StringBuffer();
			String[] res = (String[])result;
			for(int i = 0; i < res.length; i++) {
				b.append(res[i]).append("\n");
			}
			return b.toString();
		}
		return result.toString();
	}
}
