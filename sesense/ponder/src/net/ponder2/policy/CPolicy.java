/**
 * Created on Jul 10, 2005
 * Copyright 2005 Imperial College, London, England.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 *
 * Contact: Kevin Twidle <kpt@doc.ic.ac.uk>
 *
 * $Log: AuthorisationPolicy.java,v $
 * Revision 1.5  2005/11/15 16:33:25  kpt
 * Result added to create
 *
 * Revision 1.4  2005/10/21 17:15:47  kpt
 * Renamed XML Element types
 *
 * Revision 1.3  2005/10/21 14:31:39  kpt
 * Changed to faster, meaner, external QDParser with LGPL licence.
 *
 * Revision 1.2  2005/09/14 08:45:38  kpt
 * Down to import and use
 *
 * Revision 1.1  2005/09/12 10:47:07  kpt
 * Initial Checkin
 *
 */

package net.ponder2.policy;
import net.ponder2.ManagedObject;
import net.ponder2.P2ManagedObject;
import net.ponder2.apt.Ponder2op;
import net.ponder2.exception.Ponder2Exception;
import net.ponder2.objects.P2Array;
import net.ponder2.objects.P2Block;
import net.ponder2.objects.P2Object;
import net.ponder2.Util;
import net.ponder2.OID;

/**
 * This is a space holder for an Authorisation Policy, there is no support for it at present
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class CPolicy extends AuthorisationPolicy implements ManagedObject {
    private P2Block reqcond = null, repcond = null;

	@Ponder2op("subject:action:target:focus:")
	public CPolicy(P2Object subject, String action, P2Object target, String focus) {
	    super(subject, action, target, focus);
	}
	
    @Ponder2op("asString")
	P2Object asString() {
	return P2Object.create(super.toString());
    }

    @Ponder2op("isActive")
	public boolean isActive() {
	return super.isActive();
    }

    @Ponder2op("reqcondition:")
    protected P2Object operation_in_condition(P2Block aBlock) {
	super.operation_in_condition(aBlock);
	this.reqcond = aBlock;  
      return aBlock;
    }

    @Ponder2op("getDetails")
	public P2Array getDetails() {
	P2Array ret = new P2Array();

	String req = " - not present - ";
	if (null != reqcond) {
	    req = reqcond.toString();
	    req = req.replaceAll("\n", "");
	    req = req.replaceAll("\r", "");
	    req = req.replaceAll(" ", "");
	}

	return new P2Array((new Boolean(isActive())).toString(), 
			   getSubject().toString(),
			   getTarget().toString(),
			   getAction(),
			   req
);
    }

}
