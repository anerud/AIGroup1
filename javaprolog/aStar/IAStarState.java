package aStar;

import java.util.Collection;

public interface IAStarState extends Comparable<IAStarState> {

	/**
	 * @return returns the neighbouring states to this IAStarState
	 * @throws CloneNotSupportedException
	 */
	public Collection<? extends IAStarState> expand() throws CloneNotSupportedException;

	/**
	 * Returns the state value for this state. The state value is defined as v(s) + k*h(s)
	 * @return v(s) + k*h(s)
	 */
	public double getStateValue();

	/**
	 * @return a boolean if the current state is a goal state or not.
	 */
	public boolean hasReachedGoal();

	/**
	 * The correction for moving arms in different way. In this way a priority could be set between actions.
	 * Example: Holding an arm still is preferred to moving an arm which is in turn better than
	 * picking/dropping assuming that none of the actions takes you towards the goal.
	 * @return a double for the correction.
	 */
	public double correctionForMovingArms();

}
