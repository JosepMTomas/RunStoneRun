package com.josepmtomas.rockgame.util;

/**
 * Created by Josep on 11/08/2014.
 * @author Josep
 */

public enum TouchState
{
	TOUCHING,
	NOT_TOUCHING;

	public boolean isTouching()
	{
		if(this == TouchState.TOUCHING)
			return true;
		else
			return false;
	}
}
