// SMSLib for Java v3
// A Java API library for sending and receiving SMS via a GSM modem
// or other supported gateways.
// Web Site: http://www.smslib.org
//
// Copyright (C) 2002-2008, Thanasis Delenikas, Athens/GREECE.
// SMSLib is distributed under the terms of the Apache License version 2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.smslib.v3;

import java.util.ArrayList;
import java.util.List;

/**
 * Base message routing class. Service owns instance of Router (or its
 * subclass), and uses its member functions to designate gateway(s) to send
 * particular outgoing message. It is allowed that router designates more than
 * modem to send message. It is responsibility of Router to decide which gateway
 * will send the message. Custom routing rules are possible by creating
 * subclass.
 * 
 * @author Tomek Cejner
 */
public class Router
{
	/** List of candidate gateways */
	@SuppressWarnings("unchecked")
	protected List candidates;
	/** List of gateways that are allowed to send message */
	@SuppressWarnings("unchecked")
	protected List allowed;
	protected Service srv;

	@SuppressWarnings("unchecked")
	public Router(Service srv)
	{
		candidates = new ArrayList();
		allowed = new ArrayList();
		this.srv = srv;
	}

	/**
	 * Perform early-stage routing, pick gateways that meet minimal requirements
	 * to send message (for example are set to handle outbound messages).
	 * 
	 * @param msg
	 *            Message to be routed
	 */
	@SuppressWarnings("unchecked")
	protected void preroute(OutboundMessage msg)
	{
		for (int i = 0, n = srv.getGatewayList().size(); i < n; i ++)
		{
			AGateway gw = (AGateway) srv.getGatewayList().get(i);
			if (gw.isOutbound()) candidates.add(gw);
		}
	}

	/**
	 * Heart of routing & load balancing mechanism
	 * 
	 * @param msg
	 */
	protected AGateway route(OutboundMessage msg)
	{
		AGateway gw;
		beginRouting();
		preroute(msg);
		// perform custom routing
		customRouting(msg);
		// check if there are any gateways designated to send?
		if (allowed.size() > 0)
		{
			gw = srv.getLoadBalancer().balance(msg, allowed);
		}
		else
		{
			msg.setFailureCause(FailureCauses.NO_ROUTE);
			msg.setMessageStatus(MessageStatuses.FAILED);
			gw = null;
		}
		// finish
		finishRouting();
		return gw;
	}

	/**
	 * Place for custom routing performed by specialized subclass. A "positive"
	 * approach is taken. Method has to copy references to gateways from
	 * <code>candidates</code> list to <code>allowed</code>. So, default
	 * behavior is to copy all references. Another possibility is to take
	 * "negative" approach, where method should delete unwanted gateways from
	 * list. This approach was found difficult to use at this time.
	 * 
	 * @param msg
	 *            Message to be routed
	 */
	@SuppressWarnings("unchecked")
	public void customRouting(OutboundMessage msg)
	{
		if ((msg.getGatewayId().length() == 0) || (msg.getGatewayId().equals("*"))) allowed.addAll(candidates);
		else
		{
			if (srv.findGateway(msg.getGatewayId()) != null) allowed.add(srv.findGateway(msg.getGatewayId()));
		}
	}

	/**
	 * Prepare internal data for routing (clean internal data structures). Must
	 * be called when new message is routed.
	 */
	protected final void beginRouting()
	{
		candidates.clear();
		allowed.clear();
	}

	/**
	 * Cleanup after routing
	 */
	protected final void finishRouting()
	{
		candidates.clear();
		allowed.clear();
	}
}
