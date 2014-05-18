package aStar;

import java.util.Collection;

public interface IAStarState extends Comparable<IAStarState> {

	public Collection<? extends IAStarState> expand() throws CloneNotSupportedException;

	public double getStateValue();

	public boolean hasReachedGoal();

	public double correctionForMovingBothArms();

}
