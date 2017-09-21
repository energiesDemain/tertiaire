package com.ed.cgdd.derby.model.progression;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Progression {
	private ProgressionStep step = ProgressionStep.INITIALISATION;
	private int parcSize;
	private int handledParc;
	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	public ProgressionStep getStep() {
		return step;
	}

	public void setStep(ProgressionStep step) {
		ProgressionStep oldStep = this.step;
		this.step = step;
		changeSupport.firePropertyChange("step", oldStep, this.step);
	}

	public int getParcSize() {
		return parcSize;
	}

	public void setParcSize(int parcSize) {
		this.parcSize = parcSize;
	}

	public int getHandledParc() {
		return handledParc;
	}

	public void setHandledParc(int handledParc) {
		this.handledParc = handledParc;
	}

	public synchronized void addFinishedParc() {
		handledParc++;
		changeSupport.firePropertyChange("handledParc", handledParc - 1, handledParc);
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		changeSupport.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changeSupport.removePropertyChangeListener(l);
	}
}
