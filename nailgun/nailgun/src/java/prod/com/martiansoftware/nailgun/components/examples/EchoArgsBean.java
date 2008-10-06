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

package com.martiansoftware.nailgun.components.examples;

import com.martiansoftware.nailgun.NGContext;

/**
 * <p>Title:EchoArgsBean</p>
 * <p>Description: Very simple NGServer component bean that echoes all arguments.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class EchoArgsBean {
	public void echo(String target, String message) {
		StringBuffer buff = new StringBuffer("EchoArgsBean: Target=");
		buff.append(target).append("\tMessage=").append(message);
		System.out.println(buff);
	}
	
	public void nailMain(NGContext context) {
		StringBuffer buff = new StringBuffer("EchoArgsBean: Target=");
		buff.append(context.getArgs()[0]).append("\tMessage=").append(context.getArgs()[1]);
		System.out.println(buff);		
	}
}
