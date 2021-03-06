/**
 * Copyright (c) 2005, KoLmafia development team
 * http://kolmafia.sourceforge.net/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  [1] Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  [2] Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *  [3] Neither the name "KoLmafia development team" nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.kolmafia;

public class RequestThread extends Thread implements KoLConstants
{
	private int repeatCount;
	private Runnable [] requests;

	public RequestThread( Runnable request )
	{	this( request, 1 );
	}

	public RequestThread( Runnable request, int repeatCount )
	{	this( new Runnable [] { request }, repeatCount );
	}

	public RequestThread( Runnable [] requests )
	{	this( requests, 1 );
	}

	public RequestThread( Runnable [] requests, int repeatCount )
	{
		this.repeatCount = repeatCount;

		int requestCount = 0;
		for ( int i = 0; i < requests.length; ++i )
			if ( requests[i] != null )
				++requestCount;

		this.requests = new Runnable[ requestCount ];

		requestCount = 0;

		for ( int i = 0; i < requests.length; ++i )
			if ( requests[i] != null )
				this.requests[ requestCount++ ] = requests[i];

		setDaemon( true );
	}

	public void run()
	{
		if ( requests.length == 0 )
			return;

		KoLmafia.forceContinue();

		if ( !(requests[0] instanceof LoginRequest) )
			MoodSettings.execute();

		for ( int i = 0; i < requests.length && KoLmafia.permitsContinue(); ++i )
		{
			// Setting it up so that derived classes can
			// override the behavior of execution.

			if ( requests[i] instanceof KoLRequest )
			{
				run( (KoLRequest) requests[i], repeatCount );
			}

			// Standard KoL adventures are handled through the
			// client.makeRequest() method.

			else if ( requests[i] instanceof KoLAdventure )
				StaticEntity.getClient().makeRequest( requests[i], repeatCount );

			// All other runnables are run, as expected, with
			// no updates to the client.

			else
				for ( int j = 0; j < repeatCount; ++j )
					requests[i].run();
		}

		KoLmafia.enableDisplay();
	}

	protected void run( KoLRequest request, int repeatCount )
	{
		// Standard KoL requests are handled through the
		// makeRequest() method.

		if ( KoLmafia.permitsContinue() )
			StaticEntity.getClient().makeRequest( request, repeatCount );
	}
}
