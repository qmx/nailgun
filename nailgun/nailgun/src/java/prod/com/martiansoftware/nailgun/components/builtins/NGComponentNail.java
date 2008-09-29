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

package com.martiansoftware.nailgun.components.builtins;

import java.lang.reflect.Method;
import com.martiansoftware.nailgun.NGContext;


/**
* <p>Title: NGComponentNail</p>
* <p>Description: Nail service to provide output on component operations that are otherwise silent.</p>
* @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
*
*/
public class NGComponentNail {
	public static void nailMain(NGContext context)  throws java.security.NoSuchAlgorithmException, java.io.IOException {
		// Get the parameters
		String[] args = context.getArgs();
		// Get the component name
		String componentName = args[0];
		// Get the component method Name
		String componentMethodName = args[1];		
		// Get the component reference
		Object component = context.getApplicationContext().getBean(componentName);
		if(component==null) {
			context.err.println("ERROR: [NGComponentNail] No NG Component Located for name [" + componentName + "]");
		}
		// Create args array minus the first two parama which are the bean name and method name
		// and the according method signature
		String[] methodArgs = new String[args.length-2];
		Class[] methodSignature = new Class[args.length-2];
		System.arraycopy(args, 1,methodArgs, 0, args.length-2);
		for(int i = 0; i < args.length-2; i++) {
			methodSignature[i] = String.class;
		}
		// See if the methodArgs match a method
		Method cMethod = null;
		try {
			cMethod = component.getClass().getMethod(componentMethodName, methodSignature);
		} catch (NoSuchMethodException noMethod) {
			context.err.println("ERROR: [" + componentName + "] No Method Found for [" + componentMethodName + "]");
			return;
		}
		try {
			Object result = cMethod.invoke(component, methodArgs);
			if(cMethod.getReturnType().equals(Void.class)) {
				context.out.println("Invocation on [" + componentMethodName + "] Successful.");
			} else {
				context.out.println(renderObject(result));
			}
			
		} catch (Exception e) {
			context.err.println("ERROR: [" + componentMethodName + "] Exception Invoking [" + componentMethodName + "]:" + e);
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
