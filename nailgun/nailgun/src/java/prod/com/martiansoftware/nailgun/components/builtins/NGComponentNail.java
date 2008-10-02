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
import com.martiansoftware.nailgun.components.builtins.base.BaseComponentService;


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
		// Create args array 
		// if this is a direct component call
		//    ----> minus the first two params which are the bean name and method name
		// otherwise
		//    ----> minus the first param which is the bean name
		// then the according method signature
 
		int paramsToRemove = 0;
		if(context.isDirectComponentCall()) {
			paramsToRemove = 2;
		} else {
			paramsToRemove = 1;
		}
		// Now create the args and sig
		String[] methodArgs = new String[args.length-paramsToRemove];
		Class[] methodSignature = null;
		System.arraycopy(args, paramsToRemove, methodArgs, 0, args.length-paramsToRemove);
		if(context.isDirectComponentCall()) {
			methodSignature = new Class[args.length-paramsToRemove];
			for(int i = 0; i < args.length-paramsToRemove; i++) {
				methodSignature[i] = String.class;
			}			
		} else {
			methodSignature = new Class[]{NGContext.class};
			componentMethodName = "nailMain";
			context.setArgs(methodArgs);
		}
		// if this is a direct component call, we need to locate the method.
		// if not, we know the method is nailMain(NGContext)
		Method cMethod = null;
		try {
			cMethod = component.getClass().getMethod(componentMethodName, methodSignature);
		} catch (NoSuchMethodException noMethod) {
			BaseComponentService.reportError("[" + componentName + "] No Method Found for [" + componentMethodName + "]", 
					BaseComponentService.err, context.err);
			return;
		}			
		try {
			Object[] invokeParams = null;
			if(context.isDirectComponentCall()) {
				invokeParams = methodArgs;
			} else {
				invokeParams = new Object[]{context};
			}
			Object result = cMethod.invoke(component, invokeParams);
			if(cMethod.getReturnType().toString().equalsIgnoreCase("void")) {
				context.out.println("Successful Invocation on [" + componentName + "/" + componentMethodName + "].");
			} else {
				context.out.println(renderObject(result));
			}			
		} catch (Exception e) {			
			BaseComponentService.reportError("[" + componentMethodName + "] Exception Invoking [" + componentMethodName + "]:" + e,	BaseComponentService.err, context.err);
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
