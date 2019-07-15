package com.speuce.cosmetics.extra;

public enum Chord {
	C_MAJOR(0.7F, 0.9F, 1.05F),
	A_MINOR(0.6F, 0.7F, 0.9F),
	F_MAJOR(0.95F, 1.2F, 1.4F),
	G_MAJOR(0.533333F, 0.666666F, 0.8F);
	private float first, third, fifth;

	private Chord(float first, float third, float fifth) {
		this.first = first;
		this.third = third;
		this.fifth = fifth;
	}

	public float getFirst() {
		return first;
	}

	public float getThird() {
		return third;
	}

	public float getFifth() {
		return fifth;
	}
	
}
