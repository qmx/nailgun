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

package com.martiansoftware.nailgun.components;

import com.martiansoftware.nailgun.AliasManager;
import com.martiansoftware.nailgun.NGServer;
import com.martiansoftware.nailgun.NGSessionPool;

/**
 * <p>Title:NGReference</p>
 * <p>Description: A component bean that holds references to NG constructs to make them available to components in the Spring context.</p>
 * @author Nicholas Whitehead (nwhitehead at heliosdev dot org)
 *
 */
public class NGReference {
	protected NGServer server = null;
	protected NGSessionPool sessionPool = null;
	protected AliasManager aliasManager = null;
	
	/**
	 * @return the server
	 */
	public NGServer getServer() {
		return server;
	}
	/**
	 * @param server the server to set
	 */
	public void setServer(NGServer server) {
		this.server = server;
	}
	/**
	 * @return the sessionPool
	 */
	public NGSessionPool getSessionPool() {
		return sessionPool;
	}
	/**
	 * @param sessionPool the sessionPool to set
	 */
	public void setSessionPool(NGSessionPool sessionPool) {
		this.sessionPool = sessionPool;
	}
	/**
	 * @return the aliasManager
	 */
	public AliasManager getAliasManager() {
		return aliasManager;
	}
	/**
	 * @param aliasManager the aliasManager to set
	 */
	public void setAliasManager(AliasManager aliasManager) {
		this.aliasManager = aliasManager;
	}
	
}
