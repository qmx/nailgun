/**
 * 
 */
package com.martiansoftware.nailgun.jmx;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

/**
 * @author nwhitehead
 *
 */
public class NailStatsCompositeType extends CompositeType {


	
	private static final long serialVersionUID = -3357492790596450480L;

	public NailStatsCompositeType(String name, String description, String[] itemNames,
			String[] itemDescriptions, OpenType<?>[] itemTypes) throws OpenDataException {
		super(name, description, itemNames, itemDescriptions, itemTypes);
	}


}
